package br.gov.batch.servicos.micromedicao;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.model.cadastro.Categoria;
import br.gov.model.cadastro.ICategoria;
import br.gov.servicos.faturamento.ConsumoTarifaCategoriaRepositorio;
import br.gov.servicos.to.ImovelSubcategoriaTO;

public class ConsumoMinimoTest {
	
	@InjectMocks
	ConsumoBO bo;

	@Mock
	private ConsumoTarifaCategoriaRepositorio mock;
	
	private static int idSubcategoriaC1 = 5;
	
	private static int idSubcategoriaR3 = 3;
	
	@Before
	public void init(){
		bo = new ConsumoBO();
		
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void consumoCategoriaComercialUmaEconomia(){
		mockConsumoMinimoTarifa();
		
		Collection<ICategoria> categorias = buildCategorias(comercialC1());
		assertEquals(10, bo.obterConsumoMinimoLigacaoCategorias(1, 1, categorias));
	}
	
	@Test
	public void consumoCategoriaResidencialTresEconomias(){
		mockConsumoMinimoTarifa();
		
		Collection<ICategoria> categorias = buildCategorias(residencialR3());
		assertEquals(30, bo.obterConsumoMinimoLigacaoCategorias(1, 1, categorias));
	}

	@Test
	public void consumoCategoriaResidencialEComercialUmaEcoComercialTresEcoResidencial(){
		mockConsumoMinimoTarifa();
		
		Collection<ICategoria> categorias = buildCategorias(comercialC1(), residencialR3());
		assertEquals(40, bo.obterConsumoMinimoLigacaoCategorias(1, 1, categorias));
	}
	
	private void mockConsumoMinimoTarifa(){
		when(mock.consumoMinimoTarifa(any(), any())).thenReturn(10);
	}
	
	private Collection<ICategoria> buildCategorias(ICategoria... categorias){
		Collection<ICategoria> lista = new ArrayList<>();
		
		for (ICategoria categoria : categorias) {
			lista.add(categoria);
		}
		
		return lista;
	}
		
	private ICategoria comercial(){
		Categoria comercial = new Categoria();
		comercial.setId(2);
		comercial.setDescricao("COMERCIAL");

		return comercial;
	}
	
	private ICategoria residencial(){
		Categoria comercial = new Categoria();
		comercial.setId(1);
		comercial.setDescricao("RESIDENCIAL");
		
		return comercial;
	}
	
	private ICategoria comercialC1(){
		ImovelSubcategoriaTO c1 = new ImovelSubcategoriaTO(comercial().getId(), idSubcategoriaC1);
		c1.setCategoriaDescricao(comercial().getCategoriaDescricao());
		c1.setSubcategoriaDescricao("C1");
		c1.setSubcategoriaCodigo(idSubcategoriaC1);
		c1.setSubcategoriaQuantidadeEconomias(1L);
		
		return c1;
	}
	
	private ICategoria residencialR3(){
		ImovelSubcategoriaTO c1 = new ImovelSubcategoriaTO(residencial().getId(), idSubcategoriaR3);
		c1.setCategoriaDescricao(residencial().getCategoriaDescricao());
		c1.setSubcategoriaDescricao("R3");
		c1.setSubcategoriaCodigo(idSubcategoriaR3);
		c1.setSubcategoriaQuantidadeEconomias(3L);
		
		return c1;
	}	
}
