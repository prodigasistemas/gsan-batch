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
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.Rota;

@Named
public class GerarConta implements ItemProcessor {
	private static Logger logger = Logger.getLogger(GerarConta.class);
	
	@EJB
	private FaturamentoImovelBO faturamentoImovelBO;
	
    @Inject
    private BatchUtil util;
    
	public GerarConta() {
	}

    public Imovel processItem(Object param) throws Exception {
    	Imovel imovel = (Imovel) param;
    	
    	Rota rota = new Rota();
    	rota.setId(Integer.valueOf(util.parametroDoBatch("idRota")));
    	
    	FaturamentoGrupo faturamentoGrupo = new FaturamentoGrupo();
    	faturamentoGrupo.setId(Long.valueOf(util.parametroDoBatch("idGrupoFaturamento")));
    	
    	FaturamentoImovelTO to = new FaturamentoImovelTO();
    	to.setRota(rota);
    	to.setImovel(imovel);
    	to.setFaturamentoGrupo(faturamentoGrupo);
    	to.setAnoMesFaturamento(Integer.valueOf(util.parametroDoBatch("anoMesFaturamento")));
    	to.setDataVencimentoConta(Calendar.getInstance().getTime());
    	to.setGerarAtividadeGrupoFaturamento(true);
    	
		faturamentoImovelBO.preDeterminarFaturamentoImovel(to);
    	
        return imovel;
    }
}