package br.gov.batch.gerardadosleitura;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.Particao;
import br.gov.batch.servicos.micromedicao.RotaBO;
import br.gov.batch.util.BatchUtil;

@Named
public class ParticionadorRotaLeitura extends Particao {

	@EJB
    protected RotaBO rotaBO;
	
    @Inject
    private BatchUtil util;
	
    public int totalItens(){
    	int idRota = Integer.valueOf(util.parametroDoJob("idRota"));
    	return (int) rotaBO.totalImoveisParaLeitura(idRota);
    }
}
