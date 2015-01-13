package br.gov.batch.servicos.faturamento.arquivo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.micromedicao.HidrometroBO;
import br.gov.model.cadastro.Cliente;
import br.gov.model.cadastro.ClienteImovel;
import br.gov.model.cadastro.ClienteRelacaoTipo;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.ImovelContaEnvio;
import br.gov.model.cadastro.endereco.ClienteEndereco;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.FaturamentoParametro.NOME_PARAMETRO_FATURAMENTO;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.arrecadacao.DebitoAutomaticoRepositorio;
import br.gov.servicos.cadastro.ClienteEnderecoRepositorio;
import br.gov.servicos.faturamento.FaturamentoParametroRepositorio;
import br.gov.servicos.to.DadosBancariosTO;

@Stateless
public class ArquivoTextoTipo01 {
    private Imovel imovel;

    private Conta conta;

    // @EJB
    private ClienteEnderecoRepositorio clienteEnderecoRepositorio;

    // @EJB
    private DebitoAutomaticoRepositorio debitoAutomaticoRepositorio;

    // @EJB
    private HidrometroBO hidrometroBO;

    @EJB
    private FaturamentoParametroRepositorio repositorioParametros;

    private String enderecoFormatado = "";

    private StringBuilder builder = new StringBuilder();

    public ArquivoTextoTipo01() {
        builder = new StringBuilder();
    }

    public String build() {

        builder.append("01");
        builder.append(Utilitarios.completaComZerosEsquerda(9, String.valueOf(imovel.getId())));
        builder.append(Utilitarios.completaTexto(25, imovel.getLocalidade().getGerenciaRegional().getNome()));
        builder.append(Utilitarios.completaTexto(25, imovel.getLocalidade().getDescricao()));

        Cliente clienteNomeConta = null;
        Cliente clienteUsuario = null;
        Cliente clienteResponsavel = null;

        if (imovel.getClienteImoveis().size() > 0) {
            ClienteImovel clienteImovel = imovel.getClienteImoveis().get(0);

            if (clienteImovel.nomeParaConta()) {
                clienteNomeConta = clienteImovel.getCliente();
            }

            if (clienteImovel.getClienteRelacaoTipo().getId() == ClienteRelacaoTipo.USUARIO.intValue()) {
                clienteUsuario = clienteImovel.getCliente();
            } else {
                clienteResponsavel = clienteImovel.getCliente();
            }
        }

        builder.append(Utilitarios.completaTexto(30, clienteUsuario.getNome()));

        escreveVencimentoConta();

        builder.append(Utilitarios.completaTexto(17, imovel.getInscricaoFormatadaSemPonto()));

        builder.append(Utilitarios.completaTexto(70, enderecoFormatado == null ? "" : enderecoFormatado));

        escreveReferenciaConta();

        if (clienteResponsavel != null) {
            if (clienteNomeConta != null) {
                builder.append(Utilitarios.completaComZerosEsquerda(9, clienteNomeConta.getId()));
                builder.append(Utilitarios.completaTexto(25, clienteNomeConta.getNome()));
            } else {
                builder.append(Utilitarios.completaComZerosEsquerda(9, clienteResponsavel.getId()));
                builder.append(Utilitarios.completaTexto(25, clienteResponsavel.getNome()));
            }

            if (imovel.enviarContaParaImovel()) {
                builder.append(Utilitarios.completaTexto(75, enderecoFormatado == null ? "" : enderecoFormatado));
            } else {
                ClienteEndereco clienteEndereco = clienteEnderecoRepositorio.pesquisarEnderecoCliente(clienteResponsavel.getId());

                if (clienteEndereco != null) {
                    builder.append(Utilitarios.completaTexto(75, clienteEndereco.getEnderecoFormatadoAbreviado().toString()));
                }
            }
        } else {
            builder.append(Utilitarios.completaTexto(109, ""));
        }

        escreveSituacaoAguaEsgoto();

        escreveDadosBancarios();

        escreveDadosCondominio();

        builder.append(imovel.getIndicadorImovelCondominio().toString());

        builder.append(Utilitarios.completaComZerosEsquerda(2, imovel.getImovelPerfil().getId()));

        boolean houveIntslacaoHidrometro = hidrometroBO.houveSubstituicao(imovel.getId());

        // INDICADOR_FATURAMENTO_ESGOTO
        escreveIndicadorFaturamentoSituacao();

        escreveIndicadorEmissaoConta(clienteResponsavel);

        escreveConsumoMinimoAgua();
        
        escreveConsumoMinimoEsgoto();
        
        escrevePercentualAguaConsumidaColetada();

        return builder.toString();
    }

    private void escrevePercentualAguaConsumidaColetada() {
        if (imovel.getLigacaoEsgoto() != null) {
            String numero = Utilitarios.formatarBigDecimalComPonto(imovel.getLigacaoEsgoto().getPercentualAguaConsumidaColetada());
            builder.append(Utilitarios.completaComZerosEsquerda(6, numero));
        } else {
            builder.append(Utilitarios.completaTexto(6, ""));
        }
    }

