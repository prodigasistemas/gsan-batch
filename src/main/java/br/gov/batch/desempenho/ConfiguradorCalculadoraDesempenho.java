package br.gov.batch.desempenho;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.batch.api.chunk.AbstractItemReader;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;

import br.gov.batch.BatchLogger;
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
	
	private HashMap<String, Integer> indices;
	private Integer indiceContratoMedicao;
	private Integer indiceImovelContrato;

	private MedicaoPerformanceParametrosTO medicaoPerformanceParametros;
	
	@SuppressWarnings("unchecked")
	public void open(Serializable prevCheckpointInfo) throws Exception {
		Integer referencia = null;
		try {
			referencia = new Integer(util.parametroDoJob("anoMesFaturamento"));
		} catch(NumberFormatException e) {
			referencia = Util.getAnoMesComoInteger(DateTime.now().toDate()); 
		}
		
		List<ContratoMedicao> contratosMedicao = contratoMedicaoBO.getContratoMedicaoPorReferencia(referencia);
				
        if (prevCheckpointInfo != null) {
        	indiceContratoMedicao = ((HashMap<String,Integer>) prevCheckpointInfo).get("contratoMedicao");
        	indiceImovelContrato = ((HashMap<String,Integer>) prevCheckpointInfo).get("imovelContrato");
        }
        
        for (int i = indiceContratoMedicao; i < contratosMedicao.size(); i++) {
        	ContratoMedicao contratoMedicao = contratosMedicao.get(indiceContratoMedicao);
        	List<Imovel> imoveisContrato = contratoMedicaoBO.getAbrangencia(contratoMedicao.getId(), referencia);
        	for (int j = indiceImovelContrato; j < imoveisContrato.size(); j++) {
        		medicaoPerformanceParametros = new MedicaoPerformanceParametrosTO();
        		medicaoPerformanceParametros.setContratoMedicao(contratoMedicao);
        		medicaoPerformanceParametros.setImovel(imoveisContrato.get(indiceImovelContrato));
        		medicaoPerformanceParametros.setReferencia(referencia);
			}
        } 
       
        logger.info(util.parametroDoJob("idProcessoIniciado"), "Calculando desempenho para o contrato: [Id=" + contratosMedicao.get(indiceContratoMedicao) + "]");
	}
	
	@Override
	public MedicaoPerformanceParametrosTO readItem() throws Exception {
		if(medicaoPerformanceParametros != null) {
			indiceContratoMedicao++;
			indiceImovelContrato++;
		}
		
		return medicaoPerformanceParametros;
	}
	
	@Override
	public Serializable checkpointInfo() throws Exception {
		if(indices == null) {
			indices = new HashMap<String, Integer>();
			indices.put("contratoMedicao", 0);
			indices.put("imovelContrato", 0);
		} else {
			indices.put("contratoMedicao", indiceContratoMedicao);
			indices.put("imovelContrato", indiceImovelContrato);
		}
		
		return indices;
	}
}
