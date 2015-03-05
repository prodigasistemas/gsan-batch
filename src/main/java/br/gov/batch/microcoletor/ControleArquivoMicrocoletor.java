package br.gov.batch.microcoletor;

import javax.ejb.Singleton;

@Singleton
public class ControleArquivoMicrocoletor {
	private final Integer limiteRotas = 3;
	
    private Integer quantidadeRotas = 0;
    
    public synchronized void iniciaProcessamento(){
    	quantidadeRotas++;
    }
    
    public synchronized void finalizaProcessamento(){
    	quantidadeRotas--;
    }
    
	public synchronized Boolean emProcessamento(){
		return quantidadeRotas == limiteRotas;
	}
}