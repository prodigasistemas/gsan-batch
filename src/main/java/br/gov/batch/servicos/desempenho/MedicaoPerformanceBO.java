package br.gov.batch.servicos.desempenho;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.Imovel;
import br.gov.model.desempenho.ContratoMedicao;
import br.gov.model.desempenho.MedicaoPerformance;
import br.gov.servicos.desempenho.MedicaoPerformanceRepositorio;
import br.gov.servicos.to.MedicaoPerformanceTO;

@Stateless
public class MedicaoPerformanceBO {

	@EJB
	private ContratoMedicaoBO contratoMedicaoBO;
	
	@EJB
	private MedicaoPerformanceRepositorio medicaoPerformanceRepositorio;
	
	public void preencherRelatorioDesempenho(ContratoMedicao contratoMedicao, int referencia) {
		List<Imovel> imoveisAbrangencia = contratoMedicaoBO.getAbrangencia(contratoMedicao.getId());
		
		MedicaoPerformance medicaoPerformance;
		List<MedicaoPerformance> medicaoPerformances = new ArrayList<MedicaoPerformance>();
		
		for (Imovel imovel : imoveisAbrangencia) {
			MedicaoPerformanceTO medicaoPerformanceTO = contratoMedicaoBO.getMedicaoPerformanceTO(contratoMedicao, imovel, referencia);
			medicaoPerformance = new MedicaoPerformance(medicaoPerformanceTO);			
			medicaoPerformance.setDataCriacao(new Date());
			
			medicaoPerformances.add(medicaoPerformance);
		}
		
		medicaoPerformanceRepositorio.incluir(medicaoPerformances);
	}
}
