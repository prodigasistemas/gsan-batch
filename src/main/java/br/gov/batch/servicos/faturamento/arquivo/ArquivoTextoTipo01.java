package br.gov.batch.servicos.faturamento.arquivo;

import java.math.BigDecimal;
import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.faturamento.AguaEsgotoBO;
import br.gov.batch.servicos.faturamento.EsgotoBO;
import br.gov.batch.servicos.faturamento.ExtratoQuitacaoBO;
import br.gov.batch.servicos.faturamento.MensagemContaBO;
import br.gov.batch.servicos.faturamento.to.VolumeMedioAguaEsgotoTO;
import br.gov.batch.servicos.micromedicao.ConsumoBO;
import br.gov.batch.servicos.micromedicao.HidrometroBO;
import br.gov.model.cadastro.Cliente;
import br.gov.model.cadastro.ClienteImovel;
import br.gov.model.cadastro.ClienteRelacaoTipo;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.ImovelContaEnvio;
import br.gov.model.cadastro.Localidade;
import br.gov.model.cadastro.endereco.ClienteEndereco;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.faturamento.FaturamentoParametro.NOME_PARAMETRO_FATURAMENTO;
import br.gov.model.faturamento.TipoConta;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.model.micromedicao.Rota;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.arrecadacao.DebitoAutomaticoRepositorio;
import br.gov.servicos.cadastro.ClienteEnderecoRepositorio;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.faturamento.FaturamentoParametroRepositorio;
import br.gov.servicos.to.DadosBancariosTO;

@Stateless
public class ArquivoTextoTipo01 {
    private Imovel imovel;

    private Conta conta;
    
    private FaturamentoGrupo faturamentoGrupo;
    
    private Rota rota;
    
    private Integer anoMesReferencia;
    
    private Integer idImovelPerfil;

    // @EJB
    private ClienteEnderecoRepositorio clienteEnderecoRepositorio;

    // @EJB
    private DebitoAutomaticoRepositorio debitoAutomaticoRepositorio;

    @EJB
    private FaturamentoParametroRepositorio repositorioParametros;

    @EJB
    private HidrometroBO hidrometroBO;
    
    @EJB
    private EsgotoBO esgotoBO;
    
    @EJB
    private AguaEsgotoBO aguaEsgotoBO;

    @EJB
    private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;
    
    @EJB
    private MensagemContaBO mensagemContaBO;
    
    @EJB
    private ExtratoQuitacaoBO extratoQuitacaoBO;

    private String enderecoFormatado = "";

    private StringBuilder builder = new StringBuilder();
    
    @EJB
    private ConsumoBO consumoBO;

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

        boolean houveIntslacaoHidrometro = hidrometroBO.houveInstalacaoOuSubstituicao(imovel.getId());
        
        VolumeMedioAguaEsgotoTO consumoMedioLigacaoAgua = aguaEsgotoBO.obterVolumeMedioAguaEsgoto(imovel.getId(), 
        		faturamentoGrupo.getAnoMesReferencia(), LigacaoTipo.AGUA, houveIntslacaoHidrometro);
        builder.append(Utilitarios.completaComZerosEsquerda(6, consumoMedioLigacaoAgua.getConsumoMedio()));

        // INDICADOR_FATURAMENTO_ESGOTO
        escreveIndicadorFaturamentoSituacao();

        escreveIndicadorEmissaoConta(clienteResponsavel);

        escreveConsumoMinimoAgua();
        
        escreveConsumoMinimoEsgoto();
        
        escrevePercentualAguaConsumidaColetada();
        
        escrevePercentualEsgoto();
        
        builder.append(Utilitarios.completaTexto(1, imovel.getPocoTipo()));
        
        builder.append(Utilitarios.completaComZerosEsquerda(2, imovel.getConsumoTarifa().getId()));
        
        escreveDadosConsumoCategoria();
        
