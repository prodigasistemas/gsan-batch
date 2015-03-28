package br.gov.batch.servicos.batch;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.to.ControleExecucaoTO;
import br.gov.model.batch.ControleProcessoAtividade;
import br.gov.servicos.batch.ControleProcessoAtividadeRepositorio;

@Stateless
public class ControleExecucaoBO {
    @EJB
    private ControleProcessoAtividadeRepositorio repositorio;
    
    public ControleExecucaoTO criaExecucaoAtividade(Integer idControleAtividade){
        ControleProcessoAtividade controle = repositorio.obterPorID(idControleAtividade);
        
        ControleExecucaoTO execucao = new ControleExecucaoTO(controle.getId(), controle.getTotalItens(), controle.getAtividade().getLimiteExecucao());
        
        return execucao;
        
    }
}
