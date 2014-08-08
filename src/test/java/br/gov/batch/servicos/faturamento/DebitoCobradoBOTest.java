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
import br.gov.model.faturamento.DebitoCobrado;
import br.gov.model.faturamento.DebitoCobrar;
import br.gov.servicos.to.DebitosContaTO;

@RunWith(EasyMockRunner.class)
public class DebitoCobradoBOTest {
	
	@TestSubject
	private DebitosContaBO business;
	
	@Mock
	private DebitoCobrarBO debitoCobrarEJBMock;
	
	@Mock
	private DebitoCobrarCategoriaBO debitoCobrarCategoriaEJBMock;
	
	@Mock
	private DebitoCobradoCategoriaBO debitoCobradoCategoriaEJBMock;
	
	private Imovel imovel;
	
	private int anoMesFaturamento = 201403;
	
	@Before
	public void setup() {
		imovel = new Imovel();
		business = new DebitosContaBO();
	}
	
	protected void preparaMocks(int anoMesFaturamento, List<DebitoCobrar> debitos) {
		expect(debitoCobrarEJBMock.debitosCobrarVigentes(imovel))
									.andReturn(debitos);
		replay(debitoCobrarEJBMock);
		
		for (DebitoCobrar debitoCobrar : debitos) {
			BigDecimal valorPrestacao = debitoCobrar.getValorPrestacao();
			valorPrestacao = valorPrestacao.add(debitoCobrar.getResiduoPrestacao()).setScale(2);
			expect(debitoCobrarCategoriaEJBMock.dividePrestacaoDebitoPelasEconomias(debitoCobrar.getId(), valorPrestacao)).andReturn(null);
		}
		expect(debitoCobradoCategoriaEJBMock.listaDebitoCobradoCategoriaPeloCobrar(null)).andReturn(null);
		replay(debitoCobrarCategoriaEJBMock);
		replay(debitoCobradoCategoriaEJBMock);
		
		
	}
	
	@Test
	public void testParcelaDebito(){
		List<DebitoCobrar> debitos = new ArrayList<DebitoCobrar>();
		debitos.add(debitoCobrar());
		
		preparaMocks(anoMesFaturamento, debitos);
		
		DebitosContaTO to = business.gerarDebitosConta(imovel, anoMesFaturamento);
		
		assertEquals(7.25, to.getValorTotalDebito().doubleValue(), 0);
	}
	
	@Test
	public void testDebitoCobrarUltimaParcelaComResiduo(){
		List<DebitoCobrar> debitos = new ArrayList<DebitoCobrar>();
		debitos.add(debitoCobrarParceladoENaUltimaParcela());
		
		preparaMocks(anoMesFaturamento, debitos);
		
		DebitosContaTO to = business.gerarDebitosConta(imovel, anoMesFaturamento);
		
		assertEquals(5.34, to.getValorTotalDebito().doubleValue(), 0);
	}
	
	@Test
	public void testIncrementouParcela(){
		List<DebitoCobrar> debitos = new ArrayList<DebitoCobrar>();
		debitos.add(debitoCobrar());
		
		preparaMocks(anoMesFaturamento, debitos);
		
		DebitosContaTO to = business.gerarDebitosConta(imovel, anoMesFaturamento);
		
		DebitoCobrado debitoCobrado = to.getDebitosCobrados().get(0);
		
		assertEquals(debitoCobrado.getNumeroPrestacaoDebito().intValue(), debitoCobrar().getNumeroPrestacaoCobradas() + 1);
	}

	private DebitoCobrar debitoCobrar() {
		DebitoCobrar d = new DebitoCobrar();
		d.setId(3L);
		d.setValorDebito(new BigDecimal(29));
		d.setNumeroPrestacaoCobradas((short) 0);
		d.setNumeroPrestacaoDebito((short) 4);
		return d;
	}
	
	private DebitoCobrar debitoCobrarParceladoENaUltimaParcela() {
		DebitoCobrar d = new DebitoCobrar();
		d.setId(3L);
		d.setValorDebito(new BigDecimal(16));
		d.setNumeroPrestacaoCobradas((short) 2);
		d.setNumeroPrestacaoDebito((short) 3);
		return d;
	}
}
