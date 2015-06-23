package br.gov.batch.util;

import java.util.List;

import javax.batch.api.listener.JobListener;
import javax.batch.runtime.BatchStatus;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.model.batch.ControleProcessoAtividade;
import br.gov.model.batch.Processo;
import br.gov.model.batch.ProcessoSituacao;
import br.gov.servicos.batch.ControleProcessoAtividadeRepositorio;
import br.gov.servicos.batch.ProcessoRepositorio;

@Named
public class ErrorJobListener implements JobListener{
    @EJB
    protected BatchLogger logger;

    @Inject
    protected BatchUtil util;

    @EJB
    private ProcessoRepositorio repositorio;
    
    @EJB
    private ControleProcessoAtividadeRepositorio repositorioControle;
    
    public void beforeJob() throws Exception {
        
    }

    public void afterJob() throws Exception {
        if (util.getBatchStatus() == BatchStatus.FAILED){
            
            Integer idProcessoIniciado = Integer.valueOf(util.parametroDoJob("idProcessoIniciado"));
            
            Processo processo = repositorio.obterProcessoPeloIniciado(idProcessoIniciado);
            
            repositorio.terminaExecucaoProcesso(idProcessoIniciado, ProcessoSituacao.CONCLUIDO_COM_ERRO);
            
            if (util.parametroDoJob("idControleAtividade") != null){
                Integer idControleAtividade = Integer.valueOf(util.parametroDoJob("idControleAtividade"));
                
                repositorioControle.terminaExecucaoAtividade(idControleAtividade, ProcessoSituacao.CONCLUIDO_COM_ERRO);
                
                List<ControleProcessoAtividade> proximas = repositorioControle.proximasAtividades(idControleAtividade);
                
                proximas.forEach(e -> repositorioControle.terminaExecucaoAtividade(e.getId(), ProcessoSituacao.CANCELADO));
            }

            logger.error(util.parametroDoJob("idProcessoIniciado"), "Erro ao concluir processo: " + processo.getDescricao());
        }
    }
}
