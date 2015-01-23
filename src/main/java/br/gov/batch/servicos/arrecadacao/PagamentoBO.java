package br.gov.batch.servicos.arrecadacao;

import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.converteParaTexto;
import static br.gov.model.util.Utilitarios.obterDigitoVerificador;

import java.math.BigDecimal;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.exception.ParametrosIncompletosCodigoBarrasException;
import br.gov.batch.servicos.arrecadacao.to.ConsultaCodigoBarrasTO;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.TipoPagamento;
import br.gov.model.util.ConstantesSistema;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;

@Stateless
public class PagamentoBO {
    
    static String IDENTIFICACAO_PRODUTO = "8";
    
    static String IDENTIFICACAO_SEGMENTO = "2";
    
    @EJB
    private SistemaParametrosRepositorio sistemaParametrosRepositorio;
    
    SistemaParametros sistemaParametros;

    @PostConstruct
    public void init(){
        sistemaParametros = sistemaParametrosRepositorio.getSistemaParametros();
    }
    
	public String obterCodigoBarra(ConsultaCodigoBarrasTO to) {

		validaTipoPagamento(to);

		StringBuilder codigoBarra = new StringBuilder();

		codigoBarra.append(IDENTIFICACAO_PRODUTO);
		codigoBarra.append(IDENTIFICACAO_SEGMENTO);

		Short moduloVerificador = ConstantesSistema.MODULO_VERIFICADOR_10;
		String identificacaoValorRealOuReferencia = "6";

		if (sistemaParametros.moduloVerificador11()) {
			moduloVerificador = ConstantesSistema.MODULO_VERIFICADOR_11;
			identificacaoValorRealOuReferencia = "8";
		}

		codigoBarra.append(identificacaoValorRealOuReferencia);

		String valorCodigoBarra = valorCodigoBarra(to.getValorCodigoBarra());
		codigoBarra.append(valorCodigoBarra);

		String identificacaoEmpresa = Utilitarios.completaComZerosEsquerda(4, sistemaParametros.getCodigoEmpresaFebraban());
		codigoBarra.append(identificacaoEmpresa);

		String identificacaoPagamento = identificacaoPagamento(to);
		codigoBarra.append(identificacaoPagamento);
		
		codigoBarra.append(to.getTipoPagamento().getId());

		codigoBarra = new StringBuilder(codigoBarra.toString().replace(".", "").replace("-", ""));

		Integer digitoVerificadorGeral = obterDigitoVerificador(codigoBarra.toString(), moduloVerificador);

		codigoBarra = new StringBuilder();
		codigoBarra.append(IDENTIFICACAO_PRODUTO)
				   .append(IDENTIFICACAO_SEGMENTO)
				   .append(identificacaoValorRealOuReferencia)
				   .append(digitoVerificadorGeral)
				   .append(valorCodigoBarra)
				   .append(identificacaoEmpresa)
				   .append(identificacaoPagamento)
				   .append(to.getTipoPagamento().getId());

		return agrupaCodigoBarras(codigoBarra, moduloVerificador);
	}
	
    private String agrupaCodigoBarras(StringBuilder codigoBarra, Short moduloVerificador) {
        String primeiroBloco = codigoBarra.substring(0, 11);
        Integer digitoPrimeiroBloco = obterDigitoVerificador(primeiroBloco, moduloVerificador);
        String segundoBloco = codigoBarra.substring(11, 22);
        Integer digitoSegundoBloco = obterDigitoVerificador(segundoBloco, moduloVerificador);
        String terceiroBloco = codigoBarra.substring(22, 33);
        Integer digitoTerceiroBloco = obterDigitoVerificador(terceiroBloco, moduloVerificador);
        String quartoBloco = codigoBarra.substring(33, 44);
        Integer digitoQuartoBloco = obterDigitoVerificador(quartoBloco, moduloVerificador);
        

        return new StringBuilder()
        		.append(primeiroBloco).append(digitoPrimeiroBloco)
        		.append(segundoBloco).append(digitoSegundoBloco)
        		.append(terceiroBloco).append(digitoTerceiroBloco)
        		.append(quartoBloco).append(digitoQuartoBloco).toString();
    }

    private String valorCodigoBarra(BigDecimal valorCodigoBarra) {
        String numero = "";
        if (valorCodigoBarra != null){
            numero = valorCodigoBarra.setScale(2).toString().replace(".", "").replace("-", "");
        }
        return Utilitarios.completaComZerosEsquerda(11, numero);
    }

