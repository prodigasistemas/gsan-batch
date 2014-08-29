package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.cadastro.Categoria;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo02Test {
	
	@TestSubject
	private ArquivoTextoTipo02 arquivoTextoTipo02;

	@Mock
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorioMock;
	
	@Mock
	private SistemaParametros sistemaParametrosMock;
	
	private Imovel imovel;
	private Collection<ICategoria> categorias;
	
	@Before
	public void setup() {
		imovel = new Imovel();
		imovel.setId(1);
		
		Categoria categoria = new Categoria();
		categoria.setId(1L);
		categoria.setQuantidadeEconomias(1);
		categoria.setFatorEconomias((short) 1);
		categoria.setDescricao("CATEGORIA 1");
		categoria.setDescricaoAbreviada("CTG 1");
		
		categorias = new ArrayList<ICategoria>();
		categorias.add(categoria);
		
		arquivoTextoTipo02 = new ArquivoTextoTipo02();
	}
	
	@Test
	public void buildArquivoTextoTipo02Categoria() {
		expect(sistemaParametrosMock.indicadorTarifaCategoria()).andReturn(true);
		expect(imovelSubcategoriaRepositorioMock.buscarQuantidadeEconomiasPorImovel(1)).andReturn(categorias);
		replay();
		
		assertNotNull(arquivoTextoTipo02.build(imovel));
	}
	
	@Test
	public void buildArquivoTextoTipo02Subcategoria() {
		expect(sistemaParametrosMock.indicadorTarifaCategoria()).andReturn(true);
		expect(imovelSubcategoriaRepositorioMock.buscarQuantidadeEconomiasPorImovel(1)).andReturn(categorias);
		replay();
		
		assertNotNull(arquivoTextoTipo02.build(imovel));
	}
}
