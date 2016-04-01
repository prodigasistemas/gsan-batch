package br.gov.batch.servicos.faturamento;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.cadastro.CategoriaBO;
import br.gov.model.cadastro.Categoria;
import br.gov.model.faturamento.CreditoRealizado;
import br.gov.model.faturamento.CreditoRealizadoCategoria;
import br.gov.model.faturamento.CreditoRealizar;
import br.gov.model.faturamento.CreditoRealizarCategoria;
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
	
	public CreditosContaTO gerarCreditosConta(Integer idImovel, Integer anoMesFaturamento) {

		Collection<CreditoRealizar> creditosRealizar = creditosRealizar(idImovel, anoMesFaturamento);
		
		return gerarCreditos(anoMesFaturamento, creditosRealizar);
	}

	public CreditosContaTO gerarCreditos(Integer anoMesFaturamento, Collection<CreditoRealizar> creditosRealizar) {
		creditosConta = new CreditosContaTO();

		for (CreditoRealizar creditoRealizar: creditosRealizar) {
			creditoRealizar.setAnoMesReferenciaPrestacao(anoMesFaturamento);
			creditoRealizar.setValorResidualConcedidoMes(creditoRealizar.getValorResidualMesAnterior());
			
			BigDecimal valorCredito = creditoRealizar.calculaValorCredito();
			
			creditosConta.somaValorTotalCreditos(valorCredito);

			criarAtividadeGrupoFaturamento(creditoRealizar, valorCredito);
			
			creditosConta.atualizarValorCredito(creditoRealizar.getCreditoTipo(), valorCredito);
		}

		return creditosConta;
	}
	
	public void criarAtividadeGrupoFaturamento(CreditoRealizar creditoRealizar, BigDecimal valorCreditoParcelaMes) {
		CreditoRealizado creditoRealizado = criarCreditoRealizado(creditoRealizar, valorCreditoParcelaMes);

		Collection<CreditoRealizadoCategoria> colecaoCreditosRealizadoCategoria = obterColecaoCreditosRealizadoCategoria(
				creditoRealizado, creditoRealizar.getId(), valorCreditoParcelaMes);

		creditosConta.putCategoriasPorCreditoRealizado(creditoRealizado, colecaoCreditosRealizadoCategoria);
		creditosConta.addCreditoRealizar(creditoRealizar);
	}

	private Collection<CreditoRealizadoCategoria> obterColecaoCreditosRealizadoCategoria(CreditoRealizado creditoRealizado, 
			Integer idCreditoRealizar, BigDecimal valorCredito) {
		Collection<Categoria> colecaoCategorias = obterColecaoCategorias(idCreditoRealizar);
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

	public Collection<Categoria> obterColecaoCategorias(Integer idCreditoRealizar) {
		Collection<CreditoRealizarCategoria> colecaoCreditoARealizarCategoria = creditoRealizarCategoriaRepositorio.buscarCreditoRealizarCategoria(idCreditoRealizar);

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

	public CreditoRealizado criarCreditoRealizado(CreditoRealizar creditoRealizar, BigDecimal valorCredito) {
		CreditoRealizado creditoRealizado = new CreditoRealizado().new Builder()
		        .creditoRealizar(creditoRealizar)
		        .valorCredito(valorCredito)
		        .build();
		return creditoRealizado;
	}

	public Collection<CreditoRealizar> creditosRealizar(Integer idImovel, Integer anoMesFaturamento) {
		Collection<CreditoRealizar> colecaoCreditosRealizar = creditoRealizarRepositorio.buscarCreditoRealizarPorImovel(
		        idImovel, DebitoCreditoSituacao.NORMAL, anoMesFaturamento);

		if (colecaoCreditosRealizar == null) {
			colecaoCreditosRealizar = new ArrayList<CreditoRealizar>();
		}

		Collection<CreditoRealizar> colecaoCreditosRealizarNitrato = creditoRealizarRepositorio.buscarCreditoRealizarPorImovel(
		        idImovel, DebitoCreditoSituacao.PRE_FATURADA, anoMesFaturamento);

		if (colecaoCreditosRealizarNitrato != null && !colecaoCreditosRealizarNitrato.isEmpty()) {
			colecaoCreditosRealizar.addAll(colecaoCreditosRealizarNitrato);
		}

		return colecaoCreditosRealizar;
	}
}