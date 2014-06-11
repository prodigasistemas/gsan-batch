package br.gov.batch.servicos.faturamento;

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

import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.DebitoCobrar;
import br.gov.servicos.to.DebitoCobradoTO;

@RunWith(EasyMockRunner.class)
public class DebitoCobradoBOTest {
	
	@TestSubject
	private DebitoCobradoBO business;
	
	@Mock
	private DebitoCobrarBO debitoCobrarEJBMock;
	
	private Imovel imovel;
	
	private int anoMesFaturamento = 201403;
	
	@Before
	public void setup() {
		imovel = new Imovel();
		business = new DebitoCobradoBO();
	}
	
	protected void preparaMocks(int anoMesFaturamento, List<DebitoCobrar> debitos) {
		expect(debitoCobrarEJBMock.debitosCobrarVigentes(imovel))
		.andReturn(debitos);
		replay(debitoCobrarEJBMock);
	}
	
	@Test
	public void testDebitoCobrarPrimeiraParcela(){
		List<DebitoCobrar> debitos = new ArrayList<DebitoCobrar>();
		debitos.add(debitoCobrarComParcelasENaPrimeiraParcela());
		
		preparaMocks(anoMesFaturamento, debitos);
		
		DebitoCobradoTO to = business.gerarDebitoCobrado(imovel, anoMesFaturamento);
		
		assertEquals(7.25, to.getValorDebito().doubleValue(), 0);
	}
	
	@Test
	public void testDebitoCobrarUltimaParcelaComResiduo(){
		List<DebitoCobrar> debitos = new ArrayList<DebitoCobrar>();
		debitos.add(debitoCobrarComParcelasENaUltimaParcela());
		
		preparaMocks(anoMesFaturamento, debitos);
		
		DebitoCobradoTO to = business.gerarDebitoCobrado(imovel, anoMesFaturamento);
		
		assertEquals(5.34, to.getValorDebito().doubleValue(), 0);
	}

	private DebitoCobrar debitoCobrarComParcelasENaPrimeiraParcela() {
		DebitoCobrar d = new DebitoCobrar();
		d.setId(3L);
		d.setValorDebito(new BigDecimal(29));
		d.setNumeroPrestacaoCobradas((short) 0);
		d.setNumeroPrestacaoDebito((short) 4);
		return d;
	}
	
	private DebitoCobrar debitoCobrarComParcelasENaUltimaParcela() {
		DebitoCobrar d = new DebitoCobrar();
		d.setId(3L);
		d.setValorDebito(new BigDecimal(16));
		d.setNumeroPrestacaoCobradas((short) 2);
		d.setNumeroPrestacaoDebito((short) 3);
		return d;
	}
}
