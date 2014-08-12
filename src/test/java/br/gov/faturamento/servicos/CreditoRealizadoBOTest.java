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

import br.gov.batch.servicos.faturamento.CreditosContaBO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.CreditoRealizar;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.CreditoRealizarRepositorio;
import br.gov.servicos.to.CreditosContaTO;
import br.gov.servicos.to.FaturamentoAguaEsgotoTO;

@RunWith(EasyMockRunner.class)
public class CreditoRealizadoBOTest {

	@TestSubject
	private CreditosContaBO creditoRealizadoBO;
	
	@Mock
	private CreditoRealizarRepositorio creditoRealizarRepositorioMock;
	
	@Mock
	private SistemaParametrosRepositorio sistemaParametrosRepositorioMock;

	private Imovel imovel;
	private int anoMesFaturamento;
	
	private CreditosContaTO creditoRealizadoTO;
	private FaturamentoAguaEsgotoTO valoresAguaEsgotoTO;
	private CreditoRealizar creditoRealizar;

	@Before
	public void setup(){
		creditoRealizadoBO = new CreditosContaBO();
		
		imovel = new Imovel();
		imovel.setId(1L);
		
		anoMesFaturamento = 201406;
		
		creditoRealizar = new CreditoRealizar();
		creditoRealizar.setNumeroPrestacaoCredito(new Short("2"));
		creditoRealizar.setNumeroPrestacaoRealizada(new Short("0"));
		creditoRealizar.setValorResidualMesAnterior(new BigDecimal("0.00"));
		creditoRealizar.setNumeroPrestacaoCredito(new Short("1"));
		creditoRealizar.setValorCredito(new BigDecimal("2.00"));
		
		creditoRealizadoTO = new CreditosContaTO();
		valoresAguaEsgotoTO = new FaturamentoAguaEsgotoTO();
	}
	
	@Test
	public void calculaValorCorrespondenteParcelaMesComNumeroPrestacoesRealizadasMaiorQueNumeroPrestacoesCredito(){
		creditoRealizar.setNumeroPrestacaoCredito(new Short("2"));
		creditoRealizar.setNumeroPrestacaoRealizada(new Short("3"));
		
		BigDecimal retorno = creditoRealizadoBO.calculaValorCorrespondenteParcelaMes(creditoRealizar);
		
		assertEquals(new BigDecimal("0.00"), retorno);
	}
	
	@Test
	public void calculaValorCorrespondenteParcelaMesComNumeroPrestacoesRealizadasMenorQueNumeroPrestacoesCreditoEEhUltimaParcela(){
		creditoRealizar.setNumeroPrestacaoCredito(new Short("1"));
		creditoRealizar.setNumeroPrestacaoRealizada(new Short("0"));
		
		BigDecimal retorno = creditoRealizadoBO.calculaValorCorrespondenteParcelaMes(creditoRealizar);
		
		assertEquals(new BigDecimal("2.00"), retorno);
	}
	
	@Test
	public void calculaValorCorrespondenteParcelaMesComNumeroPrestacoesRealizadasMenorQueNumeroPrestacoesCreditoComMaisParcelasEEhUltimaParcela(){
		creditoRealizar.setNumeroPrestacaoCredito(new Short("4"));
		creditoRealizar.setNumeroPrestacaoRealizada(new Short("3"));
		
		BigDecimal retorno = creditoRealizadoBO.calculaValorCorrespondenteParcelaMes(creditoRealizar);
		
		assertEquals(new BigDecimal("0.50"), retorno);
	}
	
	@Test
	public void calculaValorCorrespondenteParcelaMesComNumeroPrestacoesRealizadasMenorQueNumeroPrestacoesCreditoENaoEhUltimaParcela(){
		creditoRealizar.setNumeroPrestacaoCredito(new Short("4"));
		creditoRealizar.setNumeroPrestacaoRealizada(new Short("2"));
		
		BigDecimal retorno = creditoRealizadoBO.calculaValorCorrespondenteParcelaMes(creditoRealizar);
		
		assertEquals(new BigDecimal("0.50"), retorno);
	}
	
	@Test
	public void calculaValorCorrespondenteParcelaMesComNumeroPrestacoesRealizadasMenorQueNumeroPrestacoesCreditoEEhUltimaParcelaEHaValorResidual(){
		creditoRealizar.setNumeroPrestacaoCredito(new Short("2"));
		creditoRealizar.setNumeroPrestacaoRealizada(new Short("1"));
		creditoRealizar.setValorResidualMesAnterior(new BigDecimal("0.50"));
		
		BigDecimal retorno = creditoRealizadoBO.calculaValorCorrespondenteParcelaMes(creditoRealizar);
		
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
		
		CreditosContaTO creditoRealizadoTORetorno = creditoRealizadoBO.gerarCreditosConta(imovel, anoMesFaturamento);
		
		assertEquals(creditoRealizadoTO.getColecaoCreditosARealizarUpdate().size(), creditoRealizadoTORetorno.getColecaoCreditosARealizarUpdate().size());
		assertEquals(creditoRealizadoTO.getMapValoresPorTipoCredito().size(), creditoRealizadoTORetorno.getMapValoresPorTipoCredito().size());
		assertEquals(creditoRealizadoTO.getMapCreditoRealizado().size(), creditoRealizadoTORetorno.getMapCreditoRealizado().size());
		assertEquals(0.00, creditoRealizadoTORetorno.getValorTotalCreditos().doubleValue(), 0);
	}
}