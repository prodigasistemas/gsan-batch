package br.gov.batch.util;

import javax.batch.api.listener.JobListener;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.gerardadosleitura.ControleExecucaoAtividade;
import br.gov.batch.servicos.batch.ProcessoBatchBO;
import br.gov.batch.to.ControleExecucaoTO;

@Named
public class ControleExecucaoAtividadeJobListener implements JobListener{
	
    @EJB
    protected BatchLogger logger;

    @Inject
    protected BatchUtil util;

    @Inject
    private ControleExecucaoAtividade controle;
    
    @EJB
    private ProcessoBatchBO processoBO;
	
	public void beforeJob() throws Exception {
	    logaAtividade(PONTO_PROCESSAMENTO.INICIO);
	}

	public void afterJob() throws Exception {
        logaAtividade(PONTO_PROCESSAMENTO.FIM);
		
        controle.finalizaProcessamentoItem(Integer.valueOf(util.parametroDoJob("idControleAtividade")));        
	}
	
	private void logaAtividade(PONTO_PROCESSAMENTO ponto){
        Integer idControle = Integer.valueOf(util.parametroDoJob("idControleAtividade"));
        
        ControleExecucaoTO to =  controle.obterDadosExecucao(idControle);
        
        String log = String.format(ponto + " do job: %s - Item : %s", to.getDescAtividade(), util.parametroDoJob("idRota"));
        
        logger.info(util.parametroDoJob("idProcessoIniciado"), log);	    
	}
	
	enum PONTO_PROCESSAMENTO{
	    INICIO, FIM;
	}
}