package br.gov.batch.util;

import java.util.Properties;

import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class BatchUtil {

	@Inject
	protected JobContext jobCtx;

	public String parametroDoBatch(String nomeParametro) {
		long execId = jobCtx.getExecutionId();
		Properties jobParams = BatchRuntime.getJobOperator().getParameters(execId);
		return jobParams.getProperty(nomeParametro);
	}
}