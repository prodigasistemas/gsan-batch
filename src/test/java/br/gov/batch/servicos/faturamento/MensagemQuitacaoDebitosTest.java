package br.gov.batch.servicos.faturamento;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.model.Status;
import br.gov.model.faturamento.ExtratoQuitacao;
import br.gov.servicos.faturamento.ExtratoQuitacaoRepositorio;

public class MensagemQuitacaoDebitosTest {

    @InjectMocks
    private ExtratoQuitacaoBO bo;
    
    @Mock
    private ExtratoQuitacaoRepositorio repositorio;
    
    private String msg;
    private ExtratoQuitacao extratoQuitacao;
    
    @Before
    public void init(){
        extratoQuitacao = new ExtratoQuitacao();
        extratoQuitacao.setIndicadorImpressaoNaConta((int)Status.INATIVO.getId());
        msg = "Em cumprimento a lei 12.007/2009, declaramos quitados os debitos de consumo de agua e/ou esgoto do ano de 2014.";
        bo = new ExtratoQuitacaoBO();
        
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void mensagemPreenchida(){
        when(repositorio.buscarPorImovelEAno(1, 2014)).thenReturn(extratoQuitacao);
        
        assertEquals(msg, bo.obterMsgQuitacaoDebitos(1, 201412));
    }
    
    @Test
    public void mensagemEmBranco(){
        extratoQuitacao.setIndicadorImpressaoNaConta((int)Status.ATIVO.getId());
        when(repositorio.buscarPorImovelEAno(1, 2014)).thenReturn(extratoQuitacao);
        
        assertEquals("", bo.obterMsgQuitacaoDebitos(1, 201412));
    }
}









