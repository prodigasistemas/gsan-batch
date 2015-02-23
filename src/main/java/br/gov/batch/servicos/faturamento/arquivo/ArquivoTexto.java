package br.gov.batch.servicos.faturamento.arquivo;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.cobranca.CobrancaDocumentoRepositorio;
import br.gov.servicos.faturamento.ContaRepositorio;

public abstract class ArquivoTexto {

	protected static final String TIPO_REGISTRO_01_IMOVEL = "01";
	protected static final String TIPO_REGISTRO_02_CATEGORIAS = "02";
	protected static final String TIPO_REGISTRO_03_CONSUMO_HISTORICO = "03";
	protected static final String TIPO_REGISTRO_04_PARCELAMENTO = "04";
	protected static final String TIPO_REGISTRO_05_CREDITO = "05";
	protected static final String TIPO_REGISTRO_06_IMPOSTOS = "06";
	protected static final String TIPO_REGISTRO_07_COBRANCA = "07";
	protected static final String TIPO_REGISTRO_08_MEDICAO = "08";
	protected static final String TIPO_REGISTRO_09_TARIFA = "09";
	protected static final String TIPO_REGISTRO_10_FAIXA_CONSUMO = "10";
	protected static final String TIPO_REGISTRO_11_CODIGO_BARRAS = "11";
	protected static final String TIPO_REGISTRO_12_ACAO_ANORMALIDADE = "12";
	protected static final String TIPO_REGISTRO_13_ANORMALIDADE_CONSUMO = "13";
	protected static final String TIPO_REGISTRO_14_ANORMALIDADE_LEITURA = "14";
	
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

	public void setSistemaParametros(SistemaParametros sistemaParametros) {
		this.sistemaParametros = sistemaParametros;
	}
}
