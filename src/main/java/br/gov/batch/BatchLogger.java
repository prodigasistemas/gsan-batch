package br.gov.batch;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.ObjectMessage;
import javax.jms.Queue;

import org.jboss.logging.Logger;

@Stateless
public class BatchLogger {

	@Inject
	@JMSConnectionFactory("java:comp/DefaultJMSConnectionFactory")
	private JMSContext contexto;

	@Resource(mappedName="java:global/jms/loggerProcessos")
	private Queue loggerProcessos;

	private String mensagem;

	private Logger logger;
	
	public BatchLogger(){}
	
	public BatchLogger getLogger(Class<?> klass) {
		this.logger = Logger.getLogger(klass);

		return this;
	}
	
	public String getMensagem() {
		return mensagem;
	}
	
	public void info(String idProcesso, String mensagem) {
		log(Logger.Level.INFO, idProcesso, mensagem);
	}
	
	public void error(String idProcesso, String mensagem) {
		log(Logger.Level.ERROR, idProcesso, mensagem);
	}

	public void log(Logger.Level level, String idProcesso, String mensagem) {
		logger.log(level, mensagem);

		this.mensagem = getMensagem(level, mensagem);
		
		enviaLog(this.mensagem);
	}

	private String getMensagem(Logger.Level level, String mensagem) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/YYYY HH:MM:ss:SS");
		String logDate = formatter.format(new Date());
		
		StringBuilder logMensagem = new StringBuilder();
		
		logMensagem.append(logDate);
		logMensagem.append(" - ");
		logMensagem.append(level.toString());
		logMensagem.append(" : ");
		logMensagem.append(mensagem);
		
		return logMensagem.toString();
	}
	
	private void enviaLog(String log) {
		ObjectMessage mensagem = contexto.createObjectMessage(log);
		contexto.createProducer().send(loggerProcessos, mensagem);
	}
}
