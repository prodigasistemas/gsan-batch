package br.gov.batch.servicos.faturamento;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.cadastro.CategoriaBO;
import br.gov.model.cadastro.Categoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.CreditoRealizado;
import br.gov.model.faturamento.CreditoRealizadoCategoria;
import br.gov.model.faturamento.CreditoRealizar;
import br.gov.model.faturamento.CreditoRealizarCategoria;
import br.gov.model.faturamento.CreditoTipo;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.CreditoRealizarCategoriaRepositorio;
import br.gov.servicos.faturamento.CreditoRealizarRepositorio;
import br.gov.servicos.to.CreditosContaTO;

@Stateless
public class CreditosContaBO {

	@EJB
	private CategoriaBO categoriaBO;

	@EJB
	private CreditoRealizarCategoriaRepositorio creditoRealizarCategoriaRepositorio;

	@EJB
	private CreditoRealizarRepositorio creditoRealizarRepositorio;
	
	@EJB
	private SistemaParametrosRepositorio sistemaParametrosRepositorio;
	
	private CreditosContaTO creditosConta;
	
	public CreditosContaTO gerarCreditosConta(Imovel imovel, Integer anoMesFaturamento) {

		Collection<CreditoRealizar> creditosRealizar = creditosRealizar(imovel);
		
		return gerarCreditos(anoMesFaturamento, creditosRealizar);
	}

	private CreditosContaTO gerarCreditos(Integer anoMesFaturamento, Collection<CreditoRealizar> creditosRealizar) {
		creditosConta = new CreditosContaTO();

		for (CreditoRealizar creditoRealizar: creditosRealizar) {
			creditoRealizar.setAnoMesReferenciaPrestacao(anoMesFaturamento);
			creditoRealizar.setValorResidualConcedidoMes(creditoRealizar.getValorResidualMesAnterior());
			
			BigDecimal valorCreditoParcelaMes = BigDecimal.ZERO;
			
			if (numeroPrestacoesRealizadasMenorQueNumeroPrestacoesCredito(creditoRealizar)) {
				valorCreditoParcelaMes = calculaValorCorrespondenteParcelaMes(creditoRealizar);
				creditoRealizar.setNumeroPrestacaoRealizada(new Integer(creditoRealizar.getNumeroPrestacaoRealizada().intValue() + 1).shortValue());
			}
			
			valorCreditoParcelaMes = valorCreditoParcelaMes.subtract(creditoRealizar.getValorResidualMesAnterior());

			creditosConta.somaValorTotalCreditos(valorCreditoParcelaMes);

			criarAtividadeGrupoFaturamento(creditoRealizar, valorCreditoParcelaMes);

			if (creditosConta.possuiCreditoTipo(creditoRealizar.getCreditoTipo())) {
				BigDecimal valorTotal = somaValorCreditoTipoEValorCreditoParcelaMes(creditoRealizar.getCreditoTipo(), valorCreditoParcelaMes);
				creditosConta.putValoresPorCreditoTipo(creditoRealizar.getCreditoTipo(), valorTotal);
			} else {
				creditosConta.putValoresPorCreditoTipo(creditoRealizar.getCreditoTipo(), valorCreditoParcelaMes);
			}
		}

		return creditosConta;
	}

	private void criarAtividadeGrupoFaturamento(CreditoRealizar creditoRealizar, BigDecimal valorCreditoParcelaMes) {
		CreditoRealizado creditoRealizado = criarCreditoRealizado(creditoRealizar, valorCreditoParcelaMes);

		Collection<CreditoRealizadoCategoria> colecaoCreditosRealizadoCategoria = obterColecaoCreditosRealizadoCategoria(
				creditoRealizado, creditoRealizar, valorCreditoParcelaMes);

		creditosConta.putCategoriasPorCreditoRealizado(creditoRealizado, colecaoCreditosRealizadoCategoria);
		creditosConta.addCreditoRealizar(creditoRealizar);
	}

