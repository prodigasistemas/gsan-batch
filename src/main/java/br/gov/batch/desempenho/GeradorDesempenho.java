package br.gov.batch.desempenho;

import java.util.List;

import javax.batch.api.chunk.AbstractItemWriter;
import javax.ejb.EJB;
import javax.inject.Named;

import br.gov.batch.servicos.desempenho.MedicaoPerformanceBO;
import br.gov.servicos.to.MedicaoPerformanceTO;

@Named
public class GeradorDesempenho extends AbstractItemWriter {

	@EJB
	private MedicaoPerformanceBO medicaoPerformanceBO; 
	
	@Override
	@SuppressWarnings("unchecked")
	public void writeItems(List<Object> items) throws Exception {
		items.forEach(item -> medicaoPerformanceBO.preencherRelatorioDesempenho((List<MedicaoPerformanceTO>) item));
	}
}
