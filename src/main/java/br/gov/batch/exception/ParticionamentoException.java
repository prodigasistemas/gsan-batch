package br.gov.batch.exception;

public class ParticionamentoException extends GsanBatchException {
	private static final long serialVersionUID = 107367852179413597L;
	
	public ParticionamentoException(Exception e) {
		super("erro.particionar.processo", e);
	}
}
