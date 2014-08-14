package br.gov.batch.util;

import java.util.List;

import javax.batch.api.chunk.AbstractItemWriter;
import javax.inject.Named;

@Named
public class DummyItemWriter extends AbstractItemWriter {	
	public DummyItemWriter() {
	}

    @SuppressWarnings("rawtypes")
	public void writeItems(List list) {
    }
}
