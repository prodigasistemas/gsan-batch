package br.gov.batch.servicos.arrecadacao;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.arrecadacao.DebitoAutomatico;
import br.gov.model.arrecadacao.DebitoAutomaticoMovimento;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.ContaGeral;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.servicos.arrecadacao.DebitoAutomaticoMovimentoRepositorio;
import br.gov.servicos.arrecadacao.DebitoAutomaticoRepositorio;

@RunWith(EasyMockRunner.class)
public class DebitoAutomaticoMovimentoBOTest {
    
    @TestSubject
    private DebitoAutomaticoMovimentoBO bo;
    
    @Mock
    private DebitoAutomaticoMovimentoRepositorio debitoAutomaticoMovimentoRepositorioMock;
    
    @Mock
    private DebitoAutomaticoRepositorio debitoAutomaticoRepositorioMock;
    
    Imovel imovel = null;
    
    Conta conta = null;
    
    FaturamentoGrupo faturamentoGrupo = null;
    
    DebitoAutomatico debitoAutomatico = null;
    
    DebitoAutomaticoMovimento movimento = null;
    
    @Before
    public void setUp(){
        bo = new DebitoAutomaticoMovimentoBO();
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(2015, 4, 6);
        
        imovel = new Imovel();
        imovel.setId(1);
        
        debitoAutomatico = new DebitoAutomatico();
        debitoAutomatico.setImovel(imovel);
        
        conta = new Conta();
        conta.setDataVencimentoConta(calendar.getTime());
        
        ContaGeral contaGeral = new ContaGeral();
        contaGeral.setId(100);
        conta.setContaGeral(contaGeral);
        
        faturamentoGrupo = new FaturamentoGrupo();
        faturamentoGrupo.setId(300);
        
        
        movimento = new DebitoAutomaticoMovimento().new Builder()
                .contaGeral(conta.getContaGeral())
                .debitoAutomatico(debitoAutomatico)
                .faturamentoGrupo(faturamentoGrupo)
                .dataVencimento(calendar.getTime())
                .build();
    }
    
    @Test
    public void testaImovelSemDebitoAutomatico(){
        mockImovelSemDebitoAutomatico();
        bo.gerarMovimentoDebitoAutomatico(imovel, conta, faturamentoGrupo);
    }
    
    @Test
    public void testImovelComDebitoAutomatico(){
        mockImovelComDebitoAutomatico();   
        bo.gerarMovimentoDebitoAutomatico(imovel, conta, faturamentoGrupo);
    }
    
    @Test
    public void testBuildMovimentoDebitoAutomatico(){        
        DebitoAutomaticoMovimento retorno = bo.buildMovimentoDebitoAutomatico(conta, debitoAutomatico, faturamentoGrupo);
        
        assertEquals(movimento.getContaGeral().getId(), retorno.getContaGeral().getId());
        assertEquals(movimento.getFaturamentoGrupo().getId(), retorno.getFaturamentoGrupo().getId());
        assertEquals(movimento.getDebitoAutomatico().getImovel().getId(), retorno.getDebitoAutomatico().getImovel().getId());

        Calendar c01 = Calendar.getInstance();
        c01.setTime(movimento.getDataVencimento());

        Calendar c02 = Calendar.getInstance();
        c02.setTime(retorno.getDataVencimento());
        
        assertEquals(c01.get(Calendar.MONTH)       , c02.get(Calendar.MONTH));
        assertEquals(c01.get(Calendar.DAY_OF_MONTH), c02.get(Calendar.DAY_OF_MONTH));
        assertEquals(c01.get(Calendar.YEAR)        , c02.get(Calendar.YEAR));
    }

    private void mockImovelSemDebitoAutomatico() {
        expect(debitoAutomaticoRepositorioMock.obterDebitoAutomaticoPorImovel(imovel.getId())).andReturn(null);
        replay(debitoAutomaticoRepositorioMock);
    }
   
    private void mockImovelComDebitoAutomatico() {
        expect(debitoAutomaticoRepositorioMock.obterDebitoAutomaticoPorImovel(imovel.getId())).andReturn(debitoAutomatico);
        replay(debitoAutomaticoRepositorioMock);
    }    
}
