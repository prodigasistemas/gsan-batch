package br.gov.batch.servicos.faturamento.arquivo;

public class ArquivoTexto {

	protected StringBuilder builder;

	public ArquivoTexto() {
		builder = new StringBuilder();
	}
	
	protected int getQuantidadeLinhas() {
		String[] linhas = builder.toString().split(System.getProperty("line.separator"));
		return linhas.length;
	}
}
