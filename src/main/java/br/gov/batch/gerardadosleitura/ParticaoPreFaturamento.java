package br.gov.batch.gerardadosleitura;

import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.Particao;
import br.gov.batch.util.BatchUtil;

@Named
public class ParticaoPreFaturamento extends Particao {

    @Inject
    private BatchUtil util;
	
    public long totalItens(){
    	int idRota = Integer.valueOf(util.parametroDoBatch("idRota"));
    	return rotaBO.totalImoveisParaPreFaturamento(idRota);
    }
}
