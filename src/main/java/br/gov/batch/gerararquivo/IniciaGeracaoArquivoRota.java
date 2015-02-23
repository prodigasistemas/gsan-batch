package br.gov.batch.gerararquivo;

import java.util.Properties;

import javax.batch.api.chunk.ItemProcessor;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.util.BatchUtil;
import br.gov.model.batch.ProcessoIniciado;
import br.gov.servicos.batch.ProcessoRepositorio;

@Named
public class IniciaGeracaoArquivoRota implements ItemProcessor {
	
	@EJB
	private BatchLogger logger;
	
    @Inject
    private BatchUtil util;
    
    @Inject
    private ControleProcessoGeracaoArquivo controle;
    
    @Inject
    private ProcessoRepositorio processoRepositorio;
	
	public IniciaGeracaoArquivoRota() {
	}

    public Object processItem(Object param) throws Exception {
    	
        ProcessoIniciado processo = processoRepositorio.buscarProcessoIniciadoPorId(Integer.valueOf(util.parametroDoBatch("idProcessoIniciado")));
        
        if (!processo.emProcessamento()){
        	logger.info(util.parametroDoBatch("idProcessoIniciado"), String.format("Geracao de arquivo CANCELADA para a rota %s.", param));
        }else{
            Properties processoParametros = new Properties();
            
            processoParametros.put("idProcessoIniciado", util.parametroDoBatch("idProcessoIniciado"));
            processoParametros.put("idRota", String.valueOf(param));
            processoParametros.put("anoMesFaturamento" , util.parametroDoBatch("anoMesFaturamento"));
            processoParametros.put("idGrupoFaturamento", util.parametroDoBatch("idGrupoFaturamento"));
            
            JobOperator jo = BatchRuntime.getJobOperator();
            
            Long executionRota = jo.start("job_gerar_arquivo", processoParametros);
            
            logger.logBackgroud(String.format("[executionId: %s] - Rota [%s] marcada para geracao de arquivos. ", executionRota, param));
            
            controle.iniciaGeracaoArquivoRota();
        }
        
        return param;
    }
}