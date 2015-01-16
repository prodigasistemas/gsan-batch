package br.gov.batch.exception;

public class GsanBatchException extends RuntimeException{
	private static final long serialVersionUID = -1686331844969924768L;
	
	public GsanBatchException(String msg) {
	    super(msg);
	}
	
	public GsanBatchException(String msg, Throwable erro) {
		super(msg, erro);
	}
}
