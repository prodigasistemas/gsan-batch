package br.gov.mdb;

import java.util.Properties;

import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.jboss.logging.Logger;

import br.gov.ejb.UnidadeMedidaEJB;

@MessageDriven(
	activationConfig = {
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:global/jms/processosFila")
	}
)
public class Mensageiro implements MessageListener {
	
	private static Logger logger = Logger.getLogger(Mensageiro.class);
	
	@EJB
	private UnidadeMedidaEJB ejb;

    public void onMessage(Message mensagem) {
        try {
            TextMessage tm = (TextMessage) mensagem;
            logger.info("Mensagem recebida para batch: " + tm.getText());
            JobOperator jo = BatchRuntime.getJobOperator();
            long jid = jo.start(tm.getText(), new Properties());
            logger.info("Processo submetido com id : " + jid);
        } catch (JMSException ex) {
            logger.error("Erro na inicializacao do batch: ", ex);
        }
    }
}
