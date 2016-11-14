package br.gov.batch.servicos.faturamento.tarifa;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import br.gov.batch.servicos.micromedicao.ConsumoBO;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.ConsumoTarifaVigencia;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.servicos.to.ConsumoImovelCategoriaTO;
import br.gov.servicos.to.ConsumoTarifaCategoriaTO;
import br.gov.servicos.to.ConsumoTarifaFaixaTO;
import br.gov.servicos.to.TarifasVigenciaTO;

@RunWith(MockitoJUnitRunner.class)
public class ConsumoImovelCategoriaBOTest {

	@Mock private ConsumoHistorico consumoHistoricoMock;
	@Mock private Imovel imovelMock;
	@Mock private ConsumoBO consumoBOMock;
	@Mock private ICategoria categoriaResidencialMock;
	@Mock private ICategoria categoriaComercialMock;
	@Mock private ConsumoTarifaVigencia consumoTarifaVigenciaAtualMock;
	@Mock private ConsumoTarifaVigencia consumoTarifaVigenciaAnteriorMock;
	
	@Mock private ConsumoTarifaCategoriaTO consumoTarifaAtualCategoriaResidencialMock;
	@Mock private ConsumoTarifaCategoriaTO consumoTarifaAnteriorCategoriaResidencialMock;
	@Mock private ConsumoTarifaCategoriaTO consumoTarifaAtualCategoriaComercialMock;
	@Mock private ConsumoTarifaCategoriaTO consumoTarifaAnteriorCategoriaComercialMock;
	
	@Mock private ConsumoTarifaFaixaTO faixaResidencial11a20;
	@Mock private ConsumoTarifaFaixaTO faixaResidencial21a30;
	@Mock private ConsumoTarifaFaixaTO faixaResidencial31a40;
	@Mock private ConsumoTarifaFaixaTO faixaComercialMaior10;
	
	@InjectMocks private ConsumoImovelCategoriaBO bo;
	
	private Collection<ICategoria> umaCategoria;
	private Collection<ICategoria> duasCategorias;
	
	private List<ConsumoTarifaCategoriaTO> listConsumoTarifaCategoriaResidencial;
	private List<ConsumoTarifaCategoriaTO> listConsumoTarifaCategoriaComercial;
	
	private Date dataVigencia;
	private Date dataAnterior;
	private Date dataAtual;
	
	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		bo = new ConsumoImovelCategoriaBO();
		
		MockitoAnnotations.initMocks(this);
		
		when(consumoBOMock.getConsumoMinimoTarifaPorCategoria(any(List.class), eq(categoriaResidencialMock))).thenReturn(10);
		when(consumoBOMock.getConsumoMinimoTarifaPorCategoria(any(List.class), eq(categoriaComercialMock))).thenReturn(10);
		
		dataVigencia = new DateTime(2016, 4, 15, 0, 0).toDate();
		
		dataAnterior = new DateTime(2016, 9, 1, 0, 0).toDate();
		dataAtual= new DateTime(2016, 10, 1, 0, 0).toDate();
		
		when(consumoHistoricoMock.getImovel()).thenReturn(imovelMock);
		
		umaCategoria = new ArrayList<ICategoria>();
		umaCategoria.add(categoriaResidencialMock);
		
		duasCategorias = new ArrayList<ICategoria>();
		duasCategorias.add(categoriaResidencialMock);
		duasCategorias.add(categoriaComercialMock);
		
		listConsumoTarifaCategoriaResidencial = new ArrayList<ConsumoTarifaCategoriaTO>();
		listConsumoTarifaCategoriaResidencial.add(consumoTarifaAtualCategoriaResidencialMock);
		
