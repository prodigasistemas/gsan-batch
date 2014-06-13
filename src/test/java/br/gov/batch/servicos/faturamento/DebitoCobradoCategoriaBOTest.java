package br.gov.batch.servicos.faturamento;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.faturamento.DebitoCobrarCategoria;
import br.gov.servicos.faturamento.DebitoCobrarCategoriaRepositorio;

@RunWith(EasyMockRunner.class)
public class DebitoCobradoCategoriaBOTest {
	
	@TestSubject
	private DebitoCobrarCategoriaBO business;
	
	@Mock
	private DebitoCobrarCategoriaRepositorio mock;
	
	@Before
	public void setup() {
		business = new DebitoCobrarCategoriaBO();
	}
	
	protected void preparaMocks(List<DebitoCobrarCategoria> categorias) {
		expect(mock.listaPeloDebitoCobrar(1L))
		.andReturn(categorias);
		replay(mock);
	}
	
	@Test
	public void testUmaEconomia(){
		List<DebitoCobrarCategoria> categorias = new LinkedList<DebitoCobrarCategoria>();
		categorias.add(umaEconomia());
		
		preparaMocks(categorias);
		
		categorias = business.dividePrestacaoDebitoPelasEconomias(1L, new BigDecimal(60));
		
		assertEquals(categorias.get(0).getValorPrestacaoEconomia().doubleValue(), 60.00, 0);
	}
	
	@Test
	public void testDuasEconomias(){
		List<DebitoCobrarCategoria> categorias = new LinkedList<DebitoCobrarCategoria>();
		categorias.add(umaEconomia());
		categorias.add(duasEconomias());
		
		preparaMocks(categorias);
		
		categorias = business.dividePrestacaoDebitoPelasEconomias(1L, new BigDecimal(60));
				
		assertEquals(categorias.get(0).getValorPrestacaoEconomia().doubleValue(), 20.00, 0);
		assertEquals(categorias.get(1).getValorPrestacaoEconomia().doubleValue(), 40.00, 0);
	}
	
	@Test
	public void testTresEconomias(){
		List<DebitoCobrarCategoria> categorias = new LinkedList<DebitoCobrarCategoria>();
		categorias.add(umaEconomia());
		categorias.add(duasEconomias());
		categorias.add(tresEconomias());
		
		preparaMocks(categorias);
		
		categorias = business.dividePrestacaoDebitoPelasEconomias(1L, new BigDecimal(60));
		
		assertEquals(categorias.get(0).getValorPrestacaoEconomia().doubleValue(), 10.00, 0);
		assertEquals(categorias.get(1).getValorPrestacaoEconomia().doubleValue(), 20.00, 0);
		assertEquals(categorias.get(2).getValorPrestacaoEconomia().doubleValue(), 30.00, 0);
	}
	
	@Test
	public void testEconomiasComResiduo(){
		List<DebitoCobrarCategoria> categorias = new LinkedList<DebitoCobrarCategoria>();
		categorias.add(umaEconomia());
		categorias.add(duasEconomias());
		categorias.add(tresEconomias());
		
		preparaMocks(categorias);
		
		categorias = business.dividePrestacaoDebitoPelasEconomias(1L, new BigDecimal(20));
		
		assertEquals(categorias.get(0).getValorPrestacaoEconomia().doubleValue(), 3.35, 0);
		assertEquals(categorias.get(1).getValorPrestacaoEconomia().doubleValue(), 6.66, 0);
		assertEquals(categorias.get(2).getValorPrestacaoEconomia().doubleValue(), 9.99, 0);
	}

	private DebitoCobrarCategoria umaEconomia() {
		DebitoCobrarCategoria d = new DebitoCobrarCategoria();
		d.setQuantidadeEconomia(1);
		return d;
	}
	
	private DebitoCobrarCategoria duasEconomias() {
		DebitoCobrarCategoria d = new DebitoCobrarCategoria();
		d.setQuantidadeEconomia(2);
		return d;
	}
	
	private DebitoCobrarCategoria tresEconomias() {
		DebitoCobrarCategoria d = new DebitoCobrarCategoria();
		d.setQuantidadeEconomia(3);
		return d;
	}
}
