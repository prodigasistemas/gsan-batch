package br.gov.batch.util;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Queue;

import javax.batch.api.chunk.AbstractItemReader;
import javax.inject.Named;

@Named
public class DummyItemReader extends AbstractItemReader {
	
    private Queue<String> elements = new ArrayDeque<String>();

    public void  open(Serializable ckpt) throws Exception {
    	elements.add("oneItem");
    }

    public String readItem() throws Exception {
    	if (!elements.isEmpty()){
    		return elements.poll();
    	}
    	return null;
    }
}