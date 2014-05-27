package br.gov.batch.gerardadosleitura;

import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Named;

import br.gov.model.cadastro.Imovel;

@Named
public class GerarDadosLeitura implements ItemProcessor {
	
	public GerarDadosLeitura() {
	}

    public Imovel processItem(Object param) {
    	Imovel i = new Imovel();
    	i.setNumeroImovel((String) param);
        return i;
    }
}