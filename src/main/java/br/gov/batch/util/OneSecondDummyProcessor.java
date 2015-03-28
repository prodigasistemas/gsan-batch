package br.gov.batch.util;

import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Named;

@Named
public class OneSecondDummyProcessor implements ItemProcessor {
	public OneSecondDummyProcessor() {
	}

    public Object processItem(Object param) throws Exception {
        Thread.sleep(1000);
        return param;
    }
}