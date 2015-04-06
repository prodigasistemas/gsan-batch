package br.gov.batch.mdb;

import java.util.Properties;

import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.jboss.logging.Logger;

import br.gov.model.batch.ProcessoIniciado;
import br.gov.servicos.batch.ProcessoParametroRepositorio;

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
	
    public void onMessage(Message mensagem) {
        try {
        	ProcessoIniciado processo = (ProcessoIniciado) ((ObjectMessage) mensagem).getObject();
        	
            logger.info("Processo recebido: " + processo);
            
            Properties processoParametros = repositorio.buscarParametrosPorProcessoIniciado(processo);
            
            JobOperator jo = BatchRuntime.getJobOperator();
            
            jo.start(processo.getProcesso().getNomeArquivoBatch(), processoParametros);
        } catch (JMSException ex) {
            logger.error("Erro na inicializacao do batch: ", ex);
        }
    }
}
