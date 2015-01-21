package br.gov.batch.servicos.faturamento;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.cobranca.parcelamento.ParcelamentoImovelBO;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.servicos.arrecadacao.pagamento.GuiaPagamentoRepositorio;
import br.gov.servicos.arrecadacao.pagamento.PagamentoRepositorio;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.cobranca.ContratoParcelamentoItemRepositorio;
import br.gov.servicos.faturamento.ContaRepositorio;
import br.gov.servicos.to.ConsultaDebitoImovelTO;
import br.gov.servicos.to.ContaTO;
import br.gov.servicos.to.GuiaPagamentoTO;

@Stateless
public class DebitoImovelBO {
	
	@EJB
	private SistemaParametrosRepositorio sistemaParametrosRepositorio;

	@EJB
	private ContaRepositorio contaRepositorio;
	
	@EJB
	private GuiaPagamentoRepositorio guiaPagamentoRepositorio;
	
	@EJB
	private PagamentoRepositorio pagamentoRepositorio;
	
	@EJB
	private ParcelamentoImovelBO parcelamentoImovelBO;
	
	@EJB
	private ContratoParcelamentoItemRepositorio contratoParcelamentoItemRepositorio;
	
	public boolean existeDebitoImovel(ConsultaDebitoImovelTO to){
        SistemaParametros sistemaParametros = sistemaParametrosRepositorio.getSistemaParametros();

		Collection<ContaTO> contas = this.pesquisarContasDebitoImovel(to);

		Collection<ContaTO> contasSemParcelamento = new ArrayList<ContaTO>();

		if (sistemaParametros.parametroAtivo(sistemaParametros.getIndicadorBloqueioContasContratoParcelDebitos())) {
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

		if (sistemaParametros.parametroAtivo(sistemaParametros.getIndicadorBloqueioGuiasOuAcresContratoParcelDebito())) {
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
		to.addSituacao(DebitoCreditoSituacao.NORMAL.getId());
		to.addSituacao(DebitoCreditoSituacao.RETIFICADA.getId());
		to.addSituacao(DebitoCreditoSituacao.INCLUIDA.getId());
		to.addSituacao(DebitoCreditoSituacao.PARCELADA.getId());
		
		List<ContaTO> contas = contaRepositorio.pesquisarContasImovel(to);
		
		boolean confirmarParcelamento = false;
		
		for (ContaTO conta : contas) {
			if (conta.getSituacaoAtual() == DebitoCreditoSituacao.PARCELADA.getId()) {
				confirmarParcelamento = true;
			}
		}
		
		if (confirmarParcelamento) {
			if (parcelamentoImovelBO.imovelSemParcelamento(to.getIdImovel())) {
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
}