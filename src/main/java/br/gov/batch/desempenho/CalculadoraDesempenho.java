package br.gov.batch.desempenho;

import javax.batch.api.chunk.ItemProcessor;
import javax.batch.runtime.context.JobContext;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.servicos.desempenho.ContratoMedicaoBO;
import br.gov.servicos.to.MedicaoPerformanceParametrosTO;

@Named
public class CalculadoraDesempenho implements ItemProcessor {

	@Inject
    private JobContext jobContext;
	
	@EJB
	private ContratoMedicaoBO contratoMedicaoBO;

	@Override
    public Object processItem(Object parametros) throws Exception {
		MedicaoPerformanceParametrosTO medicaoPerformanceParametrosTO = (MedicaoPerformanceParametrosTO) parametros;
        
        return contratoMedicaoBO.getMedicaoPerformanceTO(medicaoPerformanceParametrosTO.getContratoMedicao(), 
				  										 medicaoPerformanceParametrosTO.getImovel(), 
				  										 medicaoPerformanceParametrosTO.getReferencia());
    }
}
