package br.gov.batch.servicos.faturamento;

import static br.gov.model.util.Utilitarios.reduzirMeses;

import java.util.Date;

import javax.ejb.EJB;

import br.gov.batch.servicos.cadastro.ImovelSubcategoriaBO;
import br.gov.batch.servicos.micromedicao.ConsumoAnormalidadeAcaoBO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.ContaMensagem;
import br.gov.model.micromedicao.ConsumoAnormalidadeAcao;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.ContaMensagemRepositorio;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;
import br.gov.servicos.to.AnormalidadeHistoricoConsumo;
import br.gov.servicos.to.ConsultaDebitoImovelTO;

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
            Integer idGerenciaRegional = imovel.getLocalidade().getGerenciaRegional().getId();
            Integer idLocalidade       = imovel.getLocalidade().getId();
            Integer idSetorComercial   = imovel.getSetorComercial().getId();
            ContaMensagem contaMensagem = contaMensagemRepositorio.recuperaMensagemConta(anoMesReferencia, null, idGerenciaRegional, idLocalidade, idSetorComercial);
            
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
        }
        
        return mensagem;
    }
    
//    public String[] obterMensagemConta(EmitirContaHelper emitirContaHelper,        SistemaParametro sistemaParametro, int tipoConta,
//            Collection<NacionalFeriado> colecaoNacionalFeriado) {
//
//        String[] mensagem = new String[7];
//
//        // mensagem da conta para a anormalidade de consumo (Baixo Consumo,Auto
//        // Consumo e Estouro de Consumo)
//
//        mensagem = obterMensagemAnormalidadeConsumo(emitirContaHelper);

