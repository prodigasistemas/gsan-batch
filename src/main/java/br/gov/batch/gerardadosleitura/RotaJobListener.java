package br.gov.batch.gerardadosleitura;

import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.util.GenericProgressJobListener;

@Named
public class RotaJobListener extends GenericProgressJobListener{
	
	@Inject
	private ControleProcessoRota controle;
	
	public void beforeJob() throws Exception {
    	long execId = jobCtx.getExecutionId();
    	
        logger.info(util.parametroDoBatch("idProcessoIniciado"), String.format("[executionId: %s] - Inicio do processamento da rota: %s", execId, util.parametroDoBatch("idRota")));
	}

	public void afterJob() throws Exception {
		long execId = jobCtx.getExecutionId();
		logger.info(util.parametroDoBatch("idProcessoIniciado"), String.format("[executionId: %s] - Fim do processamento da rota: %s", execId, util.parametroDoBatch("idRota")));
		
		updateJobProgress();
		controle.finalizaProcessamentoRota();
	}

    public int totalSteps() {
        return parametros.getProperty("idsRota").split(",").length;
    }
}
