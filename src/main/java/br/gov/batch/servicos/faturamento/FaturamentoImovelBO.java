package br.gov.batch.servicos.faturamento;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.Status;
import br.gov.model.atendimentopublico.LigacaoAguaSituacao;
import br.gov.model.atendimentopublico.LigacaoEsgoto;
import br.gov.model.atendimentopublico.LigacaoEsgotoSituacao;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.ContaCategoria;
import br.gov.model.faturamento.ContaCategoriaPK;
import br.gov.model.faturamento.FaturamentoAtividadeCronogramaRota;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.faturamento.FaturamentoSituacaoHistorico;
import br.gov.servicos.atendimentopublico.LigacaoEsgotoRepositorio;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.faturamento.ContaCategoriaRepositorio;
import br.gov.servicos.faturamento.FaturamentoSituacaoRepositorio;
import br.gov.servicos.to.CreditoRealizadoTO;
import br.gov.servicos.to.DebitoCobradoTO;
import br.gov.servicos.to.FaturamentoAguaEsgotoTO;
import br.gov.servicos.to.GerarContaTO;
import br.gov.servicos.to.ImovelSubcategoriaTO;
import br.gov.servicos.to.ImpostosDeduzidosContaTO;

@Stateless
public class FaturamentoImovelBO {

	@EJB
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio; 

	@EJB
	private FaturamentoSituacaoRepositorio faturamentoSituacaoRepositorio;
	
	@EJB
	private AnalisadorGeracaoConta analisadorGeracaoConta;
	
	@EJB
	private LigacaoEsgotoRepositorio ligacaoEsgotoRepositorio;
	
	@EJB
	private DebitoCobradoBO debitoCobradoBO;
	
	@EJB
	private CreditoRealizadoBO creditoRealizadoBO;
	
	@EJB
	private ImpostosContaBO impostosContaBO;
	
	@EJB
	private ContaBO contaBO;
	
	@EJB
	private ContaCategoriaRepositorio contaCategoriaRepositorio;
	
	private FaturamentoAguaEsgotoTO helperValoresAguaEsgoto;

