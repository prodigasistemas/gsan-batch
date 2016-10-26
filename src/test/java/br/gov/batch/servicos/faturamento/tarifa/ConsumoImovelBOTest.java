package br.gov.batch.servicos.faturamento.tarifa;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import br.gov.batch.servicos.cadastro.EconomiasBO;
import br.gov.batch.servicos.micromedicao.ConsumoBO;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.ConsumoImovelTO;
import br.gov.model.faturamento.ConsumoTarifaVigencia;
import br.gov.model.micromedicao.ConsumoHistorico;

@RunWith(MockitoJUnitRunner.class)
public class ConsumoImovelBOTest {

	@Mock private ConsumoHistorico consumoHistoricoMock;
	@Mock private Imovel imovelMock;
	@Mock private EconomiasBO economiasBOMock;
	@Mock private ConsumoBO consumoBOMock;
	@Mock private ConsumoTarifaVigencia consumoTarifaVigenciaMock;
	@Mock private ICategoria categoriaResidencialMock;
	@Mock private ICategoria categoriaComercialMock;
	
	@InjectMocks private ConsumoImovelBO bo;
	
	@Before
	public void setup() {
		bo = new ConsumoImovelBO();
		
		MockitoAnnotations.initMocks(this);
		
		when(consumoBOMock.getConsumoMinimoTarifaPorCategoria(consumoTarifaVigenciaMock.getId(), categoriaResidencialMock)).thenReturn(10);
		when(consumoBOMock.getConsumoMinimoTarifaPorCategoria(consumoTarifaVigenciaMock.getId(), categoriaComercialMock)).thenReturn(10);
		when(consumoHistoricoMock.getImovel()).thenReturn(imovelMock);
	}
	
	@Test
	public void calculoExcessoImovelPositivo() {
		when(consumoBOMock.consumoMinimoLigacao(imovelMock.getId())).thenReturn(10);
		when(consumoHistoricoMock.getNumeroConsumoFaturadoMes()).thenReturn(24);
		int resultado = bo.calculoExcessoImovel(consumoHistoricoMock);
		
		assertEquals(14, resultado);
	}
	
	@Test
	public void addConsumoImovelTO() {
		when(economiasBOMock.getQuantidadeTotalEconomias(imovelMock.getId())).thenReturn(4);
		when(consumoBOMock.consumoMinimoLigacao(imovelMock.getId())).thenReturn(10);
		when(consumoHistoricoMock.getNumeroConsumoFaturadoMes()).thenReturn(24);

		
		bo.addConsumoImovelTO(consumoHistoricoMock, consumoTarifaVigenciaMock, categoriaResidencialMock);
		List<ConsumoImovelTO> list = bo.getConsumoImoveisTO();
		
		assertEquals(1, list.size());
		assertEquals(new Integer(10), list.get(0).getConsumoEconomiaCategoria());
		assertEquals(new Integer(3), list.get(0).getConsumoExcedenteCategoria());
	}
	
	@Test
	public void addConsumoImovelTOUmaCategoriaUmaEconomia() {
		when(economiasBOMock.getQuantidadeTotalEconomias(imovelMock.getId())).thenReturn(1);
		when(consumoBOMock.consumoMinimoLigacao(imovelMock.getId())).thenReturn(10);
		when(consumoHistoricoMock.getNumeroConsumoFaturadoMes()).thenReturn(40);
		
		bo.addConsumoImovelTO(consumoHistoricoMock, consumoTarifaVigenciaMock, categoriaResidencialMock);
		List<ConsumoImovelTO> list = bo.getConsumoImoveisTO();
		
		assertEquals(1, list.size());
		assertEquals(new Integer(10), list.get(0).getConsumoEconomiaCategoria());
		assertEquals(new Integer(30), list.get(0).getConsumoExcedenteCategoria());
	}
	
	@Test
	public void addConsumoImovelTOUmaCategoriaTresEconomias() {
		when(economiasBOMock.getQuantidadeTotalEconomias(imovelMock.getId())).thenReturn(3);
		when(consumoBOMock.consumoMinimoLigacao(imovelMock.getId())).thenReturn(30);
		when(consumoHistoricoMock.getNumeroConsumoFaturadoMes()).thenReturn(48);
		
		bo.addConsumoImovelTO(consumoHistoricoMock, consumoTarifaVigenciaMock, categoriaResidencialMock);
		List<ConsumoImovelTO> list = bo.getConsumoImoveisTO();
		
		assertEquals(1, list.size());
		assertEquals(new Integer(10), list.get(0).getConsumoEconomiaCategoria());
		assertEquals(new Integer(6), list.get(0).getConsumoExcedenteCategoria());
	}
	
	@Test
	public void addConsumoImovelTODuasCategoriasDuasEconomias() {
		when(economiasBOMock.getQuantidadeTotalEconomias(imovelMock.getId())).thenReturn(2);
		
		when(consumoBOMock.consumoMinimoLigacao(imovelMock.getId())).thenReturn(20);
		when(consumoHistoricoMock.getNumeroConsumoFaturadoMes()).thenReturn(40);
		
		bo.addConsumoImovelTO(consumoHistoricoMock, consumoTarifaVigenciaMock, categoriaResidencialMock);
		bo.addConsumoImovelTO(consumoHistoricoMock, consumoTarifaVigenciaMock, categoriaComercialMock);
		List<ConsumoImovelTO> list = bo.getConsumoImoveisTO();
		
		assertEquals(2, list.size());
		assertEquals(new Integer(10), list.get(0).getConsumoEconomiaCategoria());
		assertEquals(new Integer(10), list.get(0).getConsumoExcedenteCategoria());
		
		assertEquals(new Integer(10), list.get(1).getConsumoEconomiaCategoria());
		assertEquals(new Integer(10), list.get(1).getConsumoExcedenteCategoria());
	}
	
	@Test
	public void addConsumoImovelTODuasCategoriasSeteEconomias() {
		when(economiasBOMock.getQuantidadeTotalEconomias(imovelMock.getId())).thenReturn(7);
		
		when(consumoBOMock.consumoMinimoLigacao(imovelMock.getId())).thenReturn(70);
		when(consumoHistoricoMock.getNumeroConsumoFaturadoMes()).thenReturn(77);
		
		bo.addConsumoImovelTO(consumoHistoricoMock, consumoTarifaVigenciaMock, categoriaResidencialMock);
		bo.addConsumoImovelTO(consumoHistoricoMock, consumoTarifaVigenciaMock, categoriaComercialMock);
		List<ConsumoImovelTO> list = bo.getConsumoImoveisTO();
		
		assertEquals(2, list.size());
		assertEquals(new Integer(10), list.get(0).getConsumoEconomiaCategoria());
		assertEquals(new Integer(1), list.get(0).getConsumoExcedenteCategoria());
		
		assertEquals(new Integer(10), list.get(1).getConsumoEconomiaCategoria());
		assertEquals(new Integer(1), list.get(1).getConsumoExcedenteCategoria());
	}

}
