package br.gov.batch.servicos.faturamento;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.model.cadastro.GerenciaRegional;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.Localidade;
import br.gov.model.cadastro.SetorComercial;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.ContaMensagem;
import br.gov.model.faturamento.TipoConta;
import br.gov.servicos.arrecadacao.DebitoAutomaticoRepositorio;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.ContaMensagemRepositorio;
import br.gov.servicos.to.ConsultaDebitoImovelTO;
import br.gov.servicos.to.DadosBancariosTO;

public class MensagemContaTest {

    @InjectMocks
    private MensagemContaBO bo;
    
    @Mock
    private DebitoImovelBO debitoImovelBO;

    @Mock
    private SistemaParametrosRepositorio sistemaParametrosRepositorio;
    
    @Mock
    private ContaMensagemRepositorio contaMensagemRepositorio;
    
    @Mock
    private MensagemAnormalidadeContaBO mensagemAnormalidadeContaBO;

    @Mock
    private DebitoAutomaticoRepositorio debitoAutomaticoRepositorio;
    
    String[] msg;
    
    String[] msgAnormalidade;

    String dataVencimentoFinal = "31/01/2015";
    
    SistemaParametros sistemaParametros;
    
    ConsultaDebitoImovelTO to;
    
    Imovel imovel;
    
    Integer anoMesReferencia;
    
    ContaMensagem contaMensagem;
    
    Integer idImovelPerfil;
    
    TipoConta tipoConta;
    
    StringBuilder mensagemDebitoAutomatico = null;
    
    String msgAvisoDebitoVencimento = "";
    
    DadosBancariosTO dadosBancarios;
    
