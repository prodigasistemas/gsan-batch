package br.gov.batch.servicos.desempenho;

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.desempenho.MedicaoPerformance;
import br.gov.servicos.desempenho.MedicaoPerformanceRepositorio;
import br.gov.servicos.to.MedicaoPerformanceTO;

@Stateless
public class MedicaoPerformanceBO {

	@EJB
	private ContratoMedicaoBO contratoMedicaoBO;
	
	@EJB
	private MedicaoPerformanceRepositorio medicaoPerformanceRepositorio;
	
	public void preencherRelatorioDesempenho(MedicaoPerformanceTO medicaoPerformanceTO) {
		MedicaoPerformance medicaoPerformance = new MedicaoPerformance(medicaoPerformanceTO);			
		medicaoPerformance.setDataCriacao(new Date());
			
		medicaoPerformanceRepositorio.salvar(medicaoPerformance);
	}
}
