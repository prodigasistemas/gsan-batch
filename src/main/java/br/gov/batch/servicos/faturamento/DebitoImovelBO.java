package br.gov.batch.servicos.faturamento;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;

import br.gov.model.Status;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.cobranca.Parcelamento;
import br.gov.model.cobranca.parcelamento.ParcelamentoSituacao;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.model.faturamento.GuiaPagamento;
import br.gov.servicos.arrecadacao.pagamento.GuiaPagamentoRepositorio;
import br.gov.servicos.arrecadacao.pagamento.PagamentoRepositorio;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.cobranca.ContratoParcelamentoItemRepositorio;
import br.gov.servicos.cobranca.parcelamento.ParcelamentoRepositorio;
import br.gov.servicos.faturamento.ContaRepositorio;
import br.gov.servicos.to.ConsultaDebitoImovelTO;
import br.gov.servicos.to.ContaTO;
import br.gov.servicos.to.GuiaPagamentoTO;

public class DebitoImovelBO {
	
	@EJB
	private SistemaParametrosRepositorio sistemaParametrosRepositorio;

	@EJB
	private ContaRepositorio contaRepositorio;
	
	@EJB
	private ParcelamentoRepositorio parcelamentoRepositorio;
	
	@EJB
	private GuiaPagamentoRepositorio guiaPagamentoRepositorio;
	
	@EJB
	private PagamentoRepositorio pagamentoRepositorio;
	
	@EJB
	private ContratoParcelamentoItemRepositorio contratoParcelamentoItemRepositorio;
	
	public boolean existeDebitoImovel(ConsultaDebitoImovelTO to){
        SistemaParametros sistemaParametros = sistemaParametrosRepositorio.getSistemaParametros();

//		int indicadorDebito                            = 1 
//		int indicadorPagamento                         = 1 
//		int indicadorConta                             = 2
//		int indicadorDebitoACobrar                     = 2
//		int indicadorCreditoARealizar                  = 2
//		int indicadorNotasPromissorias                 = 2
//		int indicadorGuiasPagamento                    = 1
//		int indicadorCalcularAcrescimoImpontualidade   = 2
//		indicadorDividaAtiva                           = 3
		
		Collection<ContaTO> contas = this.pesquisarContasDebitoImovel(to);

		Collection<ContaTO> contasSemParcelamento = new ArrayList<ContaTO>();

		if (sistemaParametros.getIndicadorBloqueioContasContratoParcelDebitos() == Status.ATIVO.getId()) {
			for (ContaTO conta : contas) {
				if (!contratoParcelamentoItemRepositorio.existeContratoParcelamentoAtivoParaConta(conta.getIdConta())){
					contasSemParcelamento.add(conta);
				}
			}
		} else {
			contasSemParcelamento = contas;
		}
		
		List<GuiaPagamentoTO> guias = guiaPagamentoRepositorio.pesquisarGuiasPagamentoImovel(to.getIdImovel(), to.getVencimentoInicial(), to.getVencimentoFinal());
		
		List<GuiaPagamentoTO> guiasSemParcelamento = new ArrayList<GuiaPagamentoTO>(); 

		if (sistemaParametros.getIndicadorBloqueioGuiasOuAcresContratoParcelDebito() == Status.ATIVO.getId()) {
			for (GuiaPagamentoTO guia : guias) {
				if (!contratoParcelamentoItemRepositorio.existeContratoParcelamentoAtivoParaGuia(guia.getIdGuia())) {
					guiasSemParcelamento.add(guia);
				}
			}
		} else {
			guiasSemParcelamento = guias;
		}
		
		return !contasSemParcelamento.isEmpty() || !guiasSemParcelamento.isEmpty();
	}
	
	public List<ContaTO> pesquisarContasDebitoImovel(ConsultaDebitoImovelTO to){
       SistemaParametros sistemaParametros = sistemaParametrosRepositorio.getSistemaParametros();
	    
		to.addSituacao(DebitoCreditoSituacao.NORMAL.getId());
		to.addSituacao(DebitoCreditoSituacao.RETIFICADA.getId());
		to.addSituacao(DebitoCreditoSituacao.INCLUIDA.getId());
		to.addSituacao(DebitoCreditoSituacao.PARCELADA.getId());
		
		List<ContaTO> contas = contaRepositorio.pesquisarContasImovel(to);
		
		boolean verificaParcelamentoConfirmado = false;
		
		for (ContaTO conta : contas) {
			if (conta.getSituacaoAtual() == DebitoCreditoSituacao.PARCELADA.getId()) {
				verificaParcelamentoConfirmado = true;
			}
		}
		
		if (verificaParcelamentoConfirmado) {
			if (imovelPossuiParcelamento(to.getIdImovel())) {
				List<ContaTO> contasSemParcelamentos = new ArrayList<ContaTO>();
				
				for (ContaTO conta : contas) {
					if (conta.getIdImovel() == to.getIdImovel().intValue()) {
						if (conta.getSituacaoAtual() != DebitoCreditoSituacao.PARCELADA.getId()) {
							contasSemParcelamentos.add(conta);
						}
					}
				}
				
				contas = contasSemParcelamentos;
			}
		}		
		
		return contas;
	}
	
	protected boolean imovelPossuiParcelamento(Integer idImovel) {
        SistemaParametros sistemaParametros = sistemaParametrosRepositorio.getSistemaParametros();

		boolean estahConfirmado = false;
		
		Parcelamento parcelamento = parcelamentoRepositorio.pesquisaParcelamento(idImovel, sistemaParametros.getAnoMesArrecadacao(), ParcelamentoSituacao.NORMAL);
		
		if (parcelamento == null){
			estahConfirmado = true;
		} else {
			if (parcelamento.semEntrada() || parcelamento.confirmado()) {
				estahConfirmado = true;
			} else {
				GuiaPagamento guia = guiaPagamentoRepositorio.guiaDoParcelamento(parcelamento.getId());
				
				if (guia != null) {
					estahConfirmado = pagamentoRepositorio.guiaPaga(guia.getId());
				} else {
					List<Conta> contasDoParcelamento = contaRepositorio.recuperarPeloParcelamento(parcelamento.getId());
					
					if (contasDoParcelamento.size() > 0){
						int quantidadeContasComPagamento = 0;
						
						for (Conta conta : contasDoParcelamento) {
							if (pagamentoRepositorio.contaPaga(conta.getId())){
								quantidadeContasComPagamento++;
							}
						}
						
						estahConfirmado = quantidadeContasComPagamento == contasDoParcelamento.size();
					} else{
						estahConfirmado = true;
					}
				}
			}
		}
		
		return estahConfirmado;
	}
}