	public void preDeterminarFaturamentoImovel(Imovel imovel, boolean gerarAtividadeGrupoFaturamento, FaturamentoAtividadeCronogramaRota faturamentoAtivCronRota,
			Collection colecaoResumoFaturamento, boolean faturamentoAntecipado, Integer anoMesFaturamento, FaturamentoGrupo faturamentoGrupo) throws Exception {

		Collection<ImovelSubcategoriaTO> colecaoCategoriaOUSubcategoria = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasPorImovel(imovel.getId());

		helperValoresAguaEsgoto = new FaturamentoAguaEsgotoTO();

		if (possuiLigacaoAguaEsgotoAtivo(imovel) || possuiHidrometro(imovel)) { 

			boolean faturar = deveFaturar(imovel, anoMesFaturamento);

			if (faturar) {
				helperValoresAguaEsgoto.setValorTotalAgua(BigDecimal.ONE);
				helperValoresAguaEsgoto.setValorTotalEsgoto(BigDecimal.ONE);
			} else {
				helperValoresAguaEsgoto.setValorTotalAgua(BigDecimal.ZERO);
				helperValoresAguaEsgoto.setValorTotalEsgoto(BigDecimal.ZERO);
			}

			LigacaoEsgoto ligacaoEsgoto = ligacaoEsgotoRepositorio.buscarLigacaoEsgotoPorId(imovel.getId());
			helperValoresAguaEsgoto.setPercentualEsgoto(this.verificarPercentualEsgotoAlternativo(ligacaoEsgoto, imovel));
			helperValoresAguaEsgoto.setPercentualColetaEsgoto(ligacaoEsgoto.getPercentualAguaConsumidaColetada());
		}

		boolean gerarConta = analisadorGeracaoConta.verificarNaoGeracaoConta(valoresAguaEsgotoZerados(helperValoresAguaEsgoto, imovel), anoMesFaturamento);
		
		if (gerarConta) {
			helperValoresAguaEsgoto.setValorTotalAgua(BigDecimal.ZERO);
			helperValoresAguaEsgoto.setValorTotalEsgoto(BigDecimal.ZERO);

			boolean preFaturamento = true;

			DebitoCobradoTO gerarDebitoCobradoHelper = debitoCobradoBO.gerarDebitoCobrado(imovel, anoMesFaturamento);

			CreditoRealizadoTO gerarCreditoRealizadoHelper = creditoRealizadoBO.gerarCreditoRealizado(imovel, anoMesFaturamento, helperValoresAguaEsgoto, gerarDebitoCobradoHelper.getValorDebito(), gerarAtividadeGrupoFaturamento, preFaturamento);

			if (gerarAtividadeGrupoFaturamento) {

				ImpostosDeduzidosContaTO gerarImpostosDeduzidosContaHelper = impostosContaBO.gerarImpostosDeduzidosConta(imovel.getId(),
								anoMesFaturamento, helperValoresAguaEsgoto.getValorTotalAgua(), helperValoresAguaEsgoto.getValorTotalEsgoto(),
								gerarDebitoCobradoHelper.getValorDebito(), gerarCreditoRealizadoHelper.getValorTotalCreditos(), preFaturamento);

				GerarContaTO gerarTO = buildGerarContaTO(imovel, faturamentoAtivCronRota, anoMesFaturamento, gerarDebitoCobradoHelper,
															gerarCreditoRealizadoHelper, gerarImpostosDeduzidosContaHelper);
				Conta conta = contaBO.gerarConta(gerarTO);

				Collection<ContaCategoria> contasCategoria = this.gerarContaCategoriaValoresZerados(conta, colecaoCategoriaOUSubcategoria);

				if (contasCategoria != null && !contasCategoria.isEmpty()) {
					contaCategoriaRepositorio.inserirContasCategoria(contasCategoria);
				}

//				this.inserirClienteConta(conta, imovel);
//				this.inserirContaImpostosDeduzidos(conta, gerarImpostosDeduzidosContaHelper);
//				this.inserirDebitoCobrado(gerarDebitoCobradoHelper.getMapDebitosCobrados(), conta);
//				this.atualizarDebitoACobrarFaturamento(gerarDebitoCobradoHelper.getColecaoDebitoACobrar());
//				this.inserirCreditoRealizado(gerarCreditoRealizadoHelper.getMapCreditoRealizado(), conta);
//				this.atualizarCreditoARealizar(gerarCreditoRealizadoHelper.getColecaoCreditoARealizar());

//				if (imovel.getIndicadorDebitoConta().equals(ConstantesSistema.SIM) && conta.getContaMotivoRevisao() == null) {
//					this.gerarMovimentoDebitoAutomatico(imovel, conta, faturamentoGrupo);
//				}
			}

//			Integer anoMesReferenciaResumoFaturamento = null;
//			if (faturamentoAntecipado) {
//				anoMesReferenciaResumoFaturamento = anoMesFaturamento;
//    		}
			
			Collection<ImovelSubcategoriaTO> colecaoCategorias = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasCategoria(imovel.getId());

//			this.gerarResumoFaturamentoSimulacao(colecaoCategorias, helperValoresAguaEsgoto.getColecaoCalcularValoresAguaEsgotoHelper(),
//													gerarDebitoCobradoHelper, gerarCreditoRealizadoHelper, colecaoResumoFaturamento, imovel,
//													gerarAtividadeGrupoFaturamento, faturamentoAtivCronRota, faturamentoGrupo, anoMesReferenciaResumoFaturamento, true);
		}
	}

	private GerarContaTO buildGerarContaTO(Imovel imovel, FaturamentoAtividadeCronogramaRota faturamentoAtivCronRota, Integer anoMesFaturamento,
			DebitoCobradoTO gerarDebitoCobradoHelper, CreditoRealizadoTO gerarCreditoRealizadoHelper,
			ImpostosDeduzidosContaTO gerarImpostosDeduzidosContaHelper) {
		GerarContaTO gerarTO = new GerarContaTO();
		gerarTO.setImovel(imovel);
		gerarTO.setDataVencimentoRota(faturamentoAtivCronRota.getDataContaVencimento());
		gerarTO.setAnoMesFaturamento(anoMesFaturamento);
		gerarTO.setValorTotalCreditos(gerarCreditoRealizadoHelper.getValorTotalCreditos());
		gerarTO.setValorTotalDebitos(gerarDebitoCobradoHelper.getValorDebito());
		gerarTO.setValorTotalImposto(gerarImpostosDeduzidosContaHelper.getValorTotalImposto());
		gerarTO.setPercentualEsgoto(helperValoresAguaEsgoto.getPercentualEsgoto());
		gerarTO.setPercentualColeta(helperValoresAguaEsgoto.getPercentualColetaEsgoto());
		return gerarTO;
	}
	
