package br.gov.batch.mdb;

import java.util.List;
import java.util.Properties;

import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.jboss.logging.Logger;

import br.gov.batch.gerardadosleitura.ControleProcessoRota;
import br.gov.model.batch.ProcessoIniciado;
import br.gov.model.batch.ProcessoSituacao;
import br.gov.servicos.batch.ProcessoParametroRepositorio;
import br.gov.servicos.batch.ProcessoRepositorio;

@JMSDestinationDefinitions({
	 @JMSDestinationDefinition(name = "java:global/jms/processosFila",
	 interfaceName = "javax.jms.Queue",
	 destinationName="processosFila",
	 description="Fila de Processos")
})
@MessageDriven(
	activationConfig = {
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:global/jms/processosFila")
	}
)
public class Mensageiro implements MessageListener {
	private static Logger logger = Logger.getLogger(Mensageiro.class);
	
	@EJB
	private ProcessoParametroRepositorio repositorio;
	
	@EJB
	private ProcessoRepositorio processoEJB;
	
    @Inject
    private ControleProcessoRota controle;

    public void onMessage(Message mensagem) {
        try {
        	ProcessoIniciado processo = (ProcessoIniciado) ((ObjectMessage) mensagem).getObject();
        	
            logger.info("Processo recebido: " + processo);
            
            Properties processoParametros = repositorio.buscarParametrosPorProcessoIniciado(processo);
            
            if (processo.getProcesso().getNomeArquivoBatch().equals("job_parar_batch")){
            	parar(processoParametros);
            }else{
            	JobOperator jo = BatchRuntime.getJobOperator();
            	
            	jo.start(processo.getProcesso().getNomeArquivoBatch(), processoParametros);
            }
        } catch (JMSException ex) {
            logger.error("Erro na inicializacao do batch: ", ex);
        }
    }
    
    
//    public void parar(Properties params)  {
//    	JobOperator jo = BatchRuntime.getJobOperator();
//    	
//    	String batchCancelar = params.getProperty("batchCancelar");
//    	
//    	if (jo.getJobNames().contains(batchCancelar)){
//        	List<Long> execucoes = jo.getRunningExecutions(batchCancelar);
//        	
//        	if (execucoes.size() > 0 ){
//        		String idProcessoIniciado = params.getProperty("idProcessoIniciado");
//        		for (Long execId : execucoes) {
//        			Properties parametros = jo.getParameters(execId);
//        			if (parametros.getProperty("idProcessoIniciado", "").equals(idProcessoIniciado)){
//        				jo.stop(execId);
//        				
//        				processoEJB.atualizaSituacaoProcesso(Long.valueOf(idProcessoIniciado), ProcessoSituacao.CANCELADO);
//        				
//        				logger.info(String.format("Batch interrompido! Id da execucao: [%s] do job [%s]", execId, batchCancelar));
//        			}
//    			}
//        	}
//    	}else{
//    		logger.info(String.format("Job [%s] nao possui execucoes.", batchCancelar));
//    	}
//    }
    
    public void parar(Properties params)  {
    	JobOperator jo = BatchRuntime.getJobOperator();
    	
    	String batchCancelar = params.getProperty("batchCancelar");
    	
    	if (jo.getJobNames().contains(batchCancelar)){
        	List<Long> execucoes = jo.getRunningExecutions(batchCancelar);
        	
        	if (execucoes.size() > 0 ){
        		String idProcessoIniciado = params.getProperty("idProcessoIniciado");
        		for (Long execId : execucoes) {
        			Properties parametros = jo.getParameters(execId);
        			if (parametros.getProperty("idProcessoIniciado", "").equals(idProcessoIniciado)){
        				List<Long> jobs = controle.pararExecucao(execId);
        				
        				for (Long job : jobs) {
        					jo.stop(job);
        					
        					logger.info(String.format("Job interrompido! Id da execucao: [%s] do job [%s]", job, batchCancelar));
						}
        				
        				jo.stop(execId);
        				
        				processoEJB.atualizaSituacaoProcesso(Integer.valueOf(idProcessoIniciado), ProcessoSituacao.CANCELADO);
        				
        				logger.info(String.format("Batch interrompido! Id da execucao: [%s] do job [%s]", execId, batchCancelar));
        			}
    			}
        	}
    	}else{
    		logger.info(String.format("Job [%s] nao possui execucoes.", batchCancelar));
    	}
    }    

}
