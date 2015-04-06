package br.gov.batch.util;

import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.gerardadosleitura.ControleExecucaoAtividade;
import br.gov.batch.util.GenericProgressJobListener;

@Named
public class ControleExecucaoAtividadeJobListener extends GenericProgressJobListener{
	
    @Inject
    private ControleExecucaoAtividade controle;
	
	public void beforeJob() throws Exception {
        logger.info(util.parametroDoJob("idProcessoIniciado"), String.format("Inicio do job: %s", jobCtx.getJobName()));
	}

	public void afterJob() throws Exception {
		logger.info(util.parametroDoJob("idProcessoIniciado"), String.format("Fim do job: %s", jobCtx.getJobName()));
		
        controle.finalizaProcessamentoItem(Integer.valueOf(util.parametroDoJob("idControleAtividade")));
	}
	
	public int totalSteps(){
	    return parametros.getProperty("idsRota").split(",").length;
	}
}
