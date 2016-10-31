package br.gov.batch.servicos.desempenho;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
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

import br.gov.batch.servicos.faturamento.tarifa.ConsumoImovelCategoriaBO;
import br.gov.batch.servicos.micromedicao.ConsumoBO;
import br.gov.batch.servicos.micromedicao.ConsumoHistoricoBO;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.desempenho.ContratoMedicao;
import br.gov.model.faturamento.ConsumoImovelCategoriaTO;
import br.gov.model.faturamento.ConsumoTarifa;
import br.gov.model.faturamento.ConsumoTarifaVigencia;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.servicos.desempenho.ContratoMedicaoRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaVigenciaRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.to.ConsumoTarifaCategoriaTO;
import br.gov.servicos.to.ConsumoTarifaFaixaTO;

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
	
	@Mock private ConsumoBO consumoBOMock;
	
	@Mock private ConsumoTarifaVigencia consumoTarifaVigenciaMock;
	@Mock private ConsumoTarifaVigenciaRepositorio consumoTarifaVigenciaRepositorioMock;
	@Mock private ConsumoTarifa consumoTarifaMock;
	@Mock private ContratoMedicaoRepositorio contratoMedicacaoRepositorioMock;
	
	@Mock private MedicaoHistoricoRepositorio medicaoHistoricoRepositorioMock;
	@Mock private MedicaoHistorico medicaoHistoricoMesZeroMock;
	@Mock private MedicaoHistorico medicaoHistoricoAtualMock;
	
	@Mock private ConsumoImovelCategoriaBO consumoImovelCategoriaBO;
	
	@Mock private ConsumoTarifaCategoriaTO consumoTarifaCategoriaResidencialMock;
	@Mock private ConsumoTarifaFaixaTO faixaResidencial11a20;
	@Mock private ConsumoTarifaFaixaTO faixaResidencial21a30;
	@Mock private ConsumoTarifaFaixaTO faixaResidencial31a40;
	@Mock private ConsumoTarifaFaixaTO faixaComercialMaior10;
	
	@Mock private ConsumoImovelCategoriaTO consumoImovelCategoriaTO;
	
	@Mock private ConsumoTarifaVigencia consumotarifaVigenciaMock;

	@InjectMocks
	private ContratoMedicaoBO bo;
	
	private Integer referenciaMesZero;
	private Date dataReferenciaMesZero;
	
	private List<ConsumoTarifaVigencia> tarifasVigentes;
	private Collection<ICategoria> umaCategoria;
	private List<ConsumoTarifaCategoriaTO> listConsumoTarifaCategoriaResidencial;
	@Mock private ICategoria categoriaResidencialMock;
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
		
		tarifasVigentes = new ArrayList<ConsumoTarifaVigencia>();
		ConsumoTarifaVigencia vigencia = new ConsumoTarifaVigencia();
		
		tarifasVigentes.add(vigencia);
		
		umaCategoria = new ArrayList<ICategoria>();
		umaCategoria.add(categoriaResidencialMock);
		
		listConsumoTarifaCategoriaResidencial = new ArrayList<ConsumoTarifaCategoriaTO>();
		listConsumoTarifaCategoriaResidencial.add(consumoTarifaCategoriaResidencialMock);
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

		configurarImovelMesZero();
		configurarImovelUmaCategoriaUmaEconomia();
		
		when(consumoImovelCategoriaBO.distribuirConsumoPorCategoria(consumoHistoricoMesZeroMock, medicaoHistoricoMesZeroMock)).then(
				consumoImovelCategoriaBO.getConsumoImoveisCategoriaTO().add(consumoImovelCategoriaTO));
		//consumoImovelCategoriaBO.distribuirConsumoPorCategoria(consumoHistoricoAtualMock, medicaoHistoricoAtualMock);
		
