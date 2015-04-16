package br.gov.batch.servicos.micromedicao;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.servicos.cadastro.EconomiasBO;
import br.gov.batch.servicos.cadastro.ImovelBO;
import br.gov.model.Status;
import br.gov.model.cadastro.Categoria;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.cadastro.Subcategoria;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaCategoriaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaVigenciaRepositorio;
import br.gov.servicos.micromedicao.ConsumoMinimoAreaRepositorio;
import br.gov.servicos.to.ConsumoTarifaVigenciaTO;

@RunWith(EasyMockRunner.class)
public class ConsumoBOTest {

	@TestSubject
	private ConsumoBO bo;

	@Mock
	private SistemaParametros sistemaParametrosMock;

	@Mock
	private ConsumoTarifaRepositorio consumoTarifaRepositorioMock;

	@Mock
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorioMock;

	@Mock
	private ConsumoTarifaVigenciaRepositorio consumoTarifaVigenciaRepositorioMock;

	@Mock
	private ConsumoTarifaCategoriaRepositorio consumoTarifaCategoriaRepositorioMock;

	@Mock
	private ConsumoMinimoAreaRepositorio consumoMinimoAreaRepositorioMock;

	@Mock
	private EconomiasBO economiasBOMock;

	@Mock
	private ImovelBO imovelBOMock;

	private Collection<ICategoria> categorias;
	private Collection<ICategoria> subcategorias;
	private ConsumoTarifaVigenciaTO consumoTarifaVigenciaTO;

	@Before
	public void setup() {
		categorias = new ArrayList<ICategoria>();

		Categoria categoria = new Categoria(1);
		categoria.setQuantidadeEconomias(3);
		categorias.add(categoria);

		categoria = new Categoria(2);
		categoria.setFatorEconomias((short) 1);
		categorias.add(categoria);

		subcategorias = new ArrayList<ICategoria>();

		Subcategoria subcategoria = new Subcategoria(1);
		subcategoria.setQuantidadeEconomias(2);
		subcategoria.setCategoria(new Categoria(1));
		subcategorias.add(subcategoria);

		subcategoria = new Subcategoria(2);
		subcategoria.setCategoria(categoria);
		subcategorias.add(subcategoria);

		consumoTarifaVigenciaTO = new ConsumoTarifaVigenciaTO(2, new Date());
		
		bo = new ConsumoBO();
	}

	@Test
	public void consumoNaoMedidoMinimoLigacao() {
		mockParametroAtivo();
		mockConsumoMinimoLigacao();
		mockCategorias();
		mockMaiorDataVigencia();
		mockConsumoMinimoTarifa();

		assertEquals(50, bo.consumoNaoMedido(5, 5));
	}

	@Test
	public void consumoNaoMedidoSemTarifa() {
		mockParametroInativo();
		mockQuantidadeEconomiasVirtuais();
		mockAreaConstruida();
		mockSubcategorias();
		mockConsumoMinimoArea();

		assertEquals(60, bo.consumoNaoMedido(5, 5));
	}

	private void mockParametroAtivo() {
		expect(sistemaParametrosMock.getIndicadorNaoMedidoTarifa()).andReturn(Status.ATIVO.getId());
		replay(sistemaParametrosMock);
	}

	private void mockParametroInativo() {
		expect(sistemaParametrosMock.getIndicadorNaoMedidoTarifa()).andReturn(Status.INATIVO.getId());
		replay(sistemaParametrosMock);
	}

	private void mockConsumoMinimoLigacao() {
		expect(consumoTarifaRepositorioMock.consumoTarifaDoImovel(anyInt())).andReturn(1);
		replay(consumoTarifaRepositorioMock);
	}

	private void mockCategorias() {
		expect(imovelSubcategoriaRepositorioMock.buscarQuantidadeEconomiasPorImovel(anyInt())).andReturn(categorias);
		replay(imovelSubcategoriaRepositorioMock);
	}

	private void mockMaiorDataVigencia() {
		expect(consumoTarifaVigenciaRepositorioMock.maiorDataVigenciaConsumoTarifa(anyInt())).andReturn(consumoTarifaVigenciaTO);
		replay(consumoTarifaVigenciaRepositorioMock);
	}

	private void mockConsumoMinimoTarifa() {
		expect(consumoTarifaCategoriaRepositorioMock.consumoMinimoTarifa(anyObject(), anyInt())).andReturn(10);
		expect(consumoTarifaCategoriaRepositorioMock.consumoMinimoTarifa(anyObject(), anyInt())).andReturn(20);
		replay(consumoTarifaCategoriaRepositorioMock);
	}

	private void mockQuantidadeEconomiasVirtuais() {
		expect(economiasBOMock.quantidadeEconomiasVirtuais(anyInt())).andReturn(2);
		replay(economiasBOMock);
	}

	private void mockAreaConstruida() {
		expect(imovelBOMock.verificarAreaConstruida(anyInt())).andReturn(BigDecimal.valueOf(4.00));
		replay(imovelBOMock);
	}

	private void mockSubcategorias() {
		expect(imovelSubcategoriaRepositorioMock.buscarSubcategoria(anyInt())).andReturn(subcategorias);
		replay(imovelSubcategoriaRepositorioMock);
	}

	private void mockConsumoMinimoArea() {
		expect(consumoMinimoAreaRepositorioMock.pesquisarConsumoMinimoArea(anyObject(), anyInt(), anyInt(), anyInt())).andReturn(20).times(2);
		replay(consumoMinimoAreaRepositorioMock);
	}
}
