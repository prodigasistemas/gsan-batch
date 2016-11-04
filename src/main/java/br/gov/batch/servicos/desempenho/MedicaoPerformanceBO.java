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

@Stateless
public class MedicaoPerformanceBO {

	@EJB
	private ContratoMedicaoBO contratoMedicaoBO;
	
	@EJB
	private MedicaoPerformanceRepositorio medicaoPerformanceRepositorio;
	
	public void preencherRelatorioDesempenho(ContratoMedicao contratoMedicao, int referencia) {
		List<Imovel> imoveisAbrangencia = contratoMedicaoBO.getAbrangencia(contratoMedicao.getId());
		
		MedicaoPerformance medicaoPerformance = new MedicaoPerformance();
		List<MedicaoPerformance> medicaoPerformances = new ArrayList<MedicaoPerformance>();
		
		for (Imovel imovel : imoveisAbrangencia) {
			medicaoPerformance.setContratoMedicao(contratoMedicao);
			medicaoPerformance.setReferencia(referencia);
			medicaoPerformance.setImovel(imovel);
			medicaoPerformance.setDiferencaConsumoAgua(contratoMedicaoBO.calcularDiferencaConsumoAgua(imovel, referencia));
			medicaoPerformance.setValorDiferencaConsumoAgua(contratoMedicaoBO.calcularValorDiferencaAgua(imovel, referencia));
			//TODO Calcular a diferenca para o esgoto
			medicaoPerformance.setPercentualConsumoEsgoto(null);
			medicaoPerformance.setValorDiferencaConsumoEsgoto(contratoMedicaoBO.calcularValorDiferencaAgua(imovel, referencia));
			//TODO Pegar a situacao
			medicaoPerformance.setDebitoCreditoSituacao(null);
			medicaoPerformance.setDataCriacao(new Date());

			//TODO falta mapear o campo com o valor de repasse
			
			medicaoPerformances.add(medicaoPerformance);
		}
		
		medicaoPerformanceRepositorio.incluir(medicaoPerformances);
	}
}
