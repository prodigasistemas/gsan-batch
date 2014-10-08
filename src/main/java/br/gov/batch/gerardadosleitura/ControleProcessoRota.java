package br.gov.batch.gerardadosleitura;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Singleton;

import br.gov.batch.util.ExecucaoJob;
import br.gov.batch.util.StatusExecucao;

@Singleton
public class ControleProcessoRota {
	private final Integer limiteRotas = 3;
	
	private final List<ExecucaoJob> execucoes = new ArrayList<ExecucaoJob>();
	
    private Integer quantidadeRotas = 0;
    
    public synchronized void iniciaProcessamentoRota(){
    	quantidadeRotas++;
    }
    
    public synchronized void finalizaProcessamentoRota(){
    	quantidadeRotas--;
    }
    
	public synchronized Boolean emProcessamento(){
		return quantidadeRotas == limiteRotas;
	}
	
	public synchronized boolean insereExecucao(ExecucaoJob novo){
		boolean iniciaExecucao = false;
		boolean conjunto = false;
		
		synchronized (execucoes) {
			for (ExecucaoJob existente : execucoes) {
				if (novo.pertenceAConjunto(existente)){
					existente.adicionaItem(novo);
					iniciaExecucao = existente.emExecucao();
					conjunto = true;
				}
			}
			
			if (!conjunto){
				execucoes.add(novo);
			}
		}
		
		
		return iniciaExecucao;
	}

	public synchronized void interrompeExecucao(long executionId){
		synchronized (execucoes) {
			for (ExecucaoJob execucao : execucoes) {
				if (execucao.getExecutionId().longValue() == executionId){
					execucao.setStatus(StatusExecucao.CANCELADO);
				}
			}
		}
	}
	
	public synchronized boolean jobAtivo(long executionId){
		boolean ativo = false;
		
		synchronized (execucoes) {
			for (ExecucaoJob execucao : execucoes) {
				if (execucao.getExecutionId().longValue() == executionId && execucao.getStatus() == StatusExecucao.INICIADO){
					ativo = true;
					break;
				}
			}
		}
		
		return ativo;
	}

	public synchronized List<Long> pararExecucao(long executionId){
		List<Long> idExecucoes = new ArrayList<Long>();
		
		synchronized (execucoes) {
			for (ExecucaoJob execucao : execucoes) {
				if (execucao.getExecutionId().longValue() == executionId){
					execucao.setStatus(StatusExecucao.CANCELADO);
					for (ExecucaoJob item: execucao.getSubJobs()) {
						item.setStatus(StatusExecucao.CANCELADO);
						idExecucoes.add(item.getExecutionId());
					}
				}
			}
		}
		
		return idExecucoes;
	}	
}