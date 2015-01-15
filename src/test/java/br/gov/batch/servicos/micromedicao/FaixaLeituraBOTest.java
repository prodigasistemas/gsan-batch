package br.gov.batch.servicos.micromedicao;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.Quadra;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.FaixaLeituraEsperadaParametros;
import br.gov.model.micromedicao.Hidrometro;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.model.micromedicao.Rota;
import br.gov.model.micromedicao.StatusFaixaFalsa;
import br.gov.model.micromedicao.StatusUsoFaixaFalsa;
import br.gov.servicos.faturamento.FaturamentoRepositorio;

@RunWith(EasyMockRunner.class)
public class FaixaLeituraBOTest {

	@TestSubject
	private FaixaLeituraBO faixaLeituraBO;
	
	@Mock
	private FaturamentoRepositorio faturamentoRepositorioMock;
	
	@Mock
	private SistemaParametros sistemaParametrosMock;

	private int leituraAnterior = 200;
	private MedicaoHistorico medicaoHistorico;
	private Hidrometro hidrometro;
	private List<FaixaLeituraEsperadaParametros> faixaLeituraParametros;
	
	@Before
	public void setUp() {
		faixaLeituraBO = new FaixaLeituraBO();
		
		faixaLeituraParametros = new ArrayList<FaixaLeituraEsperadaParametros>();
		
		faixaLeituraParametros.add(buildFaixa10());
		faixaLeituraParametros.add(buildFaixa20());
		faixaLeituraParametros.add(buildFaixa45());
		faixaLeituraParametros.add(buildFaixa100());
		faixaLeituraParametros.add(buildFaixa115());
		
		hidrometro = new Hidrometro();
	}
	@Test
	public void calcularFaixaLeituraEsperadaMedia10(){
		carregarMocks();
		
		assertEquals(new Integer(200), faixaLeituraBO.calcularFaixaLeituraEsperada(10, null, null, leituraAnterior).getFaixaInferior());
		assertEquals(new Integer(220), faixaLeituraBO.calcularFaixaLeituraEsperada(10, null, null, leituraAnterior).getFaixaSuperior());
	}
	
	@Test
	public void calcularFaixaLeituraEsperadaMedia20(){
		carregarMocks();
		
		assertEquals(new Integer(208), faixaLeituraBO.calcularFaixaLeituraEsperada(20, null, null, leituraAnterior).getFaixaInferior());
		assertEquals(new Integer(232), faixaLeituraBO.calcularFaixaLeituraEsperada(20, null, null, leituraAnterior).getFaixaSuperior());
	}
	
	@Test
	public void calcularFaixaLeituraEsperadaMedia45(){
		carregarMocks();
		
		assertEquals(new Integer(223), faixaLeituraBO.calcularFaixaLeituraEsperada(45, null, null, leituraAnterior).getFaixaInferior());
		assertEquals(new Integer(268), faixaLeituraBO.calcularFaixaLeituraEsperada(45, null, null, leituraAnterior).getFaixaSuperior());
	}
	
	@Test
	public void calcularFaixaLeituraEsperadaMedia100(){
		carregarMocks();
		
		assertEquals(new Integer(260), faixaLeituraBO.calcularFaixaLeituraEsperada(100, null, null, leituraAnterior).getFaixaInferior());
		assertEquals(new Integer(340), faixaLeituraBO.calcularFaixaLeituraEsperada(100, null, null, leituraAnterior).getFaixaSuperior());
	}
	
	@Test
	public void calcularFaixaLeituraEsperadaMedia115(){
		carregarMocks();
		
		assertEquals(new Integer(280), faixaLeituraBO.calcularFaixaLeituraEsperada(115, null, null, leituraAnterior).getFaixaInferior());
		assertEquals(new Integer(350), faixaLeituraBO.calcularFaixaLeituraEsperada(115, null, null, leituraAnterior).getFaixaSuperior());
	}
	
	@Test
	public void obterDadosFaixaLeituraHidrometroNulo() {
		assertEquals(new Integer(0), faixaLeituraBO.obterDadosFaixaLeitura(null, null, null, null).getFaixaInferior());
		assertEquals(new Integer(0), faixaLeituraBO.obterDadosFaixaLeitura(null, null, null, null).getFaixaSuperior());
	}

	@Test
	public void calcularFaixaLeituraEsperada() {
		carregarMocks();
		
		int media = 45;
		assertEquals(new Integer(7125), faixaLeituraBO.calcularFaixaLeituraEsperada(media, medicaoHistorico, hidrometro, 7102).getFaixaInferior());
		assertEquals(new Integer(7170), faixaLeituraBO.calcularFaixaLeituraEsperada(media, medicaoHistorico, hidrometro, 7102).getFaixaSuperior());
	}
	
