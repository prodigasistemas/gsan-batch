package br.gov.batch.servicos.faturamento.tarifa;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import br.gov.model.faturamento.ConsumoImovelCategoriaTO;
import br.gov.model.faturamento.ConsumoTarifaVigencia;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.servicos.faturamento.ConsumoTarifaCategoriaRepositorio;
import br.gov.servicos.to.ConsumoTarifaCategoriaTO;
import br.gov.servicos.to.ConsumoTarifaFaixaTO;

@RunWith(MockitoJUnitRunner.class)
public class ConsumoImovelBOTest {

	@Mock private ConsumoHistorico consumoHistoricoMock;
	@Mock private Imovel imovelMock;
	@Mock private ConsumoBO consumoBOMock;
	@Mock private ConsumoTarifaVigencia consumoTarifaVigenciaMock;
	@Mock private ICategoria categoriaResidencialMock;
	@Mock private ICategoria categoriaComercialMock;
	@Mock private MedicaoHistorico medicaoHistoricoMock;
	@Mock private ConsumoTarifaCategoriaRepositorio consumoTarifaCategoriaRepositorioMock;
	
	@Mock private ConsumoTarifaCategoriaTO consumoTarifaCategoriaResidencialMock;
	@Mock private ConsumoTarifaCategoriaTO consumoTarifaCategoriaComercialMock;
	
	@Mock private ConsumoTarifaFaixaTO faixaResidencial11a20;
	@Mock private ConsumoTarifaFaixaTO faixaResidencial21a30;
	@Mock private ConsumoTarifaFaixaTO faixaResidencial31a40;
	
	@Mock private ConsumoTarifaFaixaTO faixaComercialMaior10;
	
	@InjectMocks private ConsumoImovelCategoriaBO bo;
	
	private Collection<ICategoria> umaCategoria;
	private Collection<ICategoria> duasCategorias;
	
	private List<ConsumoTarifaCategoriaTO> listConsumoTarifaCategoriaResidencial;
	private List<ConsumoTarifaCategoriaTO> listConsumoTarifaCategoriaComercial;
	
	@Before
	public void setup() {
		bo = new ConsumoImovelCategoriaBO();
		
		MockitoAnnotations.initMocks(this);
		
		when(consumoBOMock.getConsumoMinimoTarifaPorCategoria(consumoTarifaVigenciaMock.getId(), categoriaResidencialMock)).thenReturn(10);
		when(consumoBOMock.getConsumoMinimoTarifaPorCategoria(consumoTarifaVigenciaMock.getId(), categoriaComercialMock)).thenReturn(10);
		when(consumoHistoricoMock.getImovel()).thenReturn(imovelMock);
		
		when(medicaoHistoricoMock.getDataLeituraAnteriorFaturamento()).thenReturn(new Date());
		when(medicaoHistoricoMock.getDataLeituraAtualFaturamento()).thenReturn(new Date());
		
		umaCategoria = new ArrayList<ICategoria>();
		umaCategoria.add(categoriaResidencialMock);
		
		duasCategorias = new ArrayList<ICategoria>();
		duasCategorias.add(categoriaResidencialMock);
		duasCategorias.add(categoriaComercialMock);
		
		listConsumoTarifaCategoriaResidencial = new ArrayList<ConsumoTarifaCategoriaTO>();
		listConsumoTarifaCategoriaResidencial.add(consumoTarifaCategoriaResidencialMock);
		
		listConsumoTarifaCategoriaComercial = new ArrayList<ConsumoTarifaCategoriaTO>();
		listConsumoTarifaCategoriaComercial.add(consumoTarifaCategoriaComercialMock);
		
	}
	
	@Test
	public void addConsumoImovelTOUmaCategoriaUmaEconomia() {
		configurarImovelUmaCategoriaUmaEconomia();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, consumoTarifaVigenciaMock);
		
		List<ConsumoImovelCategoriaTO> list = bo.getConsumoImoveisCategoriaTO();
		
