package br.gov.batch.gerardadosleitura;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.AbstractItemReader;
import javax.batch.runtime.context.JobContext;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import br.gov.model.cadastro.Imovel;
import br.gov.servicos.cadastro.ImovelEJB;

@Named
public class PreGerarDadosLeitura extends AbstractItemReader {
	private Logger logger = Logger.getLogger(PreGerarDadosLeitura.class);
	
    @EJB
    private ImovelEJB ejb;
    
    @Inject
    private JobContext jobCtx;
    
    private Iterator<Imovel> imoveis;
    
    private StringTokenizer tokens = null;
    
    @Inject
    @BatchProperty(name = "primeiroItem")
    private String primeiroItem;
    
    @Inject
    @BatchProperty(name = "numItens")
    private String numItens;

    public void open(Serializable checkpoint) throws Exception {
        long firstItem0 = Long.valueOf(primeiroItem);
        long numItems0  = Long.valueOf(numItens);
        
        long firstItem = firstItem0;
        long numItems = numItems0 - (firstItem - firstItem0);

    	List<Imovel> lista = ejb.listar(firstItem, numItems);
    	
    	imoveis = lista.iterator();
    	
    	StringBuilder builder = new StringBuilder();
    	for (Imovel imovel : lista) {
    		builder.append(imovel.getNumeroImovel());
    		builder.append(";");
		}
    	tokens = new StringTokenizer(builder.toString(), ";");
    	
    	logger.info(String.format("Processando [ %s ] a partir de [ %s ]", numItems, firstItem));
    }

    public String readItem() {
    	if (tokens.hasMoreTokens()){
    		return tokens.nextToken();
    	}
    	return null;
    }
}