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
import br.gov.model.atendimentopublico.LigacaoEsgoto;
import br.gov.model.cadastro.ClienteImovel;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.ContaCategoria;
import br.gov.model.faturamento.ContaCategoriaPK;
import br.gov.model.faturamento.ContaImpostosDeduzidos;
import br.gov.model.faturamento.DebitoCobrado;
import br.gov.model.faturamento.DebitoCobradoCategoria;
import br.gov.model.faturamento.FaturamentoSituacaoHistorico;
import br.gov.model.faturamento.ImpostoTipo;
import br.gov.servicos.atendimentopublico.LigacaoEsgotoRepositorio;
import br.gov.servicos.cadastro.ClienteContaRepositorio;
import br.gov.servicos.cadastro.ClienteImovelRepositorio;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.faturamento.ContaCategoriaRepositorio;
import br.gov.servicos.faturamento.ContaImpostosDeduzidosRepositorio;
import br.gov.servicos.faturamento.DebitoCobradoCategoriaRepositorio;
import br.gov.servicos.faturamento.DebitoCobradoRepositorio;
import br.gov.servicos.faturamento.FaturamentoSituacaoRepositorio;
import br.gov.servicos.to.CreditosContaTO;
import br.gov.servicos.to.DebitosContaTO;
import br.gov.servicos.to.FaturamentoAguaEsgotoTO;
import br.gov.servicos.to.GerarContaTO;
import br.gov.servicos.to.ImovelSubcategoriaTO;
import br.gov.servicos.to.ImpostoDeduzidoTO;
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
	private ClienteContaRepositorio clienteContaRepositorio;
	
	@EJB
	private ClienteImovelRepositorio clienteImovelRepositorio;
	
	@EJB
	private ContaCategoriaRepositorio contaCategoriaRepositorio;
	
	@EJB
	private ContaImpostosDeduzidosRepositorio contaImpostosDeduzidosRepositorio;
	
	@EJB
	private DebitoCobradoRepositorio debitoCobradoRepositorio;
	
	@EJB
	private DebitoCobradoCategoriaRepositorio debitoCobradoCategoriaRepositorio;
	
	private FaturamentoAguaEsgotoTO faturamentoAguaEsgotoTO;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void preDeterminarFaturamentoImovel(FaturamentoImovelTO faturamentoTO) throws Exception {
		Imovel imovel = faturamentoTO.getImovel();
		Integer anoMesFaturamento = faturamentoTO.getAnoMesFaturamento();

		Collection<ImovelSubcategoriaTO> colecaoCategoriaOUSubcategoria = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasPorImovel(imovel.getId());

		inicializaValoresAguaEsgoto(imovel, anoMesFaturamento);
		
		if (analisadorGeracaoConta.verificarGeracaoConta(faturamentoAguaEsgotoTO.isAguaEsgotoZerados(), anoMesFaturamento, imovel)) {
			faturamentoAguaEsgotoTO.setValorTotalAgua(BigDecimal.ZERO);
			faturamentoAguaEsgotoTO.setValorTotalEsgoto(BigDecimal.ZERO);

			DebitosContaTO debitosConta = debitosContaBO.gerarDebitosConta(imovel, anoMesFaturamento);
			CreditosContaTO creditosConta = creditosContaBO.gerarCreditosConta(imovel, anoMesFaturamento);

			ImpostosDeduzidosContaTO impostosDeduzidosConta = impostosContaBO.gerarImpostosDeduzidosConta(imovel.getId(),
							anoMesFaturamento, faturamentoAguaEsgotoTO.getValorTotalAgua(), faturamentoAguaEsgotoTO.getValorTotalEsgoto(),
							debitosConta.getValorTotalDebito(), creditosConta.getValorTotalCreditos());

			GerarContaTO gerarTO = buildGerarContaTO(faturamentoTO, debitosConta, creditosConta, impostosDeduzidosConta);
			Conta conta = contaBO.gerarConta(gerarTO);

			Collection<ContaCategoria> contasCategoria = this.gerarContaCategoriaValoresZerados(conta, colecaoCategoriaOUSubcategoria);
			contaCategoriaRepositorio.inserir(contasCategoria);

			List<ClienteImovel> clienteImovelAtivos = clienteImovelRepositorio.pesquisarClienteImovelAtivos(imovel);
			clienteContaRepositorio.inserir(clienteImovelAtivos, conta);
			
			this.inserirContaImpostosDeduzidos(conta, impostosDeduzidosConta);
			this.inserirDebitoCobrado(debitosConta, conta);
//			this.atualizarDebitoACobrarFaturamento(gerarDebitoCobradoHelper.getColecaoDebitoACobrar());
//			this.inserirCreditoRealizado(gerarCreditoRealizadoHelper.getMapCreditoRealizado(), conta);
//			this.atualizarCreditoARealizar(gerarCreditoRealizadoHelper.getColecaoCreditoARealizar());

//			if (imovel.getIndicadorDebitoConta().equals(ConstantesSistema.SIM) && conta.getContaMotivoRevisao() == null) {
//				this.gerarMovimentoDebitoAutomatico(imovel, conta, faturamentoGrupo);
//			}
		}
	}
	
	private void inserirContaImpostosDeduzidos(Conta conta, ImpostosDeduzidosContaTO gerarImpostosDeduzidosContaHelper) {
		Collection<ImpostoDeduzidoTO> impostosDeduzidosTO = gerarImpostosDeduzidosContaHelper.getListaImpostosDeduzidos();
		Collection<ContaImpostosDeduzidos> contasImpostosDeduzidos = new ArrayList<ContaImpostosDeduzidos>();
		
		for (ImpostoDeduzidoTO impostoDeduzidoTO : impostosDeduzidosTO) {
			ContaImpostosDeduzidos contaImpostosDeduzidos = new ContaImpostosDeduzidos();
			contaImpostosDeduzidos.setConta(conta);
			
			ImpostoTipo impostoTipo = new ImpostoTipo();
			impostoTipo.setId(impostoDeduzidoTO.getIdImpostoTipo());
			
			contaImpostosDeduzidos.setImpostoTipo(impostoTipo);
			contaImpostosDeduzidos.setValorImposto(impostoDeduzidoTO.getValor());
			contaImpostosDeduzidos.setPercentualAliquota(impostoDeduzidoTO.getPercentualAliquota());
			contaImpostosDeduzidos.setValorBaseCalculo(gerarImpostosDeduzidosContaHelper.getValorBaseCalculo());
			contaImpostosDeduzidos.setUltimaAlteracao(new Date());
			
			contasImpostosDeduzidos.add(contaImpostosDeduzidos);
		}

		if (contasImpostosDeduzidos != null && !contasImpostosDeduzidos.isEmpty()) {
			contaImpostosDeduzidosRepositorio.inserir(contasImpostosDeduzidos);
			contasImpostosDeduzidos.clear();
			contasImpostosDeduzidos = null;
		}
	}
	
	private void inserirDebitoCobrado(DebitosContaTO debitosContaTO, Conta conta) {
		for (DebitoCobrado debitoCobrado : debitosContaTO.getDebitosCobrados()) {
			debitoCobrado.setConta(conta);
			debitoCobrado.setCobradoEm(new Date());
			debitoCobrado.setUltimaAlteracao(new Date());

			Long idDebitoCobrado = debitoCobradoRepositorio.inserir(debitoCobrado);
			
			List<DebitoCobradoCategoria> debitoCobradoCategorias = debitosContaTO.getCategorias(debitoCobrado);
			
			Collection<DebitoCobradoCategoria> debitosCobradosCategoria = new ArrayList<DebitoCobradoCategoria>();
			for (DebitoCobradoCategoria debitoCobradoCategoria : debitoCobradoCategorias) {
				debitoCobradoCategoria.getId().setDebitoCobradoId(idDebitoCobrado);
				debitoCobradoCategoria.setUltimaAlteracao(new Date());
				
				debitosCobradosCategoria.add(debitoCobradoCategoria);
			}

			debitoCobradoCategoriaRepositorio.inserir(debitosCobradosCategoria);

			if (debitosCobradosCategoria != null) {
				debitosCobradosCategoria.clear();
				debitosCobradosCategoria = null;
			}
		}
	}

	private void inicializaValoresAguaEsgoto(Imovel imovel, Integer anoMesFaturamento) throws Exception {
		faturamentoAguaEsgotoTO = new FaturamentoAguaEsgotoTO();

		if (possuiLigacaoAguaEsgotoAtivo(imovel) || possuiHidrometro(imovel)) {

			if (deveFaturar(imovel, anoMesFaturamento)) {
				faturamentoAguaEsgotoTO.setAguaEsgotoZerados(false);
				faturamentoAguaEsgotoTO.setValorTotalAgua(BigDecimal.ONE);
				faturamentoAguaEsgotoTO.setValorTotalEsgoto(BigDecimal.ONE);
			} else {
				faturamentoAguaEsgotoTO.setAguaEsgotoZerados(true);
				faturamentoAguaEsgotoTO.setValorTotalAgua(BigDecimal.ZERO);
				faturamentoAguaEsgotoTO.setValorTotalEsgoto(BigDecimal.ZERO);
			}

			LigacaoEsgoto ligacaoEsgoto = ligacaoEsgotoRepositorio.buscarLigacaoEsgotoPorIdImovel(imovel.getId());
			
			if (ligacaoEsgoto != null){
				faturamentoAguaEsgotoTO.setPercentualEsgoto(this.verificarPercentualEsgotoAlternativo(ligacaoEsgoto, imovel));
				faturamentoAguaEsgotoTO.setPercentualColetaEsgoto(ligacaoEsgoto.getPercentualAguaConsumidaColetada());
			}
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
		gerarTO.setPercentualEsgoto(faturamentoAguaEsgotoTO.getPercentualEsgoto());
		gerarTO.setPercentualColeta(faturamentoAguaEsgotoTO.getPercentualColetaEsgoto());
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