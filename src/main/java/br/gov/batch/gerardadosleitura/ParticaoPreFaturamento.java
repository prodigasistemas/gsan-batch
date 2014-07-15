package br.gov.batch.gerardadosleitura;

import java.util.Properties;

import javax.batch.runtime.BatchRuntime;
import javax.inject.Named;

import br.gov.batch.Particao;

@Named
public class ParticaoPreFaturamento extends Particao {
	
    public long totalItens(){
    	long execId = jobCtx.getExecutionId();
    	Properties jobParams = BatchRuntime.getJobOperator().getParameters(execId);
    	int idRota = Integer.valueOf(jobParams.getProperty("idRota"));
    	return ejb.totalImoveisParaPreFaturamento(idRota);
    }
}
