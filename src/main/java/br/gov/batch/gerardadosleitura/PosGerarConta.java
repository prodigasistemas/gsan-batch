package br.gov.batch.gerardadosleitura;

import java.util.List;

import javax.batch.api.chunk.AbstractItemWriter;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.logging.Logger;

@Named
public class PosGerarConta extends AbstractItemWriter {
	
//	private static int qtd = 0;
	
	private static Logger logger = Logger.getLogger(PosGerarConta.class);
	
	@PersistenceContext
	EntityManager em;

	public PosGerarConta() {
	}

    public void writeItems(List list) {
    	logger.info("Total de imoveis com contas geradas: " + list.size());
    }
}
