package br.gov.batch.gerardadosleitura;

import javax.ejb.Singleton;

@Singleton
public class ControleProcessoRota {
    private Boolean emProcessamento = false;
    
    public synchronized void iniciaProcessamentoRota(){
    	emProcessamento = true;
    }
    
    public synchronized void finalizaProcessamentoRota(){
    	emProcessamento = false;
    }
    
	public synchronized Boolean emProcessamento(){
		return emProcessamento;
	}
}