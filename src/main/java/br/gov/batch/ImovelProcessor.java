package br.gov.batch;

import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Named;

import br.gov.model.cadastro.Imovel;

@Named
public class ImovelProcessor implements ItemProcessor {
	
	public ImovelProcessor() {
	}

    @Override
    public Imovel processItem(Object param) {
        System.out.println("processItem: " + param);
        
        return new Imovel();
    }
}