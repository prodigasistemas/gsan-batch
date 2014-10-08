package br.gov.batch.gerardadosleitura;

import javax.batch.api.listener.JobListener;
import javax.batch.runtime.context.JobContext;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.util.BatchUtil;
import br.gov.servicos.batch.ProcessoRepositorio;

@Named
public class PreFaturamentoJobListener implements JobListener{
	
	@EJB
	private BatchLogger logger;
	
	@EJB
	private ProcessoRepositorio processoEJB;

	@Inject
    protected JobContext jobCtx;
	
    @Inject
    private BatchUtil util;
    
	@Override
	public void beforeJob() throws Exception {
    	long execId = jobCtx.getExecutionId();
    	
    	Integer idProcessoIniciado = Integer.valueOf(util.parametroDoBatch("idProcessoIniciado"));
		
        processoEJB.iniciaExecucaoProcesso(idProcessoIniciado, execId);
        
        logger.info(util.parametroDoBatch("idProcessoIniciado"), String.format("Inicio da execução [%s] do job [%s]", execId, jobCtx.getJobName()));
	}

	public void afterJob() throws Exception {
		long execId = jobCtx.getExecutionId();

		logger.info(util.parametroDoBatch("idProcessoIniciado"), String.format("Fim da execução [%s] do job [%s]", execId, jobCtx.getJobName()));
	}
}
