package br.gov.batch.servicos.faturamento;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.servicos.cobranca.parcelamento.ParcelamentoImovelBO;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.servicos.arrecadacao.pagamento.GuiaPagamentoRepositorio;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.ContaRepositorio;
import br.gov.servicos.to.ConsultaDebitoImovelTO;
import br.gov.servicos.to.ContaTO;

@RunWith(EasyMockRunner.class)
public class DebitoImovelBOTest {

    @TestSubject
    private DebitoImovelBO bo;

    @Mock
    private SistemaParametrosRepositorio sistemaParametrosRepositorio;
    
    @Mock
    private GuiaPagamentoRepositorio guiaPagamentoRepositorio;
    
    @Mock
    private ContaRepositorio contaRepositorio;
    
    @Mock
    private ParcelamentoImovelBO parcelamentoImovelBO;
    
    ConsultaDebitoImovelTO to;
    
    SistemaParametros parametros;
    
    
    @Before
    public void init(){
        to = new ConsultaDebitoImovelTO();
        to.setIdImovel(1);
        bo = new DebitoImovelBO();
        parametros = new SistemaParametros();
        parametros.setAnoMesArrecadacao(201412);
    }
    
    @Test
    public void apenasContasSemParcelamento(){
        mockPesquisaDeContas(listaContasSemParcelamento());
        assertEquals(2, bo.pesquisarContasDebitoImovel(to).size());
    }
    
    @Test
    public void contasParceladasMasNaoConfirmadas(){
        mockPesquisaDeContas(listaContasComParcelamento());
        mockImovelSemParcelamento(false);
        assertEquals(2, bo.pesquisarContasDebitoImovel(to).size());
    }
    
    @Test
    public void contasParceladasApenasUmaConfirmadas(){
        mockPesquisaDeContas(listaContasComParcelamento());
        mockImovelSemParcelamento(true);
        assertEquals(1, bo.pesquisarContasDebitoImovel(to).size());
    }

    private List<ContaTO> listaContasSemParcelamento(){
        List<ContaTO> contas = new ArrayList<ContaTO>();
        
        ContaTO contaTO = new ContaTO();
        contaTO.setSituacaoAtual(DebitoCreditoSituacao.NORMAL.getId());
        contas.add(contaTO);
        
        contaTO = new ContaTO();
        contaTO.setSituacaoAtual(DebitoCreditoSituacao.NORMAL.getId());
        contas.add(contaTO);
        
        return contas;
    }
    
    private List<ContaTO> listaContasComParcelamento(){
        List<ContaTO> contas = new ArrayList<ContaTO>();
        
        ContaTO contaTO = new ContaTO();
        contaTO.setSituacaoAtual(DebitoCreditoSituacao.PARCELADA.getId());
        contaTO.setIdImovel(1);
        contas.add(contaTO);
        
        contaTO = new ContaTO();
        contaTO.setSituacaoAtual(DebitoCreditoSituacao.NORMAL.getId());
        contaTO.setIdImovel(1);
        contas.add(contaTO);
        
        return contas;
    }
    
    private void mockPesquisaDeContas(List<ContaTO> contas){
        expect(contaRepositorio.pesquisarContasImovel(to)).andReturn(contas);
        replay(contaRepositorio);
    }
    
    
    private void mockImovelSemParcelamento(boolean possui){
        expect(parcelamentoImovelBO.imovelSemParcelamento(1)).andReturn(possui);
        replay(parcelamentoImovelBO);
    }
}
