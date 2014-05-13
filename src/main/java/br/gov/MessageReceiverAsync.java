package br.gov;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import br.gov.model.UnidadeMedida;
import br.gov.model.UnidadeMedidaEJB;

@MessageDriven(
	activationConfig = {
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:global/jms/myQueue")
	}
)
public class MessageReceiverAsync implements MessageListener {
	
	@Inject
	private UnidadeMedidaEJB ejb;

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage tm = (TextMessage) message;
            System.out.println("Message received on MDB: " + tm.getText());
            for(UnidadeMedida item: ejb.list()){
            	System.out.println(item.getDescricao());
            }
        } catch (JMSException ex) {
            Logger.getLogger(MessageReceiverAsync.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
