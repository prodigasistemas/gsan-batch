package br.gov.batch.servicos.faturamento;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.Status;
import br.gov.model.faturamento.ExtratoQuitacao;
import br.gov.servicos.faturamento.ExtratoQuitacaoRepositorio;

@RunWith(EasyMockRunner.class)
public class MensagemQuitacaoDebitosTest {

    @TestSubject
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
    }
    
    @Test
    public void mensagemPreenchida(){
        expect(repositorio.buscarPorImovelEAno(1, 2014)).andReturn(extratoQuitacao);
        replay(repositorio);
        
        assertEquals(msg, bo.obterMsgQuitacaoDebitos(1, 201412));
    }
    
    @Test
    public void mensagemEmBranco(){
        extratoQuitacao.setIndicadorImpressaoNaConta((int)Status.ATIVO.getId());
        expect(repositorio.buscarPorImovelEAno(1, 2014)).andReturn(extratoQuitacao);
        replay(repositorio);
        
        assertEquals("", bo.obterMsgQuitacaoDebitos(1, 201412));
    }
}









