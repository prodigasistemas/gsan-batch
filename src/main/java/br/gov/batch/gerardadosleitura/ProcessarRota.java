package br.gov.batch.gerardadosleitura;

import javax.batch.api.chunk.ItemProcessor;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.servicos.faturamento.FaturamentoImovelBO;
import br.gov.batch.servicos.faturamento.to.FaturamentoImovelTO;
import br.gov.batch.util.BatchUtil;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.Rota;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.faturamento.FaturamentoAtividadeCronRotaRepositorio;
import br.gov.servicos.to.CronogramaFaturamentoRotaTO;

//TODO: Mudar o nome para ProcessarImovelRota
@Named
public class ProcessarRota implements ItemProcessor {
    @EJB
    private ImovelRepositorio repositorio;

    @EJB
	private FaturamentoImovelBO faturamentoImovelBO;
	
	@EJB
	private FaturamentoAtividadeCronRotaRepositorio faturamentoAtividadeCronRotaRepositorio;
	
    @Inject
    private BatchUtil util;
    
	public ProcessarRota() {
	}

    public Imovel processItem(Object param) throws Exception {
    	Imovel imovel = (Imovel) param;
    	
    	Integer idRota             = Integer.valueOf(util.parametroDoBatch("idRota"));
    	Integer idGrupoFaturamento = Integer.valueOf(util.parametroDoBatch("idGrupoFaturamento"));
    	Integer anoMesFaturamento  = Integer.valueOf(util.parametroDoBatch("anoMesFaturamento"));
    	
    	CronogramaFaturamentoRotaTO cronogramaFaturamentoRotaTO = faturamentoAtividadeCronRotaRepositorio.pesquisaFaturamentoAtividadeCronogramaRota(idRota, idGrupoFaturamento, anoMesFaturamento);
    	FaturamentoGrupo faturamentoGrupo = new FaturamentoGrupo(idGrupoFaturamento);
    	faturamentoGrupo.setAnoMesReferencia(anoMesFaturamento);
    	
    	Rota rota = new Rota(idRota);
    	rota.setFaturamentoGrupo(faturamentoGrupo);

    	if (!repositorio.existeContaImovel(imovel.getId(), anoMesFaturamento)){
    		FaturamentoImovelTO to = new FaturamentoImovelTO();
    		to.setRota(rota);
    		to.setFaturamentoGrupo(faturamentoGrupo);
    		to.setAnoMesFaturamento(anoMesFaturamento);
    		to.setDataVencimentoConta(cronogramaFaturamentoRotaTO.getDataVencimentoConta());
        	to.setIdImovel(imovel.getId());
            faturamentoImovelBO.preDeterminarFaturamentoImovel(to);
        }
        
        return imovel;
    }
}