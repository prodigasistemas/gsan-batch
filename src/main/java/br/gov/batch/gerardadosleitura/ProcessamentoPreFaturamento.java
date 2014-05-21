package br.gov.batch.gerardadosleitura;

import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Named;

import br.gov.model.cadastro.Imovel;

@Named
public class ProcessamentoPreFaturamento implements ItemProcessor {
	
	public ProcessamentoPreFaturamento() {
	}

    public Imovel processItem(Object param) {
    	Imovel i = new Imovel();
    	i.setNumeroImovel((String) param);
        return i;
    }
}