//        if (mensagem == null || mensagem.equals("")) {
//
//            mensagem = new String[7];
//
//            Integer anoMesReferenciaFinal = sistemaParametro
//                    .getAnoMesFaturamento();
//            int anoMesSubtraido = Util.subtrairMesDoAnoMes(
//                    anoMesReferenciaFinal, 1);
//            Integer dataVencimentoFinalInteger = sistemaParametro
//                    .getAnoMesArrecadacao();
//            String anoMesSubtraidoString = ""
//                    + Util.subtrairMesDoAnoMes(dataVencimentoFinalInteger, 1);
//            int ano = Integer.parseInt(anoMesSubtraidoString.substring(0, 4));
//            int mes = Integer.parseInt(anoMesSubtraidoString.substring(4, 6));
//
//            // recupera o ultimo dia do anomes e passa a data como parametro
//            Calendar dataVencimentoFinal = GregorianCalendar.getInstance();
//            dataVencimentoFinal.set(Calendar.YEAR, ano);
//            dataVencimentoFinal.set(Calendar.MONTH, (mes - 1));
//            dataVencimentoFinal
//                    .set(Calendar.DAY_OF_MONTH, dataVencimentoFinal
//                            .getActualMaximum(Calendar.DAY_OF_MONTH));
//
//            Date dataFinalDate = dataVencimentoFinal.getTime();
//
//            // converte String em data
//            Date dataVencimento = Util.converteStringParaDate("01/01/1900");
//
//            ObterDebitoImovelOuClienteHelper debitoImovelClienteHelper = getControladorCobranca()
//                    .obterDebitoImovelOuCliente(1,
//                            "" + emitirContaHelper.getIdImovel(), null, null,
//                            "190001", "" + anoMesSubtraido, dataVencimento,
//                            dataFinalDate, 1, 2, 2, 2, 2, 1, 2, null);
//
//            // se o imovel possua débito(debitoImovelCobrança for diferente de
//            // nulo)
//            if (debitoImovelClienteHelper != null
//                    && ((debitoImovelClienteHelper
//                            .getColecaoGuiasPagamentoValores() != null && !debitoImovelClienteHelper
//                            .getColecaoGuiasPagamentoValores().isEmpty()) || (debitoImovelClienteHelper
//                            .getColecaoContasValores() != null && !debitoImovelClienteHelper
//                            .getColecaoContasValores().isEmpty()))) {
//
//                String dataVencimentoFinalString = Util
//                        .formatarData(dataFinalDate);
//
//                mensagem[0] = "AVISO:EM " + dataVencimentoFinalString
//                        + " CONSTA DÉBITO SUJ.CORT. IGNORE CASO PAGO";
//
//            } else {
//                mensagem[0] = "";
//            }
//
//            if (tipoConta == 4) {
//
//                StringBuilder msg = new StringBuilder();
//
//                Object[] parmsDebitoAutomatico = null;
//                try {
//                    parmsDebitoAutomatico = repositorioArrecadacao
//                            .pesquisarParmsDebitoAutomatico(emitirContaHelper
//                                    .getIdImovel());
//                } catch (ErroRepositorioException e) {
//                    sessionContext.setRollbackOnly();
//                    throw new ControladorException("erro.sistema", e);
//                }
//
//                String codigoAgencia = "";
//                String idBanco = "";
//                String indentificacaoBanco = "";
//                if (parmsDebitoAutomatico != null) {
//
//                    // codigo Agencia
//                    if (parmsDebitoAutomatico[1] != null) {
//                        codigoAgencia = ((String) parmsDebitoAutomatico[1]);
//                    }
//
//                    // id do banco
//                    if (parmsDebitoAutomatico[2] != null) {
//                        idBanco = ((Integer) parmsDebitoAutomatico[2])
//                                .toString();
//                    }
//
//                    // indentificacao do banco
//                    if (parmsDebitoAutomatico[3] != null) {
//                        indentificacaoBanco = ((String) parmsDebitoAutomatico[3]);
//                    }
//
//                    msg.append("DEBITAR NO BANCO ");
//                    msg.append(idBanco);
//                    msg.append("/");
//                    msg.append(codigoAgencia);
//                    msg.append("/");
//                    msg.append(indentificacaoBanco);
//
//                    // Mensagem 2
//                    mensagem[1] = msg.toString();
//
//                    // Mensagem 3
//                    mensagem[2] = "";
//                }
//            }
//
//            if (tipoConta == 3) {
//
//                if (emitirContaHelper.getIdClienteResponsavel() != null
//                        && !emitirContaHelper.getIdClienteResponsavel().equals(
//                                "")) {
//
//                    StringBuilder msg = new StringBuilder();
//
//                    String enderecoClienteResponsavel = null;
//
//                    // [UC0085]Obter Endereco
//                    enderecoClienteResponsavel = getControladorEndereco()
//                            .pesquisarEnderecoClienteAbreviado(
//                                    new Integer(emitirContaHelper
//                                            .getIdClienteResponsavel()));
//
//                    if (enderecoClienteResponsavel != null) {
//                        msg.append("ENTREGAR EM ");
//                        msg.append(Util.completaString(
//                                enderecoClienteResponsavel, 50));
//
//                        // Mensagem 2
//                        mensagem[1] = msg.toString();
//
//                        // Mensagem 3
//                        mensagem[2] = "";
//
//                    }
//                }
//            }
//
//            if (tipoConta == 6) {
//
//                StringBuilder msg2 = new StringBuilder();
//
//                Object[] parmsDebitoAutomatico = null;
//                try {
//                    parmsDebitoAutomatico = repositorioArrecadacao
//                            .pesquisarParmsDebitoAutomatico(emitirContaHelper
//                                    .getIdImovel());
//                } catch (ErroRepositorioException e) {
//                    sessionContext.setRollbackOnly();
//                    throw new ControladorException("erro.sistema", e);
//                }
//
//                String codigoAgencia = "";
//                String idBanco = "";
//                String indentificacaoBanco = "";
//                if (parmsDebitoAutomatico != null) {
//
//                    // codigo Agencia
//                    if (parmsDebitoAutomatico[1] != null) {
//                        codigoAgencia = ((String) parmsDebitoAutomatico[1]);
//                    }
//
//                    // id do banco
//                    if (parmsDebitoAutomatico[2] != null) {
//                        idBanco = ((Integer) parmsDebitoAutomatico[2])
//                                .toString();
//                    }
//
//                    // indentificacao do banco
//                    if (parmsDebitoAutomatico[3] != null) {
//                        indentificacaoBanco = ((String) parmsDebitoAutomatico[3]);
//                    }
//
//                    msg2.append("DEBITAR NO BANCO ");
//                    msg2.append(idBanco);
//                    msg2.append("/");
//                    msg2.append(codigoAgencia);
//                    msg2.append("/");
//                    msg2.append(indentificacaoBanco);
//
//                    if (emitirContaHelper.getIdClienteResponsavel() != null
//                            && !emitirContaHelper.getIdClienteResponsavel()
//                                    .equals("")) {
//
//                        StringBuilder msg = new StringBuilder();
//
//                        String enderecoClienteResponsavel = null;
//
//                        // [UC0085]Obter Endereco
//                        enderecoClienteResponsavel = getControladorEndereco()
//                                .pesquisarEnderecoClienteAbreviado(
//                                        new Integer(emitirContaHelper
//                                                .getIdClienteResponsavel()));
//
//                        if (enderecoClienteResponsavel != null) {
//                            msg.append("ENTREGAR EM ");
//                            msg.append(Util.completaString(
//                                    enderecoClienteResponsavel, 50));
//
//                            // Mensagem 1
//                            mensagem[0] = msg2.toString();
//
//                            // Mensagem 2
//                            mensagem[1] = msg.toString();
//
//                        }
//                    } else {
//                        // Mensagem 2
//                        mensagem[1] = msg2.toString();
//
//                        // Mensagem 3
//                        mensagem[2] = "";
//
//                    }
//                }
//            }
//
//            Object[] mensagensConta = null;
//
//            // recupera o id do grupo de faturamento da conta
//            Integer idFaturamentoGrupo = emitirContaHelper
//                    .getIdFaturamentoGrupo();
//            // recupera o id da gerencia regional da conta
//            Integer idGerenciaRegional = emitirContaHelper
//                    .getIdGerenciaRegional();
//            // recupera o id da localidade da conta
//            Integer idLocalidade = emitirContaHelper.getIdLocalidade();
//            // recupera o id do setor comercial da conta
//            Integer idSetorComercial = emitirContaHelper.getIdSetorComercial();
//
//            // caso entre em alguma condição então não entra mais nas outras
//            boolean achou = false;
//
//            try {
//                // o sistema obtem a mensagem para a conta
//                // Caso seja a condição 1
//                // (FaturamentoGrupo =null, GerenciaRegional=parmConta,
//                // Localidade=parmConta, SetorComercial=parmConta)
//                mensagensConta = repositorioFaturamento
//                        .pesquisarParmsContaMensagem(emitirContaHelper, null,
//                                idGerenciaRegional, idLocalidade,
//                                idSetorComercial);
//
//                if (mensagensConta != null) {
//
//                    // Mensagem 3
//                    if (mensagensConta[0] != null) {
//                        mensagem[2] = (String) mensagensConta[0];
//                    } else {
//                        mensagem[2] = "";
//                    }
//
//                    // Mensagem 4
//                    if (mensagensConta[1] != null) {
//                        mensagem[3] = (String) mensagensConta[1];
//                    } else {
//                        mensagem[3] = "";
//                    }
//
//                    // Mensagem 5
//                    if (mensagensConta[2] != null) {
//                        mensagem[4] = (String) mensagensConta[2];
//                    } else {
//                        mensagem[4] = "";
//                    }
//                    achou = true;
//                }
//
//                if (!achou) {
//
//                    // Caso seja a condição 2
//                    // (FaturamentoGrupo =null, GerenciaRegional=parmConta,
//                    // Localidade=null, SetorComercial=null)
//                    // Conta Mensagem 1
//                    mensagensConta = repositorioFaturamento
//                            .pesquisarParmsContaMensagem(emitirContaHelper,
//                                    null, idGerenciaRegional, idLocalidade,
//                                    null);
//
//                    if (mensagensConta != null) {
//
//                        // Mensagem 3
//                        if (mensagensConta[0] != null) {
//                            mensagem[2] = (String) mensagensConta[0];
//                        } else {
//                            mensagem[2] = "";
//                        }
//
//                        // Mensagem 4
//                        if (mensagensConta[1] != null) {
//                            mensagem[3] = (String) mensagensConta[1];
//                        } else {
//                            mensagem[3] = "";
//                        }
//
//                        // Mensagem 5
//                        if (mensagensConta[2] != null) {
//                            mensagem[4] = (String) mensagensConta[2];
//                        } else {
//                            mensagem[4] = "";
//                        }
//                        achou = true;
//                    }
//                }
//                if (!achou) {
//                    // Caso seja a condição 3
//                    // (FaturamentoGrupo =null, GerenciaRegional=parmConta,
//                    // Localidade=null, SetorComercial=null)
//                    // Conta Mensagem 1
//                    mensagensConta = repositorioFaturamento
//                            .pesquisarParmsContaMensagem(emitirContaHelper,
//                                    null, idGerenciaRegional, null, null);
//
//                    if (mensagensConta != null) {
//
//                        // Mensagem 3
//                        if (mensagensConta[0] != null) {
//                            mensagem[2] = (String) mensagensConta[0];
//                        } else {
//                            mensagem[2] = "";
//                        }
//
//                        // Mensagem 4
//                        if (mensagensConta[1] != null) {
//                            mensagem[3] = (String) mensagensConta[1];
//                        } else {
//                            mensagem[3] = "";
//                        }
//
//                        // Mensagem 5
//                        if (mensagensConta[2] != null) {
//                            mensagem[4] = (String) mensagensConta[2];
//                        } else {
//                            mensagem[4] = "";
//                        }
//                        achou = true;
//                    }
//                }
//                if (!achou) {
//                    // Caso seja a condição 4
//                    // (FaturamentoGrupo =parmConta, GerenciaRegional=null,
//                    // Localidade=null, SetorComercial=null)
//                    // Conta Mensagem 1
//                    mensagensConta = repositorioFaturamento
//                            .pesquisarParmsContaMensagem(emitirContaHelper,
//                                    idFaturamentoGrupo, null, null, null);
//
//                    if (mensagensConta != null) {
//
//                        // Mensagem 3
//                        if (mensagensConta[0] != null) {
//                            mensagem[2] = (String) mensagensConta[0];
//                        } else {
//                            mensagem[2] = "";
//                        }
//
//                        // Mensagem 4
//                        if (mensagensConta[1] != null) {
//                            mensagem[3] = (String) mensagensConta[1];
//                        } else {
//                            mensagem[3] = "";
//                        }
//
//                        // Mensagem 5
//                        if (mensagensConta[2] != null) {
//                            mensagem[4] = (String) mensagensConta[2];
//                        } else {
//                            mensagem[4] = "";
//                        }
//                        achou = true;
//                    }
//                }
//                if (!achou) {
//                    // Caso seja a condição 5
//                    // (FaturamentoGrupo =null, GerenciaRegional=null,
//                    // Localidade=null, SetorComercial=null)
//                    // Conta Mensagem 1
//                    mensagensConta = repositorioFaturamento
//                            .pesquisarParmsContaMensagem(emitirContaHelper,
//                                    null, null, null, null);
//                    if (mensagensConta != null) {
//                        // Mensagem 3
//                        if (mensagensConta[0] != null) {
//                            mensagem[2] = (String) mensagensConta[0];
//                        } else {
//                            mensagem[2] = "";
//                        }
//
//                        // Mensagem 4
//                        if (mensagensConta[1] != null) {
//                            mensagem[3] = (String) mensagensConta[1];
//                        } else {
//                            mensagem[3] = "";
//                        }
//
//                        // Mensagem 4
//                        if (mensagensConta[2] != null) {
//                            mensagem[4] = (String) mensagensConta[2];
//                        } else {
//                            mensagem[4] = "";
//                        }
//                        achou = true;
//                    }
//                }
//                // caso não tenha entrado em nenhuma das opções acima
//                // então completa a string com espaçõs em branco
//                if (!achou) {
//                    mensagem[2] = "";
//                    mensagem[3] = "";
//                    mensagem[4] = "";
//                }
//            } catch (ErroRepositorioException e) {
//                sessionContext.setRollbackOnly();
//                throw new ControladorException("erro.sistema", e);
//            }
//        }

//        return mensagem;
//    }
    
    public String[] obterMensagemAnormalidadeConsumo(Integer idImovel, Integer anoMesReferencia, Integer idImovelPerfil) {

        String[] mensagemConta = new String[]{"", "", ""};
        
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
                
                mensagemConta = quebraMensagemEmTresPartes(mensagemContaAnormalidade); 
            }
        }
        return mensagemConta;
    }

    private String[] quebraMensagemEmTresPartes(String mensagemContaAnormalidade) {
        
        return null;
    }    
}
