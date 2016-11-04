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

import br.gov.batch.servicos.faturamento.tarifa.ConsumoImovelCategoriaBO;
import br.gov.batch.servicos.micromedicao.ConsumoHistoricoBO;
import br.gov.batch.servicos.micromedicao.MedicaoHistoricoBO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.desempenho.ContratoMedicao;
import br.gov.model.faturamento.ConsumoTarifaVigencia;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.servicos.desempenho.ContratoMedicaoRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaVigenciaRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.to.ConsumoImovelCategoriaTO;

@RunWith(MockitoJUnitRunner.class)
public class ContratoMedicaoBOTest {

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
	@Mock private ConsumoHistorico consumoHistoricoMesZeroMock;
	@Mock private ConsumoHistorico consumoHistoricoAtualMock;
	
	@Mock private ConsumoTarifaVigencia consumoTarifaVigenciaMock;
	@Mock private ConsumoTarifaVigenciaRepositorio consumoTarifaVigenciaRepositorioMock;
	@Mock private ContratoMedicaoRepositorio contratoMedicacaoRepositorioMock;
	
	@Mock private MedicaoHistoricoRepositorio medicaoHistoricoRepositorioMock;
	@Mock private MedicaoHistorico medicaoHistoricoMesZeroMock;
	@Mock private MedicaoHistorico medicaoHistoricoAtualMock;
	
	@Mock private ConsumoImovelCategoriaBO consumoImovelCategoriaBOMock;
	@Mock private ConsumoImovelCategoriaTO consumoImovelCategoriaMesZeroMock;
	@Mock private ConsumoImovelCategoriaTO consumoImovelCategoriaAtualTOMock;
	
	@Mock private MedicaoHistoricoBO medicaoHistoricoBOMock;
	
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

		when(consumoHistoricoBOMock.getConsumoMes(imovelMock, referenciaMesZero, LigacaoTipo.AGUA)).thenReturn(0);
		
		when(consumoHistoricoAtualMock.getImovel()).thenReturn(imovelMock);
		when(consumoHistoricoMesZeroMock.getImovel()).thenReturn(imovelMock);
		
		when(medicaoHistoricoBOMock.getMedicaoHistorico(imovelMock.getId(), referencia)).thenReturn(medicaoHistoricoAtualMock);
		
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
		
		when(medicaoHistoricoRepositorioMock.buscarPorLigacaoAgua(eq(imovelMock.getId()), anyObject())).thenReturn(medicaoHistoricoAtualMock);
		
		mockValorConsumoTotal(consumoHistoricoAtualMock, medicaoHistoricoAtualMock, new BigDecimal(16.80));
		
		assertEquals(new BigDecimal(16.80).setScale(2, RoundingMode.HALF_DOWN), bo.calcularValorConsumo(consumoHistoricoAtualMock, medicaoHistoricoAtualMock));
	}
	
	@Test
	public void calculoDiferencaConsumoAguaPositivo() {
		Integer referencia = 201607;
		
		when(consumoHistoricoBOMock.getConsumoMes(imovelMock, referencia, LigacaoTipo.AGUA)).thenReturn(10);
		
		assertEquals(new Integer(10), bo.calcularDiferencaConsumoAgua(imovelMock, referencia));
	}
	
	@Test
	public void calculoDiferencaValorAguaPositivo() {
		Integer referencia = 201607;
		
		when(consumoHistoricoBOMock.getConsumoHistoricoPorReferencia(imovelMock, referenciaMesZero)).thenReturn(consumoHistoricoMesZeroMock);
		when(consumoHistoricoBOMock.getConsumoHistoricoPorReferencia(imovelMock, referencia)).thenReturn(consumoHistoricoAtualMock);
		
		when(medicaoHistoricoRepositorioMock.buscarPorLigacaoAgua(eq(imovelMock.getId()), anyObject())).thenReturn(medicaoHistoricoMesZeroMock, 
																													medicaoHistoricoAtualMock);
		
		mockValorConsumoTotal(consumoHistoricoMesZeroMock, medicaoHistoricoAtualMock, new BigDecimal(8.40));
		mockValorConsumoTotal(consumoHistoricoAtualMock, medicaoHistoricoAtualMock, new BigDecimal(16.80));
		
		assertEquals(new BigDecimal(8.40).setScale(2, RoundingMode.HALF_DOWN), bo.calcularValorDiferencaAgua(imovelMock, referencia));
	}
	
	private void mockValorConsumoTotal(ConsumoHistorico consumoHistorico, MedicaoHistorico medicaoHistoricoMock, BigDecimal valor) {
		when(consumoImovelCategoriaBOMock.getValorTotalConsumoImovel(consumoHistorico, medicaoHistoricoMock)).thenReturn(valor.setScale(2, RoundingMode.HALF_DOWN));
	}
}
