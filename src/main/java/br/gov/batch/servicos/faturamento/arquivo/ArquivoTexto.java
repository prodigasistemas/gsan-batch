package br.gov.batch.servicos.faturamento.arquivo;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.cobranca.CobrancaDocumentoRepositorio;
import br.gov.servicos.faturamento.ContaRepositorio;

@Stateless
public abstract class ArquivoTexto {

	// TODO - Renomear constantes com definições das linhas
	protected static final String TIPO_REGISTRO_01 = "01";
	protected static final String TIPO_REGISTRO_02 = "02";
	protected static final String TIPO_REGISTRO_03 = "03";
	protected static final String TIPO_REGISTRO_04 = "04";
	protected static final String TIPO_REGISTRO_05 = "05";
	protected static final String TIPO_REGISTRO_06 = "06";
	protected static final String TIPO_REGISTRO_07 = "07";
	protected static final String TIPO_REGISTRO_08 = "08";
	protected static final String TIPO_REGISTRO_09 = "09";
	protected static final String TIPO_REGISTRO_10 = "10";
	protected static final String TIPO_REGISTRO_11 = "11";
	protected static final String TIPO_REGISTRO_12 = "12";
	protected static final String TIPO_REGISTRO_13 = "13";
	protected static final String TIPO_REGISTRO_14 = "14";
	
	@EJB
	private ContaRepositorio contaRepositorio;
	
	@EJB
	private CobrancaDocumentoRepositorio cobrancaDocumentoRepositorio;

	@EJB
	private SistemaParametrosRepositorio sistemaParametrosRepositorio;

	protected SistemaParametros sistemaParametros;
	
	private ArquivoTextoTO to;
	
	@PostConstruct
	public void init() {
		sistemaParametros = sistemaParametrosRepositorio.getSistemaParametros();
	}

	protected StringBuilder builder;

	public ArquivoTexto() {
		builder = new StringBuilder();
	}

	public abstract String build(ArquivoTextoTO arquivoTextoTO);
	
	public ArquivoTextoTO getArquivoTextoTO() {
		return to;
	}

	public void setArquivoTextoTO(ArquivoTextoTO arquivoTextoTO) {
		this.to = arquivoTextoTO;
	}
	
	public int getQuantidadeLinhas() {
		String[] linhas = builder.toString().split(System.getProperty("line.separator"));
		return linhas.length;
	}
}
