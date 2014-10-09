package br.gov.batch.gerardadosleitura;

import java.util.Properties;

import javax.batch.api.listener.JobListener;
import javax.batch.runtime.context.JobContext;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.util.BatchUtil;
import br.gov.model.batch.ProcessoIniciado;
import br.gov.model.batch.ProcessoSituacao;
import br.gov.servicos.batch.ProcessoParametroRepositorio;
import br.gov.servicos.batch.ProcessoRepositorio;

@Named
public class RotaJobListener implements JobListener{
	
	@EJB
	private BatchLogger logger;
	
	@EJB
	private ProcessoRepositorio processoEJB;
	
	@EJB
	private ProcessoParametroRepositorio processoParametroEJB;

	@Inject
    protected JobContext jobCtx;
	
	@Inject
	private ControleProcessoRota controle;
	
    @Inject
    private BatchUtil util;

	@Override
	public void beforeJob() throws Exception {
    	long execId = jobCtx.getExecutionId();
    	
        logger.info(util.parametroDoBatch("idProcessoIniciado"), String.format("[executionId: %s] - Inicio do processamento da rota: %s", execId, util.parametroDoBatch("idRota")));
	}

	public void afterJob() throws Exception {
		long execId = jobCtx.getExecutionId();
		logger.info(util.parametroDoBatch("idProcessoIniciado"), String.format("[executionId: %s] - Fim do processamento da rota: %s", execId, util.parametroDoBatch("idRota")));
		
		updateJobProgress();
		controle.finalizaProcessamentoRota();
	}

	private void updateJobProgress() {
		Integer idProcessoIniciado = Integer.valueOf(util.parametroDoBatch("idProcessoIniciado"));
		ProcessoIniciado processoIniciado = processoEJB.buscarProcessosIniciado(idProcessoIniciado);
		
		Properties parametros = processoParametroEJB.buscarParametrosPorProcessoIniciado(processoIniciado);
		int totalRotas = parametros.getProperty("idsRota").split(",").length;
		int percentualProcessado = Integer.parseInt(parametros.getProperty("percentualProcessado"));

		int diferenca = 0;
		int percentualRestante = 0;
		int percentualAtualizado = 0;
		
		if(percentualProcessado == 1) {
			diferenca = totalRotas - 1;
			percentualRestante = Math.round((diferenca * 100) / totalRotas);
			percentualAtualizado = 100 - percentualRestante;
		} else {
			int quantidadeProcessada = Math.round((percentualProcessado * totalRotas) / 100);
			quantidadeProcessada++;
			
			diferenca = totalRotas - quantidadeProcessada;
			percentualRestante = Math.round((diferenca * 100) / totalRotas);
			percentualAtualizado = 100 - percentualRestante;
		}
		
		if (percentualAtualizado > 100) {
			percentualAtualizado = 100;
		}
		
		processoParametroEJB.atualizarParametro(processoIniciado, "percentualProcessado", String.valueOf(percentualAtualizado));
	}
}
