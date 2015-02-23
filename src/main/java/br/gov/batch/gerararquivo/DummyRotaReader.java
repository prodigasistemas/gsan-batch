package br.gov.batch.gerararquivo;

import java.io.Serializable;

import javax.batch.api.chunk.AbstractItemReader;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.util.BatchUtil;

@Named
public class DummyRotaReader extends AbstractItemReader {
    @Inject
    private BatchUtil util;
    
    private String rota = null;
        
    public void  open(Serializable ckpt) throws Exception {
        rota = util.parametroDoBatch("idRota");
    }

    public String readItem() throws Exception {
    	return rota;
    }
}