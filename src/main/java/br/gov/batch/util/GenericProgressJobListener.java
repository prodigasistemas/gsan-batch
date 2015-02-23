package br.gov.batch.util;

import java.util.Properties;

import javax.batch.api.listener.JobListener;
import javax.batch.runtime.context.JobContext;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.util.BatchUtil;
import br.gov.model.batch.ProcessoIniciado;
import br.gov.servicos.batch.ProcessoParametroRepositorio;
import br.gov.servicos.batch.ProcessoRepositorio;

public abstract class GenericProgressJobListener implements JobListener{
	
	@EJB
	protected BatchLogger logger;
	
	@EJB
	protected ProcessoRepositorio processoEJB;
	
	@EJB
	protected ProcessoParametroRepositorio processoParametroEJB;

	@Inject
    protected JobContext jobCtx;
	
    @Inject
    protected BatchUtil util;
    
    protected Properties parametros = null;

	protected void updateJobProgress() {
		Integer idProcessoIniciado = Integer.valueOf(util.parametroDoBatch("idProcessoIniciado"));
		ProcessoIniciado processoIniciado = processoEJB.buscarProcessosIniciado(idProcessoIniciado);
		
		parametros = processoParametroEJB.buscarParametrosPorProcessoIniciado(processoIniciado);
		int totalSteps = totalSteps();
		int percentualProcessado = Integer.parseInt(parametros.getProperty("percentualProcessado"));

		int diferenca = 0;
		int percentualRestante = 0;
		int percentualAtualizado = 0;
		
		if(percentualProcessado == 1) {
			diferenca = totalSteps - 1;
			percentualRestante = Math.round((diferenca * 100) / totalSteps);
			percentualAtualizado = 100 - percentualRestante;
		} else {
			int quantidadeProcessada = Math.round((percentualProcessado * totalSteps) / 100);
			quantidadeProcessada++;
			
			diferenca = totalSteps - quantidadeProcessada;
			percentualRestante = Math.round((diferenca * 100) / totalSteps);
			percentualAtualizado = 100 - percentualRestante;
		}
		
		if (percentualAtualizado > 100) {
			percentualAtualizado = 100;
		}
		
		processoParametroEJB.atualizarParametro(processoIniciado, "percentualProcessado", String.valueOf(percentualAtualizado));
	}
	
	public abstract int totalSteps();
}
