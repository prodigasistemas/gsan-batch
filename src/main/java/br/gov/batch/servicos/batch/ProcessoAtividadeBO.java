package br.gov.batch.servicos.batch;

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.batch.ControleProcessoAtividade;
import br.gov.model.batch.Processo;
import br.gov.model.batch.ProcessoAtividade;
import br.gov.model.batch.ProcessoIniciado;
import br.gov.model.batch.ProcessoSituacao;
import br.gov.servicos.batch.ControleProcessoAtividadeRepositorio;
import br.gov.servicos.batch.ProcessoAtividadeRepositorio;

@Stateless
public class ProcessoAtividadeBO {

    @EJB
    private ProcessoAtividadeRepositorio repositorio;
    
    @EJB
    private ControleProcessoAtividadeRepositorio repositorioControle;
    
    @EJB
    private ProcessoBatchBO processoBO;
    
    public void cadastrarAtividadesDoProcesso(Integer idProcessoIniciado){
        repositorioControle.apagarAtividadesDeProcesso(idProcessoIniciado);
        
        ProcessoIniciado iniciado = new ProcessoIniciado();
        iniciado.setId(idProcessoIniciado);
        
        Processo processo = processoBO.obterProcessoBatch(idProcessoIniciado);
        
        for(ProcessoAtividade atividade: processo.getAtividades()){
            ControleProcessoAtividade controle = new ControleProcessoAtividade();
            controle.setAtividade(atividade);
            controle.setProcessoIniciado(iniciado);
            controle.setItensProcessados(0);
            controle.setInicio(new Date());
            controle.setTermino(null);
            controle.setSituacao((short) ProcessoSituacao.EM_ESPERA.getId());
            
            repositorioControle.salvar(controle);
        }
    }

    public ControleProcessoAtividade prepararAtividade(Integer idProcessoIniciado, String nomeArquivo, Integer numeroItens){
        ProcessoIniciado iniciado = new ProcessoIniciado();
        iniciado.setId(idProcessoIniciado);
        
        ProcessoAtividade atividade = repositorio.obterPeloNomeArquivo(nomeArquivo);
        
        ControleProcessoAtividade controle = repositorioControle.obterExecucaoExistente(idProcessoIniciado, atividade.getId());
        
        controle.setTotalItens(numeroItens);
        controle.setItensProcessados(0);
        controle.setInicio(new Date());
        controle.setTermino(null);
        
        repositorioControle.atualizar(controle);
        
        return controle;
    }
}
