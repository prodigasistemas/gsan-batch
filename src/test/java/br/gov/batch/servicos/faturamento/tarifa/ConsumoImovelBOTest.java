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

	@Mock private ConsumoHistorico consumoHistorico;
	@Mock private Imovel imovel;
	@Mock private EconomiasBO economiasBO;
	@Mock private ConsumoBO consumoBO;
	@Mock private ConsumoTarifaVigencia consumoTarifaVigencia;
	@Mock private ICategoria categoria;
	
	@InjectMocks private ConsumoImovelBO bo;
	
	@Before
	public void setup() {
		bo = new ConsumoImovelBO();
		
		MockitoAnnotations.initMocks(this);
		
		when(consumoHistorico.getImovel()).thenReturn(imovel);
		when(consumoHistorico.getNumeroConsumoFaturadoMes()).thenReturn(24);
		when(consumoBO.consumoMinimoLigacao(imovel.getId())).thenReturn(10);
	}
	
	@Test
	public void calculoExcessoImovelPositivo() {
		int resultado = bo.calculoExcessoImovel(consumoHistorico);
		
		assertEquals(14, resultado);
	}
	
	@Test
	public void addConsumoImovelTO() {
		when(economiasBO.getQuantidadeTotalEconomias(imovel.getId())).thenReturn(4);
		when(consumoBO.getConsumoMinimoTarifaPorCategoria(consumoTarifaVigencia.getId(), categoria)).thenReturn(10);
		
		bo.addConsumoImovelTO(consumoHistorico, consumoTarifaVigencia, categoria);
		List<ConsumoImovelTO> list = bo.getConsumoImoveisTO();
		
		assertEquals(1, list.size());
		assertEquals(new Integer(10), list.get(0).getConsumoEconomiaCategoria());
		assertEquals(new Integer(3), list.get(0).getConsumoExcedenteCategoria());
	}

}
