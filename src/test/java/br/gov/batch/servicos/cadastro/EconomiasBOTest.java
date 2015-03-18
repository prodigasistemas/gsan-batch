package br.gov.batch.servicos.cadastro;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.cadastro.ICategoria;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.to.ImovelSubcategoriaTO;

@RunWith(EasyMockRunner.class)
public class EconomiasBOTest {

	@TestSubject
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
	}

	@Test
	public void quantidadeEconomiasVirtuais() {
		mockSubcategoria();

		assertEquals(5, economiasBO.quantidadeEconomiasVirtuais(1).intValue());
	}

	private void mockSubcategoria() {
		expect(imovelSubcategoriaRepositorioMock.buscarSubcategoria(1)).andReturn(subcategorias);
		replay(imovelSubcategoriaRepositorioMock);
	}
}
