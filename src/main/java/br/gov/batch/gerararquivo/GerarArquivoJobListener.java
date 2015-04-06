package br.gov.batch.gerararquivo;

import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.gerardadosleitura.ControleExecucaoAtividade;
import br.gov.batch.util.GenericProgressJobListener;

@Named
public class GerarArquivoJobListener extends GenericProgressJobListener{
	
    @Inject
    private ControleExecucaoAtividade controle;
	
	public void beforeJob() throws Exception {
    	long execId = jobCtx.getExecutionId();
    	
        logger.info(util.parametroDoJob("idProcessoIniciado"), String.format("[executionId: %s] - Inicio da geracao do arquivo para a rota: %s", execId, util.parametroDoJob("idRota")));
	}

	public void afterJob() throws Exception {
		long execId = jobCtx.getExecutionId();
		
		logger.info(util.parametroDoJob("idProcessoIniciado"), String.format("[executionId: %s] - Fim da geracao do arquivo para a rota: %s", execId, util.parametroDoJob("idRota")));
		
		updateJobProgress();
		
        controle.finalizaProcessamentoItem(Integer.valueOf(util.parametroDoJob("idControleAtividade")));
	}
	
	public int totalSteps(){
	    return parametros.getProperty("idsRota").split(",").length;
	}
}