	private Collection<CreditoRealizadoCategoria> obterColecaoCreditosRealizadoCategoria(CreditoRealizado creditoRealizado, 
			CreditoRealizar creditoRealizar, BigDecimal valorCredito) {
		Collection<Categoria> colecaoCategorias = obterColecaoCategorias(creditoRealizar);
		Collection<BigDecimal> colecaoCategoriasCalculadasValor = categoriaBO.obterValorPorCategoria(colecaoCategorias, valorCredito);

		Iterator<BigDecimal> colecaoCategoriasCalculadasValorIterator = colecaoCategoriasCalculadasValor.iterator();
		Iterator<Categoria> colecaoCategoriasObterValorIterator = colecaoCategorias.iterator();

		CreditoRealizadoCategoria creditoRealizadoCategoria = null;
		Collection<CreditoRealizadoCategoria> colecaoCreditosRealizadoCategoria = new ArrayList<CreditoRealizadoCategoria>();

		while (colecaoCategoriasCalculadasValorIterator.hasNext() && colecaoCategoriasObterValorIterator.hasNext()) {

			BigDecimal valorPorCategoria = (BigDecimal) colecaoCategoriasCalculadasValorIterator.next();

			Categoria categoria = (Categoria) colecaoCategoriasObterValorIterator.next();

			creditoRealizadoCategoria = new CreditoRealizadoCategoria(creditoRealizado.getId(), categoria.getId());
			creditoRealizadoCategoria.setValorCategoria(valorPorCategoria);
			creditoRealizadoCategoria.setCreditoRealizado(creditoRealizado);
			creditoRealizadoCategoria.setCategoria(categoria);
			creditoRealizadoCategoria.setQuantidadeEconomia(categoria.getQuantidadeEconomias());
			colecaoCreditosRealizadoCategoria.add(creditoRealizadoCategoria);
		}

		return colecaoCreditosRealizadoCategoria;
	}

	private Collection<Categoria> obterColecaoCategorias(CreditoRealizar creditoRealizar) {
		Collection<CreditoRealizarCategoria> colecaoCreditoARealizarCategoria = creditoRealizarCategoriaRepositorio.buscarCreditoRealizarCategoria(creditoRealizar.getId());

		Iterator<CreditoRealizarCategoria> colecaoCreditoARealizarCategoriaIterator = colecaoCreditoARealizarCategoria.iterator();
		Collection<Categoria> colecaoCategorias = new ArrayList<Categoria>();

		while (colecaoCreditoARealizarCategoriaIterator.hasNext()) {
			CreditoRealizarCategoria creditoRealizarCategoria = (CreditoRealizarCategoria) colecaoCreditoARealizarCategoriaIterator.next();
			Categoria categoria = new Categoria();
			categoria.setId(creditoRealizarCategoria.getCategoriaId());
			categoria.setQuantidadeEconomias(creditoRealizarCategoria.getQuantidadeEconomia());
			colecaoCategorias.add(categoria);
		}
		return colecaoCategorias;
	}

	private CreditoRealizado criarCreditoRealizado(CreditoRealizar creditoRealizar, BigDecimal valorCredito) {
		CreditoRealizado creditoRealizado = new CreditoRealizado();
		creditoRealizado.setCreditoTipo(creditoRealizar.getCreditoTipo());
		creditoRealizado.setCreditoRealizado(creditoRealizar.getGeracaoCredito());
		creditoRealizado.setLancamentoItemContabil(creditoRealizar.getLancamentoItemContabil());
		creditoRealizado.setLocalidade(creditoRealizar.getLocalidade());
		creditoRealizado.setNumeroQuadra(creditoRealizar.getNumeroQuadra());
		creditoRealizado.setCodigoSetorComercial(creditoRealizar.getCodigoSetorComercial());
		creditoRealizado.setNumeroQuadra(creditoRealizar.getNumeroQuadra());
		creditoRealizado.setNumeroLote(creditoRealizar.getNumeroLote());
		creditoRealizado.setNumeroSublote(creditoRealizar.getNumeroSublote());
		creditoRealizado.setAnoMesReferenciaCredito(creditoRealizar.getAnoMesReferenciaCredito());
		creditoRealizado.setAnoMesCobrancaCredito(creditoRealizar.getAnoMesCobrancaCredito());
		creditoRealizado.setValorCredito(valorCredito);
		creditoRealizado.setCreditoOrigem(creditoRealizar.getCreditoOrigem());
		creditoRealizado.setNumeroPrestacao(creditoRealizar.getNumeroPrestacaoCredito());
		creditoRealizado.setNumeroParcelaBonus(creditoRealizar.getNumeroParcelaBonus());
		creditoRealizado.setNumeroPrestacaoCredito(creditoRealizar.getNumeroPrestacaoRealizada());
		creditoRealizado.setCreditoRealizarGeral(creditoRealizar.getCreditoRealizarGeral());
		return creditoRealizado;
	}

