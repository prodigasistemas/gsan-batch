package br.gov.batch.servicos.desempenho;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.model.desempenho.MedicaoPerformance;
import br.gov.servicos.desempenho.MedicaoPerformanceRepositorio;
import br.gov.servicos.to.MedicaoPerformanceTO;

@Stateless
public class MedicaoPerformanceBO {

	@EJB
	private ContratoMedicaoBO contratoMedicaoBO;
	
	@EJB
	private MedicaoPerformanceRepositorio medicaoPerformanceRepositorio;
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void preencherRelatorioDesempenho(List<MedicaoPerformanceTO> medicoesPerformanceTO) {
		for (MedicaoPerformanceTO medicaoPerformanceTO : medicoesPerformanceTO) {
			MedicaoPerformance medicaoPerformance = new MedicaoPerformance(medicaoPerformanceTO);			
			medicaoPerformance.setDataCriacao(new Date());
			
			medicaoPerformanceRepositorio.salvar(medicaoPerformance);
		}
	}

	public void removerDesempenhosParaReferencia(Integer referencia) {
		medicaoPerformanceRepositorio.removerPelaReferencia(referencia);
	}
}
