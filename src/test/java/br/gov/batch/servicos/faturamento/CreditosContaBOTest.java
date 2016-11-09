package br.gov.batch.servicos.faturamento;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.batch.servicos.cadastro.CategoriaBO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.CreditoRealizado;
import br.gov.model.faturamento.CreditoRealizar;
import br.gov.model.faturamento.CreditoTipo;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.CreditoRealizarCategoriaRepositorio;
import br.gov.servicos.faturamento.CreditoRealizarRepositorio;
import br.gov.servicos.to.CreditosContaTO;

public class CreditosContaBOTest {

	@InjectMocks
	private CreditosContaBO creditosContaBO;
	
	@Mock
	private CreditoRealizarRepositorio creditoRealizarRepositorioMock;
	
	@Mock
	private SistemaParametrosRepositorio sistemaParametrosRepositorioMock;

	@Mock
	private CreditoRealizarCategoriaRepositorio creditoRealizarCategoriaRepositorio;
	
	@Mock
	private CategoriaBO categoriaBO;
	
	private Imovel imovel;
	private int anoMesFaturamento;
	
	private CreditosContaTO creditoRealizadoTO;
	private CreditoRealizar creditoRealizarDescontoAcrescimosImpontualidade;
	private CreditoRealizar creditoRealizarDescontoAntiguidadeDebito;
	
    Collection<CreditoRealizar> creditosRealizarVazia = new ArrayList<CreditoRealizar>();
    Collection<CreditoRealizar> creditosRealizarComUm = new ArrayList<CreditoRealizar>();
    Collection<CreditoRealizar> creditosRealizarComDois = new ArrayList<CreditoRealizar>();

    String DESCONTO_ACRESCIMOS_IMPONTUALIDADE = "DESCONTO_ACRESCIMOS_IMPONTUALIDADE";
    String DESCONTO_ANTIGUIDADE_DEBITO        = "DESCONTO_ANTIGUIDADE_DEBITO"; 
    
