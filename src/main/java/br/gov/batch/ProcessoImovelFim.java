package br.gov.batch;

import java.util.List;

import javax.batch.api.chunk.AbstractItemWriter;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.logging.Logger;

@Named
public class ProcessoImovelFim extends AbstractItemWriter {
	
	private static int qtd = 0;
	
	private static Logger logger = Logger.getLogger(ProcessoImovelFim.class);
	
	@PersistenceContext
	EntityManager em;

	public ProcessoImovelFim() {
	}

    public void writeItems(List list) {
    	logger.info("Imoveis processados: " + list);
    	qtd += list.size();
    	logger.info("Total: " + qtd);
    }
}
