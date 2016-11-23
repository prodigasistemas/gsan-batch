package br.gov.batch;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.jms.TextMessage;

import org.jboss.logging.Logger;

@Stateless
public class BatchLogger {

//	@Inject
//    @JMSConnectionFactory("java:jboss/DefaultJMSConnectionFactory")	
//	private JMSContext contexto;
//
//	@Resource(mappedName="java:global/jms/loggerProcessos")
//	private Queue loggerProcessos;

	private String mensagem;
	private Logger logger;
	
	public BatchLogger(){
		getLogger(BatchLogger.class);
	}
	
	private void getLogger(Class<?> klass) {
		this.logger = Logger.getLogger(klass);
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

	public void logBackgroud(String mensagem){
		log(Logger.Level.INFO, null, mensagem, false);
	}
	
	public void log(Logger.Level level, String idProcesso, String mensagem) {
		log(level, idProcesso, mensagem, true);
	}
	
	public void log(Logger.Level level, String idProcesso, String mensagem, boolean enviarParaFila) {
		logger.log(level, mensagem);

		this.mensagem = getMensagem(level, mensagem);
		
		if(enviarParaFila){
			enviaLog(idProcesso, this.mensagem);
		}
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
	
	private void enviaLog(String idProcesso, String log) {
//		TextMessage mensagem = contexto.createTextMessage(idProcesso + " :: " + log);
//		contexto.createProducer().send(loggerProcessos, mensagem);
	}
}