    CreditoTipo tipoDescontoAcrescimosImpontualidade;
    CreditoTipo tipoDescontoAntiguidadeDebito;

    
	@Before
	public void setup(){
		creditosContaBO = new CreditosContaBO();
		
		imovel = new Imovel();
		imovel.setId(1);
		
		anoMesFaturamento = 201406;
		
        tipoDescontoAcrescimosImpontualidade = new CreditoTipo();
        tipoDescontoAcrescimosImpontualidade.setId(1);
        tipoDescontoAcrescimosImpontualidade.setDescricao(DESCONTO_ACRESCIMOS_IMPONTUALIDADE);
        
        tipoDescontoAntiguidadeDebito = new CreditoTipo();
        tipoDescontoAntiguidadeDebito.setId(1);
        tipoDescontoAntiguidadeDebito.setDescricao(DESCONTO_ANTIGUIDADE_DEBITO);
		
		creditoRealizarDescontoAcrescimosImpontualidade = new CreditoRealizar();
		creditoRealizarDescontoAcrescimosImpontualidade.setNumeroPrestacaoRealizada(new Short("0"));
		creditoRealizarDescontoAcrescimosImpontualidade.setValorResidualMesAnterior(new BigDecimal("0.00"));
		creditoRealizarDescontoAcrescimosImpontualidade.setNumeroPrestacaoCredito(new Short("1"));
		creditoRealizarDescontoAcrescimosImpontualidade.setValorCredito(new BigDecimal("2.00"));
		creditoRealizarDescontoAcrescimosImpontualidade.setCreditoTipo(tipoDescontoAcrescimosImpontualidade);
		
		creditoRealizarDescontoAntiguidadeDebito = new CreditoRealizar();
		creditoRealizarDescontoAntiguidadeDebito.setNumeroPrestacaoRealizada(new Short("0"));
		creditoRealizarDescontoAntiguidadeDebito.setValorResidualMesAnterior(new BigDecimal("0.00"));
		creditoRealizarDescontoAntiguidadeDebito.setNumeroPrestacaoCredito(new Short("1"));
		creditoRealizarDescontoAntiguidadeDebito.setValorCredito(new BigDecimal("8.00"));
		creditoRealizarDescontoAntiguidadeDebito.setCreditoTipo(tipoDescontoAntiguidadeDebito);
		
		creditoRealizadoTO = new CreditosContaTO();
		
        creditosRealizarComUm.add(creditoRealizarDescontoAcrescimosImpontualidade);
        
        creditosRealizarComDois.add(creditoRealizarDescontoAcrescimosImpontualidade);
        creditosRealizarComDois.add(creditoRealizarDescontoAntiguidadeDebito);
        
        MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void calculaValorCorrespondenteParcelaMesComNumeroPrestacoesRealizadasMaiorQueNumeroPrestacoesCredito(){
		creditoRealizarDescontoAcrescimosImpontualidade.setNumeroPrestacaoCredito(new Short("2"));
		creditoRealizarDescontoAcrescimosImpontualidade.setNumeroPrestacaoRealizada(new Short("3"));
		
		BigDecimal retorno = creditoRealizarDescontoAcrescimosImpontualidade.calculaValorCorrespondenteParcelaMes();
		
		assertEquals(0.00, retorno.doubleValue(), 0);
	}
	
	@Test
	public void calculaValorCorrespondenteParcelaMesComNumeroPrestacoesRealizadasMenorQueNumeroPrestacoesCreditoEEhUltimaParcela(){
		creditoRealizarDescontoAcrescimosImpontualidade.setNumeroPrestacaoCredito(new Short("1"));
		creditoRealizarDescontoAcrescimosImpontualidade.setNumeroPrestacaoRealizada(new Short("0"));
		
		BigDecimal retorno = creditoRealizarDescontoAcrescimosImpontualidade.calculaValorCorrespondenteParcelaMes();
		
		assertEquals(new BigDecimal("2.00"), retorno);
	}
	
	@Test
	public void calculaValorCorrespondenteParcelaMesComNumeroPrestacoesRealizadasMenorQueNumeroPrestacoesCreditoComMaisParcelasEEhUltimaParcela(){
		creditoRealizarDescontoAcrescimosImpontualidade.setNumeroPrestacaoCredito(new Short("4"));
		creditoRealizarDescontoAcrescimosImpontualidade.setNumeroPrestacaoRealizada(new Short("3"));
		
		BigDecimal retorno = creditoRealizarDescontoAcrescimosImpontualidade.calculaValorCorrespondenteParcelaMes();
		
		assertEquals(new BigDecimal("0.50"), retorno);
	}
	
	@Test
	public void calculaValorCorrespondenteParcelaMesComNumeroPrestacoesRealizadasMenorQueNumeroPrestacoesCreditoENaoEhUltimaParcela(){
		creditoRealizarDescontoAcrescimosImpontualidade.setNumeroPrestacaoCredito(new Short("4"));
		creditoRealizarDescontoAcrescimosImpontualidade.setNumeroPrestacaoRealizada(new Short("2"));
		
		BigDecimal retorno = creditoRealizarDescontoAcrescimosImpontualidade.calculaValorCorrespondenteParcelaMes();
		
		assertEquals(new BigDecimal("0.50"), retorno);
	}
	
	@Test
	public void calculaValorCorrespondenteParcelaMesComNumeroPrestacoesRealizadasMenorQueNumeroPrestacoesCreditoEEhUltimaParcelaEHaValorResidual(){
		creditoRealizarDescontoAcrescimosImpontualidade.setNumeroPrestacaoCredito(new Short("2"));
		creditoRealizarDescontoAcrescimosImpontualidade.setNumeroPrestacaoRealizada(new Short("1"));
		creditoRealizarDescontoAcrescimosImpontualidade.setValorResidualMesAnterior(new BigDecimal("0.50"));
		
		BigDecimal retorno = creditoRealizarDescontoAcrescimosImpontualidade.calculaValorCredito();
		
		assertEquals(new BigDecimal("1.50"), retorno);
	}
	
	@Test
	public void gerarCreditoRealizadoSemCreditoRealizar(){
		SistemaParametros parametros = new SistemaParametros();
		parametros.setAnoMesFaturamento(201406);
		when(creditoRealizarRepositorioMock.buscarCreditoRealizarPorImovel(imovel.getId(), DebitoCreditoSituacao.NORMAL, anoMesFaturamento))
			.thenReturn(creditosRealizarVazia);
		when(creditoRealizarRepositorioMock.buscarCreditoRealizarPorImovel(imovel.getId(), DebitoCreditoSituacao.PRE_FATURADA, anoMesFaturamento))
		.thenReturn(creditosRealizarVazia);
		when(sistemaParametrosRepositorioMock.getSistemaParametros())
		.thenReturn(parametros);
		
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
	
	@Test
	public void criarCreditoRealizado(){
	    CreditoRealizado realizado = creditosContaBO.criarCreditoRealizado(creditoRealizarDescontoAcrescimosImpontualidade, new BigDecimal(80));
	    assertEquals(80, realizado.getValorCredito().intValue());
	    assertEquals(0, realizado.getNumeroPrestacaoCredito().intValue());
	}
	
	@Test
	public void buscarCreditosRealizarNormal(){
        when(creditoRealizarRepositorioMock.buscarCreditoRealizarPorImovel(imovel.getId(), DebitoCreditoSituacao.NORMAL, anoMesFaturamento))
            .thenReturn(creditosRealizarComUm);
        when(creditoRealizarRepositorioMock.buscarCreditoRealizarPorImovel(imovel.getId(), DebitoCreditoSituacao.PRE_FATURADA, anoMesFaturamento))
        .thenReturn(creditosRealizarVazia);
        
        Collection<CreditoRealizar> creditos = creditosContaBO.creditosRealizar(imovel.getId(), anoMesFaturamento);
        
        CreditoRealizar retorno = creditos.iterator().next();
        
        assertEquals(1, creditos.size());
        assertEquals(creditoRealizarDescontoAcrescimosImpontualidade.getValorCredito().intValue(), retorno.getValorCredito().intValue());
	}
	
    @Test
    public void buscarCreditosRealizarNormalEPrefaturada(){
        when(creditoRealizarRepositorioMock.buscarCreditoRealizarPorImovel(imovel.getId(), DebitoCreditoSituacao.NORMAL, anoMesFaturamento))
            .thenReturn(creditosRealizarComUm);
        when(creditoRealizarRepositorioMock.buscarCreditoRealizarPorImovel(imovel.getId(), DebitoCreditoSituacao.PRE_FATURADA, anoMesFaturamento))
        .thenReturn(creditosRealizarComUm);
        
        Collection<CreditoRealizar> creditos = creditosContaBO.creditosRealizar(imovel.getId(), anoMesFaturamento);
        
        assertEquals(2, creditos.size());
        
        BigDecimal soma = BigDecimal.ZERO;
        for (CreditoRealizar item : creditos) {
            soma = soma.add(item.getValorCredito());
        }
        assertEquals(4, soma.intValue());
    }    
}