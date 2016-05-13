package br.gov.batch.servicos.faturamento;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.model.faturamento.DebitoCobrarCategoria;
import br.gov.servicos.faturamento.DebitoCobrarCategoriaRepositorio;

public class DebitoCobrarCategoriaBOTest {
	
	@InjectMocks
	private DebitoCobrarCategoriaBO business;
	
	@Mock
	private DebitoCobrarCategoriaRepositorio mock;
	
	@Before
	public void setup() {
		business = new DebitoCobrarCategoriaBO();
		
		MockitoAnnotations.initMocks(this);
	}
	
	protected void preparaMocks(List<DebitoCobrarCategoria> categorias) {
		when(mock.listaPeloDebitoCobrar(1)).thenReturn(categorias);
	}
	
	@Test
	public void testUmaEconomia(){
		List<DebitoCobrarCategoria> categorias = new LinkedList<DebitoCobrarCategoria>();
		categorias.add(umaEconomia());
		
		preparaMocks(categorias);
		
		categorias = business.dividePrestacaoDebitoPelasEconomias(1, new BigDecimal(60));
		
		assertEquals(categorias.get(0).getValorPrestacaoEconomia().doubleValue(), 60.00, 0);
	}
	
	@Test
	public void testDuasEconomias(){
		List<DebitoCobrarCategoria> categorias = new LinkedList<DebitoCobrarCategoria>();
		categorias.add(umaEconomia());
		categorias.add(duasEconomias());
		
		preparaMocks(categorias);
		
		categorias = business.dividePrestacaoDebitoPelasEconomias(1, new BigDecimal(60));
				
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
		
		categorias = business.dividePrestacaoDebitoPelasEconomias(1, new BigDecimal(60));
		
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
		
		categorias = business.dividePrestacaoDebitoPelasEconomias(1, new BigDecimal(20));
		
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