	@Test
	public void obterDadosFaixaLeituraNormal() {
		carregarMocks();
		carregarSistemaParametrosMocks();
		
		Hidrometro hidrometro = new Hidrometro();
		Imovel imovel = new Imovel(1);
		
		medicaoHistorico = new MedicaoHistorico();
		medicaoHistorico.setLeituraAnteriorFaturamento(7102);
		
		int media = 45;
		assertNotEquals(new Integer(0), faixaLeituraBO.obterDadosFaixaLeitura(imovel, hidrometro, media, medicaoHistorico).getFaixaInferior());
		assertNotEquals(new Integer(0), faixaLeituraBO.obterDadosFaixaLeitura(imovel, hidrometro, media, medicaoHistorico).getFaixaSuperior());
	}
	
	@Test
	public void obterDadosFaixaLeituraFaixaFalsaComHidrometroSelecionado() {
		carregarMocks();
		carregarSistemaParametrosFaixaFalsaMocks();
		
		Hidrometro hidrometro = new Hidrometro();
		
		Rota rota = new Rota();
		Quadra quadra = new Quadra();
		quadra.setRota(rota);
		
		Imovel imovel = new Imovel(1);
		imovel.setQuadra(quadra);
		
		medicaoHistorico = new MedicaoHistorico();
		medicaoHistorico.setLeituraAnteriorFaturamento(7102);
		
		int media = 45;
		assertNotEquals(new Integer(0), faixaLeituraBO.obterDadosFaixaLeitura(imovel, hidrometro, media, medicaoHistorico).getFaixaInferior());
		assertNotEquals(new Integer(0), faixaLeituraBO.obterDadosFaixaLeitura(imovel, hidrometro, media, medicaoHistorico).getFaixaSuperior());
	}
	
	
	private FaixaLeituraEsperadaParametros buildFaixa10() {
		FaixaLeituraEsperadaParametros faixa = new FaixaLeituraEsperadaParametros();
		
		faixa.setMediaInicial(0);
		faixa.setMediaFinal(10);
		faixa.setFatorFaixaInicial(new BigDecimal(0));
		faixa.setFatorFaixaFinal(new BigDecimal(1));
		
		return faixa;
	}
	
	private FaixaLeituraEsperadaParametros buildFaixa20() {
		FaixaLeituraEsperadaParametros faixa = new FaixaLeituraEsperadaParametros();
		
		faixa.setMediaInicial(11);
		faixa.setMediaFinal(20);
		faixa.setFatorFaixaInicial(new BigDecimal(0.4));
		faixa.setFatorFaixaFinal(new BigDecimal(1.6));
		
		return faixa;
	}
	
	private FaixaLeituraEsperadaParametros buildFaixa45() {
		FaixaLeituraEsperadaParametros faixa = new FaixaLeituraEsperadaParametros();
		
		faixa.setMediaInicial(21);
		faixa.setMediaFinal(45);
		faixa.setFatorFaixaInicial(new BigDecimal(0.5));
		faixa.setFatorFaixaFinal(new BigDecimal(1.5));
		
		return faixa;
	}
	
	private FaixaLeituraEsperadaParametros buildFaixa100() {
		FaixaLeituraEsperadaParametros faixa = new FaixaLeituraEsperadaParametros();
		
		faixa.setMediaInicial(46);
		faixa.setMediaFinal(100);
		faixa.setFatorFaixaInicial(new BigDecimal(0.6));
		faixa.setFatorFaixaFinal(new BigDecimal(1.4));
		
		return faixa;
	}
	
	private FaixaLeituraEsperadaParametros buildFaixa115() {
		FaixaLeituraEsperadaParametros faixa = new FaixaLeituraEsperadaParametros();
		
		faixa.setMediaInicial(101);
		faixa.setMediaFinal(100000);
		faixa.setFatorFaixaInicial(new BigDecimal(0.7));
		faixa.setFatorFaixaFinal(new BigDecimal(1.3));
		
		return faixa;
	}
	
	private void carregarMocks() {
		expect(faturamentoRepositorioMock.obterFaixasLeitura()).andReturn(faixaLeituraParametros).times(2);
		replay(faturamentoRepositorioMock);
	}
	
	private void carregarSistemaParametrosMocks() {
		expect(sistemaParametrosMock.getIndicadorFaixaFalsa()).andReturn(StatusFaixaFalsa.GERAR_FAIXA_FALSA_DESATIVO.getId()).times(6);
		replay(sistemaParametrosMock);
	}
	
	private void carregarSistemaParametrosFaixaFalsaMocks() {
		expect(sistemaParametrosMock.getIndicadorFaixaFalsa()).andReturn(StatusFaixaFalsa.GERAR_FAIXA_FALSA_ROTA.getId()).times(10);
		expect(sistemaParametrosMock.getIndicadorUsoFaixaFalsa()).andReturn(StatusUsoFaixaFalsa.ROTA.getId()).times(10);
		expect(sistemaParametrosMock.getPercentualFaixaFalsa()).andReturn(new BigDecimal(2.50)).times(10);
		replay(sistemaParametrosMock);
	}
}
