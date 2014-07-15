package br.gov.batch.gerardadosleitura;

import java.util.Calendar;

import javax.batch.api.chunk.ItemProcessor;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import br.gov.batch.servicos.faturamento.FaturamentoImovelBO;
import br.gov.batch.servicos.faturamento.FaturamentoImovelTO;
import br.gov.batch.util.BatchUtil;
import br.gov.model.cadastro.Imovel;

@Named
public class GerarConta implements ItemProcessor {
	private static Logger logger = Logger.getLogger(GerarConta.class);
	
	@EJB
	private FaturamentoImovelBO faturamentoImovelBO;
	
    @Inject
    private BatchUtil util;
    
    private Integer anoMesFaturamento = 0;
	
	public GerarConta() {
	}

    public Imovel processItem(Object param) throws Exception {
    	Imovel imovel = (Imovel) param;
    	
    	FaturamentoImovelTO to = new FaturamentoImovelTO();
    	
    	to.setImovel(imovel);
    	
    	anoMesFaturamento = Integer.valueOf(util.parametroDoBatch("anoMesFaturamento"));
    	
    	to.setAnoMesFaturamento(anoMesFaturamento);
    	to.setDataVencimentoConta(Calendar.getInstance().getTime());
    	to.setGerarAtividadeGrupoFaturamento(true);
    	
		faturamentoImovelBO.preDeterminarFaturamentoImovel(to);
    	
        return imovel;
    }
}