		assertEquals(1, list.size());
		assertEquals(new Integer(10), list.get(0).getConsumoEconomiaCategoria());
		assertEquals(new Integer(30), list.get(0).getConsumoExcedenteCategoria());
	}
	
	@Test
	public void addConsumoImovelTOUmaCategoriaTresEconomias() {
		configurarImovelUmaCategoriaTresEconomias();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, consumoTarifaVigenciaMock);
		
		List<ConsumoImovelCategoriaTO> list = bo.getConsumoImoveisCategoriaTO();
		
		assertEquals(1, list.size());
		assertEquals(new Integer(10), list.get(0).getConsumoEconomiaCategoria());
		assertEquals(new Integer(6), list.get(0).getConsumoExcedenteCategoria());
	}
	
	@Test
	public void distribuirConsumoPorCategoriaDuasCategoriasDuasEconomias() {
		configurarImovelDuasCategoriasDuasEconomias();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, consumoTarifaVigenciaMock);
		
		List<ConsumoImovelCategoriaTO> list = bo.getConsumoImoveisCategoriaTO();
		
		assertEquals(2, list.size());
		assertEquals(new Integer(10), list.get(0).getConsumoEconomiaCategoria());
		assertEquals(new Integer(10), list.get(0).getConsumoExcedenteCategoria());
		
		assertEquals(new Integer(10), list.get(1).getConsumoEconomiaCategoria());
		assertEquals(new Integer(10), list.get(1).getConsumoExcedenteCategoria());
	}
	
	@Test
	public void addConsumoImovelTODuasCategoriasSeteEconomias() {
		configurarImovelDuasCategoriasSeteEconomias();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, consumoTarifaVigenciaMock);
		
		List<ConsumoImovelCategoriaTO> list = bo.getConsumoImoveisCategoriaTO();
		
		assertEquals(2, list.size());
		assertEquals(new Integer(10), list.get(0).getConsumoEconomiaCategoria());
		assertEquals(new Integer(1), list.get(0).getConsumoExcedenteCategoria());
		
		assertEquals(new Integer(10), list.get(1).getConsumoEconomiaCategoria());
		assertEquals(new Integer(1), list.get(1).getConsumoExcedenteCategoria());
	}
	
	@Test
	public void addConsumoImovelTOUmaCategoriaUmaEconomiaConsumoMenorMinimo() {
		configurarImovelUmaCategoriaConsumoMenorMinimo();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, consumoTarifaVigenciaMock);
		List<ConsumoImovelCategoriaTO> list = bo.getConsumoImoveisCategoriaTO();
		
		assertEquals(1, list.size());
		assertEquals(new Integer(9), list.get(0).getConsumoEconomiaCategoria());
		assertEquals(new Integer(0), list.get(0).getConsumoExcedenteCategoria());
	}
	
	@Test
	public void distribuirConsumoFaixasUmaCategoriaUmaEconomia() {
		configurarImovelUmaCategoriaUmaEconomia();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, consumoTarifaVigenciaMock);
		
		List<ConsumoTarifaFaixaTO> faixas = mockarFaixasResidencial(3);
		when(consumoBOMock.obterFaixas(anyObject(), eq(medicaoHistoricoMock))).thenReturn(faixas);
		
		bo.distribuirConsumoPorFaixa(medicaoHistoricoMock);

		Map<ConsumoTarifaFaixaTO, Integer> faixasConsumo = bo.getConsumoImoveisCategoriaTO().get(0).getConsumoPorFaixa();
		
		assertEquals(3, faixasConsumo.values().size());
		assertEquals(new Integer(10), faixasConsumo.get(faixaResidencial11a20));
		assertEquals(new Integer(10), faixasConsumo.get(faixaResidencial21a30));
		assertEquals(new Integer(10), faixasConsumo.get(faixaResidencial31a40));
		
	}
	
	@Test
	public void distribuirConsumoFaixasUmaCategoriaTresEconomias() {
		configurarImovelUmaCategoriaTresEconomias();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, consumoTarifaVigenciaMock);
		
		List<ConsumoTarifaFaixaTO> faixas = mockarFaixasResidencial(1);
		when(consumoBOMock.obterFaixas(anyObject(), eq(medicaoHistoricoMock))).thenReturn(faixas);
		
		bo.distribuirConsumoPorFaixa(medicaoHistoricoMock);

		Map<ConsumoTarifaFaixaTO, Integer> faixasConsumo = bo.getConsumoImoveisCategoriaTO().get(0).getConsumoPorFaixa();
		
		assertEquals(1, faixasConsumo.values().size());
		assertEquals(new Integer(6), faixasConsumo.get(faixaResidencial11a20));
		
	}
	
	@Test
	public void distribuirConsumoFaixasDuasCategoriasDuasEconomias() {
		configurarImovelDuasCategoriasDuasEconomias();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, consumoTarifaVigenciaMock);
		
		List<ConsumoTarifaFaixaTO> faixasResidenciais = mockarFaixasResidencial(1);
		List<ConsumoTarifaFaixaTO> faixasComerciais = mockarFaixasComerciais();
		
		when(consumoBOMock.obterFaixas(bo.getConsumoImoveisCategoriaTO().get(0), medicaoHistoricoMock)).thenReturn(faixasResidenciais);
		when(consumoBOMock.obterFaixas(bo.getConsumoImoveisCategoriaTO().get(1), medicaoHistoricoMock)).thenReturn(faixasComerciais);
		
		bo.distribuirConsumoPorFaixa(medicaoHistoricoMock);

		Map<ConsumoTarifaFaixaTO, Integer> faixasConsumoResidencial = bo.getConsumoImoveisCategoriaTO().get(0).getConsumoPorFaixa();
		Map<ConsumoTarifaFaixaTO, Integer> faixasConsumoComercial = bo.getConsumoImoveisCategoriaTO().get(1).getConsumoPorFaixa();
		
		assertEquals(1, faixasConsumoResidencial.values().size());
		assertEquals(new Integer(10), faixasConsumoResidencial.get(faixaResidencial11a20));
		
		assertEquals(1, faixasConsumoComercial.values().size());
		assertEquals(new Integer(10), faixasConsumoComercial.get(faixaComercialMaior10));
		
	}
	
	@Test
	public void distribuirConsumoFaixasDuasCategoriasSeteEconomias() {
		configurarImovelDuasCategoriasSeteEconomias();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, consumoTarifaVigenciaMock);
		
		List<ConsumoTarifaFaixaTO> faixasResidenciais = mockarFaixasResidencial(1);
		List<ConsumoTarifaFaixaTO> faixasComerciais = mockarFaixasComerciais();
		
		when(consumoBOMock.obterFaixas(bo.getConsumoImoveisCategoriaTO().get(0), medicaoHistoricoMock)).thenReturn(faixasResidenciais);
		when(consumoBOMock.obterFaixas(bo.getConsumoImoveisCategoriaTO().get(1), medicaoHistoricoMock)).thenReturn(faixasComerciais);
		
		bo.distribuirConsumoPorFaixa(medicaoHistoricoMock);

		Map<ConsumoTarifaFaixaTO, Integer> faixasConsumoResidencial = bo.getConsumoImoveisCategoriaTO().get(0).getConsumoPorFaixa();
		Map<ConsumoTarifaFaixaTO, Integer> faixasConsumoComercial = bo.getConsumoImoveisCategoriaTO().get(1).getConsumoPorFaixa();
		
		assertEquals(1, faixasConsumoResidencial.values().size());
		assertEquals(new Integer(1), faixasConsumoResidencial.get(faixaResidencial11a20));
		
		assertEquals(1, faixasConsumoComercial.values().size());
		assertEquals(new Integer(1), faixasConsumoComercial.get(faixaComercialMaior10));
		
	}
	
	@Test
	public void calcularValorConsumoUmaCategoriaUmaEconomia() {
		configurarImovelUmaCategoriaUmaEconomia();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, consumoTarifaVigenciaMock);
		
		List<ConsumoTarifaFaixaTO> faixas = mockarFaixasResidencial(3);
		when(consumoBOMock.obterFaixas(anyObject(), eq(medicaoHistoricoMock))).thenReturn(faixas);
		
		bo.distribuirConsumoPorFaixa(medicaoHistoricoMock);
		
		assertEquals(new BigDecimal(109.20).setScale(2, BigDecimal.ROUND_HALF_UP), bo.getConsumoImoveisCategoriaTO().get(0).getValorConsumo());
	}
	
	@Test
	public void calcularValorConsumoUmaCategoriaTresEconomias() {
		configurarImovelUmaCategoriaTresEconomias();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, consumoTarifaVigenciaMock);
		
		List<ConsumoTarifaFaixaTO> faixas = mockarFaixasResidencial(1);
		when(consumoBOMock.obterFaixas(anyObject(), eq(medicaoHistoricoMock))).thenReturn(faixas);
		
		bo.distribuirConsumoPorFaixa(medicaoHistoricoMock);
		
		assertEquals(new BigDecimal(93.60).setScale(2, BigDecimal.ROUND_HALF_UP), bo.getConsumoImoveisCategoriaTO().get(0).getValorConsumo());
	}
	
	@Test
	public void calcularValorConsumoDuasCategoriaDuasEconomias() {
		configurarImovelDuasCategoriasDuasEconomias();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, consumoTarifaVigenciaMock);
		
		List<ConsumoTarifaFaixaTO> faixas = mockarFaixasResidencial(2);
		when(consumoBOMock.obterFaixas(anyObject(), eq(medicaoHistoricoMock))).thenReturn(faixas);
		
		bo.distribuirConsumoPorFaixa(medicaoHistoricoMock);
		
		assertEquals(new BigDecimal(40.80).setScale(2, BigDecimal.ROUND_HALF_UP), bo.getConsumoImoveisCategoriaTO().get(0).getValorConsumo());
		assertEquals(new BigDecimal(112.80).setScale(2, BigDecimal.ROUND_HALF_UP), bo.getConsumoImoveisCategoriaTO().get(1).getValorConsumo());
	}
	
	@Test
	public void calcularValorConsumoDuasCategoriaSeteEconomias() {
		configurarImovelDuasCategoriasSeteEconomias();
		
		bo.distribuirConsumoPorCategoria(consumoHistoricoMock, consumoTarifaVigenciaMock);
		
		List<ConsumoTarifaFaixaTO> faixas = mockarFaixasResidencial(2);
		when(consumoBOMock.obterFaixas(anyObject(), eq(medicaoHistoricoMock))).thenReturn(faixas);
		
		bo.distribuirConsumoPorFaixa(medicaoHistoricoMock);
		
		assertEquals(new BigDecimal(96.00).setScale(2, BigDecimal.ROUND_HALF_UP), bo.getConsumoImoveisCategoriaTO().get(0).getValorConsumo());
		assertEquals(new BigDecimal(112.92).setScale(2, BigDecimal.ROUND_HALF_UP), bo.getConsumoImoveisCategoriaTO().get(1).getValorConsumo());
	}
	
	private void configurarImovelUmaCategoriaConsumoMenorMinimo() {
		when(consumoBOMock.getQuantidadeTotalEconomias(imovelMock.getId())).thenReturn(1);
		when(consumoBOMock.consumoMinimoLigacao(imovelMock.getId())).thenReturn(10);
		when(consumoHistoricoMock.getNumeroConsumoFaturadoMes()).thenReturn(9);
		when(consumoBOMock.buscarQuantidadeEconomiasPorImovel(imovelMock.getId())).thenReturn(umaCategoria);
	}
	
	private void configurarImovelUmaCategoriaUmaEconomia() {
		when(consumoBOMock.getQuantidadeTotalEconomias(imovelMock.getId())).thenReturn(1);
		when(consumoBOMock.consumoMinimoLigacao(imovelMock.getId())).thenReturn(10);
		when(consumoHistoricoMock.getNumeroConsumoFaturadoMes()).thenReturn(40);
		when(consumoBOMock.buscarQuantidadeEconomiasPorImovel(imovelMock.getId())).thenReturn(umaCategoria);
		when(consumoBOMock.getQuantidadeEconomiasPorCategoria(categoriaResidencialMock)).thenReturn(1);
		when(consumoBOMock.getConsumoTarifasCategoria(consumoTarifaVigenciaMock, categoriaResidencialMock)).thenReturn(listConsumoTarifaCategoriaResidencial);
		when(consumoTarifaCategoriaResidencialMock.getValorConsumoMinimo()).thenReturn(new BigDecimal(16.8));
	}
	
	private void configurarImovelUmaCategoriaTresEconomias() {
		when(consumoBOMock.getQuantidadeTotalEconomias(imovelMock.getId())).thenReturn(3);
		when(consumoBOMock.consumoMinimoLigacao(imovelMock.getId())).thenReturn(30);
		when(consumoHistoricoMock.getNumeroConsumoFaturadoMes()).thenReturn(48);
		when(consumoBOMock.buscarQuantidadeEconomiasPorImovel(imovelMock.getId())).thenReturn(umaCategoria);
		when(consumoBOMock.getQuantidadeEconomiasPorCategoria(categoriaResidencialMock)).thenReturn(3);
		when(consumoBOMock.getConsumoTarifasCategoria(consumoTarifaVigenciaMock, categoriaResidencialMock)).thenReturn(listConsumoTarifaCategoriaResidencial);
		when(consumoTarifaCategoriaResidencialMock.getValorConsumoMinimo()).thenReturn(new BigDecimal(16.8));
	}
	
	private void configurarImovelDuasCategoriasDuasEconomias() {
		when(consumoBOMock.getQuantidadeTotalEconomias(imovelMock.getId())).thenReturn(2);
		when(consumoBOMock.consumoMinimoLigacao(imovelMock.getId())).thenReturn(20);
		when(consumoHistoricoMock.getNumeroConsumoFaturadoMes()).thenReturn(40);
		when(consumoBOMock.buscarQuantidadeEconomiasPorImovel(imovelMock.getId())).thenReturn(duasCategorias);
		when(consumoBOMock.getQuantidadeEconomiasPorCategoria(categoriaResidencialMock)).thenReturn(1);
		when(consumoBOMock.getQuantidadeEconomiasPorCategoria(categoriaComercialMock)).thenReturn(1);
		when(consumoBOMock.getConsumoTarifasCategoria(consumoTarifaVigenciaMock, categoriaResidencialMock)).thenReturn(listConsumoTarifaCategoriaResidencial);
		when(consumoBOMock.getConsumoTarifasCategoria(consumoTarifaVigenciaMock, categoriaComercialMock)).thenReturn(listConsumoTarifaCategoriaComercial);
		when(consumoTarifaCategoriaResidencialMock.getValorConsumoMinimo()).thenReturn(new BigDecimal(16.8));
		when(consumoTarifaCategoriaComercialMock.getValorConsumoMinimo()).thenReturn(new BigDecimal(50.2));
	}
	
	private void configurarImovelDuasCategoriasSeteEconomias() {
		when(consumoBOMock.getQuantidadeTotalEconomias(imovelMock.getId())).thenReturn(7);
		when(consumoBOMock.consumoMinimoLigacao(imovelMock.getId())).thenReturn(70);
		when(consumoHistoricoMock.getNumeroConsumoFaturadoMes()).thenReturn(77);
		when(consumoBOMock.buscarQuantidadeEconomiasPorImovel(imovelMock.getId())).thenReturn(duasCategorias);
		when(consumoBOMock.getQuantidadeEconomiasPorCategoria(categoriaResidencialMock)).thenReturn(5);
		when(consumoBOMock.getQuantidadeEconomiasPorCategoria(categoriaComercialMock)).thenReturn(2);
		when(consumoBOMock.getConsumoTarifasCategoria(consumoTarifaVigenciaMock, categoriaResidencialMock)).thenReturn(listConsumoTarifaCategoriaResidencial);
		when(consumoBOMock.getConsumoTarifasCategoria(consumoTarifaVigenciaMock, categoriaComercialMock)).thenReturn(listConsumoTarifaCategoriaComercial);
		when(consumoTarifaCategoriaResidencialMock.getValorConsumoMinimo()).thenReturn(new BigDecimal(16.8));
		when(consumoTarifaCategoriaComercialMock.getValorConsumoMinimo()).thenReturn(new BigDecimal(50.2));
	}
	
	private List<ConsumoTarifaFaixaTO> mockarFaixasResidencial(int qtdFaixas) {
		
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
		
		return faixasUtilizadas;
	}
	
	private List<ConsumoTarifaFaixaTO> mockarFaixasComerciais() {
		when(faixaComercialMaior10.getNumeroConsumoFaixaInicio()).thenReturn(21);
		when(faixaComercialMaior10.getNumeroConsumoFaixaFim()).thenReturn(30);
		when(faixaComercialMaior10.getConsumoTotalFaixa()).thenReturn(10);
		when(faixaComercialMaior10.getValorConsumoTarifa()).thenReturn(new BigDecimal(6.26));

		List<ConsumoTarifaFaixaTO> faixas = new ArrayList<ConsumoTarifaFaixaTO>();
		faixas.add(faixaComercialMaior10);
		
		return faixas;
	}
}
