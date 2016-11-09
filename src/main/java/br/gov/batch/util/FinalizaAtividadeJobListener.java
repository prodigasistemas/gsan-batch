package br.gov.batch.util;

import javax.batch.api.listener.JobListener;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.ControleExecucaoAtividade;

@Named
public class FinalizaAtividadeJobListener implements JobListener{
	
    @Inject
    protected BatchUtil util;

    @Inject
    private ControleExecucaoAtividade controle;
    
	public void beforeJob() throws Exception {
	}

	public void afterJob() throws Exception {
        controle.finalizaProcessamentoItem(Integer.valueOf(util.parametroDoJob("idControleAtividade")));        
	}
}