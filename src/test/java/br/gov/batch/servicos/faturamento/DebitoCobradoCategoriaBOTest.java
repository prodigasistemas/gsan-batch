package br.gov.batch.servicos.faturamento;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.faturamento.DebitoCobradoCategoria;
import br.gov.model.faturamento.DebitoCobrarCategoria;
import br.gov.model.faturamento.DebitoCobrarCategoriaPK;

@RunWith(EasyMockRunner.class)
public class DebitoCobradoCategoriaBOTest {

	@TestSubject
	private DebitoCobradoCategoriaBO bo;

	@Before
	public void setup() {
		bo = new DebitoCobradoCategoriaBO();
	}

	@Test
	public void debitosACobrarNulo() {
		List<DebitoCobradoCategoria> debitosCobrados = bo
				.listaDebitoCobradoCategoriaPeloCobrar(new ArrayList<DebitoCobrarCategoria>());

		assertEquals(0, debitosCobrados.size());
	}

	@Test
	public void debitosCobrados() {
		List<DebitoCobradoCategoria> debitosCobrados = bo.listaDebitoCobradoCategoriaPeloCobrar(getListaDebitosACobrar());

		DebitoCobradoCategoria debitoCobrado = debitosCobrados.get(0);
		assertEquals(new Integer(1), debitoCobrado.getId().getCategoriaId());
		assertEquals(new Integer(2), debitoCobrado.getQuantidadeEconomia());
		assertEquals(new BigDecimal("10.00"), debitoCobrado.getValorCategoria());

		debitoCobrado = debitosCobrados.get(1);
		assertEquals(new Integer(2), debitoCobrado.getId().getCategoriaId());
		assertEquals(new Integer(5), debitoCobrado.getQuantidadeEconomia());
		assertEquals(new BigDecimal("50.00"), debitoCobrado.getValorCategoria());
	}

	private List<DebitoCobrarCategoria> getListaDebitosACobrar() {
		List<DebitoCobrarCategoria> debitos = new ArrayList<DebitoCobrarCategoria>();

		DebitoCobrarCategoria debito = new DebitoCobrarCategoria();
		DebitoCobrarCategoriaPK pk = new DebitoCobrarCategoriaPK(1, 1);
		debito.setId(pk);
		debito.setQuantidadeEconomia(2);
		debito.setValorPrestacaoEconomia(new BigDecimal("10.00"));

		debitos.add(debito);

		debito = new DebitoCobrarCategoria();
		pk = new DebitoCobrarCategoriaPK(2, 2);
		debito.setId(pk);
		debito.setQuantidadeEconomia(5);
		debito.setValorPrestacaoEconomia(new BigDecimal("50.00"));

		debitos.add(debito);

		return debitos;
	}
}
