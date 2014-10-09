package br.gov.batch.gerardadosleitura;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Queue;

import javax.batch.api.chunk.AbstractItemReader;
import javax.batch.runtime.context.JobContext;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.util.BatchUtil;
import br.gov.batch.util.ExecucaoJob;
import br.gov.model.batch.ProcessoIniciado;
import br.gov.servicos.batch.ProcessoRepositorio;

@Named
public class CarregarRotas extends AbstractItemReader {
	
	@EJB
	private BatchLogger logger;
    
    @Inject
    private BatchUtil util;
        
    @Inject
    private ControleProcessoRota controle;

    private Queue<String> rotas = new ArrayDeque<String>();

	@Inject
    protected JobContext jobCtx;
	
    @Inject
    private ProcessoRepositorio processoRepositorio;

    public void  open(Serializable ckpt) throws Exception {
        String[]  ids =  util.parametroDoBatch("idsRota").replaceAll("\"", "").split(",");
        
    	for (String id : ids) {
			rotas.add(id.trim());
		}
    	    	
    	logger.info(util.parametroDoBatch("idProcessoIniciado"), String.format("Processando grupo [ %s ] com rotas [ %s ].", util.parametroDoBatch("idGrupoFaturamento"), util.parametroDoBatch("idsRota")));
    	
    	ExecucaoJob execucao = new ExecucaoJob();
    	execucao.setExecutionId(jobCtx.getExecutionId());
    	controle.insereExecucao(execucao);
    }

    public String readItem() throws Exception {
    	while (controle.emProcessamento()){
    	}
    	
    	if (!rotas.isEmpty()){
    		String rota = rotas.poll();
    		
            ProcessoIniciado processo = processoRepositorio.buscarProcessoIniciadoPorId(Integer.valueOf(util.parametroDoBatch("idProcessoIniciado")));
            
            String mensagem = String.format("Leitura da rota %s para processamento.", rota);
            if (!processo.emProcessamento()){
            	mensagem = String.format("Leitura da rota %s foi CANCELADA.", rota);
            	rota = null;
            }
            
            logger.info(util.parametroDoBatch("idProcessoIniciado"), mensagem);
            
            return rota;
    	}
    	return null;
    }
}