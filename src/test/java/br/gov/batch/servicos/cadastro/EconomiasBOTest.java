package br.gov.batch.servicos.cadastro;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.model.cadastro.ICategoria;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.to.ImovelSubcategoriaTO;

public class EconomiasBOTest {

	@InjectMocks
	private EconomiasBO economiasBO;

	@Mock
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorioMock;

	private Collection<ICategoria> subcategorias;

	@Before
	public void setup() {
		economiasBO = new EconomiasBO();

		subcategorias = new ArrayList<ICategoria>();

		ImovelSubcategoriaTO subcategoriaTO = new ImovelSubcategoriaTO(null, null, null, null, null, null, null, null, null, Short.valueOf("3"), null, null, null);
		subcategorias.add(subcategoriaTO);

		subcategoriaTO = new ImovelSubcategoriaTO(null, null, null, Long.valueOf("2"), null, null, null, null, null, null, null, null, null);
		subcategorias.add(subcategoriaTO);
		
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void quantidadeEconomiasVirtuais() {
		mockSubcategoria();

		assertEquals(5, economiasBO.quantidadeEconomiasVirtuais(1).intValue());
	}

	private void mockSubcategoria() {
		when(imovelSubcategoriaRepositorioMock.buscarSubcategoria(1)).thenReturn(subcategorias);
	}
}
