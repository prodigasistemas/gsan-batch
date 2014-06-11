package br.gov.batch.servicos.faturamento;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.cadastro.servicos.CategoriaBO;
import br.gov.model.cadastro.Categoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.CreditoRealizado;
import br.gov.model.faturamento.CreditoRealizadoCategoria;
import br.gov.model.faturamento.CreditoRealizar;
import br.gov.model.faturamento.CreditoRealizarCategoria;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.servicos.faturamento.CreditoRealizarCategoriaRepositorio;
import br.gov.servicos.faturamento.CreditoRealizarRepositorio;
import br.gov.servicos.to.CreditoRealizadoTO;
import br.gov.servicos.to.ValoresFaturamentoAguaEsgotoTO;

@Stateless
public class CreditoRealizadoBO {

	@EJB
	private CategoriaBO categoriaBO;

	@EJB
	private CreditoRealizarCategoriaRepositorio creditoRealizarCategoriaRepositorio;

	@EJB
	private CreditoRealizarRepositorio creditoRealizarRepositorio;

	public CreditoRealizadoTO gerarCreditoRealizado(Imovel imovel, Integer anoMesFaturamento,
			ValoresFaturamentoAguaEsgotoTO valoresAguaEsgotoTO, BigDecimal valorTotalDebitos,
			boolean gerarAtividadeGrupoFaturamento, boolean preFaturamento) {

		Collection<CreditoRealizar> colecaoCreditosRealizar = getColecaoCreditosRealizar(imovel, anoMesFaturamento, preFaturamento);

		BigDecimal valorTotalACobrar = calculaValorTotalACobrar(valoresAguaEsgotoTO, valorTotalDebitos, preFaturamento);

		return gerarCreditoRealizado(anoMesFaturamento, valorTotalACobrar, gerarAtividadeGrupoFaturamento, preFaturamento, colecaoCreditosRealizar);
	}

	private CreditoRealizadoTO gerarCreditoRealizado(Integer anoMesFaturamento, BigDecimal valorTotalACobrar, 
			boolean gerarAtividadeGrupoFaturamento, boolean preFaturamento, Collection<CreditoRealizar> colecaoCreditosRealizar) {

		CreditoRealizadoTO creditoRealizadoTO = new CreditoRealizadoTO();

		Iterator<CreditoRealizar> iteratorColecaoCreditosARealizar = colecaoCreditosRealizar.iterator();
		CreditoRealizar creditoRealizar = null;

		while (iteratorColecaoCreditosARealizar.hasNext() && valorTotalACobrar.compareTo(new BigDecimal("0.00")) == 1) {

			creditoRealizar = (CreditoRealizar) iteratorColecaoCreditosARealizar.next();

			creditoRealizar.setAnoMesReferenciaPrestacao(anoMesFaturamento);
			creditoRealizar.setValorResidualConcedidoMes(creditoRealizar.getValorResidualMesAnterior());
			
			Short numeroParcelaBonus = getNumeroParcelaBonus(creditoRealizar);
			if (numeroPrestacoesRealizadasMenorQueNumeroPrestacoesCredito(creditoRealizar, numeroParcelaBonus)) {
				creditoRealizar.setNumeroPrestacaoRealizada(new Integer(creditoRealizar.getNumeroPrestacaoRealizada().intValue() + 1).shortValue());
			}

			BigDecimal valorCredito = calculaValorCorrespondenteParcelaMes(creditoRealizar, numeroParcelaBonus);

			if (!preFaturamento) {
				valorTotalACobrar = valorTotalACobrar.subtract(valorCredito);
			}

			if (valorTotalACobrar.compareTo(new BigDecimal("0.00")) == -1) {

				creditoRealizar.setValorResidualMesAnterior(valorTotalACobrar.multiply(new BigDecimal("-1")));

				valorCredito = valorCredito.subtract(creditoRealizar.getValorResidualMesAnterior());

				valorTotalACobrar = new BigDecimal("0.00");

			} else if (!preFaturamento) {
				creditoRealizar.setValorResidualMesAnterior(new BigDecimal("0.00"));
			}

			creditoRealizadoTO.somaValorTotalCreditos(valorCredito);

			if (gerarAtividadeGrupoFaturamento) {

				CreditoRealizado creditoRealizado = criarCreditoRealizado(creditoRealizar, numeroParcelaBonus, valorCredito);

				Collection<CreditoRealizadoCategoria> colecaoCreditosRealizadoCategoria = obterColecaoCreditosRealizadoCategoria(
						creditoRealizado, creditoRealizar, valorCredito);

				creditoRealizadoTO.putCategoriasPorCreditoRealizado(creditoRealizado, colecaoCreditosRealizadoCategoria);
				creditoRealizadoTO.addCreditoRealizar(creditoRealizar);
			}

			if (creditoRealizadoTO.possuiCreditoTipo(creditoRealizar.getCreditoTipo())) {
				BigDecimal valor = creditoRealizadoTO.getMapValoresPorTipoCredito().get(creditoRealizar.getCreditoTipo());
				creditoRealizadoTO.putValoresPorCreditoTipo(creditoRealizar.getCreditoTipo(), somaBigDecimal(valor, valorCredito));
			} else {
				creditoRealizadoTO.putValoresPorCreditoTipo(creditoRealizar.getCreditoTipo(), valorCredito);
			}
		}

		return creditoRealizadoTO;
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
			creditoRealizadoCategoria.setQuantidadeEconomia(categoria.getQuantidadeEconomiasCategoria());
			colecaoCreditosRealizadoCategoria.add(creditoRealizadoCategoria);
		}