    private void escreveConsumoMinimoEsgoto() {
        if (imovel.getLigacaoEsgoto() != null) {
            builder.append(Utilitarios.completaComZerosEsquerda(6, imovel.getLigacaoEsgoto().getConsumoMinimo()));
        } else {
            builder.append(Utilitarios.completaTexto(6, ""));
        }
    }

    private void escreveConsumoMinimoAgua() {
        if (imovel.getLigacaoAgua() != null) {
            builder.append(Utilitarios.completaComZerosEsquerda(6, imovel.getLigacaoAgua().getConsumoMinimoAgua()));
        } else {
            builder.append(Utilitarios.completaTexto(6, ""));
        }

    }

    private void escreveIndicadorEmissaoConta(Cliente clienteResponsavel) {
        Short indicadorEmissaoConta = new Short("1");

        boolean emitir = emitirConta(imovel.getImovelContaEnvio());

        if (clienteResponsavel != null && !emitir) {
            indicadorEmissaoConta = new Short("2");
        }

        builder.append(indicadorEmissaoConta);

    }

    private void escreveIndicadorFaturamentoSituacao() {
        if (conta != null) {
            builder.append(conta.getLigacaoAguaSituacao().getSituacaoFaturamento());
            builder.append(conta.getLigacaoEsgotoSituacao().getSituacaoFaturamento());
        } else {
            builder.append(imovel.getLigacaoAguaSituacao().getId());
            builder.append(imovel.getLigacaoEsgotoSituacao().getId());
        }
    }

    private void escreveSituacaoAguaEsgoto() {
        if (conta != null) {
            builder.append(conta.getLigacaoAguaSituacao().getId());
            builder.append(conta.getLigacaoEsgotoSituacao().getId());
        } else {
            builder.append(imovel.getLigacaoAguaSituacao().getId());
            builder.append(imovel.getLigacaoEsgotoSituacao().getId());
        }
    }

    private void escreveReferenciaConta() {
        if (conta != null) {
            builder.append(conta.getReferencia());
            builder.append(conta.getDigitoVerificadorConta());
        } else {
            builder.append(Utilitarios.completaTexto(7, ""));
        }
    }

    private void escreveVencimentoConta() {
        if (conta != null) {
            builder.append(Utilitarios.formataData(conta.getDataVencimentoConta(), FormatoData.ANO_MES_DIA));
            builder.append(Utilitarios.formataData(conta.getDataValidadeConta(), FormatoData.ANO_MES_DIA));
        } else {
            builder.append(Utilitarios.completaTexto(16, ""));
        }
    }

    private void escreveDadosCondominio() {
        if (imovel.getImovelCondominio() != null) {
            builder.append(Utilitarios.completaComZerosEsquerda(9, imovel.getImovelCondominio().getId()));
        } else {
            builder.append(Utilitarios.completaTexto(9, ""));
        }
    }

    private void escreveDadosBancarios() {
        DadosBancariosTO dadosBancarios = debitoAutomaticoRepositorio.dadosBancarios(imovel.getId());

        if (dadosBancarios != null) {
            builder.append(Utilitarios.completaTexto(15, dadosBancarios.getDescricaoBanco()));
            builder.append(Utilitarios.completaTexto(5, dadosBancarios.getCodigoAgencia()));
        } else {
            builder.append(Utilitarios.completaTexto(20, ""));
        }
    }

    public boolean naoEmitirConta(Integer envioConta) {
        boolean naoEmitir = false;

        boolean emitirFebraban = Boolean.valueOf(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN));

        if (!emitirFebraban) {
            if (enviaContaClienteResponsavelFinalGrupo(envioConta)) {
                naoEmitir = true;
            }
        } else {
            if (enviaConta(envioConta)) {
                naoEmitir = true;
            }
        }

        return naoEmitir;
    }

    public boolean emitirConta(Integer envioConta) {
        boolean emitir = true;

        boolean emitirFebraban = Boolean.valueOf(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN));

        if ((emitirFebraban && enviaConta(envioConta)) || enviaContaClienteResponsavelFinalGrupo(envioConta)) {
            emitir = false;
        }

        return emitir;
    }

    private boolean enviaContaClienteResponsavelFinalGrupo(Integer envioConta) {
        return envioConta != null && envioConta == ImovelContaEnvio.ENVIAR_CLIENTE_RESPONSAVEL_FINAL_GRUPO.getId();
    }

    private boolean enviaConta(Integer envioConta) {
        return envioConta != null
                && (envioConta == ImovelContaEnvio.ENVIAR_CLIENTE_RESPONSAVEL.getId()
                        || envioConta == ImovelContaEnvio.NAO_PAGAVEL_IMOVEL_PAGAVEL_RESPONSAVEL.getId()
                        || envioConta == ImovelContaEnvio.ENVIAR_CONTA_BRAILLE.getId() || envioConta == ImovelContaEnvio.ENVIAR_CONTA_BRAILLE_RESPONSAVEL
                        .getId());
    }
}