//		List<ConsumoTarifaFaixaTO> faixas = mockarFaixasResidencial(3);
//		when(consumoBOMock.obterFaixas(anyObject(), eq(medicaoHistoricoMesZeroMock))).thenReturn(faixas);
//		when(consumoBOMock.obterFaixas(anyObject(), eq(medicaoHistoricoAtualMock))).thenReturn(faixas);
		
		consumoImovelCategoriaBO.distribuirConsumoPorFaixa(medicaoHistoricoMesZeroMock);
		consumoImovelCategoriaBO.distribuirConsumoPorFaixa(medicaoHistoricoAtualMock);
		medicaoHistoricoAtualMock.setDataLeituraAnteriorFaturamento(dataLeituraAnterior.toDate());
		medicaoHistoricoAtualMock.setDataLeituraAtualFaturamento(dataLeituraAtual.toDate());
		
		when(consumoTarifaVigenciaRepositorioMock.buscarTarifasPorPeriodo(anyObject(),anyObject())).thenReturn(tarifasVigentes);
		
		
		when(bo.calcularValorConsumo(medicaoHistoricoMesZeroMock, consumoHistoricoMesZeroMock)).thenReturn(new BigDecimal(0.00));
		when(bo.calcularValorConsumo(medicaoHistoricoAtualMock, consumoHistoricoAtualMock)).thenReturn(new BigDecimal(16.80));
		
		
		assertEquals(new BigDecimal(8.40).setScale(2, RoundingMode.HALF_DOWN), bo.calcularValorDiferencaAgua(imovelMock, referencia));
	}
	
	private List<ConsumoImovelCategoriaTO> mockarConsumosMesZero() {
		List<ConsumoImovelCategoriaTO> consumos = new ArrayList<ConsumoImovelCategoriaTO>();
		
		ConsumoImovelCategoriaTO toMesZero = new ConsumoImovelCategoriaTO();
		toMesZero.setConsumoEconomiaCategoria(0);
		toMesZero.setConsumoExcedenteCategoria(0);
		return consumos;
	}
	
	private void configurarImovelUmaCategoriaUmaEconomia() {
		when(consumoBOMock.getQuantidadeTotalEconomias(imovelMock.getId())).thenReturn(1);
		when(consumoBOMock.consumoMinimoLigacao(imovelMock.getId())).thenReturn(10);
		when(consumoHistoricoAtualMock.getNumeroConsumoFaturadoMes()).thenReturn(0);
		when(consumoBOMock.buscarQuantidadeEconomiasPorImovel(imovelMock.getId())).thenReturn(umaCategoria);
		when(consumoBOMock.getQuantidadeEconomiasPorCategoria(categoriaResidencialMock)).thenReturn(1);
		when(consumoBOMock.getConsumoTarifasCategoria(imovelMock, medicaoHistoricoAtualMock, categoriaResidencialMock)).thenReturn(listConsumoTarifaCategoriaResidencial);
		when(consumoTarifaCategoriaResidencialMock.getConsumotarifaVigencia()).thenReturn(consumotarifaVigenciaMock);
		
		when(consumoTarifaCategoriaResidencialMock.getValorConsumoMinimo()).thenReturn(new BigDecimal(16.8));
	}
	
	private void configurarImovelMesZero() {
		when(consumoBOMock.getQuantidadeTotalEconomias(imovelMock.getId())).thenReturn(1);
		when(consumoBOMock.consumoMinimoLigacao(imovelMock.getId())).thenReturn(10);
		when(consumoHistoricoMesZeroMock.getNumeroConsumoFaturadoMes()).thenReturn(0);
		when(consumoBOMock.buscarQuantidadeEconomiasPorImovel(imovelMock.getId())).thenReturn(umaCategoria);
		when(consumoBOMock.getQuantidadeEconomiasPorCategoria(categoriaResidencialMock)).thenReturn(1);
		when(consumoBOMock.getConsumoTarifasCategoria(imovelMock, medicaoHistoricoMesZeroMock, categoriaResidencialMock))
		.thenReturn(listConsumoTarifaCategoriaResidencial);
		when(consumoTarifaCategoriaResidencialMock.getConsumotarifaVigencia()).thenReturn(consumotarifaVigenciaMock);
		
		when(consumoTarifaCategoriaResidencialMock.getValorConsumoMinimo()).thenReturn(new BigDecimal(16.8));
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
}
