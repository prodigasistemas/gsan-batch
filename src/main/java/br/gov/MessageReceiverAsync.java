package br.gov;

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


import br.gov.model.UnidadeMedida;
import br.gov.model.UnidadeMedidaEJB;

@MessageDriven(
	activationConfig = {
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:global/jms/myQueue")
	}
)
public class MessageReceiverAsync implements MessageListener {
	
	private static Logger logger = Logger.getLogger(MessageReceiverAsync.class);
	
	@EJB
	private UnidadeMedidaEJB ejb;

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage tm = (TextMessage) message;
            System.out.println("Message received on MDB: " + tm.getText());
            for(UnidadeMedida item: ejb.list()){
            	System.out.println(item.getDescricao());
            }
            JobOperator jo = BatchRuntime.getJobOperator();
            long jid = jo.start("myJob", new Properties());
            logger.info("Job submitted: " + jid);
            
        } catch (JMSException ex) {
            logger.error("Erro na inicializacao do batch: ", ex);
        }
    }
}
