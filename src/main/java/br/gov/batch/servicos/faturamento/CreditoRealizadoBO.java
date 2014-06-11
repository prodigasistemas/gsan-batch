package br.gov.batch.servicos.faturamento;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.cadastro.ImovelBO;
import br.gov.model.cadastro.Categoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.CreditoRealizado;
import br.gov.model.faturamento.CreditoRealizadoCategoria;
import br.gov.model.faturamento.CreditoRealizar;
import br.gov.model.faturamento.CreditoRealizarCategoria;
import br.gov.model.faturamento.CreditoTipo;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.servicos.to.CreditoRealizadoTO;
import br.gov.servicos.to.ValoresFaturamentoAguaEsgotoTO;

@Stateless
public class CreditoRealizadoBO {
	
	@EJB
	private ImovelBO imovelBO;

	public CreditoRealizadoTO gerarCreditoRealizado(Imovel imovel, Integer anoMesFaturamento,
			ValoresFaturamentoAguaEsgotoTO valoresAguaEsgotoTO, BigDecimal valorTotalDebitos,
			boolean gerarAtividadeGrupoFaturamento, boolean preFaturamento) {

		CreditoRealizadoTO creditoRealizadoTO = new CreditoRealizadoTO();

		Collection<CreditoRealizar> colecaoCreditosARealizar = this.obterCreditoRealizarImovel(imovel.getId(), DebitoCreditoSituacao.NORMAL, anoMesFaturamento);
		
		if (colecaoCreditosARealizar == null) {
			colecaoCreditosARealizar = new ArrayList<CreditoRealizar>();
		}

		// caso seja por pré-faturar, Impressão Simultânea, pesquisar os
		// créditos de Nitrato
		// também que foram gerados com a situação Pré-Faturada
		if (preFaturamento) {
			Collection<CreditoRealizar> colecaoCreditosARealizarNitrato = this.obterCreditoRealizarImovel(
																					imovel.getId(), DebitoCreditoSituacao.PRE_FATURADA, anoMesFaturamento);

			if (colecaoCreditosARealizarNitrato != null && !colecaoCreditosARealizarNitrato.isEmpty()) {
				colecaoCreditosARealizar.addAll(colecaoCreditosARealizarNitrato);
			}

		}

		BigDecimal valorTotalCreditos = new BigDecimal("0.00");
		BigDecimal valorTotalACobrar = new BigDecimal("0.00");

		BigDecimal parte1 = valorTotalACobrar.add(valoresAguaEsgotoTO.getValorTotalAgua());
		BigDecimal parte2 = parte1.add(valoresAguaEsgotoTO.getValorTotalEsgoto());
		valorTotalACobrar = parte2.add(valorTotalDebitos);

		/*
		 * Para o pré-faturamento todos os créditos a realizar serão
		 * transformados em crédito realizado, independente do valor total a
		 * cobrar.
		 */
		if (preFaturamento) {
			valorTotalACobrar = BigDecimal.ONE;
		}

		Collection<CreditoRealizar> colecaoCreditosARealizarUpdate = new ArrayList<CreditoRealizar>();
		Collection<CreditoRealizado> colecaoCreditosRealizado = new ArrayList<CreditoRealizado>();

		// Cria o map para armazenar os créditos realizados junto com os
		// créditos ralizados por categoria
		Map<CreditoRealizado, Collection<CreditoRealizadoCategoria>> mapCreditoRealizado = null;
		// Cria o map para armazenar os créditos a realizar com seus valores por
		// tipo
		Map<CreditoTipo, BigDecimal> mapValoresPorTipoCredito = null;

		if (colecaoCreditosARealizar != null && !colecaoCreditosARealizar.isEmpty()) {

			mapCreditoRealizado = new HashMap<CreditoRealizado, Collection<CreditoRealizadoCategoria>>();
			mapValoresPorTipoCredito = new HashMap<CreditoTipo, BigDecimal>();

			Iterator<CreditoRealizar> iteratorColecaoCreditosARealizar = colecaoCreditosARealizar.iterator();

			CreditoRealizar creditoRealizar = null;

			/*
			 * Para cada crédito a realizar selecionado e até que o valor total
			 * a cobrar seja igual a zero.
			 */
			while (iteratorColecaoCreditosARealizar.hasNext() && valorTotalACobrar.compareTo(new BigDecimal("0.00")) == 1) {

				creditoRealizar = (CreditoRealizar) iteratorColecaoCreditosARealizar.next();

				BigDecimal valorCorrespondenteParcelaMes = new BigDecimal("0.00");
				BigDecimal valorCredito = new BigDecimal("0.00");

				/*
				 * criação do bonus para parcelamento com RD especial
				 */
				Short numeroParcelaBonus = 0;
				if (creditoRealizar.getNumeroParcelaBonus() != null) {
					numeroParcelaBonus = creditoRealizar.getNumeroParcelaBonus();
				}

				/*
				 * Caso o nº de prestações realizadas seja menor que o nº de
				 * prestação dos créditos calcula o valor correspondente da
				 * parcela do mês.
				 */
				if (creditoRealizar.getNumeroPrestacaoRealizada().intValue() < 
						(creditoRealizar.getNumeroPrestacaoCredito().intValue() - numeroParcelaBonus.intValue())) {

					/*
					 * Valor correspondente da parcela do mês = valor do crédito
					 * / nº de pestações do crédito.
					 */
					valorCorrespondenteParcelaMes = creditoRealizar.getValorCredito().divide(new BigDecimal(
														creditoRealizar.getNumeroPrestacaoCredito()), 2, BigDecimal.ROUND_DOWN);

					/*
					 * Caso seja a última pretação o valor do crédito
					 * correspondente a parcela do mês = valor do crédito
					 * correspondente a parcela do mês + valor do crédito -
					 * (valor do crédito correspondente a parcela do mês * (o nº
					 * de prestação dos créditos menos o numero de parcela
					 * bonus))
					 */
					if (creditoRealizar.getNumeroPrestacaoRealizada().intValue() == 
							((creditoRealizar.getNumeroPrestacaoCredito().intValue() - numeroParcelaBonus.intValue()) - 1)) {

						/*
						 * PARTE 01 valor do crédito correspondente a parcela do
						 * mês * (o nº de prestação dos créditos menos o numero
						 * de parcela bonus)
						 */

						BigDecimal valorMesVezesPrestacaoCredito = valorCorrespondenteParcelaMes.multiply(
										new BigDecimal(creditoRealizar.getNumeroPrestacaoCredito())
									).setScale(2);

						/*
						 * PARTE 02 valor do crédito correspondente a parcela do
						 * mês + valor do crédito
						 */
						BigDecimal parte11 = valorCorrespondenteParcelaMes.add(creditoRealizar.getValorCredito());

						// valor do crédito correspondente a parcela do mês
						// = PARTE 02 - PARTE 01
						BigDecimal parte22 = parte11.subtract(valorMesVezesPrestacaoCredito);

						valorCorrespondenteParcelaMes = parte22;
					}

					// Atualiza o nº de prestações realizadas
					creditoRealizar.setNumeroPrestacaoRealizada(new Short((creditoRealizar.getNumeroPrestacaoRealizada().intValue() + 1) + ""));

					// anoMes da prestação será o anaMes de referência da conta
					creditoRealizar.setAnoMesReferenciaPrestacao(anoMesFaturamento);
				}

				/**TODO:COSANPA
				 * Autor: Adriana Muniz
				 * Data: 29/06/2011
				 * 
				 * Alteração para que a referência da prestação do crédito seja sempre atualizada, 
				 * se um crédito realizado tenha sido gerado a partir desse crédito a realizar
				 * */
				// anoMes da prestação será o anaMes de referência da conta
				creditoRealizar.setAnoMesReferenciaPrestacao(anoMesFaturamento);
				
				// Valor de credito
				valorCredito = valorCorrespondenteParcelaMes.add(creditoRealizar.getValorResidualMesAnterior());

				// Armazena o valor residual concedido no mês
				// (CRAR_VLRESIDUALCONCEDIDOMES = CRAR_VLRESIDUALMESANTERIOR).
				creditoRealizar.setValorResidualConcedidoMes(creditoRealizar.getValorResidualMesAnterior());

				/*
				 * Para o pré-faturamento todos os créditos a realizar serão
				 * transformados em crédito realizado, independente do valor
				 * total a cobrar.
				 */
				if (!preFaturamento) {
					// Retira o valor de credito do valor total a cobrar
					valorTotalACobrar = valorTotalACobrar.subtract(valorCredito);
				}

				/*
				 * Caso o valor total a cobrar seja menor que zero o valor
				 * residual do mês anterior vai ser igual a valor total a cobrar
				 * vezes -1(menos um) e o valor do crédito vai ser igual ao
				 * valor do crédito menos valor residual do mês anterior.
				 * 
				 * Valor Total A Cobrar = 0.00
				 * 
				 * Caso contrário o valor residual do mês anterior vai ser
				 * iguala zero.
				 */
				if (valorTotalACobrar.compareTo(new BigDecimal("0.00")) == -1) {

					creditoRealizar.setValorResidualMesAnterior(valorTotalACobrar.multiply(new BigDecimal("-1")));

					valorCredito = valorCredito.subtract(creditoRealizar.getValorResidualMesAnterior());

					valorTotalACobrar = new BigDecimal("0.00");

				} else {

					/**TODO:COSANPA
					 * autor: Adriana Muniz
					 * data:29/06/2011
					 * 
					 * alteração para não lançar para zero o valor residual dos creditos s a realizar,
					 * caso o imóvel pertença ao impressão simultanea
					 * */
					if (!preFaturamento) {
						creditoRealizar.setValorResidualMesAnterior(new BigDecimal("0.00"));
					}
				}

				// Acumula o valor do crédito
				valorTotalCreditos = valorTotalCreditos.add(valorCredito);

				// Se a atividade é faturar grupo de faturamento
				if (gerarAtividadeGrupoFaturamento) {

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

					colecaoCreditosRealizado.add(creditoRealizado);

					Collection<CreditoRealizarCategoria> colecaoCreditoARealizarCategoria = this.obterCreditoRealizarCategoria(creditoRealizar.getId());

					Iterator<CreditoRealizarCategoria> colecaoCreditoARealizarCategoriaIterator = colecaoCreditoARealizarCategoria.iterator();

					CreditoRealizarCategoria creditoRealizarCategoria = null;

					Collection<Categoria> colecaoCategoriasObterValor = new ArrayList<Categoria>();

					while (colecaoCreditoARealizarCategoriaIterator.hasNext()) {
						creditoRealizarCategoria = (CreditoRealizarCategoria) colecaoCreditoARealizarCategoriaIterator.next();
						Categoria categoria = new Categoria();
						categoria.setId(creditoRealizarCategoria.getCategoriaId());
						categoria.setQuantidadeEconomiasCategoria(creditoRealizarCategoria.getQuantidadeEconomia());
						colecaoCategoriasObterValor.add(categoria);
					}

					Collection<BigDecimal> colecaoCategoriasCalculadasValor = imovelBO.obterValorPorCategoria(colecaoCategoriasObterValor, valorCredito);

					Iterator<BigDecimal> colecaoCategoriasCalculadasValorIterator = colecaoCategoriasCalculadasValor.iterator();
					Iterator<Categoria> colecaoCategoriasObterValorIterator = colecaoCategoriasObterValor.iterator();

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

					if (colecaoCategoriasCalculadasValor != null) {
						colecaoCategoriasCalculadasValor.clear();
						colecaoCategoriasCalculadasValor = null;
					}

					// Armazena o credito realizado junto com os créditos
					// realizados por categoria
					mapCreditoRealizado.put(creditoRealizado, colecaoCreditosRealizadoCategoria);

					// Adiciona o crédito a realizar para ser atualizado
					colecaoCreditosARealizarUpdate.add(creditoRealizar);

				}

				// Verifica se debito a cobrar já foi inserido, caso sim
				// acumala os valores.
				if (mapValoresPorTipoCredito.containsKey(creditoRealizar.getCreditoTipo())) {
					BigDecimal valor = mapValoresPorTipoCredito.get(creditoRealizar.getCreditoTipo());
					mapValoresPorTipoCredito.put(creditoRealizar.getCreditoTipo(), somaBigDecimal(valor, valorCredito));
				}
				// Caso contrario inseri na coleção
				// primeiro registro do tipo.
				else {
					mapValoresPorTipoCredito.put(creditoRealizar.getCreditoTipo(), valorCredito);
				}
			}
		}

		creditoRealizadoTO.setColecaoCreditoARealizar(colecaoCreditosARealizarUpdate);
		creditoRealizadoTO.setMapCreditoRealizado(mapCreditoRealizado);
		creditoRealizadoTO.setValorTotalCredito(valorTotalCreditos);
		creditoRealizadoTO.setMapValoresPorTipoCredito(mapValoresPorTipoCredito);

		return creditoRealizadoTO;
	}
	
	private Collection<CreditoRealizarCategoria> obterCreditoRealizarCategoria(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	private Collection<CreditoRealizar> obterCreditoRealizarImovel(Long id, DebitoCreditoSituacao normal, Integer anoMesFaturamento) {
		// TODO Auto-generated method stub
		return null;
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