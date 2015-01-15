package br.gov.batch.servicos.faturamento;

import static br.gov.model.util.Utilitarios.reduzirMeses;

import java.util.Date;

import javax.ejb.EJB;

import br.gov.batch.servicos.cadastro.ImovelSubcategoriaBO;
import br.gov.batch.servicos.micromedicao.ConsumoAnormalidadeAcaoBO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.ContaMensagem;
import br.gov.model.faturamento.TipoConta;
import br.gov.model.micromedicao.ConsumoAnormalidadeAcao;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.arrecadacao.DebitoAutomaticoRepositorio;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.ContaMensagemRepositorio;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;
import br.gov.servicos.to.AnormalidadeHistoricoConsumo;
import br.gov.servicos.to.ConsultaDebitoImovelTO;
import br.gov.servicos.to.DadosBancariosTO;

public class MensagemContaBO {
    
    @EJB
    private SistemaParametrosRepositorio sistemaParametrosRepositorio;
    
    @EJB
    private DebitoImovelBO debitoImovelBO;

    @EJB
    private ContaMensagemRepositorio contaMensagemRepositorio;
    
    @EJB
    private ConsumoHistoricoRepositorio consumoHistoricoRepositorio;
    
    @EJB
    private ImovelSubcategoriaBO imovelSubcategoriaBO;
    
    @EJB
    private ConsumoAnormalidadeAcaoBO consumoAnormalidadeAcaoBO;
    
    @EJB
    private DebitoAutomaticoRepositorio debitoAutomaticoRepositorio;
    
    public String[] obterMensagemConta3Partes(Imovel imovel, Integer anoMesReferencia){
        SistemaParametros sistemaParametros  = sistemaParametrosRepositorio.getSistemaParametros();
        
        Integer anoMesFaturamento = sistemaParametros.getAnoMesFaturamento();
        Integer anoMesArrecadacao = sistemaParametros.getAnoMesArrecadacao();
        Integer anoMesAnterior    = Utilitarios.reduzirMeses(anoMesArrecadacao, 1);
        Date vencimentoFinal      = Utilitarios.converteParaDataComUltimoDiaMes(anoMesAnterior);

        ConsultaDebitoImovelTO consultaTO = new ConsultaDebitoImovelTO();
        consultaTO.setIdImovel(imovel.getId());
        consultaTO.setReferenciaInicial(190001);
        consultaTO.setReferenciaFinal(Utilitarios.reduzirMeses(anoMesFaturamento, 1));
        consultaTO.setVencimentoInicial(Utilitarios.ano1900());
        consultaTO.setVencimentoFinal(vencimentoFinal);

        String[] mensagem = new String[]{"", "", ""};
        if (debitoImovelBO.existeDebitoImovel(consultaTO)){
            String dataVencimentoFinalString = Utilitarios.formataData(vencimentoFinal);
            mensagem[0] = "SR. USUÁRIO: EM  " + dataVencimentoFinalString + ",    REGISTRAMOS QUE V.SA. ESTAVA EM DEBITO COM A "
                    + sistemaParametros.getNomeAbreviadoEmpresa() + ".";
            mensagem[1] = "COMPARECA A UM DOS NOSSOS POSTOS DE ATENDIMENTO PARA REGULARIZAR SUA SITUACAO.EVITE O CORTE.";
            mensagem[2] = "CASO O SEU DEBITO TENHA SIDO PAGO APOS A DATA INDICADA,DESCONSIDERE ESTE AVISO.";
        }else{
            
            String[] complemento = this.complementoMensagem(imovel, anoMesReferencia, anoMesFaturamento);
            
            mensagem[0] = complemento[0];
            mensagem[1] = complemento[1];
            mensagem[2] = complemento[2];
        }
        
        return mensagem;
    }
    
    public String[] obterMensagemConta(Imovel imovel, Integer anoMesReferencia, Integer idImovelPerfil, TipoConta tipoConta) {
        SistemaParametros sistemaParametros  = sistemaParametrosRepositorio.getSistemaParametros();

        String[] mensagem = obterMensagemAnormalidadeConsumo(imovel.getId(), anoMesReferencia, idImovelPerfil);


        if (mensagem == null) {
            mensagem = new String[]{"","","","","","",""};

            Integer anoMesFaturamento = sistemaParametros.getAnoMesFaturamento();
            Integer anoMesArrecadacao = sistemaParametros.getAnoMesArrecadacao();
            Integer anoMesArrecadacaoAnterior    = Utilitarios.reduzirMeses(anoMesArrecadacao, 1);
            Date vencimentoFinal = Utilitarios.converteParaDataComUltimoDiaMes(anoMesArrecadacaoAnterior);

            ConsultaDebitoImovelTO consultaTO = new ConsultaDebitoImovelTO();
            consultaTO.setIdImovel(imovel.getId());
            consultaTO.setReferenciaInicial(190001);
            consultaTO.setReferenciaFinal(Utilitarios.reduzirMeses(anoMesFaturamento, 1));
            consultaTO.setVencimentoInicial(Utilitarios.ano1900());
            consultaTO.setVencimentoFinal(vencimentoFinal);

            if (debitoImovelBO.existeDebitoImovel(consultaTO)){
                String dataVencimentoFinalString = Utilitarios.formataData(vencimentoFinal);

                mensagem[0] = "AVISO:EM " + dataVencimentoFinalString + " CONSTA DÉBITO SUJ.CORT. IGNORE CASO PAGO";
            }
            
            if (tipoConta == TipoConta.CONTA_DEBITO_AUTOMATICO){
                mensagem[1] = mensagemDebitoAutomatico(imovel.getId());
            }
            
            String[] complemento = complementoMensagem(imovel, anoMesReferencia, anoMesFaturamento);
            
            mensagem[2] = complemento[0];
            mensagem[3] = complemento[1];
            mensagem[4] = complemento[2];
        }

        return mensagem;
    }
    
