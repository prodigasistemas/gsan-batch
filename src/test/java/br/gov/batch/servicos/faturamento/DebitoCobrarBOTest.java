package br.gov.batch.servicos.faturamento;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.cobranca.Parcelamento;
import br.gov.model.faturamento.DebitoCobrar;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.DebitoCobrarRepositorio;

public class DebitoCobrarBOTest {
	
	@InjectMocks
	private DebitoCobrarBO business;
	
	@Mock
	private DebitoCobrarRepositorio debitoCobrarEJBMock;
	
	@Mock
	private SistemaParametrosRepositorio parametrosMock;
	
	private Integer idImovel = 10;
	
	private int anoMesReferencia = 201403;
	
	private SistemaParametros parametros;
	
	@Before
	public void setup() {
		business = new DebitoCobrarBO();
		parametros = new SistemaParametros();
		parametros.setAnoMesFaturamento(anoMesReferencia);
		
		MockitoAnnotations.initMocks(this);
	}
	
	protected void preparaMocks(int anoMesFaturamento, List<DebitoCobrar> debitos) {
		when(debitoCobrarEJBMock.debitosCobrarPorImovelComPendenciaESemRevisao(idImovel))
		.thenReturn(debitos);
		when(parametrosMock.getSistemaParametros())
		.thenReturn(parametros);
		
		business.init();
	}
	
	@Test
	public void testDebitoCobrarVigentesApenasSemParcelamento(){
		List<DebitoCobrar> debitos = new ArrayList<DebitoCobrar>();
		debitos.add(debitoCobrarSimples());
		
		preparaMocks(anoMesReferencia, debitos);
		
		assertEquals(1, business.debitosCobrarVigentes(idImovel).size());
	}

	@Test
	public void testDebitoCobrarComDataParcelamentoSuperior(){
		List<DebitoCobrar> debitos = new ArrayList<DebitoCobrar>();
		debitos.add(debitoCobrarSimples());
		debitos.add(debitoCobrarComDataParcelamentoFutura());
		
		preparaMocks(anoMesReferencia, debitos);
		
		assertEquals(1, business.debitosCobrarVigentes(idImovel).size());
	}
	
	@Test
	public void testDebitoCobrarComDataParcelamentoVencido(){
		List<DebitoCobrar> debitos = new ArrayList<DebitoCobrar>();
		debitos.add(debitoCobrarSimples());
		debitos.add(debitoCobrarComDataParcelamentoFutura());
		debitos.add(debitoCobrarComDataParcelamentoVencido());
		
		preparaMocks(anoMesReferencia, debitos);
		
		assertEquals(2, business.debitosCobrarVigentes(idImovel).size());
	}
	
	@Test
	public void testDebitoCobrarComDataParcelamentoSuperiorPoremAntecipado(){
		List<DebitoCobrar> debitos = new ArrayList<DebitoCobrar>();
		debitos.add(debitoCobrarSimples());
		debitos.add(debitoCobrarComDataParcelamentoFutura());
		debitos.add(debitoCobrarComDataParcelamentoFuturaPoremAntecipado());
		
		preparaMocks(anoMesReferencia, debitos);
		
		assertEquals(2, business.debitosCobrarVigentes(idImovel).size());
	}

	private DebitoCobrar debitoCobrarComDataParcelamentoVencido() {
		Parcelamento p = new Parcelamento();
		p.setAnoMesReferenciaFaturamento(anoMesReferencia - 1);
		DebitoCobrar d = new DebitoCobrar();
		d.setId(3);
		d.setValorDebito(new BigDecimal(28));
		d.setNumeroPrestacaoDebito((short) 4);
		d.setParcelamento(p);
		
		return d;
	}

	private DebitoCobrar debitoCobrarSimples() {
		DebitoCobrar d = new DebitoCobrar();
		d.setId(3);
		d.setValorDebito(new BigDecimal(28));
		d.setNumeroPrestacaoDebito((short) 4);
		return d;
	}
	
	private DebitoCobrar debitoCobrarComDataParcelamentoFutura() {
		Parcelamento p = new Parcelamento();
		p.setAnoMesReferenciaFaturamento(anoMesReferencia + 1);
		DebitoCobrar d = new DebitoCobrar();
		d.setId(3);
		d.setValorDebito(new BigDecimal(28));
		d.setNumeroPrestacaoDebito((short) 4);
		d.setParcelamento(p);
		
		return d;
	}
	
	private DebitoCobrar debitoCobrarComDataParcelamentoFuturaPoremAntecipado() {
		Parcelamento p = new Parcelamento();
		p.setAnoMesReferenciaFaturamento(anoMesReferencia + 1);
		DebitoCobrar d = new DebitoCobrar();
		d.setId(3);
		d.setValorDebito(new BigDecimal(28));
		d.setNumeroPrestacaoDebito((short) 4);
		d.setParcelamento(p);
		d.setNumeroPrestacaoCobradas((short)  1);
		
		return d;
	}
}
