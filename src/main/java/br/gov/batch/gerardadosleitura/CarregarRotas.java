package br.gov.batch.gerardadosleitura;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Queue;

import javax.batch.api.chunk.AbstractItemReader;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.util.BatchUtil;

@Named
public class CarregarRotas extends AbstractItemReader {
	private BatchLogger logger = new BatchLogger().getLogger(CarregarRotas.class);
    
    @Inject
    private BatchUtil util;
        
    @Inject
    private ControleProcessoRota controle;

    private Queue<String> rotas = new ArrayDeque<String>();

    public void  open(Serializable ckpt) throws Exception {
        String[]  ids =  util.parametroDoBatch("idsRota").replaceAll("\"", "").split(",");
        
    	for (String id : ids) {
			rotas.add(id.trim());
		}
    	    	
    	logger.info(util.parametroDoBatch("idProcessoIniciado"), String.format("Processando grupo [ %s ] com rotas [ %s ].", util.parametroDoBatch("idGrupoFaturamento"), util.parametroDoBatch("idsRota")));
    }

    public String readItem() throws Exception {
    	while (controle.emProcessamento()){
    	}
    	
    	if (!rotas.isEmpty()){
    		String rota = rotas.poll();
    		logger.info(util.parametroDoBatch("idProcessoIniciado"), "Leitura da rota: " + rota);
    		return rota;
    	}
    	return null;
    }
}