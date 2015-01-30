package br.gov.batch.servicos.faturamento.arquivo;

public class ArquivoTexto {

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
	
	protected StringBuilder builder;

	public ArquivoTexto() {
		builder = new StringBuilder();
	}
	
	protected int getQuantidadeLinhas() {
		String[] linhas = builder.toString().split(System.getProperty("line.separator"));
		return linhas.length;
	}
}