    private String mensagemDebitoAutomatico(Integer idImovel) {
        StringBuilder msg = new StringBuilder();

        DadosBancariosTO dados = debitoAutomaticoRepositorio.dadosBancarios(idImovel);

        if (dados != null) {
            msg.append("DEBITAR NO BANCO ")
            .append(dados.getIdBanco() != null ? String.valueOf(dados.getIdBanco()) : "")
            .append("/")
            .append(dados.getCodigoAgencia() != null ? String.valueOf(dados.getCodigoAgencia()) : "")
            .append("/")
            .append(dados.getIdentificacaoClienteBanco() != null ? String.valueOf(dados.getIdentificacaoClienteBanco()) : "");
        }
        
        return msg.toString();
    }

    public String[] obterMensagemAnormalidadeConsumo(Integer idImovel, Integer anoMesReferencia, Integer idImovelPerfil) {

        String[] mensagemConta = null;
        
        AnormalidadeHistoricoConsumo anormalidadeConsumo = 
                consumoHistoricoRepositorio.anormalidadeHistoricoConsumo(idImovel, LigacaoTipo.AGUA, anoMesReferencia);
        
        if (anormalidadeConsumo == null){
            anormalidadeConsumo = consumoHistoricoRepositorio.anormalidadeHistoricoConsumo(idImovel, LigacaoTipo.ESGOTO, anoMesReferencia);
        }
        
        if (anormalidadeConsumo != null && anormalidadeConsumo.anormalidadeporBaixoAltoOuEstouroConsumo()){
            Integer idCategoria = imovelSubcategoriaBO.buscaIdCategoriaComMaisEconomias(idImovel);
            
            ConsumoAnormalidadeAcao acao = consumoAnormalidadeAcaoBO.acaoASerTomada(anormalidadeConsumo.getIdAnormalidade(), idCategoria, idImovelPerfil);
            
            if (acao != null){
                anormalidadeConsumo = consumoHistoricoRepositorio.anormalidadeHistoricoConsumo(idImovel, LigacaoTipo.ESGOTO, reduzirMeses(anoMesReferencia, 1));
                
                String mensagemContaAnormalidade = "";
                
                if (anormalidadeConsumo == null){
                    mensagemContaAnormalidade = acao.getDescricaoContaMensagemMes1();
                }else{
                    anormalidadeConsumo = consumoHistoricoRepositorio
                            .anormalidadeHistoricoConsumo(idImovel, LigacaoTipo.ESGOTO, reduzirMeses(anoMesReferencia, 1), anormalidadeConsumo.getIdAnormalidade());
                    
                    if (anormalidadeConsumo == null) {
                        mensagemContaAnormalidade = acao.getDescricaoContaMensagemMes2();
                    } else {
                        mensagemContaAnormalidade = acao.getDescricaoContaMensagemMes3();
                    }
                }
                
                mensagemConta = quebraMensagemEmDuasPartes(mensagemContaAnormalidade); 
            }
        }
        
        return mensagemConta;
    }
    
    public String[] complementoMensagem(Imovel imovel, Integer anoMesReferencia, Integer anoMesFaturamento){
        Integer idGerenciaRegional = imovel.getLocalidade().getGerenciaRegional().getId();
        Integer idLocalidade       = imovel.getLocalidade().getId();
        Integer idSetorComercial   = imovel.getSetorComercial().getId();
        ContaMensagem contaMensagem = contaMensagemRepositorio.recuperaMensagemConta(anoMesReferencia, null, idGerenciaRegional, idLocalidade, idSetorComercial);
        
        String mensagem[] = new String[]{"", "", ""};
        if (contaMensagem == null){
            contaMensagem = contaMensagemRepositorio.recuperaMensagemConta(anoMesReferencia, null, idGerenciaRegional, idLocalidade, null);
        }
        
        if (contaMensagem == null){
            contaMensagem = contaMensagemRepositorio.recuperaMensagemConta(anoMesReferencia, null, idGerenciaRegional, null, null);
        }
        
        if (contaMensagem == null){
            contaMensagem = contaMensagemRepositorio.recuperaMensagemConta(anoMesReferencia, anoMesFaturamento, null, null, null);
        }
        
        if (contaMensagem == null){
            contaMensagem = contaMensagemRepositorio.recuperaMensagemConta(anoMesReferencia, null, null, null, null);
        }
        
        if (contaMensagem != null){
            mensagem[0] = contaMensagem.getDescricaoContaMensagem01();
            mensagem[1] = contaMensagem.getDescricaoContaMensagem02();
            mensagem[2] = contaMensagem.getDescricaoContaMensagem03();
        }
        
        return mensagem;
    }

    private String[] quebraMensagemEmDuasPartes(String mensagemContaAnormalidade) {
        String[] msg = new String[]{"", ""};
        
        if (mensagemContaAnormalidade.length() < 60) {
            msg[0] = mensagemContaAnormalidade;
        } else {
            msg[0] = mensagemContaAnormalidade.substring(0, 60);
            msg[1] = mensagemContaAnormalidade.substring(60);
        }
        
        return msg;
    }    
}
