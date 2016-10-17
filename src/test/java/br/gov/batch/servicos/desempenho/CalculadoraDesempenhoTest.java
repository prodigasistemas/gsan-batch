package br.gov.batch.servicos.desempenho;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import br.gov.batch.servicos.micromedicao.ConsumoHistoricoBO;
import br.gov.batch.util.Util;
import br.gov.model.cadastro.Imovel;
import br.gov.model.desempenho.ContratoMedicao;
import br.gov.model.micromedicao.LigacaoTipo;

@RunWith(MockitoJUnitRunner.class)
public class CalculadoraDesempenhoTest {

	/*
	 * 1 - Matricula do Imovel
	 * 2 - Referência do calculo
	 * 3 - Diferença do consumo de água
	 * 4 - Valor da diferença do consumo de água
	 * 5 - Percentual de esgoto
	 * 6 - Valor da diferença do consumo de esgoto
	 * 7 - id do contrato medicao
	 * 8 - DebitoCreditoSituacao (Normal, Cancelado, Retificado)
	 */
	
	@Mock private Imovel imovelMock;	
	@Mock private ContratoMedicao contratoMedicaoMock;
	@Mock private ConsumoHistoricoBO consumoHistoricoBOMock;
	
	@Test
	public void calculoDaDiferencaConsumoAguaPositivo() {
		Integer referencia = 201607;
		
		when(contratoMedicaoMock.getDataAssinatura()).thenReturn(Util.getData("01", "05", "2016"));
		when(consumoHistoricoBOMock.getConsumoMes(imovelMock, 201605, LigacaoTipo.AGUA)).thenReturn(0);
		when(consumoHistoricoBOMock.getConsumoMes(imovelMock, referencia, LigacaoTipo.AGUA)).thenReturn(10);
		
		CalculadoraDesempenho calculadora = new CalculadoraDesempenho(imovelMock, contratoMedicaoMock, consumoHistoricoBOMock);
		
		assertEquals(new Integer(10), calculadora.calcularDiferencaConsumoAgua(referencia));
	}
	
	@Test
	public void calculoDaDiferencaValorAguaPositivo() {
		Integer referencia = 201607;
		
		when(contratoMedicaoMock.getDataAssinatura()).thenReturn(Util.getData("01", "05", "2016"));
		when(consumoHistoricoBOMock.getConsumoMes(imovelMock, 201605, LigacaoTipo.AGUA)).thenReturn(0);
		when(consumoHistoricoBOMock.getConsumoMes(imovelMock, referencia, LigacaoTipo.AGUA)).thenReturn(10);
		
		CalculadoraDesempenho calculadora = new CalculadoraDesempenho(imovelMock, contratoMedicaoMock, consumoHistoricoBOMock);
		
		assertEquals(new BigDecimal(8.40), calculadora.calcularValorDiferencaAgua(referencia));
	}
}
