package br.gov.batch.util;

import javax.batch.api.Batchlet;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.model.batch.Processo;
import br.gov.servicos.batch.ProcessoRepositorio;

@Named
public class AlterarStatusJobParaProcessamento implements Batchlet{
    @EJB
    private BatchLogger logger;
    
    @EJB
    private ProcessoRepositorio repositorio;

    @Inject
    private BatchUtil util;
    
	public String process() throws Exception {
        Integer idProcessoIniciado = Integer.valueOf(util.parametroDoJob("idProcessoIniciado"));
        
        repositorio.iniciaExecucaoProcesso(idProcessoIniciado);
        
        Processo processo = repositorio.obterProcessoPeloIniciado(idProcessoIniciado);
        
        logger.info(util.parametroDoJob("idProcessoIniciado"), String.format("Inicio da execução do [%s]", processo.getDescricao()));
	    	    
		return null;
	}

	public void stop() throws Exception {
		
	}
}
