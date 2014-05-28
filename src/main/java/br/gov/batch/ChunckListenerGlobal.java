package br.gov.batch;

import javax.batch.api.chunk.listener.ChunkListener;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

@Named
public class ChunckListenerGlobal implements ChunkListener{
	
	private static Logger logger = Logger.getLogger(ChunckListenerGlobal.class);
	
	@Inject
	private JobContext jobCtx;

	public void beforeChunk() throws Exception {
	}

	public void onError(Exception ex) throws Exception {
		logger.error("Erro ao processar " + jobCtx.getExecutionId(), ex);
	}

	public void afterChunk() throws Exception {
		
	}

}
