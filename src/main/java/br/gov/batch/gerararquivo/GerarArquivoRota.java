package br.gov.batch.gerararquivo;

import javax.batch.api.chunk.ItemProcessor;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.servicos.faturamento.GeradorArquivoTextoFaturamento;
import br.gov.batch.util.BatchUtil;

@Named
public class GerarArquivoRota implements ItemProcessor {

	@EJB
	private GeradorArquivoTextoFaturamento gerarArquivoBO;

	@Inject
	private BatchUtil util;

	public GerarArquivoRota() {
	}

	public Object processItem(Object param) throws Exception {
		Integer idRota = Integer.valueOf(util.parametroDoJob("idRota"));
		
		//gerarArquivoBO.gerar(idRota, new Date());
		
		Thread.sleep(50);

		return param;
	}
}