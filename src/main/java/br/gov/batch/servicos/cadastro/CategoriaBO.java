package br.gov.batch.servicos.cadastro;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.Stateless;

import br.gov.model.cadastro.Categoria;
import br.gov.model.cadastro.ICategoria;

@Stateless
public class CategoriaBO {

	public Collection<BigDecimal> obterValorPorCategoria(Collection<Categoria> colecaoCategorias, BigDecimal valor) {

		Collection<BigDecimal> colecaoValoresPorCategoria = new ArrayList<BigDecimal>();

		int somatorioQuantidadeEconomiasCadaCategoria = 0;
		if (colecaoCategorias != null && !colecaoCategorias.isEmpty()) {
			Iterator<Categoria> iteratorColecaoCategorias = colecaoCategorias.iterator();

			while (iteratorColecaoCategorias.hasNext()) {
				ICategoria categoria = (ICategoria) iteratorColecaoCategorias.next();
				somatorioQuantidadeEconomiasCadaCategoria = somatorioQuantidadeEconomiasCadaCategoria + categoria.getQuantidadeEconomias().intValue();
			}
		}

		BigDecimal fatorMultiplicacao = somatorioQuantidadeEconomiasCadaCategoria > 0 
				? valor.divide(new BigDecimal(somatorioQuantidadeEconomiasCadaCategoria), 2, BigDecimal.ROUND_DOWN)
				: BigDecimal.ZERO;

		BigDecimal valorPorCategoriaAcumulado = BigDecimal.ZERO;


		if (colecaoCategorias != null && !colecaoCategorias.isEmpty()) {
			Iterator<Categoria> iteratorColecaoCategorias = colecaoCategorias.iterator();

			while (iteratorColecaoCategorias.hasNext()) {
				ICategoria categoria = (ICategoria) iteratorColecaoCategorias.next();

				BigDecimal valorPorCategoria = fatorMultiplicacao.multiply(new BigDecimal(categoria.getQuantidadeEconomias()));

				BigDecimal valorTruncado = valorPorCategoria.setScale(2, BigDecimal.ROUND_DOWN);

				valorPorCategoriaAcumulado = valorPorCategoriaAcumulado.add(valorTruncado);

				colecaoValoresPorCategoria.add(valorTruncado);
			}
		}

		valorPorCategoriaAcumulado = valorPorCategoriaAcumulado.setScale(7);
		
		if (colecaoValoresPorCategoria.iterator().hasNext()){
			if (valorPorCategoriaAcumulado.setScale(2, BigDecimal.ROUND_HALF_UP).compareTo(valor.setScale(2, BigDecimal.ROUND_HALF_UP)) == -1) {
				
				BigDecimal diferenca = valor.subtract(valorPorCategoriaAcumulado).setScale(2, BigDecimal.ROUND_HALF_UP);
				
				BigDecimal categoriaPrimeira = (BigDecimal) colecaoValoresPorCategoria.iterator().next();
				
				categoriaPrimeira = categoriaPrimeira.add(diferenca);
				
				((ArrayList<BigDecimal>) colecaoValoresPorCategoria).set(0, categoriaPrimeira);
			}
		}


		return colecaoValoresPorCategoria;
	}
}