    @Before
    public void init(){
        anoMesReferencia = 201501;
        
        to = new ConsultaDebitoImovelTO();
        to.setIdImovel(1);
        
        imovel = new Imovel();
        imovel.setId(1);
        imovel.setLocalidade(new Localidade());
        imovel.getLocalidade().setGerenciaRegional(new GerenciaRegional());
        imovel.setSetorComercial(new SetorComercial());
        
        sistemaParametros = new SistemaParametros();
        sistemaParametros.setNomeAbreviadoEmpresa("EMP");
        sistemaParametros.setAnoMesFaturamento(201501);
        sistemaParametros.setAnoMesArrecadacao(201502);
        
        dadosBancarios = new DadosBancariosTO();
        dadosBancarios.setCodigoAgencia("001");
        dadosBancarios.setDescricaoBanco("BB");
        dadosBancarios.setIdBanco(1);
        dadosBancarios.setIdentificacaoClienteBanco("PRIME");
        
        msg    = new String[3];
        
        msg[0] = "SR. USUARIO: EM  " + dataVencimentoFinal + ",    REGISTRAMOS QUE V.SA. ESTAVA EM DEBITO COM A "
                + sistemaParametros.getNomeAbreviadoEmpresa() + ".";
        msg[1] = "COMPARECA A UM DOS NOSSOS POSTOS DE ATENDIMENTO PARA REGULARIZAR SUA SITUACAO.EVITE O CORTE.";
        msg[2] = "CASO O SEU DEBITO TENHA SIDO PAGO APOS A DATA INDICADA,DESCONSIDERE ESTE AVISO.";
        
        msgAnormalidade = new String[3];
        msgAnormalidade[0] = "ANORMALIDADE MSG PARTE01";
        msgAnormalidade[1] = "ANORMALIDADE MSG PARTE02";
        msgAnormalidade[2] = "ANORMALIDADE MSG PARTE03";
        
        contaMensagem = new ContaMensagem();
        contaMensagem.setDescricaoContaMensagem01("PARTE COMPLEMENTO 01");
        contaMensagem.setDescricaoContaMensagem02("PARTE COMPLEMENTO 02");
        contaMensagem.setDescricaoContaMensagem03("PARTE COMPLEMENTO 03");
        
        mensagemDebitoAutomatico = new StringBuilder();
        mensagemDebitoAutomatico.append("DEBITAR NO BANCO ")
        .append(dadosBancarios.getIdBanco()).append("/")
        .append(dadosBancarios.getCodigoAgencia()).append("/")
        .append(dadosBancarios.getIdentificacaoClienteBanco());
        
        msgAvisoDebitoVencimento = "AVISO:EM " + dataVencimentoFinal + " CONSTA DEBITO SUJ.CORT. IGNORE CASO PAGO";
        
        bo = new MensagemContaBO();
        
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void mensagemTresPartesParaImovelComDebito(){
        mockExisteDebito(true);
        mockSistemaParametros(sistemaParametros);
        String[] retorno = bo.obterMensagemConta3Partes(imovel, anoMesReferencia, null);
        assertEquals(msg[0], retorno[0]);
        assertEquals(msg[1], retorno[1]);
        assertEquals(msg[2], retorno[2]);
    }
    
    @Test
    public void mensagemTresPartesParaImovelSemDebito(){
        mockExisteDebito(false);
        mockSistemaParametros(sistemaParametros);
        mockRecuperaMensagemConta();
        String[] retorno = bo.obterMensagemConta3Partes(imovel, anoMesReferencia, null);
        assertEquals(contaMensagem.getDescricaoContaMensagem01(), retorno[0]);
        assertEquals(contaMensagem.getDescricaoContaMensagem02(), retorno[1]);
        assertEquals(contaMensagem.getDescricaoContaMensagem03(), retorno[2]);
    }
    
    @Test
    public void mensagemComAnormalidade(){
        mockSistemaParametros(sistemaParametros);
        mockMensagemAnormalidade(msgAnormalidade);
        String[] retorno = bo.obterMensagemConta(imovel, anoMesReferencia, TipoConta.CONTA_CLIENTE_RESPONSAVEL);
        assertEquals(msgAnormalidade[0], retorno[0]);
        assertEquals(msgAnormalidade[1], retorno[1]);
        assertEquals(msgAnormalidade[2], retorno[2]);
    }
    
    @Test
    public void mensagemSemAnormalidadeImovelComDebito(){
        mockSistemaParametros(sistemaParametros);
        mockMensagemAnormalidade(null);
        mockExisteDebito(true);
        mockRecuperaMensagemConta();
        String[] retorno = bo.obterMensagemConta(imovel, anoMesReferencia, TipoConta.CONTA_CLIENTE_RESPONSAVEL);
        assertEquals(msgAvisoDebitoVencimento, retorno[0]);
    }
    
    @Test
    public void mensagemSemAnormalidadeImovelComDebitoEDebitoAutomarico(){
        mockSistemaParametros(sistemaParametros);
        mockMensagemAnormalidade(null);
        mockExisteDebito(true);
        mockRecuperaMensagemConta();
        mockDadosBancarios();
        String[] retorno = bo.obterMensagemConta(imovel, anoMesReferencia, TipoConta.CONTA_DEBITO_AUTOMATICO);
        assertEquals(msgAvisoDebitoVencimento, retorno[0]);
        assertEquals(mensagemDebitoAutomatico.toString(), retorno[1]);
    }
    
    public void mockExisteDebito(boolean existe){
        when(debitoImovelBO.existeDebitoImovel(to)).thenReturn(existe);
    }

    private void mockSistemaParametros(SistemaParametros parametros){
        when(sistemaParametrosRepositorio.getSistemaParametros()).thenReturn(parametros);
    }
    
    private void mockRecuperaMensagemConta(){
        when(contaMensagemRepositorio.recuperaMensagemConta(anoMesReferencia, null, null, null, null)).thenReturn(contaMensagem);
    }
    
    private void mockMensagemAnormalidade(String[] msg){
        when(mensagemAnormalidadeContaBO.obterMensagemAnormalidadeConsumo(imovel, anoMesReferencia)).thenReturn(msg);
    }
    
    private void mockDadosBancarios(){
        when(debitoAutomaticoRepositorio.dadosBancarios(imovel.getId())).thenReturn(dadosBancarios);
    }
}