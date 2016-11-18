package br.gov.batch.desempenho;

import java.util.ArrayList;
import java.util.List;

import javax.batch.api.chunk.ItemProcessor;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.servicos.desempenho.ContratoMedicaoBO;
import br.gov.batch.util.BatchUtil;
import br.gov.servicos.to.MedicaoPerformanceParametrosTO;
import br.gov.servicos.to.MedicaoPerformanceTO;

@Named
public class CalculadoraDesempenho implements ItemProcessor {

	@EJB
	private BatchLogger logger;
	
	@Inject
	private BatchUtil util;
	
	@EJB
	private ContratoMedicaoBO contratoMedicaoBO;

	@Override
	@SuppressWarnings("unchecked")
    public Object processItem(Object parametros) throws Exception {
		List<MedicaoPerformanceParametrosTO> medicoesPerformanceParametrosTO = (List<MedicaoPerformanceParametrosTO>) parametros;
		
		List<MedicaoPerformanceTO> medicoesPerformanceTO = new ArrayList<MedicaoPerformanceTO>();
		for (MedicaoPerformanceParametrosTO medicaoPerformanceParametrosTO : medicoesPerformanceParametrosTO) {
			MedicaoPerformanceTO medicaoPerformanceTO = contratoMedicaoBO.getMedicaoPerformanceTO(medicaoPerformanceParametrosTO.getContratoMedicao(), 
																								  medicaoPerformanceParametrosTO.getImovel(), 
																								  medicaoPerformanceParametrosTO.getReferencia());
			
			logger.info(util.parametroDoJob("idProcessoIniciado"), 
					"\tDesempenho calculado para o imovel [Id=" + medicaoPerformanceParametrosTO.getImovel().getId() + "] = " 
																+ medicaoPerformanceTO.getValorMedicao() + "\n" 
																+ " | valor mes zero = " + medicaoPerformanceTO.getValorAguaFaturadoMesZero()
																+ " | valor mes refe = " + medicaoPerformanceTO.getValorAguaFaturado());
			
			medicoesPerformanceTO.add(medicaoPerformanceTO);
		}
        
        return medicoesPerformanceTO;
    }
}
