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

import br.gov.model.faturamento.DebitoCobrado;
import br.gov.model.faturamento.DebitoCobrar;
import br.gov.servicos.to.DebitosContaTO;

public class DebitoCobradoBOTest {
	
	@InjectMocks
	private DebitosContaBO business;
	
	@Mock
	private DebitoCobrarBO debitoCobrarEJBMock;
	
	@Mock
	private DebitoCobrarCategoriaBO debitoCobrarCategoriaEJBMock;
	
	@Mock
	private DebitoCobradoCategoriaBO debitoCobradoCategoriaEJBMock;
	
	private Integer idImovel = 10;
	
	private int anoMesFaturamento = 201403;
	
	@Before
	public void setup() {
		business = new DebitosContaBO();
		
		MockitoAnnotations.initMocks(this);
	}
	
	protected void preparaMocks(int anoMesFaturamento, List<DebitoCobrar> debitos) {
		when(debitoCobrarEJBMock.debitosCobrarVigentes(idImovel)).thenReturn(debitos);
		
		for (DebitoCobrar debitoCobrar : debitos) {
			BigDecimal valorPrestacao = debitoCobrar.getValorPrestacao();
			valorPrestacao = valorPrestacao.add(debitoCobrar.getResiduoPrestacao()).setScale(2);
			when(debitoCobrarCategoriaEJBMock.dividePrestacaoDebitoPelasEconomias(debitoCobrar.getId(), valorPrestacao)).thenReturn(null);
		}
		when(debitoCobradoCategoriaEJBMock.listaDebitoCobradoCategoriaPeloCobrar(null)).thenReturn(null);
	}
	
	@Test
	public void testParcelaDebito(){
		List<DebitoCobrar> debitos = new ArrayList<DebitoCobrar>();
		debitos.add(debitoCobrar());
		
		preparaMocks(anoMesFaturamento, debitos);
		
		DebitosContaTO to = business.gerarDebitosConta(idImovel, anoMesFaturamento);
		
		assertEquals(7.25, to.getValorTotalDebito().doubleValue(), 0);
	}
	
	@Test
	public void testDebitoCobrarUltimaParcelaComResiduo(){
		List<DebitoCobrar> debitos = new ArrayList<DebitoCobrar>();
		debitos.add(debitoCobrarParceladoENaUltimaParcela());
		
		preparaMocks(anoMesFaturamento, debitos);
		
		DebitosContaTO to = business.gerarDebitosConta(idImovel, anoMesFaturamento);
		
		assertEquals(5.34, to.getValorTotalDebito().doubleValue(), 0);
	}
	
	@Test
	public void testIncrementouParcela(){
		List<DebitoCobrar> debitos = new ArrayList<DebitoCobrar>();
		debitos.add(debitoCobrar());
		
		preparaMocks(anoMesFaturamento, debitos);
		
		DebitosContaTO to = business.gerarDebitosConta(idImovel, anoMesFaturamento);
		
		DebitoCobrado debitoCobrado = to.getDebitosCobrados().get(0);
		
		assertEquals(debitoCobrado.getNumeroPrestacaoDebito().intValue(), debitoCobrar().getNumeroPrestacaoCobradas() + 1);
	}

	private DebitoCobrar debitoCobrar() {
		DebitoCobrar d = new DebitoCobrar();
		d.setId(3);
		d.setValorDebito(new BigDecimal(29));
		d.setNumeroPrestacaoCobradas((short) 0);
		d.setNumeroPrestacaoDebito((short) 4);
		return d;
	}
	
	private DebitoCobrar debitoCobrarParceladoENaUltimaParcela() {
		DebitoCobrar d = new DebitoCobrar();
		d.setId(3);
		d.setValorDebito(new BigDecimal(16));
		d.setNumeroPrestacaoCobradas((short) 2);
		d.setNumeroPrestacaoDebito((short) 3);
		return d;
	}
}
