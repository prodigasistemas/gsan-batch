package br.gov.batch.gerardadosleitura;

import javax.batch.api.listener.JobListener;
import javax.batch.runtime.context.JobContext;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.util.BatchUtil;
import br.gov.model.batch.ProcessoIniciado;
import br.gov.model.batch.ProcessoSituacao;
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
    	long execId = jobCtx.getExecutionId();
    	
    	Integer idProcessoIniciado = Integer.valueOf(util.parametroDoBatch("idProcessoIniciado"));
		
        processoRepositorio.iniciaExecucaoProcesso(idProcessoIniciado, execId);
        
        logger.info(util.parametroDoBatch("idProcessoIniciado"), String.format("[executionId: %s] - Inicio da execução do [%s]", execId, jobCtx.getJobName()));
	}

	public void afterJob() throws Exception {
		Integer idProcessoIniciado = Integer.valueOf(util.parametroDoBatch("idProcessoIniciado"));
		
		ProcessoIniciado processo = processoRepositorio.buscarProcessoIniciadoPorId(idProcessoIniciado);
		
		if (processo.emProcessamento()){
			processoRepositorio.atualizaSituacaoProcesso(idProcessoIniciado, ProcessoSituacao.CONCLUIDO);
		}
		
		long execId = jobCtx.getExecutionId();

		logger.info(util.parametroDoBatch("idProcessoIniciado"), String.format("[executionId: %s] - Fim da execução do [%s]", execId, jobCtx.getJobName()));
	}
}