        builder.append(Utilitarios.completaComZerosEsquerda(3, faturamentoGrupo.getId()));
        builder.append(Utilitarios.completaComZerosEsquerda(7, rota.getCodigo()));
        builder.append(Utilitarios.completaTexto(9, ""));
        builder.append(Utilitarios.completaComZerosEsquerda(2, imovel.tarifaTipoCalculo()));
        
        escreveCodigoConta();
        
        builder.append(Utilitarios.completaComZerosEsquerda(2, imovel.tarifaTipoCalculo()));
        
        escreveDadosLocalidade();
        
        builder.append(Utilitarios.completaComZerosEsquerda(9, imovel.getNumeroSequencialRota()));
        
        escreveMensagemConta();
        
        escreverQualidadeDaAgua();
        
        builder.append(Utilitarios.completaTexto(9, extratoQuitacaoBO.obterMsgQuitacaoDebitos(imovel.getId(), anoMesReferencia)));
        
        builder.append(Utilitarios.completaComZerosEsquerda(6, consumoBO.consumoMinimoLigacao(imovel.getId())));
        
        builder.append(Utilitarios.completaComZerosEsquerda(6, consumoBO.consumoNaoMedido(imovel.getId(), anoMesReferencia)));

        return builder.toString();
    }

    private void escreverQualidadeDaAgua() {
//    	Integer anoMesReferenciaQualidadeAgua = null;
    	
//		if (.getNomeEmpresa() != null
//				&& sistemaParametro.getNomeEmpresa().equals(
//						SistemaParametro.EMPRESA_COMPESA)) {
//			anoMesReferenciaQualidadeAgua = Util
//					.subtraiAteSeisMesesAnoMesReferencia(
//							faturamentoGrupo.getAnoMesReferencia(), 1);
//		} else {
//			anoMesReferenciaQualidadeAgua = faturamentoGrupo
//					.getAnoMesReferencia();
//		}
//		/**
//		 * autor: Adriana Muniz Data: 04/05/2011 Como o crit�rio de
//		 * preenchimento da qualidade da agua da cosanpa n�o � usa os campos
//		 * usados como filtros na consulta da qualidade da agua, foi necess�rio
//		 * acrescentar mais um parametro na assinatura no m�todo, idQuadraFace,
//		 * que ser� usado na primeira op��o de busca pela qualidade da �gua
//		 * 
//		 * Foi necess�rio acrescentar um parametro a mais na assinatura do
//		 * m�todo, o sistema de parametro para que a rotina de consulta da
//		 * qualidade da agua seja diferente.
//		 * */
//		arquivoTextoRegistroTipo01 = arquivoTextoRegistroTipo01
//				.append(gerarArquivoTextoQualidadeAgua(idLocalidade,
//						idSetorComercial, anoMesReferenciaQualidadeAgua,
//						idQuadraFace));
	}

	private void escreveMensagemConta() {
        boolean mensagemEmTresPartes = Boolean.valueOf(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.ESCREVER_MENSAGEM_CONTA_TRES_PARTES));
        
        String[] mensagemConta = null;
        
        if (mensagemEmTresPartes){
            mensagemConta = mensagemContaBO.obterMensagemConta3Partes(imovel, anoMesReferencia);

            builder.append(Utilitarios.completaTexto(100, mensagemConta[0]));
            builder.append(Utilitarios.completaTexto(100, mensagemConta[1]));
            builder.append(Utilitarios.completaTexto(100, mensagemConta[2]));
        }else{
            mensagemConta = mensagemContaBO.obterMensagemConta(imovel, anoMesReferencia, idImovelPerfil, TipoConta.CONTA_DEBITO_AUTOMATICO);
            
            builder.append(Utilitarios.completaTexto(60, mensagemConta[0]));
            builder.append(Utilitarios.completaTexto(60, mensagemConta[1]));
            builder.append(Utilitarios.completaTexto(60, mensagemConta[2]));            
            builder.append(Utilitarios.completaTexto(60, mensagemConta[3]));            
            builder.append(Utilitarios.completaTexto(60, mensagemConta[4]));            
        }
    }

    private void escreveDadosLocalidade() {
        Localidade localidade = imovel.getLocalidade();
        StringBuilder descricaoAtendimento = localidade.getEnderecoFormatadoTituloAbreviado();
        builder.append(Utilitarios.completaTexto(70, descricaoAtendimento));
        
        String dddMunicipio = "";
        if (localidade.getLogradouroBairro() != null && localidade.getLogradouroBairro().temMunicipio()) {
            dddMunicipio = Utilitarios.completaTexto(2, localidade.getLogradouroBairro().getBairro().getMunicipio().getDdd());
        }

        String fone = localidade.getFone() != null ? localidade.getFone() : "";

        builder.append(Utilitarios.completaTexto(11, dddMunicipio + fone));
    }

    private void escreveCodigoConta() {
        if (conta != null) {
            builder.append(Utilitarios.completaComZerosEsquerda(9, conta.getId()));
        } else {
            builder.append(Utilitarios.completaTexto(9, ""));
        }        
    }

    private void escreveDadosConsumoCategoria() {
        Collection<ICategoria> categorias = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasCategoria(imovel.getId());
        
        int consumoTotalReferenciaAltoConsumo = 0;
        int consumoTotalReferenciaEstouroConsumo = 0;
        int consumoTotalReferenciaBaixoConsumo = 0;
        int consumoMaximoCobrancaEstouroConsumo = 0;
        int maiorQuantidadeEconomia = 0;
        BigDecimal vezesMediaAltoConsumo = new BigDecimal(0);
        BigDecimal vezesMediaEstouroConsumo = new BigDecimal(0);
        BigDecimal percentualDeterminacaoBaixoConsumo = new BigDecimal(0);
        
        for (ICategoria categoria : categorias) {
            consumoTotalReferenciaAltoConsumo += categoria.getConsumoAlto().intValue() * categoria.getQuantidadeEconomias().intValue();
            
            consumoTotalReferenciaEstouroConsumo += categoria.getConsumoEstouro().intValue() * categoria.getQuantidadeEconomias().intValue();
            
            consumoMaximoCobrancaEstouroConsumo += categoria.getNumeroConsumoMaximoEc().intValue() * categoria.getQuantidadeEconomias().intValue();
            
            consumoTotalReferenciaBaixoConsumo += categoria.getMediaBaixoConsumo().intValue() * categoria.getQuantidadeEconomias().intValue();
            
            if (maiorQuantidadeEconomia < categoria.getQuantidadeEconomias().intValue()) {
                
                maiorQuantidadeEconomia = categoria.getQuantidadeEconomias().intValue();
                
                vezesMediaAltoConsumo = categoria.getVezesMediaAltoConsumo();
                vezesMediaEstouroConsumo = categoria.getVezesMediaEstouro();
                percentualDeterminacaoBaixoConsumo = categoria.getPorcentagemMediaBaixoConsumo();
            }
        }

        builder.append(Utilitarios.completaComZerosEsquerda(6, Math.min(consumoTotalReferenciaEstouroConsumo, 999999)));
        builder.append(Utilitarios.completaComZerosEsquerda(6, Math.min(consumoTotalReferenciaAltoConsumo, 999999)));
        builder.append(Utilitarios.completaComZerosEsquerda(6, Math.min(consumoTotalReferenciaBaixoConsumo, 999999)));
        builder.append(Utilitarios.completaTexto(4, vezesMediaEstouroConsumo));
        builder.append(Utilitarios.completaTexto(4, vezesMediaAltoConsumo));
        builder.append(Utilitarios.completaComZerosEsquerda(6, Utilitarios.formatarBigDecimalComPonto(percentualDeterminacaoBaixoConsumo)));
        builder.append(Utilitarios.completaComZerosEsquerda(6, Math.min(consumoMaximoCobrancaEstouroConsumo, 999999)));        
    }

    private void escrevePercentualEsgoto() {
        BigDecimal percentual = esgotoBO.percentualEsgotoAlternativo(imovel);
        
        builder.append(Utilitarios.completaComZerosEsquerda(6, Utilitarios.formatarBigDecimalComPonto(percentual)));
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
