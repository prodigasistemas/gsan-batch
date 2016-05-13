package br.gov.batch.servicos.faturamento;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import br.gov.model.cadastro.ICategoria;
import br.gov.servicos.to.ImovelSubcategoriaTO;

public class ContaCategoriaBOTest {

	private ContaCategoriaBO contaCategoriaBO;
	
	@Before
	public void setup(){
		contaCategoriaBO = new ContaCategoriaBO();
	}
	
	@Test
	public void gerarContaCategoriaValoresZerados(){
		
		try {
			assertEquals(1, contaCategoriaBO.gerarContaCategoriaValoresZerados(1, getCategorias()).size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Collection<ICategoria> getCategorias(){
		Collection<ICategoria> categorias = new ArrayList<ICategoria>();

		ImovelSubcategoriaTO subcategoriaTO = new ImovelSubcategoriaTO(2, null, null, Long.valueOf("10"), null, null, null, null, null, Short.valueOf("3"), null, null, null);
		categorias.add(subcategoriaTO);
		
		return categorias;
	}
}
