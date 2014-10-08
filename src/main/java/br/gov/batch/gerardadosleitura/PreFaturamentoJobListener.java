package br.gov.batch.gerardadosleitura;

import java.util.Properties;

import javax.batch.api.listener.JobListener;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.mdb.Mensageiro;
import br.gov.batch.util.BatchUtil;
import br.gov.model.batch.ProcessoSituacao;
import br.gov.servicos.batch.ProcessoRepositorio;

@Named
public class PreFaturamentoJobListener implements JobListener{
	
	private static BatchLogger logger = new BatchLogger().getLogger(Mensageiro.class);
	
	@EJB
	private ProcessoRepositorio processoEJB;

	@Inject
    protected JobContext jobCtx;
	
	@Inject
	private ControleProcessoRota controle;
	
    @Inject
    private BatchUtil util;

	@Override
	public void beforeJob() throws Exception {
    	long execId = jobCtx.getExecutionId();
    	Properties jobParams = BatchRuntime.getJobOperator().getParameters(execId);
    	
    	Long idProcessoIniciado = Long.valueOf(jobParams.getProperty("idProcessoIniciado"));
		
        processoEJB.atualizaSituacaoProcesso(idProcessoIniciado, ProcessoSituacao.EM_PROCESSAMENTO);
        
        logger.info(util.parametroDoBatch("idProcessoIniciado"), String.format("Inicio da execução [%s] do job [%s]", execId, jobCtx.getJobName()));
	}

	public void afterJob() throws Exception {
		long execId = jobCtx.getExecutionId();
		logger.info(util.parametroDoBatch("idProcessoIniciado"), String.format("Fim da execução [%s] do job [%s]", execId, jobCtx.getJobName()));
		controle.finalizaProcessamentoRota();
	}
}
