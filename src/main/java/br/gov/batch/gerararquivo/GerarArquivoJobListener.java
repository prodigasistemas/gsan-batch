package br.gov.batch.gerararquivo;

import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.util.GenericProgressJobListener;

@Named
public class GerarArquivoJobListener extends GenericProgressJobListener{
	
	@Inject
	private ControleProcessoGeracaoArquivo controle;
	
	public void beforeJob() throws Exception {
    	long execId = jobCtx.getExecutionId();
    	
        logger.info(util.parametroDoBatch("idProcessoIniciado"), String.format("[executionId: %s] - Inicio da geracao do arquivo para a rota: %s", execId, util.parametroDoBatch("idRota")));
	}

	public void afterJob() throws Exception {
		long execId = jobCtx.getExecutionId();
		logger.info(util.parametroDoBatch("idProcessoIniciado"), String.format("[executionId: %s] - Fim da geracao do arquivo para a rota: %s", execId, util.parametroDoBatch("idRota")));
		
		updateJobProgress();
		controle.finalizaGeracaoArquivoRota();
	}
	
	public int totalSteps(){
	    return parametros.getProperty("idsRota").split(",").length;
	}
}
