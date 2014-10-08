package br.gov.batch.gerardadosleitura;

import java.util.Properties;

import javax.batch.api.chunk.ItemProcessor;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.util.BatchUtil;

@Named
public class IniciaProcessamentoRota implements ItemProcessor {
	private static BatchLogger logger = new BatchLogger().getLogger(IniciaProcessamentoRota.class);
	
    @Inject
    private BatchUtil util;
    
    @Inject
    private ControleProcessoRota controle;
    
	public IniciaProcessamentoRota() {
	}

    public Object processItem(Object param) throws Exception {
        Properties processoParametros = new Properties();
        
        processoParametros.put("idProcessoIniciado", util.parametroDoBatch("idProcessoIniciado"));
        processoParametros.put("idRota", String.valueOf(param));
        processoParametros.put("anoMesFaturamento" , util.parametroDoBatch("anoMesFaturamento"));
        processoParametros.put("idGrupoFaturamento", util.parametroDoBatch("idGrupoFaturamento"));
        
        JobOperator jo = BatchRuntime.getJobOperator();
        
        logger.info(util.parametroDoBatch("idProcessoIniciado"), "Rota marcada para processamento: " + param);
            
        jo.start("ProcessarRota", processoParametros);
        
        controle.iniciaProcessamentoRota();

        return param;
    }
}