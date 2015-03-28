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
	private ProcessoRepositorio processoRepositorio;

	@Inject
    protected JobContext jobCtx;
	
    @Inject
    private BatchUtil util;
    
	@Override
	public void beforeJob() throws Exception {
    	Integer idProcessoIniciado = Integer.valueOf(util.parametroDoJob("idProcessoIniciado"));
		
        processoRepositorio.iniciaExecucaoProcesso(idProcessoIniciado);
        
        logger.info(util.parametroDoJob("idProcessoIniciado"), String.format("Inicio da execução do [%s]", jobCtx.getJobName()));
	}

	public void afterJob() throws Exception {
	}
}
