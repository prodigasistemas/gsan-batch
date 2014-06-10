package br.gov.faturamento.servicos;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.CreditoRealizar;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.servicos.faturamento.CreditoRealizarRepositorio;
import br.gov.servicos.to.CreditoRealizadoTO;
import br.gov.servicos.to.ValoresFaturamentoAguaEsgotoTO;

@RunWith(EasyMockRunner.class)
public class CreditoRealizadoBOTest {

	@TestSubject
	private CreditoRealizadoBO creditoRealizadoBO;
	
	@Mock
	private CreditoRealizarRepositorio creditoRealizarRepositorioMock;

	private Imovel imovel;
	private boolean preFaturamento;
	private int anoMesFaturamento;
	
	private CreditoRealizadoTO creditoRealizadoTO;
	private ValoresFaturamentoAguaEsgotoTO valoresAguaEsgotoTO;

	@Before
	public void setup(){
		creditoRealizadoBO = new CreditoRealizadoBO();
		preFaturamento = false;
		
		imovel = new Imovel();
		imovel.setId(1L);
		
		anoMesFaturamento = 201406;
		
		creditoRealizadoTO = new CreditoRealizadoTO();
		valoresAguaEsgotoTO = new ValoresFaturamentoAguaEsgotoTO();
	}
	
	@Test
	public void gerarCreditoRealizadoSemCreditoRealizarNaoEhPreFaturamento(){
		Collection<CreditoRealizar> creditosRealizar = new ArrayList<CreditoRealizar>();
		expect(creditoRealizarRepositorioMock.obterCreditoRealizarImovel(imovel.getId(), DebitoCreditoSituacao.NORMAL, anoMesFaturamento))
			.andReturn(creditosRealizar);
		replay(creditoRealizarRepositorioMock);
		
		CreditoRealizadoTO creditoRealizadoTORetorno = creditoRealizadoBO.gerarCreditoRealizado(imovel, anoMesFaturamento, valoresAguaEsgotoTO, new BigDecimal("0.00"), false, preFaturamento);
		
		assertEquals(creditoRealizadoTO.getColecaoCreditosARealizarUpdate().size(), creditoRealizadoTORetorno.getColecaoCreditosARealizarUpdate().size());
		assertEquals(creditoRealizadoTO.getMapValoresPorTipoCredito().size(), creditoRealizadoTORetorno.getMapValoresPorTipoCredito().size());
		assertEquals(creditoRealizadoTO.getMapCreditoRealizado().size(), creditoRealizadoTORetorno.getMapCreditoRealizado().size());
		assertEquals(new BigDecimal("0.00"), creditoRealizadoTORetorno.getValorTotalCreditos());
	}
	
	@Test
	public void calculaValorTotalACobrarEhPreFaturamento() {
		ValoresFaturamentoAguaEsgotoTO valoresAguaEsgotoTO = new ValoresFaturamentoAguaEsgotoTO();
		valoresAguaEsgotoTO.setValorTotalAgua(new BigDecimal("0.00"));
		valoresAguaEsgotoTO.setValorTotalEsgoto(new BigDecimal("0.00"));
		BigDecimal valorTotalDebitos = new BigDecimal("0.00");
		
		preFaturamento = true;
		
		assertEquals(new BigDecimal("1"), creditoRealizadoBO.calculaValorTotalACobrar(valoresAguaEsgotoTO, valorTotalDebitos, preFaturamento));
	}
	
	@Test
	public void calculaValorTotalACobrarZeradoNaoEhPreFaturamento() {
		ValoresFaturamentoAguaEsgotoTO valoresAguaEsgotoTO = new ValoresFaturamentoAguaEsgotoTO();
		valoresAguaEsgotoTO.setValorTotalAgua(new BigDecimal("0.00"));
		valoresAguaEsgotoTO.setValorTotalEsgoto(new BigDecimal("0.00"));
		BigDecimal valorTotalDebitos = new BigDecimal("0.00");
		
		assertEquals(new BigDecimal("0.00"), creditoRealizadoBO.calculaValorTotalACobrar(valoresAguaEsgotoTO, valorTotalDebitos, preFaturamento));
	}
	
	@Test
	public void calculaValorTotalACobrarAguaZeradoNaoEhPreFaturamento() {
		ValoresFaturamentoAguaEsgotoTO valoresAguaEsgotoTO = new ValoresFaturamentoAguaEsgotoTO();
		valoresAguaEsgotoTO.setValorTotalAgua(new BigDecimal("0.00"));
		valoresAguaEsgotoTO.setValorTotalEsgoto(new BigDecimal("1.00"));
		BigDecimal valorTotalDebitos = new BigDecimal("1.00");
		
		assertEquals(new BigDecimal("2.00"), creditoRealizadoBO.calculaValorTotalACobrar(valoresAguaEsgotoTO, valorTotalDebitos, preFaturamento));
		assertEquals(new BigDecimal("1.00"), valoresAguaEsgotoTO.getValorTotalEsgoto());
		assertEquals(new BigDecimal("0.00"), valoresAguaEsgotoTO.getValorTotalAgua());
	}
	
	@Test
	public void calculaValorTotalACobrarAguaEEsgotoZeradosNaoEhPreFaturamento() {
		ValoresFaturamentoAguaEsgotoTO valoresAguaEsgotoTO = new ValoresFaturamentoAguaEsgotoTO();
		valoresAguaEsgotoTO.setValorTotalAgua(new BigDecimal("0.00"));
		valoresAguaEsgotoTO.setValorTotalEsgoto(new BigDecimal("0.00"));
		BigDecimal valorTotalDebitos = new BigDecimal("1.00");
		
		assertEquals(new BigDecimal("1.00"), creditoRealizadoBO.calculaValorTotalACobrar(valoresAguaEsgotoTO, valorTotalDebitos, preFaturamento));
		assertEquals(new BigDecimal("0.00"), valoresAguaEsgotoTO.getValorTotalEsgoto());
		assertEquals(new BigDecimal("0.00"), valoresAguaEsgotoTO.getValorTotalAgua());
	}
	
	@Test
	public void calculaValorTotalACobrarTotalDebitosZeradosNaoEhPreFaturamento() {
		ValoresFaturamentoAguaEsgotoTO valoresAguaEsgotoTO = new ValoresFaturamentoAguaEsgotoTO();
		valoresAguaEsgotoTO.setValorTotalAgua(new BigDecimal("1.00"));
		valoresAguaEsgotoTO.setValorTotalEsgoto(new BigDecimal("1.00"));
		BigDecimal valorTotalDebitos = new BigDecimal("0.00");
		
		assertEquals(new BigDecimal("2.00"), creditoRealizadoBO.calculaValorTotalACobrar(valoresAguaEsgotoTO, valorTotalDebitos, preFaturamento));
		assertEquals(new BigDecimal("1.00"), valoresAguaEsgotoTO.getValorTotalEsgoto());
		assertEquals(new BigDecimal("1.00"), valoresAguaEsgotoTO.getValorTotalAgua());
	}
}