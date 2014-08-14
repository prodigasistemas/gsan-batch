package br.gov.batch.gerardadosleitura;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.AbstractItemReader;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import br.gov.batch.servicos.micromedicao.RotaBO;
import br.gov.batch.util.BatchUtil;
import br.gov.model.cadastro.Imovel;
import br.gov.servicos.cadastro.ImovelRepositorio;

@Named
public class CarregarImoveisRota extends AbstractItemReader {
	private Logger logger = Logger.getLogger(CarregarImoveisRota.class);
	
    @EJB
    private ImovelRepositorio repositorio;
    
    @EJB
    private RotaBO rotaBO;
    
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
    
    public void  open(Serializable ckpt) throws Exception {
        int firstItem0 = Integer.valueOf(primeiroItem);
        int numItems0  = Integer.valueOf(numItens);
        
        int firstItem = firstItem0;
        int numItems = numItems0 - (firstItem - firstItem0);

        Integer idRota = Integer.valueOf(util.parametroDoBatch("idRota"));
        
    	List<Imovel> lista = rotaBO.imoveisParaPreFaturamento(idRota, firstItem, numItems);
    	
    	imoveis = new ArrayDeque<Imovel>(lista);
    	    	
    	anoMesFaturamento = Integer.valueOf(util.parametroDoBatch("anoMesFaturamento"));
//    	logger.info(String.format("Rota: %s - Processando [ %s ] a partir de [ %s ].", idRota, lista.size(), firstItem));
    }

    public Imovel readItem() throws Exception {
    	if (!imoveis.isEmpty()){
    		Imovel imovel = imoveis.poll();
    		if (!repositorio.existeContaImovel(imovel.getId(), anoMesFaturamento)){
    			return imovel;
    		}
    	}
    	return null;
    }
}