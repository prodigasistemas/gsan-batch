package br.gov.batch.microcoletor;

import javax.batch.api.chunk.ItemProcessor;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.servicos.faturamento.GeradorArquivoTextoMicrocoletor;
import br.gov.batch.util.BatchUtil;

@Named
public class GerarArquivoMicrocoletor implements ItemProcessor {

    @EJB
	private GeradorArquivoTextoMicrocoletor gerarArquivoMicrocoletorBO;
	
    @Inject
    private BatchUtil util;
    
	public GerarArquivoMicrocoletor() {
	}

    public Object processItem(Object param) throws Exception {
    	Integer idRota = Integer.valueOf(util.parametroDoBatch("idRota"));
    	
    	gerarArquivoMicrocoletorBO.gerar(idRota);
    	
        return param;
    }
}