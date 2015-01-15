package br.gov.batch.servicos.micromedicao;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.micromedicao.FaixaLeituraEsperadaParametros;
import br.gov.servicos.faturamento.FaturamentoRepositorio;

@RunWith(EasyMockRunner.class)
public class FaixaLeituraBOTest {

	@TestSubject
	private FaixaLeituraBO faixaLeituraBO;
	
	@Mock
	private FaturamentoRepositorio faturamentoRepositorioMock;
	
	private int leituraAnterior = 200;
	
	List<FaixaLeituraEsperadaParametros> faixaLeituraParametros;
	
	@Before
	public void setUp() {
		faixaLeituraBO = new FaixaLeituraBO();
		
		faixaLeituraParametros = new ArrayList<FaixaLeituraEsperadaParametros>();
		
		faixaLeituraParametros.add(buildFaixa10());
		faixaLeituraParametros.add(buildFaixa20());
		faixaLeituraParametros.add(buildFaixa45());
		faixaLeituraParametros.add(buildFaixa100());
		faixaLeituraParametros.add(buildFaixa115());
	}
	@Test
	public void media10(){
		carregarMocks();
		
		assertEquals(new Integer(200), faixaLeituraBO.calcularFaixaLeituraEsperada(10, null, null, leituraAnterior).getFaixaInferior());
		assertEquals(new Integer(220), faixaLeituraBO.calcularFaixaLeituraEsperada(10, null, null, leituraAnterior).getFaixaSuperior());
	}
	
	@Test
	public void media20(){
		carregarMocks();
		
		assertEquals(new Integer(208), faixaLeituraBO.calcularFaixaLeituraEsperada(20, null, null, leituraAnterior).getFaixaInferior());
		assertEquals(new Integer(232), faixaLeituraBO.calcularFaixaLeituraEsperada(20, null, null, leituraAnterior).getFaixaSuperior());
	}
	
	@Test
	public void media45(){
		carregarMocks();
		
		assertEquals(new Integer(223), faixaLeituraBO.calcularFaixaLeituraEsperada(45, null, null, leituraAnterior).getFaixaInferior());
		assertEquals(new Integer(268), faixaLeituraBO.calcularFaixaLeituraEsperada(45, null, null, leituraAnterior).getFaixaSuperior());
	}
	
	@Test
	public void media100(){
		carregarMocks();
		
		assertEquals(new Integer(260), faixaLeituraBO.calcularFaixaLeituraEsperada(100, null, null, leituraAnterior).getFaixaInferior());
		assertEquals(new Integer(340), faixaLeituraBO.calcularFaixaLeituraEsperada(100, null, null, leituraAnterior).getFaixaSuperior());
	}
	
	@Test
	public void media115(){
		carregarMocks();
		
		assertEquals(new Integer(280), faixaLeituraBO.calcularFaixaLeituraEsperada(115, null, null, leituraAnterior).getFaixaInferior());
		assertEquals(new Integer(350), faixaLeituraBO.calcularFaixaLeituraEsperada(115, null, null, leituraAnterior).getFaixaSuperior());
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
}
