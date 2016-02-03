package br.gov.batch.servicos.faturamento;

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

import br.gov.batch.servicos.faturamento.CreditosContaBO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.CreditoRealizar;
import br.gov.model.faturamento.CreditoRealizarCategoria;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.CreditoRealizarCategoriaRepositorio;
import br.gov.servicos.faturamento.CreditoRealizarRepositorio;
import br.gov.servicos.to.CreditosContaTO;

@RunWith(EasyMockRunner.class)
public class CreditosContaBOTest {

	@TestSubject
	private CreditosContaBO creditosContaBO;
	
	@Mock
	private CreditoRealizarRepositorio creditoRealizarRepositorioMock;
	
	@Mock
	private SistemaParametrosRepositorio sistemaParametrosRepositorioMock;

	@Mock
	private CreditoRealizarCategoriaRepositorio creditoRealizarCategoriaRepositorio;
	
	private Imovel imovel;
	private int anoMesFaturamento;
	
	private CreditosContaTO creditoRealizadoTO;
	private CreditoRealizar creditoRealizar;
	private Collection<CreditoRealizarCategoria> categoriasCreditoRealizar;

	@Before
	public void setup(){
		creditosContaBO = new CreditosContaBO();
		
		imovel = new Imovel();
		imovel.setId(1);
		
		anoMesFaturamento = 201406;
		
		creditoRealizar = new CreditoRealizar();
		creditoRealizar.setNumeroPrestacaoCredito(new Short("2"));
		creditoRealizar.setNumeroPrestacaoRealizada(new Short("0"));
		creditoRealizar.setValorResidualMesAnterior(new BigDecimal("0.00"));
		creditoRealizar.setNumeroPrestacaoCredito(new Short("1"));
		creditoRealizar.setValorCredito(new BigDecimal("2.00"));
		
		creditoRealizadoTO = new CreditosContaTO();
		categoriasCreditoRealizar = new ArrayList<CreditoRealizarCategoria>();		
	}
	
	@Test
	public void calculaValorCorrespondenteParcelaMesComNumeroPrestacoesRealizadasMaiorQueNumeroPrestacoesCredito(){
		creditoRealizar.setNumeroPrestacaoCredito(new Short("2"));
		creditoRealizar.setNumeroPrestacaoRealizada(new Short("3"));
		
		BigDecimal retorno = creditoRealizar.calculaValorCorrespondenteParcelaMes();
		
		assertEquals(0.00, retorno.doubleValue(), 0);
	}
	
	@Test
	public void calculaValorCorrespondenteParcelaMesComNumeroPrestacoesRealizadasMenorQueNumeroPrestacoesCreditoEEhUltimaParcela(){
		creditoRealizar.setNumeroPrestacaoCredito(new Short("1"));
		creditoRealizar.setNumeroPrestacaoRealizada(new Short("0"));
		
		BigDecimal retorno = creditoRealizar.calculaValorCorrespondenteParcelaMes();
		
		assertEquals(new BigDecimal("2.00"), retorno);
	}
	
	@Test
	public void calculaValorCorrespondenteParcelaMesComNumeroPrestacoesRealizadasMenorQueNumeroPrestacoesCreditoComMaisParcelasEEhUltimaParcela(){
		creditoRealizar.setNumeroPrestacaoCredito(new Short("4"));
		creditoRealizar.setNumeroPrestacaoRealizada(new Short("3"));
		
		BigDecimal retorno = creditoRealizar.calculaValorCorrespondenteParcelaMes();
		
		assertEquals(new BigDecimal("0.50"), retorno);
	}
	
	@Test
	public void calculaValorCorrespondenteParcelaMesComNumeroPrestacoesRealizadasMenorQueNumeroPrestacoesCreditoENaoEhUltimaParcela(){
		creditoRealizar.setNumeroPrestacaoCredito(new Short("4"));
		creditoRealizar.setNumeroPrestacaoRealizada(new Short("2"));
		
		BigDecimal retorno = creditoRealizar.calculaValorCorrespondenteParcelaMes();
		
		assertEquals(new BigDecimal("0.50"), retorno);
	}
	
