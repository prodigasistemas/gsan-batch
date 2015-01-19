package br.gov.batch.exception;

public class ParametrosIncompletosCodigoBarrasException extends GsanBatchException {
	private static final long serialVersionUID = 107367852179413597L;
	
	public ParametrosIncompletosCodigoBarrasException() {
		super("atencao.parametros.incompletos.codigobarra");
	}
}
