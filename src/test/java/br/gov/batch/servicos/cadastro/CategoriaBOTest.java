package br.gov.batch.servicos.cadastro;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import static org.junit.Assert.*;

import br.gov.model.cadastro.Categoria;

public class CategoriaBOTest {
	
	private CategoriaBO categoriaBO = new CategoriaBO();
	
	@Test
	public void calculaValorSemCategorias(){
		Collection<Categoria> lista = new ArrayList<Categoria>();
		Collection<BigDecimal> valor = categoriaBO.obterValorPorCategoria(lista, BigDecimal.ZERO);
		
		assertEquals(0, valor.size());
	}
}