	private Collection<ContaCategoria> gerarContaCategoriaValoresZerados(Conta conta, Collection<ImovelSubcategoriaTO> colecaoCategorias) throws Exception {
		Collection<ContaCategoria> helper = new ArrayList<ContaCategoria>();
		
		ContaCategoria contaCategoria = null;
		ContaCategoriaPK contaCategoriaPK = null;

		for (ImovelSubcategoriaTO categoria : colecaoCategorias) {
			
			contaCategoria = new ContaCategoria();
			contaCategoriaPK = new ContaCategoriaPK();
			contaCategoriaPK.setContaId(conta.getId());
			contaCategoriaPK.setCategoriaId(categoria.getCategoria().getId());
			contaCategoriaPK.setSubcategoriaId(categoria.getSubcategoria().getId());
			contaCategoria.setPk(contaCategoriaPK);
			contaCategoria.setQuantidadeEconomia(categoria.getQuantidadeEconomias().shortValue());
			contaCategoria.setValorAgua(new BigDecimal("0.00"));
			contaCategoria.setConsumoAgua(0);
			contaCategoria.setValorEsgoto(new BigDecimal("0.00"));
			contaCategoria.setConsumoEsgoto(0);
			contaCategoria.setValorTarifaMinimaAgua(new BigDecimal("0.00"));
			contaCategoria.setConsumoMinimoAgua(0);
			contaCategoria.setValorTarifaMinimaEsgoto(new BigDecimal("0.00"));
			contaCategoria.setConsumoMinimoEsgoto(0);
			contaCategoria.setUltimaAlteracao(new Date(System.currentTimeMillis()));
			
			helper.add(contaCategoria);
		}

		return helper;
	}

	private BigDecimal verificarPercentualEsgotoAlternativo(LigacaoEsgoto ligacaoEsgoto, Imovel imovel) throws Exception {

		if (imovel.getLigacaoEsgotoSituacao().getSituacaoFaturamento().equals(Status.ATIVO) 
				&& ligacaoEsgoto.getNumeroConsumoPercentualAlternativo().intValue() >= 0 
				&& ligacaoEsgoto.getPercentualAlternativo() != null
				&& ligacaoEsgoto.getPercentualAlternativo().compareTo(ligacaoEsgoto.getPercentual()) == -1) {
			
			return ligacaoEsgoto.getPercentualAlternativo();
		} else {
			return BigDecimal.ZERO;
		}
	}
	
	private boolean valoresAguaEsgotoZerados(FaturamentoAguaEsgotoTO helperValoresAguaEsgoto, Imovel imovel) {
		return (helperValoresAguaEsgoto.getValorTotalAgua().compareTo(BigDecimal.ZERO) == 0 
				&& helperValoresAguaEsgoto.getValorTotalEsgoto().compareTo(BigDecimal.ZERO) == 0)
				|| (helperValoresAguaEsgoto.getValorTotalAgua().compareTo(BigDecimal.ZERO) != 0 && 
					helperValoresAguaEsgoto.getValorTotalEsgoto().compareTo(BigDecimal.ZERO) != 0 &&
					!imovel.getLigacaoAguaSituacao().getId().equals(LigacaoAguaSituacao.LIGADO) &&
					!imovel.getLigacaoEsgotoSituacao().getId().equals(LigacaoEsgotoSituacao.LIGADO) &&
					imovel.getImovelCondominio() == null);
	}

	private boolean deveFaturar(Imovel imovel, Integer anoMesFaturamento) {
		boolean faturar = true;

		if (imovel.getFaturamentoSituacaoTipo() != null && !imovel.getFaturamentoSituacaoTipo().equals("")) {

			List<FaturamentoSituacaoHistorico> faturamentosSituacaoHistorico = faturamentoSituacaoRepositorio.faturamentosHistoricoVigentesPorImovel(imovel.getId());
			FaturamentoSituacaoHistorico faturamentoSituacaoHistorico = faturamentosSituacaoHistorico.get(0);

			if ((faturamentoSituacaoHistorico != null 
					&& anoMesFaturamento >= faturamentoSituacaoHistorico.getAnoMesFaturamentoSituacaoInicio() 
					&& anoMesFaturamento <= faturamentoSituacaoHistorico.getAnoMesFaturamentoSituacaoFim())
					&& (imovel.getFaturamentoSituacaoTipo() != null 
					&& imovel.getFaturamentoSituacaoTipo().getParalisacaoFaturamento() == Status.ATIVO 
					&& imovel.getFaturamentoSituacaoTipo().getValidoAgua() == Status.ATIVO)) {
				faturar = false;
			}
		}

		return faturar;
	}

	private boolean possuiLigacaoAguaEsgotoAtivo(Imovel imovel) {
		return imovel.getLigacaoAguaSituacao().getSituacaoFaturamento().equals(Status.ATIVO) || imovel.getLigacaoEsgotoSituacao().getSituacaoFaturamento().equals(Status.ATIVO);
	}

	private boolean possuiHidrometro(Imovel imovel) {
		boolean hidrometro = false;
		if ((imovel.getHidrometroInstalacaoHistorico() != null && imovel.getHidrometroInstalacaoHistorico().getId() != null)
				|| (imovel.getLigacaoAgua() != null 
				&& imovel.getLigacaoAgua().getHidrometroInstalacaoHistorico() != null 
				&& imovel.getLigacaoAgua().getHidrometroInstalacaoHistorico().getId() != null)) {
			hidrometro = true;
		}

		return hidrometro;
	}
}