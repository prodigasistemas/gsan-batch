package br.gov.batch.util;

import javax.batch.api.Batchlet;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.gerardadosleitura.ControleExecucaoAtividade;

@Named
public class IniciarAtividadeJob implements Batchlet{
    @Inject
    private BatchUtil util;
    
    @EJB
    private ControleExecucaoAtividade controleExecucao;
    
    public String process() throws Exception {
        controleExecucao.iniciaExecucao(Integer.valueOf(util.parametroDoJob("idControleAtividade")));

		return null;
	}

	public void stop() throws Exception {
		
	}
}
