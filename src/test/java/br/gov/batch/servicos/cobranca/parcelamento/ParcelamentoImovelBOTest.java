package br.gov.batch.servicos.cobranca.parcelamento;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.cobranca.Parcelamento;
import br.gov.model.cobranca.parcelamento.ParcelamentoSituacao;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.GuiaPagamento;
import br.gov.servicos.arrecadacao.pagamento.GuiaPagamentoRepositorio;
import br.gov.servicos.arrecadacao.pagamento.PagamentoRepositorio;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.cobranca.parcelamento.ParcelamentoRepositorio;
import br.gov.servicos.faturamento.ContaRepositorio;
import br.gov.servicos.to.ConsultaDebitoImovelTO;

@RunWith(EasyMockRunner.class)
public class ParcelamentoImovelBOTest {

    @TestSubject
    private ParcelamentoImovelBO bo;

    @Mock
    private ContaRepositorio contaRepositorio;
    
    @Mock
    private ParcelamentoRepositorio parcelamentoRepositorio;
    
    @Mock
    private SistemaParametrosRepositorio sistemaParametrosRepositorio;
    
    @Mock
    private GuiaPagamentoRepositorio guiaPagamentoRepositorio;
    
    @Mock
    private PagamentoRepositorio pagamentoRepositorio;
    
    ConsultaDebitoImovelTO to;
    
    SistemaParametros parametros;
    
    
    @Before
    public void init(){
        to = new ConsultaDebitoImovelTO();
        bo = new ParcelamentoImovelBO();
        parametros = new SistemaParametros();
        parametros.setAnoMesArrecadacao(201412);
    }
    
    @Test
    public void imovelSemParcelamento(){
        mockPesquisaParcelamento(null);
        mockSistemaParametros();
        assertTrue(bo.imovelSemParcelamento(1));
    }
    
    @Test
    public void imovelSemParcelamentoPorqueNaoHaEntrada(){
        mockPesquisaParcelamento(parcelamentoSemEntrada());
        mockSistemaParametros();
        assertTrue(bo.imovelSemParcelamento(1));
    }
    
    @Test
    public void imovelSemParcelamentoPorqueAGuiaFoiPaga(){
        mockSistemaParametros();
        mockPesquisaParcelamento(parcelamentoComEntrada());
        mockGuiasParcelamento(guia());
        mockGuiasPagas(1);
        assertTrue(bo.imovelSemParcelamento(1));
    }
    
    @Test
    public void imovelComParcelamentoPorqueAGuiaFoiPaga(){
        mockSistemaParametros();
        mockPesquisaParcelamento(parcelamentoComEntrada());
        mockGuiasParcelamento(guia());
        mockGuiasPendentes(1);
        assertFalse(bo.imovelSemParcelamento(1));
    }
    
    @Test
    public void imovelSemParcelamentoPorqueNaoHaGuiasEContas(){
        mockSistemaParametros();
        mockPesquisaParcelamento(parcelamentoComEntrada());
        mockGuiasParcelamento(null);
        mockContasDeParcelamento(listaContasVazia());
        assertTrue(bo.imovelSemParcelamento(1));
    }
    
    @Test
    public void imovelSemParcelamentoPorqueNaoHaGuiasETodasContasForamPagas(){
        mockSistemaParametros();
        mockPesquisaParcelamento(parcelamentoComEntrada());
        mockGuiasParcelamento(null);
        mockContasDeParcelamento(listaContasUnitaria());
        mockContaPaga(1);
        assertTrue(bo.imovelSemParcelamento(1));
    }
    
    @Test
    public void imovelEmParcelamentoPorqueNaoHaGuiasENemTodasContasForamPagas(){
        mockSistemaParametros();
        mockPesquisaParcelamento(parcelamentoComEntrada());
        mockGuiasParcelamento(null);
        mockContasDeParcelamento(listaContasUnitaria());
        mockContaNaoPaga(1);
        assertFalse(bo.imovelSemParcelamento(1));
    }
    
    private List<Conta> listaContasVazia(){
        List<Conta> contas = new ArrayList<Conta>();
        return contas;
    }
    
    private List<Conta> listaContasUnitaria(){
        List<Conta> contas = new ArrayList<Conta>();
        Conta conta = new Conta();
        conta.setId(1);
        contas.add(conta);
        return contas;
    }
    
    private Parcelamento parcelamentoSemEntrada(){
        Parcelamento p = new Parcelamento();
        p.setId(1);
        return p;
    }
    
    private Parcelamento parcelamentoComEntrada(){
        Parcelamento p = new Parcelamento();
        p.setValorEntrada(BigDecimal.ONE);
        p.setId(1);
        return p;
    }
    
    private GuiaPagamento guia(){
        GuiaPagamento guia = new GuiaPagamento();
        guia.setId(1);
        return guia;
    }
    
    private void mockPesquisaParcelamento(Parcelamento parcelamento){
        expect(parcelamentoRepositorio.pesquisaParcelamento(1, 201412, ParcelamentoSituacao.NORMAL)).andReturn(parcelamento);
        replay(parcelamentoRepositorio);
    }
    
    private void mockGuiasPagas(Integer idGuia){
        expect(pagamentoRepositorio.guiaPaga(1)).andReturn(true);
        replay(pagamentoRepositorio);
    }
    
    private void mockGuiasPendentes(Integer idGuia){
        expect(pagamentoRepositorio.guiaPaga(1)).andReturn(false);
        replay(pagamentoRepositorio);
    }
    
    private void mockSistemaParametros(){
        expect(sistemaParametrosRepositorio.getSistemaParametros()).andReturn(parametros);
        replay(sistemaParametrosRepositorio);
    }
    
    private void mockGuiasParcelamento(GuiaPagamento guia){
        expect(guiaPagamentoRepositorio.guiaDoParcelamento(1)).andReturn(guia);
        replay(guiaPagamentoRepositorio);
    }
    
    private void mockContasDeParcelamento(List<Conta> contas){
        expect(contaRepositorio.recuperarPeloParcelamento(1)).andReturn(contas);
        replay(contaRepositorio);
    }
    
    private void mockContaPaga(Integer idConta){
        expect(pagamentoRepositorio.contaPaga(1)).andReturn(true);
        replay(pagamentoRepositorio);
    }
    
    private void mockContaNaoPaga(Integer idConta){
        expect(pagamentoRepositorio.contaPaga(1)).andReturn(false);
        replay(pagamentoRepositorio);
    }
}
