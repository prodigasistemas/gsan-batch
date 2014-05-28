package br.gov.batch.gerardadosleitura;

import java.util.List;

import javax.batch.api.chunk.AbstractItemWriter;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.logging.Logger;

@Named
public class PosGerarDadosLeitura extends AbstractItemWriter {
	
	private static int qtd = 0;
	
	private static Logger logger = Logger.getLogger(PosGerarDadosLeitura.class);
	
	@PersistenceContext
	EntityManager em;

	public PosGerarDadosLeitura() {
	}

    public void writeItems(List list) {
    	qtd += list.size();
    	logger.info("Total: " + qtd);
    }
}
