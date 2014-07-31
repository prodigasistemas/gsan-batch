package br.gov.batch.gerardadosleitura;

import java.util.List;

import javax.batch.api.chunk.AbstractItemWriter;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Named
public class PosGerarConta extends AbstractItemWriter {
	
	@PersistenceContext
	EntityManager em;

	public PosGerarConta() {
	}

    public void writeItems(List list) {
    }
}
