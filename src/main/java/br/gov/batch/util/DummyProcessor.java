package br.gov.batch.util;

import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Named;

@Named
public class DummyProcessor implements ItemProcessor {
	public DummyProcessor() {
	}

    public Object processItem(Object param) throws Exception {
        return param;
    }
}