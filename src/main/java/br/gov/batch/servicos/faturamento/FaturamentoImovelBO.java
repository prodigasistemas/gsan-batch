package br.gov.batch.servicos.faturamento;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.logging.Logger;

import br.gov.model.Status;
import br.gov.model.atendimentopublico.LigacaoAguaSituacao;
import br.gov.model.atendimentopublico.LigacaoEsgoto;
import br.gov.model.atendimentopublico.LigacaoEsgotoSituacao;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.ContaCategoria;
import br.gov.model.faturamento.ContaCategoriaPK;
import br.gov.model.faturamento.FaturamentoSituacaoHistorico;
import br.gov.servicos.atendimentopublico.LigacaoEsgotoRepositorio;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.faturamento.ContaCategoriaRepositorio;
import br.gov.servicos.faturamento.FaturamentoSituacaoRepositorio;
import br.gov.servicos.to.CreditosContaTO;
import br.gov.servicos.to.DebitosContaTO;
import br.gov.servicos.to.FaturamentoAguaEsgotoTO;
import br.gov.servicos.to.GerarContaTO;
import br.gov.servicos.to.ImovelSubcategoriaTO;
import br.gov.servicos.to.ImpostosDeduzidosContaTO;

@Stateless
public class FaturamentoImovelBO {
	private static Logger logger = Logger.getLogger(FaturamentoImovelBO.class);
	
	@EJB
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio; 

	@EJB
	private FaturamentoSituacaoRepositorio faturamentoSituacaoRepositorio;
	
	@EJB
	private AnalisadorGeracaoConta analisadorGeracaoConta;
	
	@EJB
	private LigacaoEsgotoRepositorio ligacaoEsgotoRepositorio;
	
	@EJB
	private DebitosContaBO debitosContaBO;
	
	@EJB
	private CreditosContaBO creditosContaBO;
	
	@EJB
	private ImpostosContaBO impostosContaBO;
	
	@EJB
	private ContaBO contaBO;
	
	@EJB
	private ContaCategoriaRepositorio contaCategoriaRepositorio;
	
