package br.gov.batch.servicos.micromedicao;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

public class ConsumoBOTest {

	@InjectMocks
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
		
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void consumoNaoMedidoMinimoLigacao() {
		mockParametroAtivo();
		mockConsumoMinimoLigacao();
		mockCategorias();
		mockMaiorDataVigencia();
		mockConsumoMinimoTarifa();

		assertEquals(80, bo.consumoNaoMedido(5, null));
	}

	@Test
	public void consumoNaoMedidoSemTarifa() {
		mockParametroInativo();
		mockAreaConstruida();
		mockSubcategorias();
		mockConsumoMinimoArea();

		assertEquals(60, bo.consumoNaoMedido(5, 5));
	}

	private void mockParametroAtivo() {
		when(sistemaParametrosMock.getIndicadorNaoMedidoTarifa()).thenReturn(Status.ATIVO.getId());
	}

	private void mockParametroInativo() {
		when(sistemaParametrosMock.getIndicadorNaoMedidoTarifa()).thenReturn(Status.INATIVO.getId());
	}

	private void mockConsumoMinimoLigacao() {
		when(consumoTarifaRepositorioMock.consumoTarifaDoImovel(any())).thenReturn(1);
	}

	private void mockCategorias() {
		when(imovelSubcategoriaRepositorioMock.buscarQuantidadeEconomiasPorImovel(any())).thenReturn(categorias);
	}

	private void mockMaiorDataVigencia() {
		when(consumoTarifaVigenciaRepositorioMock.maiorDataVigenciaConsumoTarifa(any())).thenReturn(consumoTarifaVigenciaTO);
	}

	private void mockConsumoMinimoTarifa() {
		when(consumoTarifaCategoriaRepositorioMock.consumoMinimoTarifa(any(), any())).thenReturn(10);
		when(consumoTarifaCategoriaRepositorioMock.consumoMinimoTarifa(any(), any())).thenReturn(20);
	}

	private void mockAreaConstruida() {
		when(imovelBOMock.verificarAreaConstruida(any())).thenReturn(BigDecimal.valueOf(4.00));
	}

	private void mockSubcategorias() {
		when(imovelSubcategoriaRepositorioMock.buscarSubcategoria(any())).thenReturn(subcategorias);
	}

	private void mockConsumoMinimoArea() {
		when(consumoMinimoAreaRepositorioMock.pesquisarConsumoMinimoArea(any(), any(), any(), any())).thenReturn(20);
	}
}
