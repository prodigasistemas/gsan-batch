package br.gov.batch.servicos.cadastro;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.Stateless;

import br.gov.model.cadastro.Categoria;

@Stateless
public class CategoriaBO {

	public Collection<BigDecimal> obterValorPorCategoria(Collection<Categoria> categorias, BigDecimal valor) {

		int totalEconomias = 0;
		if (categorias != null && !categorias.isEmpty()) {
			for (Categoria categoria : categorias) {
				totalEconomias += categoria.getQuantidadeEconomias().intValue();
			}
		}

		BigDecimal fatorMultiplicacao = totalEconomias > 0 ? valor.divide(new BigDecimal(totalEconomias), 2, BigDecimal.ROUND_DOWN) : BigDecimal.ZERO;

		BigDecimal valorAcumuladoPorCategoria = BigDecimal.ZERO;
		Collection<BigDecimal> valoresPorCategoria = new ArrayList<BigDecimal>();
		if (categorias != null && !categorias.isEmpty()) {
			for (Categoria categoria : categorias) {
				BigDecimal valorPorCategoria = fatorMultiplicacao.multiply(new BigDecimal(categoria.getQuantidadeEconomias()));
				BigDecimal valorTruncado = valorPorCategoria.setScale(2, BigDecimal.ROUND_DOWN);

				valorAcumuladoPorCategoria = valorAcumuladoPorCategoria.add(valorTruncado);
				valoresPorCategoria.add(valorTruncado);
			}
		}

		valorAcumuladoPorCategoria = valorAcumuladoPorCategoria.setScale(7);

		if (valoresPorCategoria.iterator().hasNext()) {
			if (valorAcumuladoPorCategoria.setScale(2, BigDecimal.ROUND_HALF_UP).compareTo(valor.setScale(2, BigDecimal.ROUND_HALF_UP)) == -1) {
				BigDecimal diferenca = valor.subtract(valorAcumuladoPorCategoria).setScale(2, BigDecimal.ROUND_HALF_UP);

				BigDecimal primeiraCategoria = (BigDecimal) valoresPorCategoria.iterator().next();

				primeiraCategoria = primeiraCategoria.add(diferenca);

				((ArrayList<BigDecimal>) valoresPorCategoria).set(0, primeiraCategoria);
			}
		}

		return valoresPorCategoria;
	}
}