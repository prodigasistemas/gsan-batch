package br.gov.batch.servicos.cobranca.parcelamento;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

public class ParcelamentoImovelBOTest {

    @InjectMocks
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
        
        MockitoAnnotations.initMocks(this);
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
        when(parcelamentoRepositorio.pesquisaParcelamento(1, 201412, ParcelamentoSituacao.NORMAL)).thenReturn(parcelamento);
    }
    
    private void mockGuiasPagas(Integer idGuia){
        when(pagamentoRepositorio.guiaPaga(1)).thenReturn(true);
    }
    
    private void mockGuiasPendentes(Integer idGuia){
        when(pagamentoRepositorio.guiaPaga(1)).thenReturn(false);
    }
    
    private void mockSistemaParametros(){
        when(sistemaParametrosRepositorio.getSistemaParametros()).thenReturn(parametros);
    }
    
    private void mockGuiasParcelamento(GuiaPagamento guia){
        when(guiaPagamentoRepositorio.guiaDoParcelamento(1)).thenReturn(guia);
    }
    
    private void mockContasDeParcelamento(List<Conta> contas){
        when(contaRepositorio.recuperarPeloParcelamento(1)).thenReturn(contas);
    }
    
    private void mockContaPaga(Integer idConta){
        when(pagamentoRepositorio.contaPaga(1)).thenReturn(true);
    }
    
    private void mockContaNaoPaga(Integer idConta){
        when(pagamentoRepositorio.contaPaga(1)).thenReturn(false);
    }
}
