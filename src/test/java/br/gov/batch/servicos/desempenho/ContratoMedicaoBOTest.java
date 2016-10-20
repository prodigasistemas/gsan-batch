package br.gov.batch.servicos.desempenho;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import br.gov.batch.servicos.micromedicao.ConsumoHistoricoBO;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.desempenho.ContratoMedicao;
import br.gov.model.faturamento.ConsumoTarifa;
import br.gov.model.faturamento.ConsumoTarifaVigencia;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.servicos.desempenho.ContratoMedicaoRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaVigenciaRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;

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
	@Mock private ICategoria categoriaMock;
	@Mock private ContratoMedicao contratoMedicaoMock;
	
	@Mock private ConsumoHistoricoBO consumoHistoricoBOMock;
	@Mock private ConsumoHistorico consumoHistoricoMesZeroMock;
	@Mock private ConsumoHistorico consumoHistoricoAtualMock;
	
	@Mock private ConsumoTarifaVigencia consumoTarifaVigenciaMock;
	@Mock private ConsumoTarifaVigenciaRepositorio consumoTarifaVigenciaRepositorioMock;
	@Mock private ConsumoTarifa consumoTarifaMock;
	@Mock private ContratoMedicaoRepositorio contratoMedicacaoRepositorioMock;
	
	@Mock private MedicaoHistoricoRepositorio medicaoHistoricoRepositorioMock;
	@Mock private MedicaoHistorico medicaoHistoricoMesZeroMock;
	@Mock private MedicaoHistorico medicaoHistoricoAtualMock;
	
	@InjectMocks
	private ContratoMedicaoBO bo;
	
	private Integer referenciaMesZero;
	private Date dataReferenciaMesZero;
	
	@Before
	public void setup() {		
		bo = new ContratoMedicaoBO();
		MockitoAnnotations.initMocks(this);
		
		this.referenciaMesZero = 201605;
		this.dataReferenciaMesZero = new DateTime(2016, 5, 1, 0, 0, 0).toDate();
		
		when(contratoMedicaoMock.getDataAssinatura()).thenReturn(dataReferenciaMesZero);
		when(consumoHistoricoBOMock.getConsumoMes(imovelMock, referenciaMesZero, LigacaoTipo.AGUA)).thenReturn(0);
		
		when(consumoHistoricoAtualMock.getImovel()).thenReturn(imovelMock);
		when(consumoHistoricoMesZeroMock.getImovel()).thenReturn(imovelMock);
		
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
		List<ConsumoTarifaVigencia> tarifasVigentes = new ArrayList<ConsumoTarifaVigencia>();
		tarifasVigentes.add(consumoTarifaVigenciaMock);
		
		when(imovelMock.getConsumoTarifa()).thenReturn(consumoTarifaMock);
		when(consumoTarifaMock.getTarifaTipoCalculo()).thenReturn(1);
		
		DateTime dataLeituraAnterior = new DateTime(2016, 9, 1, 0, 0, 0);
		DateTime dataLeituraAtual = new DateTime(2016, 10, 1, 0, 0, 0);
		when(medicaoHistoricoRepositorioMock.buscarPorLigacaoAgua(imovelMock.getId(), referenciaMesZero)).thenReturn(medicaoHistoricoAtualMock);
		when(medicaoHistoricoAtualMock.getDataLeituraAnteriorFaturamento()).thenReturn(dataLeituraAnterior.toDate());
		when(medicaoHistoricoAtualMock.getDataLeituraAtualInformada()).thenReturn(dataLeituraAtual.toDate());
		
		assertEquals(new BigDecimal(16.80).setScale(2, RoundingMode.HALF_DOWN), bo.calcularValorConsumo(medicaoHistoricoAtualMock, consumoHistoricoAtualMock));
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
		
		when(consumoHistoricoBOMock.getConsumoMes(imovelMock, referencia, LigacaoTipo.AGUA)).thenReturn(10);
		when(consumoHistoricoBOMock.getConsumoHistoricoPorReferencia(imovelMock, referenciaMesZero)).thenReturn(consumoHistoricoMesZeroMock);
		when(consumoHistoricoBOMock.getConsumoHistoricoPorReferencia(imovelMock, referencia)).thenReturn(consumoHistoricoAtualMock);
		
		DateTime dataLeituraAnterior = new DateTime(2016, 4, 1, 0, 0, 0);
		DateTime dataLeituraAtual = new DateTime(2016, 5, 1, 0, 0, 0);
		when(medicaoHistoricoRepositorioMock.buscarPorLigacaoAgua(imovelMock.getId(), referenciaMesZero)).thenReturn(medicaoHistoricoMesZeroMock);
		when(medicaoHistoricoMesZeroMock.getDataLeituraAnteriorFaturamento()).thenReturn(dataLeituraAnterior.toDate());
		when(medicaoHistoricoMesZeroMock.getDataLeituraAtualInformada()).thenReturn(dataLeituraAtual.toDate());
		
		dataLeituraAnterior = new DateTime(2016, 6, 1, 0, 0, 0);
		dataLeituraAtual = new DateTime(2016, 7, 1, 0, 0, 0);
		when(medicaoHistoricoRepositorioMock.buscarPorLigacaoAgua(imovelMock.getId(), referencia)).thenReturn(medicaoHistoricoAtualMock);
		when(medicaoHistoricoAtualMock.getDataLeituraAnteriorFaturamento()).thenReturn(dataLeituraAnterior.toDate());
		when(medicaoHistoricoAtualMock.getDataLeituraAtualInformada()).thenReturn(dataLeituraAtual.toDate());
		
		List<ConsumoTarifaVigencia> tarifasVigentes = new ArrayList<ConsumoTarifaVigencia>();
		tarifasVigentes.add(consumoTarifaVigenciaMock);
		when(consumoTarifaVigenciaRepositorioMock.buscarTarifasPorPeriodo(medicaoHistoricoAtualMock.getDataLeituraAnteriorFaturamento(), 
																			medicaoHistoricoAtualMock.getDataLeituraAtualInformada())).thenReturn(tarifasVigentes);
		
		assertEquals(new BigDecimal(8.40).setScale(2, RoundingMode.HALF_DOWN), bo.calcularValorDiferencaAgua(imovelMock, referencia));
	}
}
