package br.gov.batch.util;

import javax.batch.api.Batchlet;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.servicos.batch.ProcessoParametroRepositorio;

@Named
public class ApagarParametrosTemporarios implements Batchlet{
    @Inject
    private BatchUtil util;

    @EJB
    private ProcessoParametroRepositorio repositorioParametros;
    
	public String process() throws Exception {
        repositorioParametros.excluirParametrosTemporarios(Integer.valueOf(util.parametroDoJob("idProcessoIniciado")));

		return null;
	}

	public void stop() throws Exception {
		
	}
}
