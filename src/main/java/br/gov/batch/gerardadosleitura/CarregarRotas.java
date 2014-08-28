package br.gov.batch.gerardadosleitura;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Queue;

import javax.batch.api.chunk.AbstractItemReader;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import br.gov.batch.util.BatchUtil;

@Named
public class CarregarRotas extends AbstractItemReader {
	private Logger logger = Logger.getLogger(CarregarRotas.class);
    
    @Inject
    private BatchUtil util;
        
    @Inject
    private ControleProcessoRota controle;

    private Queue<String> rotas = new ArrayDeque<String>();

    public void  open(Serializable ckpt) throws Exception {
        String[]  ids =  util.parametroDoBatch("idsRota").split(",");
        
    	for (String id : ids) {
			rotas.add(id.trim());
		}
    	    	
    	logger.info(String.format("Processando grupo [ %s ] com rotas [ %s ].", util.parametroDoBatch("idGrupoFaturamento"), util.parametroDoBatch("idsRota")));
    }

    public String readItem() throws Exception {
    	while (controle.emProcessamento()){
    	}
    	
    	if (!rotas.isEmpty()){
    		String rota = rotas.poll();
    		logger.info("Leitura da rota: " + rota);
    		return rota;
    	}
    	return null;
    }
}