package br.gov.batch.util;

import javax.batch.api.listener.JobListener;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;

@Named
public class LoggerJobListener implements JobListener{
	
    @EJB
    protected BatchLogger logger;

    @Inject
    protected BatchUtil util;

	public void beforeJob() throws Exception {
	    logaAtividade(PONTO_PROCESSAMENTO.INICIO);
	}

	public void afterJob() throws Exception {
        logaAtividade(PONTO_PROCESSAMENTO.FIM);
	}
	
	private void logaAtividade(PONTO_PROCESSAMENTO ponto){
        String log = String.format(ponto + " do job: %s", util.getNomeProcesso());
        
        logger.info(util.parametroDoJob("idProcessoIniciado"), log);
	}
	
	enum PONTO_PROCESSAMENTO{
	    INICIO, FIM;
	}
}