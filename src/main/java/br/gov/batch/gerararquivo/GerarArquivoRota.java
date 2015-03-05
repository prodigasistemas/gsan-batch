package br.gov.batch.gerararquivo;

import java.util.Date;
import java.util.Properties;

import javax.batch.api.chunk.ItemProcessor;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.microcoletor.ControleArquivoMicrocoletor;
import br.gov.batch.servicos.faturamento.GeradorArquivoTextoFaturamento;
import br.gov.batch.servicos.faturamento.GeradorArquivoTextoMicrocoletor;
import br.gov.batch.util.BatchUtil;
import br.gov.model.micromedicao.LeituraTipo;
import br.gov.model.micromedicao.Rota;
import br.gov.servicos.micromedicao.RotaRepositorio;

@Named
public class GerarArquivoRota implements ItemProcessor {

    @EJB
	private GeradorArquivoTextoFaturamento gerarArquivoBO;
    
    @EJB
	private GeradorArquivoTextoMicrocoletor gerarArquivoMicrocoletorBO;
	
    @Inject
    private BatchUtil util;
    
    @EJB
    private RotaRepositorio rotaRepositorio;
    
    @Inject
    private ControleArquivoMicrocoletor controleArquivoMicrocoletor;
    
	public GerarArquivoRota() {
	}

    public Object processItem(Object param) throws Exception {
    	Integer idRota = Integer.valueOf(util.parametroDoBatch("idRota"));
    	
    	Rota rota = rotaRepositorio.obterPorID(idRota);

    	// TODO - Adicionar o GerarArquivoTextoMicrocoletor
    	if (rota.getLeituraTipo().intValue() == LeituraTipo.LEITURA_E_ENTRADA_SIMULTANEA.getId()) {
    		gerarArquivoBO.gerar(idRota, new Date());
    	} else if (rota.getLeituraTipo().intValue() == LeituraTipo.MICROCOLETOR.getId()){
    		
            Properties processoParametros = new Properties();
            
            processoParametros.put("idRota", String.valueOf(param));
            
            JobOperator jo = BatchRuntime.getJobOperator();
            
            Long executionRota = jo.start("job_gerar_arquivo_microcoletor", processoParametros);
            
//            logger.logBackgroud(String.format("[executionId: %s] - Rota [%s] marcada para geracao de arquivos. ", executionRota, param));
            
            controleArquivoMicrocoletor.iniciaProcessamento();
    		
    		
    	}
    	
        return param;
    }
}