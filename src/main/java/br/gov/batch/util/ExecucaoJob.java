package br.gov.batch.util;

import java.util.ArrayList;
import java.util.List;

public class ExecucaoJob {
	private Long executionId;
	
	private StatusExecucao status = StatusExecucao.INICIADO;

	private ExecucaoJob parentJob;
	
	private List<ExecucaoJob> subJobs = new ArrayList<ExecucaoJob>();

	public Long getExecutionId() {
		return executionId;
	}

	public void setExecutionId(Long executionId) {
		this.executionId = executionId;
	}

	public List<ExecucaoJob> getSubJobs() {
		return subJobs;
	}

	public void setSubJobs(List<ExecucaoJob> subJobs) {
		this.subJobs = subJobs;
	}

	public ExecucaoJob getParentJob() {
		return parentJob;
	}

	public void setParentJob(ExecucaoJob parentJob) {
		this.parentJob = parentJob;
	}

	public StatusExecucao getStatus() {
		return status;
	}

	public void setStatus(StatusExecucao status) {
		this.status = status;
	}

	public void adicionaItem(ExecucaoJob execucao){
		subJobs.add(execucao);
	}
	
	public boolean pertenceAConjunto(ExecucaoJob execucao) {
		boolean pertence = false;
		if (parentJob != null){
			pertence = parentJob.getExecutionId() == execucao.getExecutionId(); 
		}
		return pertence;
	}
	
	public boolean emExecucao(){
		return status == StatusExecucao.INICIADO;
	}
	
}