		return colecaoCreditosRealizadoCategoria;
	}

	private Collection<Categoria> obterColecaoCategorias(CreditoRealizar creditoRealizar) {
		Collection<CreditoRealizarCategoria> colecaoCreditoARealizarCategoria = creditoRealizarCategoriaRepositorio.obterCreditoRealizarCategoria(creditoRealizar.getId());

		Iterator<CreditoRealizarCategoria> colecaoCreditoARealizarCategoriaIterator = colecaoCreditoARealizarCategoria.iterator();
		Collection<Categoria> colecaoCategorias = new ArrayList<Categoria>();

		while (colecaoCreditoARealizarCategoriaIterator.hasNext()) {
			CreditoRealizarCategoria creditoRealizarCategoria = (CreditoRealizarCategoria) colecaoCreditoARealizarCategoriaIterator.next();
			Categoria categoria = new Categoria();
			categoria.setId(creditoRealizarCategoria.getCategoriaId());
			categoria.setQuantidadeEconomiasCategoria(creditoRealizarCategoria.getQuantidadeEconomia());
			colecaoCategorias.add(categoria);
		}
		return colecaoCategorias;
	}

	private CreditoRealizado criarCreditoRealizado(CreditoRealizar creditoRealizar, Short numeroParcelaBonus, BigDecimal valorCredito) {
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
		creditoRealizado.setNumeroParcelaBonus(numeroParcelaBonus);
		creditoRealizado.setNumeroPrestacaoCredito(creditoRealizar.getNumeroPrestacaoRealizada());
		creditoRealizado.setCreditoRealizarGeral(creditoRealizar.getCreditoRealizarGeral());
		return creditoRealizado;
	}

	public BigDecimal calculaValorCorrespondenteParcelaMes(CreditoRealizar creditoRealizar, Short numeroParcelaBonus) {
		BigDecimal valorCorrespondenteParcelaMes = new BigDecimal("0.00");

		if (numeroPrestacoesRealizadasMenorQueNumeroPrestacoesCredito(creditoRealizar, numeroParcelaBonus)) {
			valorCorrespondenteParcelaMes = creditoRealizar.getValorCredito().divide(new BigDecimal(
					creditoRealizar.getNumeroPrestacaoCredito()), 2, BigDecimal.ROUND_DOWN);

			if (ehUltimaParcela(creditoRealizar, numeroParcelaBonus)) {

				BigDecimal valorMesVezesPrestacaoCredito = valorCorrespondenteParcelaMes.multiply(
						new BigDecimal(creditoRealizar.getNumeroPrestacaoCredito())).setScale(2);

				valorCorrespondenteParcelaMes = valorCorrespondenteParcelaMes.add(creditoRealizar.getValorCredito()).subtract(valorMesVezesPrestacaoCredito);
			}
		}

		return valorCorrespondenteParcelaMes.add(creditoRealizar.getValorResidualMesAnterior());
	}

	private boolean ehUltimaParcela(CreditoRealizar creditoRealizar, Short numeroParcelaBonus) {
		return creditoRealizar.getNumeroPrestacaoRealizada().intValue() == 
				((creditoRealizar.getNumeroPrestacaoCredito().intValue() - numeroParcelaBonus.intValue()) - 1);
	}

	private boolean numeroPrestacoesRealizadasMenorQueNumeroPrestacoesCredito(CreditoRealizar creditoRealizar, Short numeroParcelaBonus) {
		return creditoRealizar.getNumeroPrestacaoRealizada().intValue() <
		(creditoRealizar.getNumeroPrestacaoCredito().intValue() - numeroParcelaBonus.intValue());
	}

	private Short getNumeroParcelaBonus(CreditoRealizar creditoRealizar) {
		if (creditoRealizar.getNumeroParcelaBonus() != null) {
			return creditoRealizar.getNumeroParcelaBonus();
		} else {
			return 0;
		}
	}

	private Collection<CreditoRealizar> getColecaoCreditosRealizar(Imovel imovel, Integer anoMesFaturamento, boolean preFaturamento) {
		Collection<CreditoRealizar> colecaoCreditosRealizar = creditoRealizarRepositorio.obterCreditoRealizarImovel(
				imovel.getId(), DebitoCreditoSituacao.NORMAL, anoMesFaturamento);

		if (colecaoCreditosRealizar == null) {
			colecaoCreditosRealizar = new ArrayList<CreditoRealizar>();
		}

		if (preFaturamento) {
			Collection<CreditoRealizar> colecaoCreditosRealizarNitrato = creditoRealizarRepositorio.obterCreditoRealizarImovel(
					imovel.getId(), DebitoCreditoSituacao.PRE_FATURADA, anoMesFaturamento);

			if (colecaoCreditosRealizarNitrato != null && !colecaoCreditosRealizarNitrato.isEmpty()) {
				colecaoCreditosRealizar.addAll(colecaoCreditosRealizarNitrato);
			}

		}
		return colecaoCreditosRealizar;
	}

	public BigDecimal calculaValorTotalACobrar(ValoresFaturamentoAguaEsgotoTO valoresAguaEsgotoTO, BigDecimal valorTotalDebitos, boolean preFaturamento) {
		if (preFaturamento) {
			return BigDecimal.ONE;
		}
		BigDecimal valorTotalACobrar = new BigDecimal("0.00");
		BigDecimal somaAguaEsgoto = valoresAguaEsgotoTO.getValorTotalAgua().add(valoresAguaEsgotoTO.getValorTotalEsgoto());

		return valorTotalACobrar.add(somaAguaEsgoto).add(valorTotalDebitos);
	}

	public BigDecimal somaBigDecimal(BigDecimal value1, BigDecimal value2) {

		BigDecimal v1 = BigDecimal.ZERO;
		BigDecimal v2 = BigDecimal.ZERO;

		if (value1 != null) {
			v1 = value1;
		}
		if (value2 != null) {
			v2 = value2;
		}

		return v1.add(v2);
	}
}