package br.gov.batch;

import java.util.List;

import javax.batch.api.chunk.AbstractItemWriter;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.logging.Logger;

@Named
public class ImovelWriter extends AbstractItemWriter {
	
	private static Logger logger = Logger.getLogger(ImovelWriter.class);
	
	@PersistenceContext
	EntityManager em;

	public ImovelWriter() {
	}

    public void writeItems(List list) {
    	logger.info("Imoveis processados: " + list);
    }
}
