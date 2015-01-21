package br.gov.batch.servicos.faturamento;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.servicos.cobranca.parcelamento.ParcelamentoImovelBO;
import br.gov.model.Status;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.servicos.arrecadacao.pagamento.GuiaPagamentoRepositorio;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.cobranca.ContratoParcelamentoItemRepositorio;
import br.gov.servicos.faturamento.ContaRepositorio;
import br.gov.servicos.to.ConsultaDebitoImovelTO;
import br.gov.servicos.to.ContaTO;
import br.gov.servicos.to.GuiaPagamentoTO;

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
    
    @Mock
    private ContratoParcelamentoItemRepositorio contratoParcelamentoItemRepositorio;
    
    ConsultaDebitoImovelTO to;
    
    SistemaParametros parametros;
    
    
    @Before
    public void init(){
        to = new ConsultaDebitoImovelTO();
        to.setIdImovel(1);
        Calendar cal = Calendar.getInstance();
        cal.set(2014, 0, 1);
        to.setVencimentoInicial(cal.getTime());
        cal.set(2014, 10, 1);
        to.setVencimentoFinal(cal.getTime());
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
    
    @Test
    public void naoExisteDebitoImovelPorqueNaoHaContasEGuias(){
        mockSistemaParametros(parametros);
        mockPesquisaDeContas(new ArrayList<ContaTO>());
        mockPesquisaDeGuias(new ArrayList<GuiaPagamentoTO>());
        assertFalse(bo.existeDebitoImovel(to));
    }

    @Test
    public void existeDebitoImovelPorqueHaContasMasNaoGuias(){
        mockSistemaParametros(parametros);
        mockPesquisaDeContas(listaContasUnitaria());
        mockPesquisaDeGuias(new ArrayList<GuiaPagamentoTO>());
        assertTrue(bo.existeDebitoImovel(to));
    }
    
    @Test
    public void existeDebitoImovelPorqueHaContasEGuias(){
        mockSistemaParametros(parametros);
        mockPesquisaDeContas(listaContasUnitaria());
        mockPesquisaDeGuias(listaGuiasUnitaria());
        assertTrue(bo.existeDebitoImovel(to));
    }
    
    @Test
    public void naoExisteDebitoImovelPorqueHaContasNaoHaGuiasEAsContasTemContratoParcelamentoAtivo(){
        parametros.setIndicadorBloqueioContasContratoParcelDebitos(Status.ATIVO.getId());
        mockSistemaParametros(parametros);
        mockPesquisaDeContas(listaContasUnitaria());
        mockPesquisaDeGuias(new ArrayList<GuiaPagamentoTO>());
        mockContratoParcelamentoParaConta(true);        
        assertFalse(bo.existeDebitoImovel(to));
    }
    
    @Test
    public void existeDebitoImovelPorqueHaContasNaoHaGuiasEAsContasNaoTemContratoParcelamentoAtivo(){
        parametros.setIndicadorBloqueioContasContratoParcelDebitos(Status.ATIVO.getId());
        mockSistemaParametros(parametros);
        mockPesquisaDeContas(listaContasUnitaria());
        mockPesquisaDeGuias(new ArrayList<GuiaPagamentoTO>());
        mockContratoParcelamentoParaConta(false);
        assertTrue(bo.existeDebitoImovel(to));
    }

    @Test
    public void naoExisteDebitoImovelPorqueNaoHaContasHaGuiasEAsGuiasTemContratoParcelamentoAtivo(){
        parametros.setIndicadorBloqueioGuiasOuAcresContratoParcelDebito(Status.ATIVO.getId());
        mockSistemaParametros(parametros);
        mockPesquisaDeContas(new ArrayList<ContaTO>());
        mockPesquisaDeGuias(listaGuiasUnitaria());
        mockContratoParcelamentoParaGuia(true);
        assertFalse(bo.existeDebitoImovel(to));
    }
    

    @Test
    public void existeDebitoImovelPorqueNaoHaContasHaGuiasEAsGuiasNaoTemContratoParcelamentoAtivo(){
        parametros.setIndicadorBloqueioGuiasOuAcresContratoParcelDebito(Status.ATIVO.getId());
        mockSistemaParametros(parametros);
        mockPesquisaDeContas(new ArrayList<ContaTO>());
        mockPesquisaDeGuias(listaGuiasUnitaria());
        mockContratoParcelamentoParaGuia(false);
        assertTrue(bo.existeDebitoImovel(to));
    }
    
    @Test
    public void existeDebitoImovelPorqueHaContasHaGuiasEAmbasTemContratoParcelamentoAtivo(){
        parametros.setIndicadorBloqueioGuiasOuAcresContratoParcelDebito(Status.ATIVO.getId());
        parametros.setIndicadorBloqueioContasContratoParcelDebitos(Status.ATIVO.getId());
        mockSistemaParametros(parametros);
        mockPesquisaDeContas(listaContasUnitaria());
        mockPesquisaDeGuias(listaGuiasUnitaria());
        mockContratoParcelamentoParaGuiaEConta(false);
        assertTrue(bo.existeDebitoImovel(to));
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
    
    private List<ContaTO> listaContasUnitaria(){
        List<ContaTO> contas = new ArrayList<ContaTO>();
        
        ContaTO contaTO = new ContaTO();
        contaTO.setIdConta(1);
        contaTO.setSituacaoAtual(DebitoCreditoSituacao.NORMAL.getId());
        contaTO.setIdImovel(1);
        contas.add(contaTO);
        
        return contas;
    }
    
    private List<GuiaPagamentoTO> listaGuiasUnitaria(){
        List<GuiaPagamentoTO> lista = new ArrayList<GuiaPagamentoTO>();
        
        GuiaPagamentoTO to = new GuiaPagamentoTO();
        to.setIdGuia(1);
        lista.add(to);
        
        return lista;
    }
    
    private void mockPesquisaDeContas(List<ContaTO> contas){
        expect(contaRepositorio.pesquisarContasImovel(to)).andReturn(contas);
        replay(contaRepositorio);
    }
    
    private void mockImovelSemParcelamento(boolean possui){
        expect(parcelamentoImovelBO.imovelSemParcelamento(1)).andReturn(possui);
        replay(parcelamentoImovelBO);
    }
    
    private void mockSistemaParametros(SistemaParametros parametros){
        expect(sistemaParametrosRepositorio.getSistemaParametros()).andReturn(parametros);
        replay(sistemaParametrosRepositorio);
    }
    
    private void mockContratoParcelamentoParaConta(boolean ativo){
        expect(contratoParcelamentoItemRepositorio.existeContratoParcelamentoAtivoParaConta(1)).andReturn(ativo);
        replay(contratoParcelamentoItemRepositorio);
    }
    
    private void mockContratoParcelamentoParaGuia(boolean ativo){
        expect(contratoParcelamentoItemRepositorio.existeContratoParcelamentoAtivoParaGuia(1)).andReturn(ativo);
        replay(contratoParcelamentoItemRepositorio);
    }
    
    private void mockContratoParcelamentoParaGuiaEConta(boolean ativo){
        expect(contratoParcelamentoItemRepositorio.existeContratoParcelamentoAtivoParaConta(1)).andReturn(ativo);
        expect(contratoParcelamentoItemRepositorio.existeContratoParcelamentoAtivoParaGuia(1)).andReturn(ativo);
        replay(contratoParcelamentoItemRepositorio);
    }
    
    private void mockPesquisaDeGuias(List<GuiaPagamentoTO> guias){
        expect(guiaPagamentoRepositorio.pesquisarGuiasPagamentoImovel(1, to.getVencimentoInicial(), to.getVencimentoFinal())).andReturn(guias);
        replay(guiaPagamentoRepositorio);
    }
}
