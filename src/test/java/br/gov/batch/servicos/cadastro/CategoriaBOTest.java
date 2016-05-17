package br.gov.batch.servicos.cadastro;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import br.gov.model.cadastro.Categoria;

public class CategoriaBOTest {

	private CategoriaBO categoriaBO;

	@Before
	public void setup() {
		categoriaBO = new CategoriaBO();
	}

	@Test
	public void calcularValorPorCategoria() {
		Collection<Categoria> categorias = new ArrayList<Categoria>();
		Categoria categoria = new Categoria();
		categoria.setQuantidadeEconomias(5);
		categorias.add(categoria);
		
		categoria = new Categoria();
		categoria.setQuantidadeEconomias(2);
		categorias.add(categoria);
		
		Collection<BigDecimal> valores = categoriaBO.obterValorPorCategoria(categorias, BigDecimal.valueOf(5.00));
		
		BigDecimal valor = valores.iterator().next();
		
		assertEquals(BigDecimal.valueOf(3.58), valor);
	}
	
	@Test
	public void calcularValorSemCategorias() {
		Collection<BigDecimal> valores = categoriaBO.obterValorPorCategoria(new ArrayList<Categoria>(), BigDecimal.ZERO);

		assertEquals(0, valores.size());
	}
	
	@Test
	public void calcularValorComListaNula() {
		Collection<BigDecimal> valores = categoriaBO.obterValorPorCategoria(null, BigDecimal.ZERO);

		assertEquals(0, valores.size());
	}
}
