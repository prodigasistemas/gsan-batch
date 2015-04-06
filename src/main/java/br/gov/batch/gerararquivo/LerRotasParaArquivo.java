package br.gov.batch.gerararquivo;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Queue;

import javax.batch.api.chunk.AbstractItemReader;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.gerardadosleitura.ControleExecucaoAtividade;
import br.gov.batch.util.BatchUtil;

@Named
public class LerRotasParaArquivo extends AbstractItemReader {
	
	@EJB
	private BatchLogger logger;
    
    @Inject
    private BatchUtil util;
        
    @Inject
    private ControleExecucaoAtividade controle;

    private Queue<String> rotas = new ArrayDeque<String>();

    public void  open(Serializable ckpt) throws Exception {
        String[]  ids =  util.parametroDoJob("idsRota").replaceAll("\"", "").split(",");
        
    	for (String id : ids) {
			rotas.add(id.trim());
		}

    	logger.info(util.parametroDoJob("idProcessoIniciado"), String.format("Gerando arquivos para o grupo [ %s ] com rotas [ %s ].", util.parametroDoJob("idGrupoFaturamento"), util.parametroDoJob("idsRota")));
    }

    public String readItem() throws Exception {
        while (controle.atingiuLimiteProcessamento(Integer.valueOf(util.parametroDoJob("idControleAtividade")))){
            Thread.sleep(500);
        }
    	
        String rota = rotas.poll();
        
        if (rota != null){
            logger.info(util.parametroDoJob("idProcessoIniciado"), "Gerando arquivo para rota: " + rota);
        }
        
        return rota;
    }
}