	@Test
	public void calculaValorCorrespondenteParcelaMesComNumeroPrestacoesRealizadasMenorQueNumeroPrestacoesCreditoEEhUltimaParcelaEHaValorResidual(){
		creditoRealizar.setNumeroPrestacaoCredito(new Short("2"));
		creditoRealizar.setNumeroPrestacaoRealizada(new Short("1"));
		creditoRealizar.setValorResidualMesAnterior(new BigDecimal("0.50"));
		
		BigDecimal retorno = creditoRealizar.calculaValorCredito();
		
		assertEquals(new BigDecimal("1.50"), retorno);
	}
	
	@Test
	public void gerarCreditoRealizadoSemCreditoRealizar(){
		SistemaParametros parametros = new SistemaParametros();
		parametros.setAnoMesFaturamento(201406);
		Collection<CreditoRealizar> creditosRealizar = new ArrayList<CreditoRealizar>();
		expect(creditoRealizarRepositorioMock.buscarCreditoRealizarPorImovel(imovel.getId(), DebitoCreditoSituacao.NORMAL, anoMesFaturamento))
			.andReturn(creditosRealizar);
		expect(creditoRealizarRepositorioMock.buscarCreditoRealizarPorImovel(imovel.getId(), DebitoCreditoSituacao.PRE_FATURADA, anoMesFaturamento))
		.andReturn(creditosRealizar);
		expect(sistemaParametrosRepositorioMock.getSistemaParametros())
		.andReturn(parametros);
		replay(sistemaParametrosRepositorioMock);
		replay(creditoRealizarRepositorioMock);
		
		CreditosContaTO creditoRealizadoTORetorno = creditosContaBO.gerarCreditosConta(imovel.getId(), anoMesFaturamento);
		
		assertEquals(creditoRealizadoTO.getCreditosRealizar().size(), creditoRealizadoTORetorno.getCreditosRealizar().size());
		assertEquals(creditoRealizadoTO.getMapValoresPorTipoCredito().size(), creditoRealizadoTORetorno.getMapValoresPorTipoCredito().size());
		assertEquals(creditoRealizadoTO.getMapCreditoRealizado().size(), creditoRealizadoTORetorno.getMapCreditoRealizado().size());
		assertEquals(0.00, creditoRealizadoTORetorno.getValorTotalCreditos().doubleValue(), 0);
	}
	
	@Test
	public void contasSemCreditoRealizar(){
		Collection<CreditoRealizar> creditosRealizar = new ArrayList<CreditoRealizar>();
		
		CreditosContaTO creditos = creditosContaBO.gerarCreditos(anoMesFaturamento, creditosRealizar);
		
		assertEquals(BigDecimal.ZERO, creditos.getValorTotalCreditos());
	}
	
	@Test
	public void contasComCreditoRealizarApenasResidual(){
		CreditoRealizar credito = new CreditoRealizar();
		credito.setValorResidualMesAnterior(new BigDecimal(2.50));
		
		assertEquals(new BigDecimal(2.50), credito.calculaValorCredito());
	}
	
	@Test
	public void contasComCreditoRealizarParceladosSemResiduo(){
		CreditoRealizar credito = new CreditoRealizar();
		credito.setNumeroPrestacaoCredito((short) 10);
		credito.setValorCredito(new BigDecimal(40));
		
		assertEquals(4.00, credito.calculaValorCredito().doubleValue(), 0);
	}
	
	@Test
	public void contasComCreditoRealizarParceladosComResiduo(){
		CreditoRealizar credito = new CreditoRealizar();
		credito.setNumeroPrestacaoCredito((short) 10);
		credito.setValorCredito(new BigDecimal(40));
		credito.setValorResidualMesAnterior(new BigDecimal(2.50));
		
		assertEquals(6.50, credito.calculaValorCredito().doubleValue(), 0);
	}
}