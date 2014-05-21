package br.gov.batch;

import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Named;

import br.gov.model.cadastro.Imovel;

@Named
public class ProcessoImovel implements ItemProcessor {
	
	public ProcessoImovel() {
	}

    public Imovel processItem(Object param) {
    	Imovel i = new Imovel();
    	i.setNumeroImovel((String) param);
        return i;
    }
}