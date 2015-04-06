package br.gov.batch.gerardadosleitura;

import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.util.GenericProgressJobListener;

@Named
public class RotaJobListener extends GenericProgressJobListener{
	
	@Inject
	private ControleExecucaoAtividade controle;
	
	public void beforeJob() throws Exception {
    	long execId = jobCtx.getExecutionId();
    	
        logger.info(util.parametroDoJob("idProcessoIniciado"), String.format("[executionId: %s] - Inicio do processamento da rota: %s", execId, util.parametroDoJob("idRota")));
	}

	public void afterJob() throws Exception {
		long execId = jobCtx.getExecutionId();
		logger.info(util.parametroDoJob("idProcessoIniciado"), String.format("[executionId: %s] - Fim do processamento da rota: %s", execId, util.parametroDoJob("idRota")));
		
		controle.finalizaProcessamentoItem(Integer.valueOf(util.parametroDoJob("idControleAtividade")));
		
		updateJobProgress();
	}

    public int totalSteps() {
        return parametros.getProperty("idsRota").split(",").length;
    }
}
