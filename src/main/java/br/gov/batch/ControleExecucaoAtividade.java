package br.gov.batch;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Singleton;

import br.gov.batch.to.ControleExecucaoTO;
import br.gov.model.batch.ControleProcessoAtividade;
import br.gov.model.batch.ProcessoSituacao;
import br.gov.servicos.batch.ControleProcessoAtividadeRepositorio;

@Singleton
public class ControleExecucaoAtividade {
    
    Map<Integer, ControleExecucaoTO> map = new HashMap<Integer, ControleExecucaoTO>();
    
    @EJB
    private ControleProcessoAtividadeRepositorio repositorio;
    
    public synchronized void iniciaExecucao(ControleExecucaoTO execucao){
        map.put(execucao.getIdControle(), execucao);
        
        ControleProcessoAtividade controle = repositorio.obterPorID(execucao.getIdControle());
        controle.setSituacao((short) ProcessoSituacao.EM_PROCESSAMENTO.getId());
        controle.setItensProcessados(0);
        repositorio.atualizar(controle);
    }
    
    public synchronized void excluiExecucao(ControleExecucaoTO execucao){
        map.remove(execucao.getIdControle());
    }
    
    public synchronized void iniciaProcessamentoItem(Integer idControle){
        ControleExecucaoTO execucao = map.get(idControle);
        execucao.processaItem();
    }
    
    public synchronized void finalizaProcessamentoItem(Integer idControle){
        ControleExecucaoTO execucao = map.get(idControle);
        execucao.finalizaItem();
        
        ControleProcessoAtividade controle = repositorio.obterPorID(idControle);
        controle.setItensProcessados(execucao.getItensExecutados());
        repositorio.atualizar(controle);
    }
    
	public synchronized Boolean atingiuLimiteProcessamento(Integer idControle){
		return map.get(idControle).estaNoLimite();
	}
	
	public synchronized Boolean execucaoConcluida(Integer idControle){
	    return map.get(idControle).execucaoConcluida();
	}
	
	public ControleExecucaoTO obterDadosExecucao(Integer idControle){
	    return map.get(idControle);
	}
}