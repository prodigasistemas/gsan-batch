package br.gov.batch.util;

import javax.batch.api.listener.JobListener;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.gerardadosleitura.ControleExecucaoAtividade;
import br.gov.batch.to.ControleExecucaoTO;

@Named
public class ControleExecucaoAtividadeJobListener implements JobListener{
	
    @EJB
    protected BatchLogger logger;

    @Inject
    protected BatchUtil util;

    @Inject
    private ControleExecucaoAtividade controle;
	
	public void beforeJob() throws Exception {
        ControleExecucaoTO to =  controle.obterDadosExecucao(Integer.valueOf(util.parametroDoJob("idControleAtividade")));
        
        logger.info(util.parametroDoJob("idProcessoIniciado"), String.format("Inicio do job: %s - Item a processar: %s", to.getDescAtividade(), util.parametroDoJob("idRota")));
	}

	public void afterJob() throws Exception {
        ControleExecucaoTO to =  controle.obterDadosExecucao(Integer.valueOf(util.parametroDoJob("idControleAtividade")));
        
        logger.info(util.parametroDoJob("idProcessoIniciado"), String.format("Fim do job: %s - Item processado: %s", to.getDescAtividade(), util.parametroDoJob("idRota")));
		
        controle.finalizaProcessamentoItem(Integer.valueOf(util.parametroDoJob("idControleAtividade")));
	}	
}
