package br.gov.batch.servicos.faturamento;

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.ContaMensagem;
import br.gov.model.faturamento.TipoConta;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.arrecadacao.DebitoAutomaticoRepositorio;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.ContaMensagemRepositorio;
import br.gov.servicos.to.ConsultaDebitoImovelTO;
import br.gov.servicos.to.DadosBancariosTO;

@Stateless
public class MensagemContaBO {
    
    @EJB
    private SistemaParametrosRepositorio sistemaParametrosRepositorio;
    
    @EJB
    private DebitoImovelBO debitoImovelBO;

    @EJB
    private ContaMensagemRepositorio contaMensagemRepositorio;
    
    @EJB
    private DebitoAutomaticoRepositorio debitoAutomaticoRepositorio;
    
    @EJB
    private MensagemAnormalidadeContaBO mensagemAnormalidadeContaBO;
    
    public String[] obterMensagemConta3Partes(Imovel imovel, Integer anoMesReferencia, Integer idFaturamentoGrupo){
        SistemaParametros parametros  = sistemaParametrosRepositorio.getSistemaParametros();
        
        Integer anoMesFaturamento = parametros.getAnoMesFaturamento();

        ConsultaDebitoImovelTO consultaTO = montaConsultaDebitoImovel(imovel.getId(), anoMesFaturamento, parametros.getAnoMesArrecadacao());

        String[] mensagem = new String[]{"", "", ""};
        if (debitoImovelBO.existeDebitoImovel(consultaTO)){
            String dataVencimentoFinalString = Utilitarios.formataData(consultaTO.getVencimentoFinal());
            mensagem[0] = "SR. USUÁRIO: EM  " + dataVencimentoFinalString + ",    REGISTRAMOS QUE V.SA. ESTAVA EM DEBITO COM A "
                    + parametros.getNomeAbreviadoEmpresa() + ".";
            mensagem[1] = "COMPARECA A UM DOS NOSSOS POSTOS DE ATENDIMENTO PARA REGULARIZAR SUA SITUACAO.EVITE O CORTE.";
            mensagem[2] = "CASO O SEU DEBITO TENHA SIDO PAGO APOS A DATA INDICADA,DESCONSIDERE ESTE AVISO.";
        }else{
            
            String[] complemento = this.complementoMensagem(imovel, anoMesReferencia, idFaturamentoGrupo);
            
            mensagem[0] = complemento[0];
            mensagem[1] = complemento[1];
            mensagem[2] = complemento[2];
        }
        
        return mensagem;
    }
    
    public String[] obterMensagemConta(Imovel imovel, Integer anoMesReferencia, TipoConta tipoConta) {
        SistemaParametros sistemaParametros  = sistemaParametrosRepositorio.getSistemaParametros();

        String[] mensagem = mensagemAnormalidadeContaBO.obterMensagemAnormalidadeConsumo(imovel, anoMesReferencia);


        if (mensagem == null) {
            mensagem = new String[]{"","","","","","",""};

            Integer anoMesFaturamento = sistemaParametros.getAnoMesFaturamento();
            Integer anoMesArrecadacao = sistemaParametros.getAnoMesArrecadacao();

            ConsultaDebitoImovelTO consultaTO = montaConsultaDebitoImovel(imovel.getId(), anoMesFaturamento, anoMesArrecadacao);

            if (debitoImovelBO.existeDebitoImovel(consultaTO)){
                String dataVencimentoFinalString = Utilitarios.formataData(consultaTO.getVencimentoFinal());

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
    
    public String[] complementoMensagem(Imovel imovel, Integer anoMesReferencia, Integer idFaturamentoGrupo){
        Integer idGerenciaRegional = imovel.getLocalidade().getGerenciaRegional().getId();
        Integer idLocalidade       = imovel.getLocalidade().getId();
        Integer idSetorComercial   = imovel.getSetorComercial().getId();
        ContaMensagem contaMensagem = contaMensagemRepositorio.recuperaMensagemConta(anoMesReferencia, null, idGerenciaRegional, idLocalidade, idSetorComercial);
        
        if (contaMensagem == null){
            contaMensagem = contaMensagemRepositorio.recuperaMensagemConta(anoMesReferencia, idFaturamentoGrupo, null, null, null);
        }
        
        String mensagem[] = new String[]{"", "", ""};
        if (contaMensagem != null){
            mensagem[0] = contaMensagem.getDescricaoContaMensagem01();
            mensagem[1] = contaMensagem.getDescricaoContaMensagem02();
            mensagem[2] = contaMensagem.getDescricaoContaMensagem03();
        }
        
        return mensagem;
    }

    private ConsultaDebitoImovelTO montaConsultaDebitoImovel(Integer idImovel, Integer anoMesFaturamento, Integer anoMesArrecadacao){
        Integer anoMesAnterior    = Utilitarios.reduzirMeses(anoMesArrecadacao, 1);
        Date vencimentoFinal      = Utilitarios.converteParaDataComUltimoDiaMes(anoMesAnterior);

        ConsultaDebitoImovelTO consultaTO = new ConsultaDebitoImovelTO();
        consultaTO.setIdImovel(idImovel);
        consultaTO.setReferenciaInicial(190001);
        consultaTO.setReferenciaFinal(Utilitarios.reduzirMeses(anoMesFaturamento, 1));
        consultaTO.setVencimentoInicial(Utilitarios.ano1900());
        consultaTO.setVencimentoFinal(vencimentoFinal);
                
        return consultaTO;
    }    
}
