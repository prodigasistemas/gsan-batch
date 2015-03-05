package br.gov.batch.gerardadosleitura;

import javax.ejb.Singleton;

@Singleton
public class ControleProcessoRotaLeitura {
	private final Integer limiteRotas = 3;
	
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
}