package br.gov.batch.util;

import java.util.Properties;

import javax.batch.api.Batchlet;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import br.gov.batch.servicos.batch.ProcessoAtividadeBO;
import br.gov.batch.servicos.batch.ProcessoBatchBO;
import br.gov.model.batch.Processo;
import br.gov.model.batch.ProcessoAtividade;
import br.gov.servicos.batch.ControleProcessoAtividadeRepositorio;

@Named
public class IniciarJob implements Batchlet{
    private static Logger logger = Logger.getLogger(IniciarJob.class);
	
    @Inject
    private BatchUtil util;
    
    @EJB
    private ProcessoBatchBO processoBO;
    
    @EJB
    private ProcessoAtividadeBO atividadeBO;
    
    @EJB
    
    private ControleProcessoAtividadeRepositorio repositorioControleAtividade;
    
    
	public String process() throws Exception {
	    logger.info("Inicio do batch");
	    
	    Integer idProcessoIniciado = Integer.valueOf(util.parametroDoJob("idProcessoIniciado"));

	    Processo processo = processoBO.obterProcessoBatch(idProcessoIniciado);
	    
	    logger.info(processo.getDescricao());
	    
	    ProcessoAtividade atividade = processo.getAtividades().get(0);
	    
	    atividadeBO.cadastrarAtividadesDoProcesso(idProcessoIniciado);
	    
	    logger.info("Nome atividade: "  + atividade.getNomeArquivoBatch());
	    
        Properties properties = util.parametrosDoJob();
        JobOperator jo = BatchRuntime.getJobOperator();
	    jo.start(atividade.getNomeArquivoBatch(), properties);
	    
		return null;
	}

	public void stop() throws Exception {
		
	}
}
