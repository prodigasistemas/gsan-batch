package br.gov.batch.gerararquivo;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Queue;

import javax.batch.api.chunk.AbstractItemReader;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.util.BatchUtil;

@Named
public class LerRotasParaArquivo extends AbstractItemReader {
	
	@EJB
	private BatchLogger logger;
    
    @Inject
    private BatchUtil util;
        
    @Inject
    private ControleProcessoGeracaoArquivo controle;

    private Queue<String> rotas = new ArrayDeque<String>();

    public void  open(Serializable ckpt) throws Exception {
        String[]  ids =  util.parametroDoBatch("idsRota").replaceAll("\"", "").split(",");
        
    	for (String id : ids) {
			rotas.add(id.trim());
		}
    }

    public String readItem() throws Exception {
    	while (controle.emProcessamento()){
    		Thread.sleep(500);
    	}
    	
    	if (!rotas.isEmpty()){
    		String rota = rotas.poll();
    		logger.info(util.parametroDoBatch("idProcessoIniciado"), "Gerando arquivo para rota: " + rota);
    		return rota;
    	}
    	return null;
    }
}