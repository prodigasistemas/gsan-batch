package br.gov.batch.servicos.batch;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.to.ControleAtividadeTO;
import br.gov.model.batch.ControleProcessoAtividade;
import br.gov.model.batch.Processo;
import br.gov.model.batch.ProcessoAtividade;
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

    public ControleAtividadeTO iniciarProximaAtividadeBatch(Integer idProcessoIniciado, Integer idControleAtividade, Integer numeroItens) {
        ControleProcessoAtividade controleAtividade = repositorioControleAtividade.obterPorID(idControleAtividade);
        
        final short ordem = controleAtividade.getAtividade().getOrdemExecucao();
        
        Processo processo = obterProcessoBatch(idProcessoIniciado);
        
        List<ProcessoAtividade> atividades = processo.getAtividades().stream()
                .filter(e -> e.getOrdemExecucao().shortValue() > ordem)
                .collect(Collectors.toList());
        
        ControleAtividadeTO to = null;
        
        if (!atividades.isEmpty()){
            ControleProcessoAtividade controle = atividadeBO.cadastrarAtividade(idProcessoIniciado
                    , atividades.get(0).getNomeArquivoBatch()
                    , numeroItens);
            
            controle.getAtividade().getNomeArquivoBatch();
            
            to = new ControleAtividadeTO(controle.getAtividade().getNomeArquivoBatch(), controle.getId());
        }

        return to;
    }
}
