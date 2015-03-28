package br.gov.batch.util;

import javax.batch.api.Batchlet;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.gerardadosleitura.ControleExecucaoAtividade;
import br.gov.batch.servicos.batch.ControleExecucaoBO;
import br.gov.batch.servicos.batch.ProcessoAtividadeBO;
import br.gov.batch.servicos.batch.ProcessoBatchBO;
import br.gov.batch.to.ControleExecucaoTO;

@Named
public class IniciarAtividadeJob implements Batchlet{
    @Inject
    private BatchUtil util;
    
    @EJB
    private ProcessoBatchBO processoBO;
    
    @EJB
    private ProcessoAtividadeBO atividadeBO;
    
    @EJB
    private ControleExecucaoAtividade controleExecucao;
    
    @EJB
    private ControleExecucaoBO controleBO;
    
    
    public String process() throws Exception {
        ControleExecucaoTO execucao = controleBO.criaExecucaoAtividade(Integer.valueOf(util.parametroDoJob("idControleAtividade")));
        
        controleExecucao.cadastraExecucao(execucao);

		return null;
	}

	public void stop() throws Exception {
		
	}
}
