package br.gov.batch.gerardadosleitura;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.AbstractItemReader;
import javax.batch.runtime.context.JobContext;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import br.gov.model.cadastro.Imovel;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.util.BatchUtil;

@Named
public class PreGerarDadosLeitura extends AbstractItemReader {
	private Logger logger = Logger.getLogger(PreGerarDadosLeitura.class);
	
    @EJB
    private ImovelRepositorio ejb;
    
    @Inject
    private JobContext jobCtx;
    
    @Inject
    private BatchUtil util;
        
    @Inject
    @BatchProperty(name = "primeiroItem")
    private String primeiroItem;
    
    @Inject
    @BatchProperty(name = "numItens")
    private String numItens;
    
    private Queue<Imovel> imoveis = new ArrayDeque<Imovel>();
    
    private Integer anoMesFaturamento = 0;

    public void open(Serializable ckpt) throws Exception {
        long firstItem0 = Long.valueOf(primeiroItem);
        long numItems0  = Long.valueOf(numItens);
        
        long firstItem = firstItem0;
        long numItems = numItems0 - (firstItem - firstItem0);

    	List<Imovel> lista = ejb.listar(firstItem, numItems);
    	
    	imoveis = new ArrayDeque<Imovel>(lista);
    	    	
    	anoMesFaturamento = Integer.valueOf(util.parametroDoBatch("anoMesFaturamento"));
    	
    	logger.info(String.format("Processando [ %s ] a partir de [ %s ]", numItems, firstItem));
    }

    public Imovel readItem() {
    	if (!imoveis.isEmpty()){
    		Imovel imovel = imoveis.poll();
    		if (!ejb.existeContaImovel(imovel.getId(), anoMesFaturamento)){
    			return imovel;
    		}
    	}
    	return null;
    }
}