package br.gov.batch.servicos.batch;

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.batch.ControleProcessoAtividade;
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
    
    public ControleProcessoAtividade cadastrarAtividade(Integer idProcessoIniciado, String nomeArquivo, Integer numeroRotas){
        ProcessoIniciado iniciado = new ProcessoIniciado();
        iniciado.setId(idProcessoIniciado);
        
        ProcessoAtividade atividade = repositorio.obterPeloNomeArquivo(nomeArquivo);
        
        ControleProcessoAtividade controle = new ControleProcessoAtividade();
        controle.setAtividade(atividade);
        controle.setProcessoIniciado(iniciado);
        controle.setTotalItens(numeroRotas);
        controle.setInicio(new Date());
        controle.setSituacao((short) ProcessoSituacao.EM_ESPERA.getId());
        
        repositorioControle.salvar(controle);
        
        return controle;
    }
}
