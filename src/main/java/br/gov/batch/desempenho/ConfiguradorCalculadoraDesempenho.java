package br.gov.batch.desempenho;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javax.batch.api.chunk.AbstractItemReader;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;

import br.gov.batch.BatchLogger;
import br.gov.batch.ControleExecucaoAtividade;
import br.gov.batch.servicos.desempenho.ContratoMedicaoBO;
import br.gov.batch.servicos.desempenho.MedicaoPerformanceBO;
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
	
	@EJB
	private MedicaoPerformanceBO medicaoPerformanceBO;
	
	@Inject
    private ControleExecucaoAtividade controle;
	
	private MedicaoPerformanceParametrosTO medicaoPerformanceParametros;
	
	private Integer referencia;
	private Queue<ContratoMedicao> contratosMedicao = new ArrayDeque<ContratoMedicao>();
	
	@Override
	public void open(Serializable prevCheckpointInfo) throws Exception {
		logger.info(util.parametroDoJob("idProcessoIniciado"), "Limpando a base de desempenho para a referencia: " + getReferencia());
		medicaoPerformanceBO.removerDesempenhosParaReferencia(getReferencia());
		
		logger.info(util.parametroDoJob("idProcessoIniciado"), "Recuperando Contratos para a referencia: " + getReferencia());
		
		contratosMedicao.addAll(contratoMedicaoBO.getContratoMedicaoPorReferencia(getReferencia()));
		logger.info(util.parametroDoJob("idProcessoIniciado"), "Numero de contratos encontratos para a referencia: " + contratosMedicao.size());
	}
	
	@Override
	public List<MedicaoPerformanceParametrosTO> readItem() throws Exception {

		ContratoMedicao contratoMedicao = contratosMedicao.poll();
		if(contratoMedicao == null) {
			return null;
		}
		
		Integer referencia = getReferencia();
		List<Imovel> imoveisContrato = contratoMedicaoBO.getAbrangencia(contratoMedicao.getId(), referencia);
		logger.info(util.parametroDoJob("idProcessoIniciado"), "Quantidade de imoveis do contrato [Id=" + contratoMedicao.getId() + "] = " 
																															+ imoveisContrato.size());
		
		List<MedicaoPerformanceParametrosTO> medicoesPerformanceParametros =  new ArrayList<MedicaoPerformanceParametrosTO>();
		if(!imoveisContrato.isEmpty()) {
			for (Imovel imovelContrato : imoveisContrato) {
				medicaoPerformanceParametros = new MedicaoPerformanceParametrosTO();
				medicaoPerformanceParametros.setContratoMedicao(contratoMedicao);
				medicaoPerformanceParametros.setImovel(imovelContrato);
				medicaoPerformanceParametros.setReferencia(referencia);
				
				logger.info(util.parametroDoJob("idProcessoIniciado"), "Imovel [Id=" + imovelContrato.getId() + "] adicionado para calculo de desempenho ");
				medicoesPerformanceParametros.add(medicaoPerformanceParametros);
			}
		}
		
		controle.iniciaProcessamentoItem(Integer.valueOf(util.parametroDoJob("idControleAtividade")));
		
		return medicoesPerformanceParametros;
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
