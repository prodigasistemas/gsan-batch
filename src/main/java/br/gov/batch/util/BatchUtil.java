package br.gov.batch.util;

import java.util.Properties;

import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class BatchUtil {

	@Inject
	protected JobContext jobCtx;
	
	public String getNomeProcesso(){
	    return jobCtx.getJobName();
	}
	
	public BatchStatus getBatchStatus(){
	    return jobCtx.getBatchStatus();
	}

	public String parametroDoJob(String nomeParametro) {
		return parametrosDoJob().getProperty(nomeParametro);
	}
	
	public Properties parametrosDoJob() {
	    long execId = jobCtx.getExecutionId();
	    Properties jobParams = BatchRuntime.getJobOperator().getParameters(execId);
	    return jobParams;
	}
}