package br.gov.batch.microcoletor;

import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.util.GenericProgressJobListener;

@Named
public class ArquivoMicrocoletorJobListener extends GenericProgressJobListener{
	
	@Inject
	private ControleArquivoMicrocoletor controle;
	
	public void beforeJob() throws Exception {
    	long execId = jobCtx.getExecutionId();
    	
        logger.info(util.parametroDoBatch("idProcessoIniciado"), String.format("[executionId: %s] - Inicio da geração do arquivo de microcoletor para a rota: %s", execId, util.parametroDoBatch("idRota")));
	}

	public void afterJob() throws Exception {
		long execId = jobCtx.getExecutionId();
		logger.info(util.parametroDoBatch("idProcessoIniciado"), String.format("[executionId: %s] - Fim da geração do arquivo de microcoletor para a rota: %s", execId, util.parametroDoBatch("idRota")));
		
		updateJobProgress();
		controle.finalizaProcessamento();
	}

    public int totalSteps() {
        return parametros.getProperty("idsRota").split(",").length;
    }
}
