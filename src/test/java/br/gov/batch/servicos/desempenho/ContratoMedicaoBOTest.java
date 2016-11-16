package br.gov.batch.servicos.desempenho;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import br.gov.batch.servicos.faturamento.FaturamentoAtividadeCronogramaBO;
import br.gov.batch.servicos.faturamento.tarifa.ConsumoImovelCategoriaBO;
import br.gov.batch.servicos.micromedicao.ConsumoHistoricoBO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.desempenho.ContratoMedicao;
import br.gov.model.faturamento.ConsumoTarifaVigencia;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.servicos.desempenho.ContratoMedicaoRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaVigenciaRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.to.ConsumoImovelCategoriaTO;

@RunWith(MockitoJUnitRunner.class)
public class ContratoMedicaoBOTest {
	@Mock private Imovel imovelMock;
	@Mock private ContratoMedicao contratoMedicaoMock;
	
	@Mock private ConsumoHistoricoBO consumoHistoricoBOMock;
	@Mock private ConsumoHistorico consumoHistoricoMesZeroMock;
	@Mock private ConsumoHistorico consumoHistoricoAtualMock;
	
	@Mock private ConsumoTarifaVigencia consumoTarifaVigenciaMock;
	@Mock private ConsumoTarifaVigenciaRepositorio consumoTarifaVigenciaRepositorioMock;
	@Mock private ContratoMedicaoRepositorio contratoMedicacaoRepositorioMock;
	
	@Mock private MedicaoHistoricoRepositorio medicaoHistoricoRepositorioMock;
	
	@Mock private ConsumoImovelCategoriaBO consumoImovelCategoriaBOMock;
	@Mock private ConsumoImovelCategoriaTO consumoImovelCategoriaMesZeroMock;
	@Mock private ConsumoImovelCategoriaTO consumoImovelCategoriaAtualTOMock;
	
	@Mock private FaturamentoAtividadeCronogramaBO faturamentoAtividadeCronogramaBO;
	
	@InjectMocks
	private ContratoMedicaoBO bo;
	
	private Integer referenciaMesZero;
	private Date dataReferenciaMesZero;
	private Integer referencia;
	
	@Before
	public void setup() {		
		bo = new ContratoMedicaoBO();
		MockitoAnnotations.initMocks(this);
		
		this.referencia = 201607;
		this.referenciaMesZero = 201605;
		this.dataReferenciaMesZero = new DateTime(2016, 5, 1, 0, 0, 0).toDate();
		when(contratoMedicaoMock.getDataAssinatura()).thenReturn(dataReferenciaMesZero);

		when(consumoHistoricoBOMock.getConsumoHistoricoPorReferencia(imovelMock, referenciaMesZero)).thenReturn(consumoHistoricoMesZeroMock);
		
		when(consumoHistoricoAtualMock.getImovel()).thenReturn(imovelMock);
		when(consumoHistoricoMesZeroMock.getImovel()).thenReturn(imovelMock);
		
		when(consumoHistoricoAtualMock.getNumeroConsumoFaturadoMes()).thenReturn(10);
		when(consumoHistoricoMesZeroMock.getNumeroConsumoFaturadoMes()).thenReturn(0);
		
		when(faturamentoAtividadeCronogramaBO.obterDataLeituraAnterior(imovelMock, referencia)).thenReturn(new DateTime(2016,5,1,0,0).toDate());
		when(faturamentoAtividadeCronogramaBO.obterDataLeituraAtual(imovelMock, referencia)).thenReturn(new DateTime(2016,6,1,0,0).toDate());
		
		when(contratoMedicacaoRepositorioMock.buscarContratoAtivoPorImovel(imovelMock.getId())).thenReturn(contratoMedicaoMock);
	}
	
	@Test
	public void calculoQuantidadeDiasLeitura() {
		DateTime dataLeituraAnterior = new DateTime(2016, 9, 1, 0, 0, 0);
		DateTime dataLeituraAtual = new DateTime(2016, 10, 1, 0, 0, 0);
		
		assertEquals(30, bo.calcularQuantidadeDiasLeitura(dataLeituraAnterior, dataLeituraAtual));
	}
	
	@Test
	public void calculoTarifaProporcional() {
		when(consumoHistoricoBOMock.getConsumoHistoricoPorReferencia(imovelMock, referencia)).thenReturn(consumoHistoricoAtualMock);
		
		mockValorConsumoTotal(consumoHistoricoAtualMock, new BigDecimal(16.80));
		
		assertEquals(new BigDecimal(16.80).setScale(2, RoundingMode.HALF_DOWN), bo.calcularValorConsumo(imovelMock, 
																										referencia, 
																										referencia));
	}
	
	@Test
	public void calculoDiferencaConsumoAguaPositivo() {
		Integer referencia = 201607;
		
		when(consumoHistoricoBOMock.getConsumoHistoricoPorReferencia(imovelMock, referencia)).thenReturn(consumoHistoricoAtualMock);
		
		assertEquals(new Integer(10), bo.calcularDiferencaConsumoAgua(imovelMock, referenciaMesZero, referencia));
	}
	
	@Test
	public void calculoDiferencaValorAguaPositivo() {
		Integer referencia = 201607;
		
		when(consumoHistoricoBOMock.getConsumoHistoricoPorReferencia(imovelMock, referenciaMesZero)).thenReturn(consumoHistoricoMesZeroMock);
		when(consumoHistoricoBOMock.getConsumoHistoricoPorReferencia(imovelMock, referencia)).thenReturn(consumoHistoricoAtualMock);
		
		mockValorConsumoTotal(consumoHistoricoAtualMock, new BigDecimal(16.80));
		mockValorConsumoTotal(consumoHistoricoMesZeroMock, new BigDecimal(8.40));
		
		assertEquals(new BigDecimal(8.40).setScale(2, RoundingMode.HALF_DOWN), bo.calcularValorDiferencaAgua(imovelMock, referencia));
	}
	
	private void mockValorConsumoTotal(ConsumoHistorico consumoHistorico, BigDecimal valor) {
		when(consumoImovelCategoriaBOMock.getValorTotalConsumoImovel(eq(consumoHistorico), anyObject(), anyObject())).thenReturn(valor.setScale(2, RoundingMode.HALF_DOWN));
	}
}
