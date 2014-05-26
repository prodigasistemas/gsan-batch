package br.gov.batch.gerardadosleitura;

import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Named;

@Named
public class ProcessamentoPreFaturamento implements ItemProcessor {
	
	public ProcessamentoPreFaturamento() {
	}

    public ImovelPreFaturamento processItem(Object param) {
    	ImovelPreFaturamento imovel = (ImovelPreFaturamento) param;
        return imovel;
    }
}