    private void validaTipoPagamento(ConsultaCodigoBarrasTO to) {
        if (to.getTipoPagamento() == null) {
            throw new ParametrosIncompletosCodigoBarrasException();
        }
        
        validaParametrosPagamentoConta(to);
        validaParametrosPagamentoGuiaImovel(to);
        validaParametrosPagamentoDocumentoCobranca(to);
        validaParametrosPagamentoGuiaCliente(to);
        validaParametrosPagamentoFaturaClienteReponsavel(to);
        validaParametrosPagamentoDocumentoCliente(to);
    }

    private void validaParametrosPagamentoDocumentoCliente(ConsultaCodigoBarrasTO to) {
        if (to.getTipoPagamento() == TipoPagamento.DOCUMENTO_COBRANCA_CLIENTE) {
            if (to.getIdCliente() == null || to.getSequencialDocumentoCobranca() == null || to.getTipoDocumento() == null) {
                throw new ParametrosIncompletosCodigoBarrasException();
            }
        }
    }

    private void validaParametrosPagamentoFaturaClienteReponsavel(ConsultaCodigoBarrasTO to) {
        if (to.getTipoPagamento() == TipoPagamento.FATURA_CLIENTE_RESPONSAVEL) {
            if (to.getIdCliente() == null || to.getMesAnoReferenciaConta() == null || to.getSeqFaturaClienteResponsavel() == null) {
                throw new ParametrosIncompletosCodigoBarrasException();
            }
        }
    }

    private void validaParametrosPagamentoGuiaCliente(ConsultaCodigoBarrasTO to) {
        if (to.getTipoPagamento() == TipoPagamento.GUIA_CLIENTE) {
            if (to.getIdLocalidade() == null || to.getIdCliente() == null 
                    || to.getIdTipoDebito() == null || to.getAnoEmissaoGuiaPagamento() == null) {
                throw new ParametrosIncompletosCodigoBarrasException();
            }            
        }
    }

    private void validaParametrosPagamentoDocumentoCobranca(ConsultaCodigoBarrasTO to) {
        if (to.getTipoPagamento() == TipoPagamento.DOCUMENTO_COBRANCA_IMOVEL) {
            if (to.getIdLocalidade() == null || to.getMatriculaImovel() == null 
                    || to.getSequencialDocumentoCobranca() == null 
                    || to.getTipoDocumento() == null) {
                throw new ParametrosIncompletosCodigoBarrasException();
            }
        }
    }

    private void validaParametrosPagamentoGuiaImovel(ConsultaCodigoBarrasTO to) {
        if (to.getTipoPagamento() == TipoPagamento.GUIA_IMOVEL) {
            if (to.getIdLocalidade() == null || to.getMatriculaImovel() == null 
                    || to.getIdTipoDebito() == null 
                    || to.getAnoEmissaoGuiaPagamento() == null) {
                throw new ParametrosIncompletosCodigoBarrasException();
            }
        }
    }

    private void validaParametrosPagamentoConta(ConsultaCodigoBarrasTO to) {
        if (to.getTipoPagamento() == TipoPagamento.CONTA) {
            if (to.getIdLocalidade() == null || to.getMatriculaImovel() == null 
                    || to.getMesAnoReferenciaConta() == null 
                    || to.getDigitoVerificadorRefContaModulo10() == null) {
                throw new ParametrosIncompletosCodigoBarrasException();
            }
        }
    }
    
    // Verificar se todos estão implementados
    public String identificacaoPagamento(ConsultaCodigoBarrasTO to) {
        StringBuilder identificacaoPagamento = new StringBuilder();
        
        identificacaoPagamento.append(identificacaoPagamentoDocumentoUM(to));
        identificacaoPagamento.append(identificacaoPagamentoConta(to));
        identificacaoPagamento.append(identificacaoPagamentoGuiaImovel(to));
        identificacaoPagamento.append(identificacaoPagamentoDocumentoCobrancaImovel(to));
        identificacaoPagamento.append(identificacaoPagamentoGuiaCliente(to));
        identificacaoPagamento.append(identificacaoPagamentoFaturaClienteResponsavel(to));
        identificacaoPagamento.append(identificacaoPagamentoDocumentoCobrancaCliente(to));
        identificacaoPagamento.append(identificacaoPagamentoDocumentoNOVE(to));


        return identificacaoPagamento.toString();
    }

    private String identificacaoPagamentoDocumentoUM(ConsultaCodigoBarrasTO to) {
        StringBuilder identificacao = new StringBuilder();
        
        if (to.getTipoPagamento() == TipoPagamento.UM) {
            identificacao.append(completaComZerosEsquerda(3, to.getIdLocalidade()))
			             .append(completaComZerosEsquerda(9, to.getMatriculaImovel()))
			             .append(completaComZerosEsquerda(9, to.getIdGuiaPagamento()))
			             .append("00")
			             .append("1");
        }
        
        return identificacao.toString();        
    }

