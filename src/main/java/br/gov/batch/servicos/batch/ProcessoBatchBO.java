package br.gov.batch.servicos.batch;

import java.util.Comparator;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.to.ControleExecucaoTO;
import br.gov.model.batch.ControleProcessoAtividade;
import br.gov.model.batch.Processo;
import br.gov.model.batch.ProcessoAtividade;
import br.gov.model.batch.ProcessoSituacao;
import br.gov.servicos.batch.ControleProcessoAtividadeRepositorio;
import br.gov.servicos.batch.ProcessoIniciadoRepositorio;

@Stateless
public class ProcessoBatchBO {

    @EJB
    private ProcessoIniciadoRepositorio repositorio;
    
    @EJB
    private ControleProcessoAtividadeRepositorio repositorioControleAtividade;
    
    @EJB
    private ProcessoAtividadeBO atividadeBO;
    
    public Processo obterProcessoBatch(Integer idProcessoIniciado){
        Processo processo = repositorio.obterPorID(idProcessoIniciado).getProcesso();

        processo.getAtividades().sort(Comparator.comparing(e -> e.getOrdemExecucao()));
        
        return processo;
    }
    
    public void finalizaAtividade(Integer idControleAtividade, ProcessoSituacao situacao) {
        ControleProcessoAtividade controleAtividade = repositorioControleAtividade.obterPorID(idControleAtividade);
        controleAtividade.setSituacao((short) situacao.getId());
        repositorioControleAtividade.atualizar(controleAtividade);
    }
    
    public ControleExecucaoTO iniciarProximaAtividadeBatch(Integer idProcessoIniciado, Integer idControleAtividade, Integer numeroItens) {
        ProcessoAtividade atividade = obterProximaAtividade(idControleAtividade);
        
        ControleExecucaoTO to = null;
        
        if (atividade != null){
            ControleProcessoAtividade controle = atividadeBO.prepararAtividade(idProcessoIniciado
                    , atividade.getNomeArquivoBatch()
                    , numeroItens);
            
            controle.getAtividade().getNomeArquivoBatch();
            
            to = new ControleExecucaoTO(controle.getId()
                    , controle.getTotalItens()
                    , controle.getAtividade().getLimiteExecucao()
                    , controle.getAtividade().getDescricao()
                    , controle.getAtividade().getNomeArquivoBatch());
        }

        return to;
    }

    public boolean continuaExecucao(Integer idProcessoIniciado, Integer idControleAtividade) {
        ProcessoAtividade atividade = obterProximaAtividade(idControleAtividade); 
        
        ControleProcessoAtividade proximaAtividade = repositorioControleAtividade.obterExecucaoExistente(idProcessoIniciado, atividade.getId());
        
        return proximaAtividade.concluidaComErro() || proximaAtividade.execucaoCancelada() || proximaAtividade.emEspera();
    }
    
    public boolean atividadeProcessaVariosItens(Integer idControleAtividade) {
        ProcessoAtividade atividade = obterProximaAtividade(idControleAtividade); 

        return atividade.isProcessaVariosItens();
    }
    
    public ProcessoAtividade obterProximaAtividade(Integer idControleAtividade){
        ControleProcessoAtividade controleAtividade = repositorioControleAtividade.obterPorID(idControleAtividade);
        
        final short ordem = controleAtividade.getAtividade().getOrdemExecucao();
        
        Processo processo = controleAtividade.getAtividade().getProcesso();
        
        processo.getAtividades().sort(Comparator.comparing(e -> e.getOrdemExecucao()));
        
        ProcessoAtividade atividade = processo.getAtividades().stream()
                .filter(e -> e.getOrdemExecucao().shortValue() > ordem)
                .findFirst().get();
        
        return atividade;
    }
}
