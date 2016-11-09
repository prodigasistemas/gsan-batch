package br.gov.batch.desempenho;

import java.util.List;

import javax.batch.api.chunk.AbstractItemWriter;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.servicos.desempenho.MedicaoPerformanceBO;
import br.gov.batch.util.BatchUtil;
import br.gov.servicos.to.MedicaoPerformanceTO;

@Named
public class GeradorDesempenho extends AbstractItemWriter {

	@EJB
	private BatchLogger logger;
	
	@Inject
	private BatchUtil util;
	
	@EJB
	private MedicaoPerformanceBO medicaoPerformanceBO; 
	
	@Override
	@SuppressWarnings("unchecked")
	public void writeItems(List<Object> items) throws Exception {
		logger.info(util.parametroDoJob("idProcessoIniciado"), "Armazenando imoveis calculados");
		items.forEach(item -> medicaoPerformanceBO.preencherRelatorioDesempenho((List<MedicaoPerformanceTO>) item));
	}
}