		listConsumoTarifaCategoriaComercial = new ArrayList<ConsumoTarifaCategoriaTO>();
		listConsumoTarifaCategoriaComercial.add(consumoTarifaAtualCategoriaComercialMock);
		
	}
	
	@Test
	public void distribuirConsumoPorCategoriaUmaCategoriaUmaEconomia() {
		configurarImovelUmaCategoriaUmaEconomia();
		
		List<ConsumoImovelCategoriaTO> list = bo.distribuirConsumoPorCategoria(consumoHistoricoMock, dataAnterior, dataAtual);
		
		assertEquals(1, list.size());
		assertEquals(new Integer(10), list.get(0).getConsumoEconomiaCategoria());
		assertEquals(new Integer(30), list.get(0).getConsumoExcedenteCategoria());
	}
	
	@Test
	public void distribuirConsumoPorCategoriaUmaCategoriaTresEconomias() {
		configurarImovelUmaCategoriaTresEconomias();
		
		List<ConsumoImovelCategoriaTO> list = bo.distribuirConsumoPorCategoria(consumoHistoricoMock, dataAnterior, dataAtual);
		
		assertEquals(1, list.size());
		assertEquals(new Integer(10), list.get(0).getConsumoEconomiaCategoria());
		assertEquals(new Integer(6), list.get(0).getConsumoExcedenteCategoria());
	}
	
	@Test
	public void distribuirConsumoPorCategoriaDuasCategoriasDuasEconomias() {
		configurarImovelDuasCategoriasDuasEconomias();
		
		List<ConsumoImovelCategoriaTO> list = bo.distribuirConsumoPorCategoria(consumoHistoricoMock, dataAnterior, dataAtual);
		
		assertEquals(2, list.size());
		assertEquals(new Integer(10), list.get(0).getConsumoEconomiaCategoria());
		assertEquals(new Integer(10), list.get(0).getConsumoExcedenteCategoria());
		
		assertEquals(new Integer(10), list.get(1).getConsumoEconomiaCategoria());
		assertEquals(new Integer(10), list.get(1).getConsumoExcedenteCategoria());
	}
	
	@Test
	public void distribuirConsumoPorCategoriaDuasCategoriasSeteEconomias() {
		configurarImovelDuasCategoriasSeteEconomias();
		
		List<ConsumoImovelCategoriaTO> list = bo.distribuirConsumoPorCategoria(consumoHistoricoMock, dataAnterior, dataAtual);
		
		assertEquals(2, list.size());
		assertEquals(new Integer(10), list.get(0).getConsumoEconomiaCategoria());
		assertEquals(new Integer(1), list.get(0).getConsumoExcedenteCategoria());
		
		assertEquals(new Integer(10), list.get(1).getConsumoEconomiaCategoria());
		assertEquals(new Integer(1), list.get(1).getConsumoExcedenteCategoria());
	}
	
	@Test
	public void distribuirConsumoPorCategoriaUmaCategoriaUmaEconomiaConsumoMenorMinimo() {
		configurarImovelUmaCategoriaConsumoMenorMinimo();
		
		List<ConsumoImovelCategoriaTO> list = bo.distribuirConsumoPorCategoria(consumoHistoricoMock, dataAnterior, dataAtual);
		
		assertEquals(1, list.size());
		assertEquals(new Integer(9), list.get(0).getConsumoEconomiaCategoria());
		assertEquals(new Integer(0), list.get(0).getConsumoExcedenteCategoria());
	}
	
	@Test
	public void distribuirConsumoFaixasUmaCategoriaUmaEconomia() {
		configurarImovelUmaCategoriaUmaEconomia();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, dataAnterior, dataAtual);
		
		List<TarifasVigenciaTO> faixas = mockarFaixasResidencial(3);
		when(consumoBOMock.obterFaixas(anyObject())).thenReturn(faixas);
		
		List<ConsumoImovelCategoriaTO> consumoImoveisCategoriaTO = bo.distribuirConsumoPorFaixa();

		Map<ConsumoTarifaFaixaTO, Integer> faixasConsumo = consumoImoveisCategoriaTO.get(0).getTabelaTarifas().get(0).getConsumoPorFaixa();
		
		assertEquals(3, faixasConsumo.values().size());
		assertEquals(new Integer(10), faixasConsumo.get(faixaResidencial11a20));
		assertEquals(new Integer(10), faixasConsumo.get(faixaResidencial21a30));
		assertEquals(new Integer(10), faixasConsumo.get(faixaResidencial31a40));
	}
	
	@Test
	public void distribuirConsumoFaixasUmaCategoriaTresEconomias() {
		configurarImovelUmaCategoriaTresEconomias();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, dataAnterior, dataAtual);
		
		List<TarifasVigenciaTO> faixas = mockarFaixasResidencial(1);
		when(consumoBOMock.obterFaixas(anyObject())).thenReturn(faixas);

		List<ConsumoImovelCategoriaTO> consumoImoveisCategoriaTO = bo.distribuirConsumoPorFaixa();
		
		Map<ConsumoTarifaFaixaTO, Integer> faixasConsumo = consumoImoveisCategoriaTO.get(0).getTabelaTarifas().get(0).getConsumoPorFaixa();
		
		assertEquals(1, faixasConsumo.values().size());
		assertEquals(new Integer(6), faixasConsumo.get(faixaResidencial11a20));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void distribuirConsumoFaixasDuasCategoriasDuasEconomias() {
		configurarImovelDuasCategoriasDuasEconomias();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, dataAnterior, dataAtual);
		
		List<TarifasVigenciaTO> faixasResidenciais = mockarFaixasResidencial(1);
		List<TarifasVigenciaTO> faixasComerciais = mockarFaixasComerciais(dataVigencia);
		
		when(consumoBOMock.obterFaixas(anyObject())).thenReturn(faixasResidenciais, faixasComerciais);

		List<ConsumoImovelCategoriaTO> consumoImoveisCategoriaTO = bo.distribuirConsumoPorFaixa();
		
		Map<ConsumoTarifaFaixaTO, Integer> faixasConsumoResidencial = consumoImoveisCategoriaTO.get(0).getTabelaTarifas().get(0).getConsumoPorFaixa();
		Map<ConsumoTarifaFaixaTO, Integer> faixasConsumoComercial = consumoImoveisCategoriaTO.get(1).getTabelaTarifas().get(0).getConsumoPorFaixa();
		
		assertEquals(1, faixasConsumoResidencial.values().size());
		assertEquals(new Integer(10), faixasConsumoResidencial.get(faixaResidencial11a20));
		
		assertEquals(1, faixasConsumoComercial.values().size());
		assertEquals(new Integer(10), faixasConsumoComercial.get(faixaComercialMaior10));
		
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void distribuirConsumoFaixasDuasCategoriasSeteEconomias() {
		configurarImovelDuasCategoriasSeteEconomias();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, dataAnterior, dataAtual);
		
		List<TarifasVigenciaTO> faixasResidenciais = mockarFaixasResidencial(1);
		List<TarifasVigenciaTO> faixasComerciais = mockarFaixasComerciais(dataVigencia);
		
		when(consumoBOMock.obterFaixas(anyObject())).thenReturn(faixasResidenciais, faixasComerciais);
		
		List<ConsumoImovelCategoriaTO> consumoImoveisCategoriaTO = bo.distribuirConsumoPorFaixa();

		Map<ConsumoTarifaFaixaTO, Integer> faixasConsumoResidencial = consumoImoveisCategoriaTO.get(0).getTabelaTarifas().get(0).getConsumoPorFaixa();
		Map<ConsumoTarifaFaixaTO, Integer> faixasConsumoComercial = consumoImoveisCategoriaTO.get(1).getTabelaTarifas().get(0).getConsumoPorFaixa();
		
		assertEquals(1, faixasConsumoResidencial.values().size());
		assertEquals(new Integer(1), faixasConsumoResidencial.get(faixaResidencial11a20));
		
		assertEquals(1, faixasConsumoComercial.values().size());
		assertEquals(new Integer(1), faixasConsumoComercial.get(faixaComercialMaior10));
		
	}
	
	@Test
	public void calcularValorConsumoUmaCategoriaUmaEconomia() {
		configurarImovelUmaCategoriaUmaEconomia();
		configurarVigenciasUmaCategoria(null);
		
		List<TarifasVigenciaTO> faixas = mockarFaixasResidencial(3);
		when(consumoBOMock.obterFaixas(anyObject())).thenReturn(faixas);
		
		List<ConsumoImovelCategoriaTO> consumoImoveisCategoriaTO = bo.getConsumoImoveisCategoriaTO(consumoHistoricoMock, dataAnterior, dataAtual);
		
		assertEquals(new BigDecimal(109.20).setScale(2, BigDecimal.ROUND_HALF_UP), bo.getValorConsumoTotal(consumoImoveisCategoriaTO.get(0)));
	}
	
	@Test
	public void calcularValorConsumoUmaCategoriaTresEconomias() {
		configurarImovelUmaCategoriaTresEconomias();
		configurarVigenciasUmaCategoria(null);
		
		List<TarifasVigenciaTO> faixas = mockarFaixasResidencial(1);
		when(consumoBOMock.obterFaixas(anyObject())).thenReturn(faixas);
		
		List<ConsumoImovelCategoriaTO> consumoImoveisCategoriaTO = bo.getConsumoImoveisCategoriaTO(consumoHistoricoMock, dataAnterior, dataAtual);
		
		assertEquals(new BigDecimal(93.60).setScale(2, BigDecimal.ROUND_HALF_UP), bo.getValorConsumoTotal(consumoImoveisCategoriaTO.get(0)));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void calcularValorConsumoDuasCategoriaDuasEconomias() {
		configurarImovelDuasCategoriasDuasEconomias();
		configurarVigenciasDuasCategoria(null);
		
		List<TarifasVigenciaTO> faixasResidenciais = mockarFaixasResidencial(1);
		List<TarifasVigenciaTO> faixasComerciais = mockarFaixasComerciais(dataVigencia);
		
		when(consumoBOMock.obterFaixas(anyObject())).thenReturn(faixasResidenciais, faixasComerciais);
		
		List<ConsumoImovelCategoriaTO> consumoImoveisCategoriaTO = bo.getConsumoImoveisCategoriaTO(consumoHistoricoMock, dataAnterior, dataAtual);
		
		assertEquals(new BigDecimal(40.80).setScale(2, BigDecimal.ROUND_HALF_UP), bo.getValorConsumoTotal(consumoImoveisCategoriaTO.get(0)));
		assertEquals(new BigDecimal(112.80).setScale(2, BigDecimal.ROUND_HALF_UP), bo.getValorConsumoTotal(consumoImoveisCategoriaTO.get(1)));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void calcularValorConsumoDuasCategoriaSeteEconomias() {
		configurarImovelDuasCategoriasSeteEconomias();
		configurarVigenciasDuasCategoria(null);
		
		List<TarifasVigenciaTO> faixasResidenciais = mockarFaixasResidencial(1);
		List<TarifasVigenciaTO> faixasComerciais = mockarFaixasComerciais(dataVigencia);
		
		when(consumoBOMock.obterFaixas(anyObject())).thenReturn(faixasResidenciais, faixasComerciais);
		
		List<ConsumoImovelCategoriaTO> consumoImoveisCategoriaTO = bo.getConsumoImoveisCategoriaTO(consumoHistoricoMock, dataAnterior, dataAtual);
		
		assertEquals(new BigDecimal(96.00).setScale(2, BigDecimal.ROUND_HALF_UP), bo.getValorConsumoTotal(consumoImoveisCategoriaTO.get(0)));
		assertEquals(new BigDecimal(112.92).setScale(2, BigDecimal.ROUND_HALF_UP), bo.getValorConsumoTotal(consumoImoveisCategoriaTO.get(1)));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void calcularValorTotalConsumoDuasCategoriaSeteEconomias() {
		configurarImovelDuasCategoriasSeteEconomias();
		configurarVigenciasDuasCategoria(null);
		
		List<TarifasVigenciaTO> faixasResidenciais = mockarFaixasResidencial(1);
		List<TarifasVigenciaTO> faixasComerciais = mockarFaixasComerciais(dataVigencia);
		
		when(consumoBOMock.obterFaixas(anyObject())).thenReturn(faixasResidenciais, faixasComerciais);
		
		BigDecimal valorConsumoTotal = bo.getValorTotalConsumoImovel(consumoHistoricoMock, dataAnterior, dataAtual);
		
		assertEquals(new BigDecimal(208.92).setScale(2, BigDecimal.ROUND_HALF_UP), valorConsumoTotal);
	}
	
	@Test
	public void calcularValorTotalConsumoUmaCategoriaUmaEconomiaDuasVigencias() {
		Date dataVigenciaAnterior = new DateTime(2009, 7, 7, 0, 0).toDate();

		configurarImovelUmaCategoriaUmaEconomia();
		configurarVigenciasUmaCategoria(dataVigenciaAnterior);
		
		List<TarifasVigenciaTO> faixas = mockarFaixasResidencialComDataVigenciaAnterior(dataVigenciaAnterior, 1);
		faixas.addAll(mockarFaixasResidencial(1));
		
		when(consumoBOMock.obterFaixas(anyObject())).thenReturn(faixas);
		
		BigDecimal valorConsumoTotal = bo.getValorTotalConsumoImovel(consumoHistoricoMock, dataAnterior, dataAtual);
		
		assertEquals(new BigDecimal(60.33).setScale(2, BigDecimal.ROUND_HALF_UP), valorConsumoTotal);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void calcularValorTotalConsumoDuasCategoriaDuasEconomiaDuasVigencias() {
		Date dataVigenciaAnterior = new DateTime(2009, 7, 7, 0, 0).toDate();

		configurarImovelDuasCategoriasDuasEconomias();
		configurarVigenciasDuasCategoria(dataVigenciaAnterior);
		
		List<TarifasVigenciaTO> faixasResidenciais = mockarFaixasResidencialComDataVigenciaAnterior(dataVigenciaAnterior, 1);
		faixasResidenciais.addAll(mockarFaixasResidencial(1));
		
		List<TarifasVigenciaTO> faixasComerciais = mockarFaixasComerciais(dataVigenciaAnterior);
		faixasComerciais.addAll(mockarFaixasComerciais(dataVigencia));
		
		when(consumoBOMock.obterFaixas(anyObject())).thenReturn(faixasResidenciais, faixasComerciais);
		
		BigDecimal valorConsumoTotal = bo.getValorTotalConsumoImovel(consumoHistoricoMock, dataAnterior, dataAtual);
		
		assertEquals(new BigDecimal(79.55).setScale(2, BigDecimal.ROUND_HALF_UP), valorConsumoTotal);
	}

	private void configurarImovelUmaCategoriaConsumoMenorMinimo() {
		when(consumoBOMock.getQuantidadeTotalEconomias(imovelMock.getId())).thenReturn(1);
		when(consumoBOMock.consumoMinimoLigacao(imovelMock.getId())).thenReturn(10);
		when(consumoHistoricoMock.getNumeroConsumoFaturadoMes()).thenReturn(9);
		when(consumoBOMock.buscarQuantidadeEconomiasPorImovel(imovelMock.getId())).thenReturn(umaCategoria);	
		when(consumoBOMock.getConsumoTarifasCategoria(imovelMock, dataAnterior, dataAtual, categoriaResidencialMock)).thenReturn(listConsumoTarifaCategoriaResidencial);
		
		when(consumoTarifaAtualCategoriaResidencialMock.possuiVigencia(anyObject())).thenReturn(true);
		when(consumoTarifaAtualCategoriaResidencialMock.getConsumoTarifaVigencia()).thenReturn(consumoTarifaVigenciaAtualMock);
	}
	
	private void configurarImovelUmaCategoriaUmaEconomia() {
		when(consumoBOMock.getQuantidadeTotalEconomias(imovelMock.getId())).thenReturn(1);
		when(consumoBOMock.consumoMinimoLigacao(imovelMock.getId())).thenReturn(10);
		when(consumoHistoricoMock.getNumeroConsumoFaturadoMes()).thenReturn(40);
		when(consumoBOMock.buscarQuantidadeEconomiasPorImovel(imovelMock.getId())).thenReturn(umaCategoria);
		when(consumoBOMock.getQuantidadeEconomiasPorCategoria(categoriaResidencialMock)).thenReturn(1);
	}
	
	private void configurarImovelUmaCategoriaTresEconomias() {
		when(consumoBOMock.getQuantidadeTotalEconomias(imovelMock.getId())).thenReturn(3);
		when(consumoBOMock.consumoMinimoLigacao(imovelMock.getId())).thenReturn(30);
		when(consumoHistoricoMock.getNumeroConsumoFaturadoMes()).thenReturn(48);
		when(consumoBOMock.buscarQuantidadeEconomiasPorImovel(imovelMock.getId())).thenReturn(umaCategoria);
		when(consumoBOMock.getQuantidadeEconomiasPorCategoria(categoriaResidencialMock)).thenReturn(3);
	}
	
	private void configurarVigenciasUmaCategoria(Date dataVigenciaAnterior) {
		Date dataAnterior = this.dataAnterior;
		Date dataAtual = this.dataAtual;
		
		if(dataVigenciaAnterior != null) {
			dataAnterior = new DateTime(2016, 4, 10, 0, 0).toDate();
			dataAtual = new DateTime(2016, 5, 10, 0, 0).toDate();
			
			listConsumoTarifaCategoriaResidencial.add(consumoTarifaAnteriorCategoriaResidencialMock);
			
			when(consumoTarifaAnteriorCategoriaResidencialMock.possuiVigencia(dataVigenciaAnterior)).thenReturn(true);
			when(consumoTarifaAnteriorCategoriaResidencialMock.getConsumoTarifaVigencia()).thenReturn(consumoTarifaVigenciaAnteriorMock);
			when(consumoTarifaAnteriorCategoriaResidencialMock.getValorConsumoMinimo()).thenReturn(new BigDecimal(14));
		}
		
		when(consumoBOMock.getConsumoTarifasCategoria(imovelMock, dataAnterior, dataAtual, categoriaResidencialMock)).thenReturn(listConsumoTarifaCategoriaResidencial);
		when(consumoTarifaAtualCategoriaResidencialMock.possuiVigencia(dataVigencia)).thenReturn(true);
		when(consumoTarifaAtualCategoriaResidencialMock.getConsumoTarifaVigencia()).thenReturn(consumoTarifaVigenciaAtualMock);
		when(consumoTarifaAtualCategoriaResidencialMock.getValorConsumoMinimo()).thenReturn(new BigDecimal(16.8));
	}
	
	private void configurarImovelDuasCategoriasDuasEconomias() {
		when(consumoBOMock.getQuantidadeTotalEconomias(imovelMock.getId())).thenReturn(2);
		when(consumoBOMock.consumoMinimoLigacao(imovelMock.getId())).thenReturn(20);
		when(consumoHistoricoMock.getNumeroConsumoFaturadoMes()).thenReturn(40);
		when(consumoBOMock.buscarQuantidadeEconomiasPorImovel(imovelMock.getId())).thenReturn(duasCategorias);
		when(consumoBOMock.getQuantidadeEconomiasPorCategoria(categoriaResidencialMock)).thenReturn(1);
		when(consumoBOMock.getQuantidadeEconomiasPorCategoria(categoriaComercialMock)).thenReturn(1);
	}
	
	private void configurarImovelDuasCategoriasSeteEconomias() {
		when(consumoBOMock.getQuantidadeTotalEconomias(imovelMock.getId())).thenReturn(7);
		when(consumoBOMock.consumoMinimoLigacao(imovelMock.getId())).thenReturn(70);
		when(consumoHistoricoMock.getNumeroConsumoFaturadoMes()).thenReturn(77);
		when(consumoBOMock.buscarQuantidadeEconomiasPorImovel(imovelMock.getId())).thenReturn(duasCategorias);
		when(consumoBOMock.getQuantidadeEconomiasPorCategoria(categoriaResidencialMock)).thenReturn(5);
		when(consumoBOMock.getQuantidadeEconomiasPorCategoria(categoriaComercialMock)).thenReturn(2);
	}
	
	private void configurarVigenciasDuasCategoria(Date dataVigenciaAnterior) {
		Date dataAnterior = this.dataAnterior;
		Date dataAtual = this.dataAtual;
		
		if(dataVigenciaAnterior != null) {
			dataAnterior = new DateTime(2016, 4, 10, 0, 0).toDate();
			dataAtual= new DateTime(2016, 5, 10, 0, 0).toDate();
			
			listConsumoTarifaCategoriaResidencial.add(consumoTarifaAnteriorCategoriaResidencialMock);
			listConsumoTarifaCategoriaComercial.add(consumoTarifaAnteriorCategoriaComercialMock);
			
			when(consumoTarifaAnteriorCategoriaResidencialMock.possuiVigencia(dataVigenciaAnterior)).thenReturn(true);
			when(consumoTarifaAnteriorCategoriaComercialMock.possuiVigencia(dataVigenciaAnterior)).thenReturn(true);
			when(consumoTarifaAnteriorCategoriaResidencialMock.getConsumoTarifaVigencia()).thenReturn(consumoTarifaVigenciaAnteriorMock);
			when(consumoTarifaAnteriorCategoriaComercialMock.getConsumoTarifaVigencia()).thenReturn(consumoTarifaVigenciaAnteriorMock);
			when(consumoTarifaAnteriorCategoriaResidencialMock.getValorConsumoMinimo()).thenReturn(new BigDecimal(14));
			when(consumoTarifaAnteriorCategoriaComercialMock.getValorConsumoMinimo()).thenReturn(new BigDecimal(41.8));
		}
		
		when(consumoBOMock.getConsumoTarifasCategoria(imovelMock, dataAnterior, dataAtual, categoriaResidencialMock)).thenReturn(listConsumoTarifaCategoriaResidencial);
		when(consumoBOMock.getConsumoTarifasCategoria(imovelMock, dataAnterior, dataAtual, categoriaComercialMock)).thenReturn(listConsumoTarifaCategoriaComercial);
		when(consumoTarifaAtualCategoriaResidencialMock.possuiVigencia(dataVigencia)).thenReturn(true);
		when(consumoTarifaAtualCategoriaComercialMock.possuiVigencia(dataVigencia)).thenReturn(true);
		when(consumoTarifaAtualCategoriaResidencialMock.getConsumoTarifaVigencia()).thenReturn(consumoTarifaVigenciaAtualMock);
		when(consumoTarifaAtualCategoriaComercialMock.getConsumoTarifaVigencia()).thenReturn(consumoTarifaVigenciaAtualMock);
		when(consumoTarifaAtualCategoriaResidencialMock.getValorConsumoMinimo()).thenReturn(new BigDecimal(16.8));
		when(consumoTarifaAtualCategoriaComercialMock.getValorConsumoMinimo()).thenReturn(new BigDecimal(50.2));
	}
	
	private List<TarifasVigenciaTO> mockarFaixasResidencial(int qtdFaixas) {
		
		when(faixaResidencial11a20.getNumeroConsumoFaixaInicio()).thenReturn(11);
		when(faixaResidencial11a20.getNumeroConsumoFaixaFim()).thenReturn(20);
		when(faixaResidencial11a20.getConsumoTotalFaixa()).thenReturn(10);
		when(faixaResidencial11a20.getValorConsumoTarifa()).thenReturn(new BigDecimal(2.4));
		
		when(faixaResidencial21a30.getNumeroConsumoFaixaInicio()).thenReturn(21);
		when(faixaResidencial21a30.getNumeroConsumoFaixaFim()).thenReturn(30);
		when(faixaResidencial21a30.getConsumoTotalFaixa()).thenReturn(10);
		when(faixaResidencial21a30.getValorConsumoTarifa()).thenReturn(new BigDecimal(3.22));
		
		when(faixaResidencial31a40.getNumeroConsumoFaixaInicio()).thenReturn(31);
		when(faixaResidencial31a40.getNumeroConsumoFaixaFim()).thenReturn(40);
		when(faixaResidencial31a40.getConsumoTotalFaixa()).thenReturn(10);
		when(faixaResidencial31a40.getValorConsumoTarifa()).thenReturn(new BigDecimal(3.62));

		List<ConsumoTarifaFaixaTO> faixas = new ArrayList<ConsumoTarifaFaixaTO>();
		faixas.add(faixaResidencial11a20);
		faixas.add(faixaResidencial21a30);
		faixas.add(faixaResidencial31a40);
		
		List<ConsumoTarifaFaixaTO> faixasUtilizadas = new ArrayList<ConsumoTarifaFaixaTO>();
		for (int i = 0; i < qtdFaixas; i++) {
			faixasUtilizadas.add(faixas.get(i));
		}
		
		List<TarifasVigenciaTO> tabelaTarifas = new ArrayList<TarifasVigenciaTO>();
		tabelaTarifas.add(new TarifasVigenciaTO(dataVigencia, faixasUtilizadas));
		
		return tabelaTarifas;
	}
	
	private List<TarifasVigenciaTO> mockarFaixasResidencialComDataVigenciaAnterior(Date dataVigenciaAnterior, int qtdFaixas) {
		
		when(faixaResidencial11a20.getNumeroConsumoFaixaInicio()).thenReturn(11);
		when(faixaResidencial11a20.getNumeroConsumoFaixaFim()).thenReturn(20);
		when(faixaResidencial11a20.getConsumoTotalFaixa()).thenReturn(10);
		when(faixaResidencial11a20.getValorConsumoTarifa()).thenReturn(new BigDecimal(2.0));
		
		when(faixaResidencial21a30.getNumeroConsumoFaixaInicio()).thenReturn(21);
		when(faixaResidencial21a30.getNumeroConsumoFaixaFim()).thenReturn(30);
		when(faixaResidencial21a30.getConsumoTotalFaixa()).thenReturn(10);
		when(faixaResidencial21a30.getValorConsumoTarifa()).thenReturn(new BigDecimal(2.68));
		
		when(faixaResidencial31a40.getNumeroConsumoFaixaInicio()).thenReturn(31);
		when(faixaResidencial31a40.getNumeroConsumoFaixaFim()).thenReturn(40);
		when(faixaResidencial31a40.getConsumoTotalFaixa()).thenReturn(10);
		when(faixaResidencial31a40.getValorConsumoTarifa()).thenReturn(new BigDecimal(3.02));

		List<ConsumoTarifaFaixaTO> faixas = new ArrayList<ConsumoTarifaFaixaTO>();
		faixas.add(faixaResidencial11a20);
		faixas.add(faixaResidencial21a30);
		faixas.add(faixaResidencial31a40);
		
		List<ConsumoTarifaFaixaTO> faixasUtilizadas = new ArrayList<ConsumoTarifaFaixaTO>();
		for (int i = 0; i < qtdFaixas; i++) {
			faixasUtilizadas.add(faixas.get(i));
		}
		
		List<TarifasVigenciaTO> tabelaTarifas = new ArrayList<TarifasVigenciaTO>();
		tabelaTarifas.add(new TarifasVigenciaTO(dataVigenciaAnterior, faixasUtilizadas));
		
		return tabelaTarifas;
	}
	
	private List<TarifasVigenciaTO> mockarFaixasComerciais(Date vigencia) {
		when(faixaComercialMaior10.getNumeroConsumoFaixaInicio()).thenReturn(11);
		when(faixaComercialMaior10.getNumeroConsumoFaixaFim()).thenReturn(99999);
		when(faixaComercialMaior10.getConsumoTotalFaixa()).thenReturn(10);
		when(faixaComercialMaior10.getValorConsumoTarifa()).thenReturn(new BigDecimal(6.26));

		List<ConsumoTarifaFaixaTO> faixas = new ArrayList<ConsumoTarifaFaixaTO>();
		faixas.add(faixaComercialMaior10);
		
		List<TarifasVigenciaTO> tabelaTarifas = new ArrayList<TarifasVigenciaTO>();
		tabelaTarifas.add(new TarifasVigenciaTO(vigencia, faixas));
		
		return tabelaTarifas;
	}
}
