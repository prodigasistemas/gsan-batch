package br.gov.batch.mdb;

import java.util.Properties;

import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.jboss.logging.Logger;

import br.gov.model.batch.ProcessoIniciado;
import br.gov.servicos.batch.ProcessoParametroRepositorio;

@MessageDriven(
	activationConfig = {
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:global/jms/processosFila")
	}
)
public class Mensageiro implements MessageListener {
	
	private static Logger logger = Logger.getLogger(Mensageiro.class);
	
	@EJB
	private ProcessoParametroRepositorio processoParametroEJB;

    public void onMessage(Message mensagem) {
        try {
        	ProcessoIniciado processoIniciado = (ProcessoIniciado)((ObjectMessage) mensagem).getObject();
            logger.info("Processo recebido: " + processoIniciado);
            
            JobOperator jo = BatchRuntime.getJobOperator();
            
            Properties processoParametros = processoParametroEJB.buscarParametrosPorProcessoIniciado(processoIniciado);
                        
            jo.start(processoIniciado.getProcesso().getNomeArquivoBatch(), processoParametros);
        } catch (JMSException ex) {
            logger.error("Erro na inicializacao do batch: ", ex);
        }
    }
}
