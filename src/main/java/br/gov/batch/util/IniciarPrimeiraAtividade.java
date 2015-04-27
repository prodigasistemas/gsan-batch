package br.gov.batch.util;

import java.util.Properties;

import javax.batch.api.Batchlet;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import br.gov.batch.gerardadosleitura.ControleExecucaoAtividade;
import br.gov.batch.servicos.batch.ProcessoAtividadeBO;
import br.gov.batch.servicos.batch.ProcessoBatchBO;
import br.gov.batch.to.ControleExecucaoTO;
import br.gov.model.batch.ControleProcessoAtividade;
import br.gov.model.batch.Processo;
import br.gov.model.batch.ProcessoAtividade;

@Named
public class IniciarPrimeiraAtividade implements Batchlet{
    private static Logger logger = Logger.getLogger(IniciarPrimeiraAtividade.class);
	
    @Inject
    private BatchUtil util;
    
    @EJB
    private ProcessoBatchBO processoBO;
    
    @EJB
    private ProcessoAtividadeBO atividadeBO;
    
    @EJB
    private ControleExecucaoAtividade controleExecucao;

	public String process() throws Exception {
	    logger.info("Inicio do batch");
	    
	    Processo processo = processoBO.obterProcessoBatch(Integer.valueOf(util.parametroDoJob("idProcessoIniciado")));
	    
	    logger.info(processo.getDescricao());
	    
	    Integer posicaoAtividade = util.parametroDoJob("atividadeIniciada") != null ? Integer.valueOf(util.parametroDoJob("atividadeIniciada"))  - 1: 1;
	    
	    ProcessoAtividade atividade = processo.getAtividades().get(posicaoAtividade);
	    
	    logger.info("Nome atividade: "  + atividade.getNomeArquivoBatch());
	    
        int totalItens = 1;
        
        if (atividade.isProcessaVariosItens()){
            totalItens = util.parametroDoJob("idsRota").replaceAll("\"", "").split(",").length;
        }

        ControleProcessoAtividade controle = atividadeBO.prepararAtividade(Integer.valueOf(util.parametroDoJob("idProcessoIniciado"))
                , atividade.getNomeArquivoBatch()
                , totalItens);
        
        ControleExecucaoTO to = new ControleExecucaoTO(controle.getId()
                , controle.getTotalItens()
                , controle.getAtividade().getLimiteExecucao()
                , controle.getAtividade().getDescricao()
                , controle.getAtividade().getNomeArquivoBatch());

        
        controleExecucao.cadastraExecucao(to);
        
        Properties properties = util.parametrosDoJob();
        properties.put("idControleAtividade", String.valueOf(controle.getId()));
        
        JobOperator jo = BatchRuntime.getJobOperator();
	    jo.start(atividade.getNomeArquivoBatch(), properties);
	    
		return null;
	}

	public void stop() throws Exception {
		
	}
}
