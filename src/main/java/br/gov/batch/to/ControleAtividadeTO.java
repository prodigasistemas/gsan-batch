package br.gov.batch.to;

public class ControleAtividadeTO {
    private String nomeArquivoBatch;
    
    private Integer idControleAtividade;

    public ControleAtividadeTO(String nomeArquivoBatch, Integer idControleAtividade) {
        this.nomeArquivoBatch = nomeArquivoBatch;
        this.idControleAtividade = idControleAtividade;
    }

    public String getNomeArquivoBatch() {
        return nomeArquivoBatch;
    }

    public void setNomeArquivoBatch(String nomeArquivoBatch) {
        this.nomeArquivoBatch = nomeArquivoBatch;
    }

    public Integer getIdControleAtividade() {
        return idControleAtividade;
    }

    public void setIdControleAtividade(Integer idControleAtividade) {
        this.idControleAtividade = idControleAtividade;
    }
}
