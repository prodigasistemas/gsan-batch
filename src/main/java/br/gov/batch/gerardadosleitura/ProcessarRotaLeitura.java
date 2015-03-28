package br.gov.batch.gerardadosleitura;

import javax.batch.api.chunk.ItemProcessor;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.servicos.micromedicao.DadosLeituraBO;
import br.gov.batch.servicos.micromedicao.to.DadosLeituraTO;
import br.gov.batch.util.BatchUtil;
import br.gov.model.cadastro.Imovel;

@Named
public class ProcessarRotaLeitura implements ItemProcessor {

    @EJB
	private DadosLeituraBO dadosLeituraBO;
	
    @Inject
    private BatchUtil util;
    
	public ProcessarRotaLeitura() {
	}

	public Imovel processItem(Object param) throws Exception {
		Imovel imovel = (Imovel) param;

		DadosLeituraTO to = new DadosLeituraTO();
		to.setIdImovel(imovel.getId());
		to.setIdRota(Integer.valueOf(util.parametroDoJob("idRota")));
		to.setAnoMesFaturamento(Integer.valueOf(util.parametroDoJob("anoMesFaturamento")));
		to.setIdGrupo(Integer.valueOf(util.parametroDoJob("idGrupoFaturamento")));
		
		dadosLeituraBO.gerarDadosParaLeitura(to);

		return imovel;
	}
}