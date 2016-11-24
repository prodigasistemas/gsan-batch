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
		
		List<TarifasVigenciaTO> faixas = buildFaixasResidencial(dataVigencia, 3);
		when(consumoBOMock.obterFaixas(anyObject())).thenReturn(faixas);
		
		List<ConsumoImovelCategoriaTO> consumoImoveisCategoriaTO = bo.distribuirConsumoPorFaixa();

		Map<Integer, ConsumoTarifaFaixaTO> faixasConsumo = consumoImoveisCategoriaTO.get(0).getVigencias().get(0).getFaixas();
		
		assertEquals(3, faixasConsumo.values().size());
		assertEquals(new Integer(10), faixasConsumo.get(faixaResidencial11a20.getIdConsumoTarifa()).getConsumo());
		assertEquals(new Integer(10), faixasConsumo.get(faixaResidencial21a30.getIdConsumoTarifa()).getConsumo());
		assertEquals(new Integer(10), faixasConsumo.get(faixaResidencial31a40.getIdConsumoTarifa()).getConsumo());
	}
	
	@Test
	public void distribuirConsumoFaixasUmaCategoriaTresEconomias() {
		configurarImovelUmaCategoriaTresEconomias();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, dataAnterior, dataAtual);
		
		List<TarifasVigenciaTO> faixas = buildFaixasResidencial(dataVigencia, 1);
		when(consumoBOMock.obterFaixas(anyObject())).thenReturn(faixas);

		List<ConsumoImovelCategoriaTO> consumoImoveisCategoriaTO = bo.distribuirConsumoPorFaixa();
		
		Map<Integer, ConsumoTarifaFaixaTO> faixasConsumo = consumoImoveisCategoriaTO.get(0).getVigencias().get(0).getFaixas();
		
		assertEquals(1, faixasConsumo.values().size());
		assertEquals(new Integer(6), faixasConsumo.get(faixaResidencial11a20.getIdConsumoTarifa()).getConsumo());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void distribuirConsumoFaixasDuasCategoriasDuasEconomias() {
		configurarImovelDuasCategoriasDuasEconomias();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, dataAnterior, dataAtual);
		
		List<TarifasVigenciaTO> faixasResidenciais = buildFaixasResidencial(dataVigencia, 1);
		List<TarifasVigenciaTO> faixasComerciais = buildFaixasComerciais(dataVigencia);
		
		when(consumoBOMock.obterFaixas(anyObject())).thenReturn(faixasResidenciais, faixasComerciais);

		List<ConsumoImovelCategoriaTO> consumoImoveisCategoriaTO = bo.distribuirConsumoPorFaixa();
		
		Map<Integer, ConsumoTarifaFaixaTO> faixasConsumoResidencial = consumoImoveisCategoriaTO.get(0).getVigencias().get(0).getFaixas();
		Map<Integer, ConsumoTarifaFaixaTO> faixasConsumoComercial   = consumoImoveisCategoriaTO.get(1).getVigencias().get(0).getFaixas();
		
		assertEquals(1, faixasConsumoResidencial.values().size());
		assertEquals(new Integer(10), faixasConsumoResidencial.get(faixaResidencial11a20.getIdConsumoTarifa()).getConsumo());
		
		assertEquals(1, faixasConsumoComercial.values().size());
		assertEquals(new Integer(10), faixasConsumoComercial.get(faixaComercialMaior10.getIdConsumoTarifa()).getConsumo());
		
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void distribuirConsumoFaixasDuasCategoriasSeteEconomias() {
		configurarImovelDuasCategoriasSeteEconomias();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, dataAnterior, dataAtual);
		
		List<TarifasVigenciaTO> faixasResidenciais = buildFaixasResidencial(dataVigencia, 1);
		List<TarifasVigenciaTO> faixasComerciais = buildFaixasComerciais(dataVigencia);
		
		when(consumoBOMock.obterFaixas(anyObject())).thenReturn(faixasResidenciais, faixasComerciais);
		
		List<ConsumoImovelCategoriaTO> consumoImoveisCategoriaTO = bo.distribuirConsumoPorFaixa();

		Map<Integer, ConsumoTarifaFaixaTO> faixasConsumoResidencial = consumoImoveisCategoriaTO.get(0).getVigencias().get(0).getFaixas();
		Map<Integer, ConsumoTarifaFaixaTO> faixasConsumoComercial   = consumoImoveisCategoriaTO.get(1).getVigencias().get(0).getFaixas();
		
		assertEquals(1, faixasConsumoResidencial.values().size());
		assertEquals(new Integer(1), faixasConsumoResidencial.get(faixaResidencial11a20.getIdConsumoTarifa()).getConsumo());
		
		assertEquals(1, faixasConsumoComercial.values().size());
		assertEquals(new Integer(1), faixasConsumoComercial.get(faixaComercialMaior10.getIdConsumoTarifa()).getConsumo());
		
	}
	
	@Test
	public void calcularValorConsumoUmaCategoriaUmaEconomia() {
		configurarImovelUmaCategoriaUmaEconomia();
		configurarVigenciasUmaCategoria(null);
		
		List<TarifasVigenciaTO> vigencias = buildFaixasResidencial(dataVigencia, 3);
		
		List<ConsumoImovelCategoriaTO> consumoImoveisCategoriaTO = bo.getConsumoImoveisCategoriaTO(consumoHistoricoMock, dataAnterior, dataAtual);
		
		ConsumoImovelCategoriaTO consumoImovel = consumoImoveisCategoriaTO.get(0);
		
		consumoImovel.setVigencias(vigencias);
		
		assertEquals(new BigDecimal(109.20).setScale(2, BigDecimal.ROUND_HALF_UP), bo.getValorConsumoTotal(consumoImovel));
	}
	
	@Test
	public void calcularValorConsumoUmaCategoriaTresEconomias() {
		configurarImovelUmaCategoriaTresEconomias();
		configurarVigenciasUmaCategoria(null);
		
		List<TarifasVigenciaTO> faixas = buildFaixasResidencial(dataVigencia, 1);
		when(consumoBOMock.obterFaixas(anyObject())).thenReturn(faixas);
		
		List<ConsumoImovelCategoriaTO> consumoImoveisCategoriaTO = bo.getConsumoImoveisCategoriaTO(consumoHistoricoMock, dataAnterior, dataAtual);
		
		assertEquals(new BigDecimal(93.60).setScale(2, BigDecimal.ROUND_HALF_UP), bo.getValorConsumoTotal(consumoImoveisCategoriaTO.get(0)));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void calcularValorConsumoDuasCategoriaDuasEconomias() {
		configurarImovelDuasCategoriasDuasEconomias();
		configurarVigenciasDuasCategoria(null);
		
		List<TarifasVigenciaTO> faixasResidenciais = buildFaixasResidencial(dataVigencia, 1);
		List<TarifasVigenciaTO> faixasComerciais = buildFaixasComerciais(dataVigencia);
		
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
		
		List<TarifasVigenciaTO> faixasResidenciais = buildFaixasResidencial(dataVigencia, 1);
		List<TarifasVigenciaTO> faixasComerciais = buildFaixasComerciais(dataVigencia);
		
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
		
		List<TarifasVigenciaTO> faixasResidenciais = buildFaixasResidencial(dataVigencia, 1);
		List<TarifasVigenciaTO> faixasComerciais = buildFaixasComerciais(dataVigencia);
		
		when(consumoBOMock.obterFaixas(anyObject())).thenReturn(faixasResidenciais, faixasComerciais);
		
		BigDecimal valorConsumoTotal = bo.getValorTotalConsumoImovel(consumoHistoricoMock, dataAnterior, dataAtual);
		
		assertEquals(new BigDecimal(208.92).setScale(2, BigDecimal.ROUND_HALF_UP), valorConsumoTotal);
	}
	
	@Test
	public void calcularValorTotalConsumoUmaCategoriaUmaEconomiaDuasVigencias() {
		Date dataVigenciaAnterior = new DateTime(2009, 7, 7, 0, 0).toDate();
		
		dataAnterior = new DateTime(2016, 4, 1, 0, 0).toDate();
		dataAtual= new DateTime(2016, 5, 1, 0, 0).toDate();

		configurarImovelUmaCategoriaUmaEconomia();
		configurarVigenciasUmaCategoria(dataVigenciaAnterior);
		
		List<TarifasVigenciaTO> faixas = buildFaixasResidencial(dataVigenciaAnterior, 1);
		faixas.addAll(buildFaixasResidencial(dataVigencia, 1));
		
		when(consumoBOMock.obterFaixas(anyObject())).thenReturn(faixas);
		
		BigDecimal valorConsumoTotal = bo.getValorTotalConsumoImovel(consumoHistoricoMock, dataAnterior, dataAtual);
		
		assertEquals(new BigDecimal(52.29).setScale(2, BigDecimal.ROUND_HALF_UP), valorConsumoTotal);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void calcularValorTotalConsumoDuasCategoriaDuasEconomiaDuasVigencias() {
		Date dataVigenciaAnterior = new DateTime(2009, 7, 7, 0, 0).toDate();

		dataAnterior = new DateTime(2016, 4, 1, 0, 0).toDate();
		dataAtual= new DateTime(2016, 5, 1, 0, 0).toDate();
		
		configurarImovelDuasCategoriasDuasEconomias();
		configurarVigenciasDuasCategoria(dataVigenciaAnterior);
		
		List<TarifasVigenciaTO> faixasResidenciais = buildFaixasResidencial(dataVigenciaAnterior, 1);
		faixasResidenciais.addAll(buildFaixasResidencial(dataVigencia, 1));
		
		List<TarifasVigenciaTO> faixasComerciais = buildFaixasComerciais(dataVigenciaAnterior);
		faixasComerciais.addAll(buildFaixasComerciais(dataVigencia));
		
		when(consumoBOMock.obterFaixas(anyObject())).thenReturn(faixasResidenciais, faixasComerciais);
		
		BigDecimal valorConsumoTotal = bo.getValorTotalConsumoImovel(consumoHistoricoMock, dataAnterior, dataAtual);
		
		assertEquals(new BigDecimal(102.17).setScale(2, BigDecimal.ROUND_HALF_UP), valorConsumoTotal);
	}
	
	private void configurarImovelUmaCategoriaConsumoMenorMinimo() {
		when(consumoBOMock.getQuantidadeTotalEconomias(imovelMock.getId())).thenReturn(1);
		when(consumoBOMock.consumoMinimoLigacao(imovelMock.getId())).thenReturn(10);
		when(consumoHistoricoMock.getNumeroConsumoFaturadoMes()).thenReturn(9);
		when(consumoBOMock.buscarQuantidadeEconomiasPorImovel(imovelMock.getId())).thenReturn(umaCategoria);	
		when(consumoBOMock.getConsumoTarifasCategoria(imovelMock, dataAnterior, dataAtual, categoriaResidencialMock)).thenReturn(listConsumoTarifaCategoriaResidencial);
		
		when(consumoTarifaAtualCategoriaResidencialMock.possuiVigencia(anyObject())).thenReturn(true);
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
		if(dataVigenciaAnterior != null) {
			when(consumoTarifaAnteriorCategoriaResidencialMock.possuiVigencia(dataVigenciaAnterior)).thenReturn(true);
			when(consumoTarifaAnteriorCategoriaResidencialMock.getValorConsumoMinimo()).thenReturn(new BigDecimal(14));

			listConsumoTarifaCategoriaResidencial.add(consumoTarifaAnteriorCategoriaResidencialMock);
		}
		
		when(consumoBOMock.getConsumoTarifasCategoria(imovelMock, dataAnterior, dataAtual, categoriaResidencialMock)).thenReturn(listConsumoTarifaCategoriaResidencial);
		when(consumoTarifaAtualCategoriaResidencialMock.possuiVigencia(dataVigencia)).thenReturn(true);
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
		if(dataVigenciaAnterior != null) {
			when(consumoTarifaAnteriorCategoriaResidencialMock.possuiVigencia(dataVigenciaAnterior)).thenReturn(true);
			when(consumoTarifaAnteriorCategoriaComercialMock.possuiVigencia(dataVigenciaAnterior)).thenReturn(true);
			when(consumoTarifaAnteriorCategoriaResidencialMock.getValorConsumoMinimo()).thenReturn(new BigDecimal(14));
			when(consumoTarifaAnteriorCategoriaComercialMock.getValorConsumoMinimo()).thenReturn(new BigDecimal(41.8));
			
			listConsumoTarifaCategoriaResidencial.add(consumoTarifaAnteriorCategoriaResidencialMock);
			listConsumoTarifaCategoriaComercial.add(consumoTarifaAnteriorCategoriaComercialMock);
		}
		
		when(consumoBOMock.getConsumoTarifasCategoria(imovelMock, dataAnterior, dataAtual, categoriaResidencialMock)).thenReturn(listConsumoTarifaCategoriaResidencial);
		when(consumoBOMock.getConsumoTarifasCategoria(imovelMock, dataAnterior, dataAtual, categoriaComercialMock)).thenReturn(listConsumoTarifaCategoriaComercial);
		when(consumoTarifaAtualCategoriaResidencialMock.possuiVigencia(dataVigencia)).thenReturn(true);
		when(consumoTarifaAtualCategoriaComercialMock.possuiVigencia(dataVigencia)).thenReturn(true);
		when(consumoTarifaAtualCategoriaResidencialMock.getValorConsumoMinimo()).thenReturn(new BigDecimal(16.8));
		when(consumoTarifaAtualCategoriaComercialMock.getValorConsumoMinimo()).thenReturn(new BigDecimal(50.2));
	}
	
	private List<TarifasVigenciaTO> buildFaixasResidencial(Date dataVigencia, int qtdFaixas) {
		
		
		faixaResidencial11a20 = new ConsumoTarifaFaixaTO();
		faixaResidencial11a20.setIdConsumoTarifa(1);
		faixaResidencial11a20.setNumeroConsumoFaixaInicio(11);
		faixaResidencial11a20.setNumeroConsumoFaixaFim(20);
		faixaResidencial11a20.setValorTarifa(new BigDecimal(2.4));
		faixaResidencial11a20.setConsumo(10);
		
		faixaResidencial21a30 = new ConsumoTarifaFaixaTO();
		faixaResidencial21a30.setIdConsumoTarifa(2);
		faixaResidencial21a30.setNumeroConsumoFaixaInicio(21);
		faixaResidencial21a30.setNumeroConsumoFaixaFim(30);
		faixaResidencial21a30.setValorTarifa(new BigDecimal(3.22));
		faixaResidencial21a30.setConsumo(10);
		
		faixaResidencial31a40 = new ConsumoTarifaFaixaTO();
		faixaResidencial31a40.setIdConsumoTarifa(3);
		faixaResidencial31a40.setNumeroConsumoFaixaInicio(31);
		faixaResidencial31a40.setNumeroConsumoFaixaFim(40);
		faixaResidencial31a40.setValorTarifa(new BigDecimal(3.62));
		faixaResidencial31a40.setConsumo(10);

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
		
	private List<TarifasVigenciaTO> buildFaixasComerciais(Date vigencia) {
		faixaComercialMaior10 = new ConsumoTarifaFaixaTO();
		faixaComercialMaior10.setIdConsumoTarifa(1);
		faixaComercialMaior10.setNumeroConsumoFaixaInicio(11);
		faixaComercialMaior10.setNumeroConsumoFaixaFim(99999);
		faixaComercialMaior10.setValorTarifa(new BigDecimal(6.26));
		faixaComercialMaior10.setConsumo(10);

		List<ConsumoTarifaFaixaTO> faixas = new ArrayList<ConsumoTarifaFaixaTO>();
		faixas.add(faixaComercialMaior10);
		
		List<TarifasVigenciaTO> tabelaTarifas = new ArrayList<TarifasVigenciaTO>();
		tabelaTarifas.add(new TarifasVigenciaTO(vigencia, faixas));
		
		return tabelaTarifas;
	}
}
