package br.gov.batch.gerararquivo;

import javax.ejb.Singleton;

@Singleton
public class ControleProcessoGeracaoArquivo {
	private final Integer limiteRotas = 3;
	
    private Integer quantidadeRotas = 0;
        
    public synchronized void iniciaGeracaoArquivoRota(){
    	quantidadeRotas++;
    }
    
    public synchronized void finalizaGeracaoArquivoRota(){
    	quantidadeRotas--;
    }
    
	public synchronized Boolean emProcessamento(){
		return quantidadeRotas == limiteRotas;
	}
}