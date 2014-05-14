package br.gov.batch;

import java.io.Serializable;

import javax.batch.api.chunk.AbstractItemReader;
import javax.inject.Named;

@Named
public class ImovelReader extends AbstractItemReader {
	
    
    public ImovelReader() {
	}

    public void open(Serializable checkpoint) throws Exception {
    }

    public String readItem() {
        return "Alow";
    }
}