package br.gov.batch.util;

import javax.batch.api.BatchProperty;
import javax.batch.api.Batchlet;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.model.batch.Processo;
import br.gov.model.batch.ProcessoSituacao;
import br.gov.servicos.batch.ProcessoRepositorio;

@Named
public class AlterarStatusJob implements Batchlet{
    @EJB
    private BatchLogger logger;
    
    @EJB
    private ProcessoRepositorio repositorio;

    @Inject
    private BatchUtil util;
    
    @Inject
    @BatchProperty(name = "status")
    private String status;
    
    @Inject
    @BatchProperty(name = "atividade")
    private String atividade;
    
	public String process() throws Exception {
	    ProcessoSituacao situacao = ProcessoSituacao.valueOf(status);
	    
        Integer idProcessoIniciado = Integer.valueOf(util.parametroDoJob("idProcessoIniciado"));
        
        Processo processo = repositorio.obterProcessoPeloIniciado(idProcessoIniciado);
        
        logger.info(util.parametroDoJob("idProcessoIniciado"), String.format(atividade + " da execução do [%s]", processo.getDescricao()));
        
        if (situacao == ProcessoSituacao.EM_PROCESSAMENTO) {
            repositorio.iniciaExecucaoProcesso(idProcessoIniciado);
        } else if (situacao == ProcessoSituacao.CONCLUIDO) {
            repositorio.terminaExecucaoProcesso(idProcessoIniciado, situacao);
        }
	    	    
		return null;
	}

	public void stop() throws Exception {
		
	}
}
