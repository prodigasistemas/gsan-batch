package br.gov.batch.util;

import javax.batch.api.listener.JobListener;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.ControleExecucaoAtividade;
import br.gov.batch.servicos.batch.ControleExecucaoBO;
import br.gov.batch.to.ControleExecucaoTO;
import br.gov.model.batch.ProcessoSituacao;
import br.gov.servicos.batch.ProcessoRepositorio;

@Named
public class IniciaAtividadeJobListener implements JobListener{
    @EJB
    protected BatchLogger logger;

    @Inject
    protected BatchUtil util;

    @EJB
    private ProcessoRepositorio repositorio;
    
    @Inject
    private ControleExecucaoAtividade controle;
    
    @EJB
    private ControleExecucaoBO execucaoBO;

    public void beforeJob() throws Exception {
        Integer idProcessoIniciado = Integer.valueOf(util.parametroDoJob("idProcessoIniciado"));
        
        repositorio.atualizaSituacaoProcesso(idProcessoIniciado, ProcessoSituacao.EM_PROCESSAMENTO);
        
        Integer idControleAtividade = Integer.valueOf(util.parametroDoJob("idControleAtividade"));
        
        ControleExecucaoTO execucaoTO = execucaoBO.criaExecucaoAtividade(idControleAtividade);
        
        controle.iniciaExecucao(execucaoTO);
        
        logger.logBackgroud("Inicio da atividade: " + execucaoTO.getDescAtividade());
    }

    public void afterJob() throws Exception {
    }
}