    private String identificacaoPagamentoDocumentoNOVE(ConsultaCodigoBarrasTO to) {
        StringBuilder identificacao = new StringBuilder();
        
        if (to.getTipoPagamento() == TipoPagamento.NOVE) {
            identificacao.append(completaComZerosEsquerda(3, to.getIdLocalidade()))
			             .append(completaComZerosEsquerda(9, to.getIdCliente()))
			             .append(completaComZerosEsquerda(9, to.getIdGuiaPagamento()))
			             .append("00")
			             .append("1");
        }
        
        return identificacao.toString();        
    }

    private String identificacaoPagamentoDocumentoCobrancaImovel(ConsultaCodigoBarrasTO to) {
        StringBuilder identificacao = new StringBuilder();
        
        if (to.getTipoPagamento() == TipoPagamento.DOCUMENTO_COBRANCA_IMOVEL) {
            identificacao.append(completaComZerosEsquerda(3, to.getIdLocalidade()))
					     .append(completaComZerosEsquerda(9, to.getMatriculaImovel()))
					     .append(completaComZerosEsquerda(9, to.getSequencialDocumentoCobranca()))
					     .append(completaComZerosEsquerda(2, to.getTipoDocumento().getId()))
					     .append("1");
        }
        
        return identificacao.toString();        
    }

    private String identificacaoPagamentoDocumentoCobrancaCliente(ConsultaCodigoBarrasTO to) {
        StringBuilder identificacao = new StringBuilder();
        
        if (to.getTipoPagamento() == TipoPagamento.DOCUMENTO_COBRANCA_CLIENTE) {
            identificacao.append("000")
			             .append(completaComZerosEsquerda(8, to.getIdCliente()))
			             .append(completaComZerosEsquerda(9, to.getSequencialDocumentoCobranca()))
			             .append(completaComZerosEsquerda(2, to.getTipoDocumento().getId()))
			             .append("00");
        }
        
        return identificacao.toString();        
    }
    
    private String identificacaoPagamentoFaturaClienteResponsavel(ConsultaCodigoBarrasTO to) {
        StringBuilder identificacao = new StringBuilder();
        
        if (to.getTipoPagamento() == TipoPagamento.FATURA_CLIENTE_RESPONSAVEL) {
            identificacao.append(completaComZerosEsquerda(9, to.getIdCliente()))
		                 .append("00")
		                 .append(converteParaTexto(to.getMesAnoReferenciaConta()))
		                 .append(converteParaTexto(to.getDigitoVerificadorRefContaModulo10()))
		                 .append(completaComZerosEsquerda(6, to.getSeqFaturaClienteResponsavel()));
        }
        
        return identificacao.toString();        
    }

    private String identificacaoPagamentoGuiaCliente(ConsultaCodigoBarrasTO to) {
        StringBuilder identificacao = new StringBuilder();
        
        if (to.getTipoPagamento() == TipoPagamento.GUIA_CLIENTE) {
            identificacao.append(completaComZerosEsquerda(3, to.getIdLocalidade()))
			             .append(completaComZerosEsquerda(8, to.getIdCliente()))
			             .append("000")
			             .append(completaComZerosEsquerda(3, to.getIdTipoDebito()))
			             .append(converteParaTexto(to.getAnoEmissaoGuiaPagamento()))
			             .append("000");
        }
        
        return identificacao.toString();        
    }
    
    private String identificacaoPagamentoGuiaImovel(ConsultaCodigoBarrasTO to) {
        StringBuilder identificacao = new StringBuilder();
        
        if (to.getTipoPagamento() == TipoPagamento.GUIA_IMOVEL) {
            identificacao.append(completaComZerosEsquerda(3, to.getIdLocalidade()))
            			 .append(completaComZerosEsquerda(9, to.getMatriculaImovel()))
            			 .append("0")
            			 .append("1")
            			 .append(completaComZerosEsquerda(3, to.getIdTipoDebito()))
            			 .append(converteParaTexto(to.getAnoEmissaoGuiaPagamento()))
            			 .append("000");
        }
        
        return identificacao.toString();
    }

    private String identificacaoPagamentoConta(ConsultaCodigoBarrasTO to) {
        StringBuilder identificacao = new StringBuilder();
        
        if (to.getTipoPagamento() == TipoPagamento.CONTA) {
            identificacao.append(completaComZerosEsquerda(3, to.getIdLocalidade()))
            			 // TODO - Completar com 8
						 .append(completaComZerosEsquerda(9, to.getMatriculaImovel()))
						 // TODO - Adicionar 000
//						 .append("000")
						 .append("0")
						 .append("1")
						 .append(converteParaTexto(to.getMesAnoReferenciaConta()))
						 .append(converteParaTexto(to.getDigitoVerificadorRefContaModulo10()))
						 .append("000");
        }
        
        return identificacao.toString();
    }
}