	public BigDecimal calculaValorCorrespondenteParcelaMes(CreditoRealizar creditoRealizar) {
		BigDecimal valorCorrespondenteParcelaMes = BigDecimal.ZERO;

		if (numeroPrestacoesRealizadasMenorQueNumeroPrestacoesCredito(creditoRealizar)) {
			valorCorrespondenteParcelaMes = creditoRealizar.getValorCredito().divide(new BigDecimal(
					creditoRealizar.getNumeroPrestacaoCredito()), 2, BigDecimal.ROUND_DOWN);

			if (ehUltimaParcela(creditoRealizar)) {

				BigDecimal valorMesVezesPrestacaoCredito = valorCorrespondenteParcelaMes.multiply(
						new BigDecimal(creditoRealizar.getNumeroPrestacaoCredito())).setScale(2);

				valorCorrespondenteParcelaMes = valorCorrespondenteParcelaMes.add(creditoRealizar.getValorCredito()).subtract(valorMesVezesPrestacaoCredito);
			}
		}

		return valorCorrespondenteParcelaMes.add(creditoRealizar.getValorResidualMesAnterior());
	}

	private boolean ehUltimaParcela(CreditoRealizar creditoRealizar) {
		return creditoRealizar.getNumeroPrestacaoRealizada().intValue() == 
				((creditoRealizar.getNumeroPrestacaoCredito().intValue() - creditoRealizar.getNumeroParcelaBonus().intValue()) - 1);
	}

	private boolean numeroPrestacoesRealizadasMenorQueNumeroPrestacoesCredito(CreditoRealizar creditoRealizar) {
		return creditoRealizar.getNumeroPrestacaoRealizada().intValue() <
		(creditoRealizar.getNumeroPrestacaoCredito().intValue() - creditoRealizar.getNumeroParcelaBonus().intValue());
	}

	private Collection<CreditoRealizar> creditosRealizar(Imovel imovel) {
		Integer anoMesFaturamento = sistemaParametrosRepositorio.getSistemaParametros().getAnoMesFaturamento();
		
		Collection<CreditoRealizar> colecaoCreditosRealizar = creditoRealizarRepositorio.buscarCreditoRealizarPorImovel(
				imovel.getId(), DebitoCreditoSituacao.NORMAL, anoMesFaturamento);

		if (colecaoCreditosRealizar == null) {
			colecaoCreditosRealizar = new ArrayList<CreditoRealizar>();
		}

		Collection<CreditoRealizar> colecaoCreditosRealizarNitrato = creditoRealizarRepositorio.buscarCreditoRealizarPorImovel(
				imovel.getId(), DebitoCreditoSituacao.PRE_FATURADA, anoMesFaturamento);

		if (colecaoCreditosRealizarNitrato != null && !colecaoCreditosRealizarNitrato.isEmpty()) {
			colecaoCreditosRealizar.addAll(colecaoCreditosRealizarNitrato);
		}

		return colecaoCreditosRealizar;
	}

	public BigDecimal somaValorCreditoTipoEValorCreditoParcelaMes(CreditoTipo creditoTipo, BigDecimal valorCreditoParcelaMes) {
		BigDecimal valorCreditoTipo = creditosConta.getValorCreditoTipo(creditoTipo);
		
		if (valorCreditoParcelaMes == null) {
			valorCreditoParcelaMes = BigDecimal.ZERO;
		}
		
		return valorCreditoTipo.add(valorCreditoParcelaMes);
	}
}