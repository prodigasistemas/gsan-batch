package br.gov.batch.gerardadosleitura;

import java.util.Properties;

import javax.batch.api.listener.JobListener;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import br.gov.mdb.Mensageiro;
import br.gov.model.batch.ProcessoSituacao;
import br.gov.servicos.batch.ProcessoEJB;

@Named
public class JobListenerPreFaturamento implements JobListener{
	
	private static Logger logger = Logger.getLogger(Mensageiro.class);
	
	@EJB
	private ProcessoEJB processoEJB;

	@Inject
    protected JobContext jobCtx;

	@Override
	public void beforeJob() throws Exception {
    	long execId = jobCtx.getExecutionId();
    	Properties jobParams = BatchRuntime.getJobOperator().getParameters(execId);
    	
    	Long idProcessoIniciado = Long.valueOf(jobParams.getProperty("idProcessoIniciado"));
		
        processoEJB.atualizaSituacaoProcesso(idProcessoIniciado, ProcessoSituacao.EM_PROCESSAMENTO);
        
        logger.info("Batch Iniciado: " + jobParams.getProperty("nomeArquivoBatch") + " - ExecutionId: " + execId);
	}

	@Override
	public void afterJob() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
