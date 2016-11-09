package br.gov.batch.desempenho;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.batch.api.chunk.AbstractItemReader;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;

import br.gov.batch.BatchLogger;
import br.gov.batch.ControleExecucaoAtividade;
import br.gov.batch.servicos.desempenho.ContratoMedicaoBO;
import br.gov.batch.util.BatchUtil;
import br.gov.batch.util.Util;
import br.gov.model.cadastro.Imovel;
import br.gov.model.desempenho.ContratoMedicao;
import br.gov.servicos.to.MedicaoPerformanceParametrosTO;

@Named
public class ConfiguradorCalculadoraDesempenho extends AbstractItemReader {

	@EJB
	private BatchLogger logger;
	
	@Inject
	private BatchUtil util;
	
	@EJB
	private ContratoMedicaoBO contratoMedicaoBO;
	
	@Inject
    private ControleExecucaoAtividade controle;
	
	private Integer indiceContratoMedicao;

	private MedicaoPerformanceParametrosTO medicaoPerformanceParametros;
	
	private Integer referencia;
	private ContratoMedicao contratoMedicao;
	
	@Override
	public void open(Serializable prevCheckpointInfo) throws Exception {
		logger.info(util.parametroDoJob("idProcessoIniciado"), "Recuperando Contratos para a referencia: " + getReferencia());
		
		List<ContratoMedicao> contratosMedicao = contratoMedicaoBO.getContratoMedicaoPorReferencia(getReferencia());
		logger.info(util.parametroDoJob("idProcessoIniciado"), "Numero de contratos encontratos para a referencia: " + contratosMedicao.size());
		
		if (prevCheckpointInfo != null) {
			indiceContratoMedicao = (Integer) prevCheckpointInfo;
		} else {
			indiceContratoMedicao = 0;
		}
		
		if(!contratosMedicao.isEmpty() && contratosMedicao.size() > indiceContratoMedicao) {
			logger.info(util.parametroDoJob("idProcessoIniciado"), "Calculando desempenho para o contrato: [Id=" + contratosMedicao.get(indiceContratoMedicao) + "]");
			contratoMedicao = contratosMedicao.get(indiceContratoMedicao);
		}
		
	}
	
	@Override
	public List<MedicaoPerformanceParametrosTO> readItem() throws Exception {
		List<MedicaoPerformanceParametrosTO> medicoesPerformanceParametros = new ArrayList<MedicaoPerformanceParametrosTO>();

		Integer referencia = getReferencia();
		if(contratoMedicao != null && referencia != null) {
			List<Imovel> imoveisContrato = contratoMedicaoBO.getAbrangencia(contratoMedicao.getId(), referencia);
			logger.info(util.parametroDoJob("idProcessoIniciado"), "Quantidade de imoveis do contrato [Id=" + contratoMedicao.getId() + "] = " 
																																	+ imoveisContrato.size());
			for (Imovel imovelContrato : imoveisContrato) {
				medicaoPerformanceParametros = new MedicaoPerformanceParametrosTO();
				medicaoPerformanceParametros.setContratoMedicao(contratoMedicao);
				medicaoPerformanceParametros.setImovel(imovelContrato);
				medicaoPerformanceParametros.setReferencia(referencia);
				
				logger.info(util.parametroDoJob("idProcessoIniciado"), "Imovel [Id=" + imovelContrato.getId() + "] adicionado para calculo de desempenho ");
				medicoesPerformanceParametros.add(medicaoPerformanceParametros);
			}
			
			controle.iniciaProcessamentoItem(Integer.valueOf(util.parametroDoJob("idControleAtividade")));
			indiceContratoMedicao++;
		}
		
		logger.info(util.parametroDoJob("idProcessoIniciado"), "Imoveis adicionados para processamento");
		return medicoesPerformanceParametros;
	}
	
	public Serializable checkpointInfo() throws Exception {
		return indiceContratoMedicao;
	}
	
	private Integer getReferencia() {
		try {
			if(referencia == null) {
				referencia = new Integer(util.parametroDoJob("anoMesFaturamento"));
			}
		} catch(NumberFormatException e) {
			return Util.getAnoMesComoInteger(DateTime.now().toDate()); 
		}

		return referencia;
	}
}