	private FaturamentoAguaEsgotoTO helperValoresAguaEsgoto;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void preDeterminarFaturamentoImovel(FaturamentoImovelTO faturamentoTO) throws Exception {
		Imovel imovel = faturamentoTO.getImovel();
		Integer anoMesFaturamento = faturamentoTO.getAnoMesFaturamento();

		Collection<ImovelSubcategoriaTO> colecaoCategoriaOUSubcategoria = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasPorImovel(imovel.getId());

		helperValoresAguaEsgoto = new FaturamentoAguaEsgotoTO();

		if (possuiLigacaoAguaEsgotoAtivo(imovel) || possuiHidrometro(imovel)) { 

			if (deveFaturar(imovel, anoMesFaturamento)) {
				helperValoresAguaEsgoto.setAguaEsgotoZerados(false);
				helperValoresAguaEsgoto.setValorTotalAgua(BigDecimal.ONE);
				helperValoresAguaEsgoto.setValorTotalEsgoto(BigDecimal.ONE);
			} else {
				helperValoresAguaEsgoto.setAguaEsgotoZerados(true);
				helperValoresAguaEsgoto.setValorTotalAgua(BigDecimal.ZERO);
				helperValoresAguaEsgoto.setValorTotalEsgoto(BigDecimal.ZERO);
			}

			LigacaoEsgoto ligacaoEsgoto = ligacaoEsgotoRepositorio.buscarLigacaoEsgotoPorIdImovel(imovel.getId());
			
			if (ligacaoEsgoto != null){
				helperValoresAguaEsgoto.setPercentualEsgoto(this.verificarPercentualEsgotoAlternativo(ligacaoEsgoto, imovel));
				helperValoresAguaEsgoto.setPercentualColetaEsgoto(ligacaoEsgoto.getPercentualAguaConsumidaColetada());
			}
		}

		boolean gerarConta = analisadorGeracaoConta.verificarGeracaoConta(helperValoresAguaEsgoto.isAguaEsgotoZerados(), anoMesFaturamento, imovel);
		
		if (gerarConta) {
			helperValoresAguaEsgoto.setValorTotalAgua(BigDecimal.ZERO);
			helperValoresAguaEsgoto.setValorTotalEsgoto(BigDecimal.ZERO);

			DebitosContaTO debitosConta = debitosContaBO.gerarDebitosConta(imovel, anoMesFaturamento);

			CreditosContaTO creditosConta = creditosContaBO.gerarCreditosConta(imovel, anoMesFaturamento);

			ImpostosDeduzidosContaTO impostosDeduzidosConta = impostosContaBO.gerarImpostosDeduzidosConta(imovel.getId(),
							anoMesFaturamento, helperValoresAguaEsgoto.getValorTotalAgua(), helperValoresAguaEsgoto.getValorTotalEsgoto(),
							debitosConta.getValorTotalDebito(), creditosConta.getValorTotalCreditos());

			GerarContaTO gerarTO = buildGerarContaTO(faturamentoTO, debitosConta,
														creditosConta, impostosDeduzidosConta);
			
			Conta conta = contaBO.gerarConta(gerarTO);

//			Collection<ContaCategoria> contasCategoria = this.gerarContaCategoriaValoresZerados(conta, colecaoCategoriaOUSubcategoria);
//
//			if (contasCategoria != null && !contasCategoria.isEmpty()) {
//				contaCategoriaRepositorio.inserirContasCategoria(contasCategoria);
//			}
//
//			this.inserirClienteConta(conta, imovel);
//			this.inserirContaImpostosDeduzidos(conta, gerarImpostosDeduzidosContaHelper);
//			this.inserirDebitoCobrado(gerarDebitoCobradoHelper.getMapDebitosCobrados(), conta);
//			this.atualizarDebitoACobrarFaturamento(gerarDebitoCobradoHelper.getColecaoDebitoACobrar());
//			this.inserirCreditoRealizado(gerarCreditoRealizadoHelper.getMapCreditoRealizado(), conta);
//			this.atualizarCreditoARealizar(gerarCreditoRealizadoHelper.getColecaoCreditoARealizar());

//			if (imovel.getIndicadorDebitoConta().equals(ConstantesSistema.SIM) && conta.getContaMotivoRevisao() == null) {
//				this.gerarMovimentoDebitoAutomatico(imovel, conta, faturamentoGrupo);
//			}

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

	private GerarContaTO buildGerarContaTO(FaturamentoImovelTO faturamentoTO, 
			DebitosContaTO gerarDebitoCobradoHelper, CreditosContaTO creditosContaTO,
			ImpostosDeduzidosContaTO impostosDeduzidosTO) {
		GerarContaTO gerarTO = new GerarContaTO();
		gerarTO.setFaturamentoGrupo(faturamentoTO.getFaturamentoGrupo());
		gerarTO.setImovel(faturamentoTO.getImovel());
		gerarTO.setRota(faturamentoTO.getRota());
		gerarTO.setDataVencimentoRota(faturamentoTO.getDataVencimentoConta());
		gerarTO.setFaturamentoGrupo(faturamentoTO.getFaturamentoGrupo());
		gerarTO.setAnoMesFaturamento(faturamentoTO.getAnoMesFaturamento());
		gerarTO.setValorTotalCreditos(creditosContaTO.getValorTotalCreditos());
		gerarTO.setValorTotalDebitos(gerarDebitoCobradoHelper.getValorTotalDebito());
		gerarTO.setValorTotalImposto(impostosDeduzidosTO.getValorTotalImposto());
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
			contaCategoria.setUltimaAlteracao(new java.sql.Date(System.currentTimeMillis()));
			
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
	
	private boolean deveFaturar(Imovel imovel, Integer anoMesFaturamento) {
		boolean faturar = true;

		if (imovel.getFaturamentoSituacaoTipo() != null) {

			List<FaturamentoSituacaoHistorico> faturamentosSituacaoHistorico = faturamentoSituacaoRepositorio.faturamentosHistoricoVigentesPorImovel(imovel.getId());
			FaturamentoSituacaoHistorico faturamentoSituacaoHistorico = faturamentosSituacaoHistorico.get(0);

			if ((faturamentoSituacaoHistorico != null 
					&& anoMesFaturamento >= faturamentoSituacaoHistorico.getAnoMesFaturamentoSituacaoInicio() 
					&& anoMesFaturamento <= faturamentoSituacaoHistorico.getAnoMesFaturamentoSituacaoFim())
					&& imovel.paralisacaoFaturamento() 
					&& imovel.faturamentoAguaValido()) {
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