package br.gov.batch.servicos.micromedicao;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
import br.gov.model.faturamento.ConsumoTarifa;
import br.gov.model.faturamento.ConsumoTarifaVigencia;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaCategoriaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaFaixaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaVigenciaRepositorio;
import br.gov.servicos.micromedicao.ConsumoMinimoAreaRepositorio;
import br.gov.servicos.to.ConsumoImovelCategoriaTO;
import br.gov.servicos.to.ConsumoTarifaCategoriaTO;
import br.gov.servicos.to.ConsumoTarifaFaixaTO;
import br.gov.servicos.to.ConsumoTarifaVigenciaTO;
import br.gov.servicos.to.TarifasVigenciaTO;

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
	private ConsumoTarifaFaixaRepositorio consumoTarifaFaixaRepositorio;

	@Mock
	private ImovelBO imovelBOMock;

	private Collection<ICategoria> categorias;
	private Collection<ICategoria> subcategorias;
	private ConsumoTarifaVigenciaTO consumoTarifaVigenciaTO;
	
	private Date data2016_01_23 = null;

	@Before
	public void setup() {
		Calendar cal = Calendar.getInstance();
		cal.set(2016, 0, 23);
		data2016_01_23 = cal.getTime();
		
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
	
	@Test
	public void obterFaixas(){
		Categoria residencial = new Categoria();
		residencial.setDescricao("RESIDENCIAL");
		residencial.setId(1);
		
		Categoria comercial = new Categoria();
		comercial.setDescricao("COMERCIAL");
		comercial.setId(2);
		
		ConsumoTarifa tarifa = new ConsumoTarifa();
		tarifa.setId(1);
		tarifa.setDescricao("NORMAL");
		
		ConsumoTarifaVigencia vigencia = new ConsumoTarifaVigencia();
		vigencia.setDataVigencia(data2016_01_23);
		vigencia.setConsumoTarifa(tarifa);
		
		ConsumoTarifaCategoriaTO consumoTarifaCatgResidencial = new ConsumoTarifaCategoriaTO();
		consumoTarifaCatgResidencial.setId(1);
		consumoTarifaCatgResidencial.setIdCategoria(residencial.getId());
		consumoTarifaCatgResidencial.setConsumoMinimo(10);
		consumoTarifaCatgResidencial.setIdTarifa(tarifa.getId());
		consumoTarifaCatgResidencial.setIdVigencia(vigencia.getId());
		consumoTarifaCatgResidencial.setDataVigencia(vigencia.getDataVigencia());
		consumoTarifaCatgResidencial.setValorTarifaMinima(new BigDecimal(16.80));
		
		ConsumoTarifaCategoriaTO consumoTarifaCatgComercial = new ConsumoTarifaCategoriaTO();
		consumoTarifaCatgComercial.setId(2);
		consumoTarifaCatgComercial.setIdCategoria(comercial.getId());
		consumoTarifaCatgComercial.setConsumoMinimo(10);
		consumoTarifaCatgComercial.setIdTarifa(tarifa.getId());
		consumoTarifaCatgComercial.setIdVigencia(vigencia.getId());
		consumoTarifaCatgResidencial.setDataVigencia(vigencia.getDataVigencia());
		consumoTarifaCatgComercial.setValorTarifaMinima(new BigDecimal(50.20));

		ConsumoImovelCategoriaTO consumoImovelCategoriaTO = new ConsumoImovelCategoriaTO();
		consumoImovelCategoriaTO.addConsumoTarifaCategoria(consumoTarifaCatgResidencial);
		consumoImovelCategoriaTO.addConsumoTarifaCategoria(consumoTarifaCatgComercial);
		
		ConsumoTarifaFaixaTO faixa01 = new ConsumoTarifaFaixaTO();
		faixa01.setDataVigencia(data2016_01_23);
		faixa01.setIdConsumoTarifa(tarifa.getId());
		faixa01.setNumeroConsumoFaixaInicio(11);
		faixa01.setNumeroConsumoFaixaFim(20);
		faixa01.setValorTarifa(new BigDecimal(2.40));
		
	    ConsumoTarifaFaixaTO faixa02 = new ConsumoTarifaFaixaTO();
	    faixa02.setDataVigencia(data2016_01_23);
	    faixa02.setIdConsumoTarifa(tarifa.getId());
	    faixa02.setNumeroConsumoFaixaInicio(21);
	    faixa02.setNumeroConsumoFaixaFim(30);
	    faixa02.setValorTarifa(new BigDecimal(3.22));
	    
	    ConsumoTarifaFaixaTO faixa03 = new ConsumoTarifaFaixaTO();
	    faixa03.setDataVigencia(data2016_01_23);
	    faixa03.setIdConsumoTarifa(tarifa.getId());
	    faixa03.setNumeroConsumoFaixaInicio(31);
	    faixa03.setNumeroConsumoFaixaFim(40);
	    faixa03.setValorTarifa(new BigDecimal(3.62));
	    
	    ConsumoTarifaFaixaTO faixa04 = new ConsumoTarifaFaixaTO();
	    faixa04.setDataVigencia(data2016_01_23);
	    faixa04.setIdConsumoTarifa(tarifa.getId());
	    faixa04.setNumeroConsumoFaixaInicio(41);
	    faixa04.setNumeroConsumoFaixaFim(50);
	    faixa04.setValorTarifa(new BigDecimal(5.02));
	    
	    ConsumoTarifaFaixaTO faixa05 = new ConsumoTarifaFaixaTO();
	    faixa05.setDataVigencia(data2016_01_23);
	    faixa05.setIdConsumoTarifa(tarifa.getId());
	    faixa05.setNumeroConsumoFaixaInicio(51);
	    faixa05.setNumeroConsumoFaixaFim(999999);
	    faixa05.setValorTarifa(new BigDecimal(6.52));
		
		List<ConsumoTarifaFaixaTO> faixasResidenciais = new ArrayList<>();
		faixasResidenciais.add(faixa01);
		faixasResidenciais.add(faixa02);
		faixasResidenciais.add(faixa03);
		faixasResidenciais.add(faixa04);
		faixasResidenciais.add(faixa05);

	    ConsumoTarifaFaixaTO faixaComercial = new ConsumoTarifaFaixaTO();
	    faixaComercial.setDataVigencia(data2016_01_23);
	    faixaComercial.setIdConsumoTarifa(tarifa.getId());
	    faixaComercial.setNumeroConsumoFaixaInicio(11);
	    faixaComercial.setNumeroConsumoFaixaFim(999999);
	    faixaComercial.setValorTarifa(new BigDecimal(6.26));
		
	    List<ConsumoTarifaFaixaTO> faixasComerciais = new ArrayList<>();
	    faixasComerciais.add(faixaComercial);
		
	    mockConsumoTarifaFaixaPelaVigencia(vigencia, consumoTarifaCatgResidencial, faixasResidenciais);
	    mockConsumoTarifaFaixaPelaVigencia(vigencia, consumoTarifaCatgComercial, faixasComerciais);
		
	    List<TarifasVigenciaTO> tarifasVigencia = bo.obterFaixas(consumoImovelCategoriaTO);
		assertEquals(2, tarifasVigencia.size());
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
		when(consumoTarifaVigenciaRepositorioMock.buscarConsumoTarifaVigenciaAtualPelaTarifa(any())).thenReturn(consumoTarifaVigenciaTO);
	}

	private void mockConsumoMinimoTarifa() {
		when(consumoTarifaCategoriaRepositorioMock.consumoMinimoTarifa(any(), any())).thenReturn(10, 20);
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
	
	private void mockConsumoTarifaFaixaPelaVigencia(ConsumoTarifaVigencia vigencia, ConsumoTarifaCategoriaTO consumoTarifaCategoria, List<ConsumoTarifaFaixaTO> faixas){
		when(consumoTarifaFaixaRepositorio.getConsumoTarifaFaixaPelaVigencia(vigencia.getDataVigencia(), consumoTarifaCategoria.getId())).thenReturn(faixas);
	}
}
