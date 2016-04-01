package br.gov.batch.servicos.faturamento;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.cadastro.ICategoria;
import br.gov.servicos.to.ImovelSubcategoriaTO;

@RunWith(EasyMockRunner.class)
public class ContaCategoriaBOTest {

	@TestSubject
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
