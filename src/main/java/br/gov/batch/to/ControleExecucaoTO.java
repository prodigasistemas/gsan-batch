package br.gov.batch.to;

public class ControleExecucaoTO {
    private Integer idControle;
    
    private String descAtividade;
    
    private Integer totalItens;
    
    private Short maximoExecucoes;
    
    private Short itensEmExecucao = (short) 0;
    
    private Integer itensExecutados = new Integer(0);

    public ControleExecucaoTO(Integer idControle, Integer totalItens, Short maximoExecucoes, String descAtividade) {
        this.idControle = idControle;
        this.totalItens = totalItens;
        this.maximoExecucoes = maximoExecucoes;
        this.descAtividade = descAtividade;
    }
    
    public boolean execucaoConcluida(){
        return totalItens.intValue() == itensExecutados.intValue();
    }
    
    public boolean estaNoLimite(){
        return maximoExecucoes.shortValue() == itensEmExecucao.shortValue();
    }
    
    public void finalizaItem(){
        itensEmExecucao--;
        itensExecutados++;
    }
    
    public void processaItem(){
        itensEmExecucao++;
    }

    public Integer getIdControle() {
        return idControle;
    }
    public void setIdControle(Integer idControle) {
        this.idControle = idControle;
    }
    public Integer getTotalItens() {
        return totalItens;
    }
    public void setTotalItens(Integer totalItens) {
        this.totalItens = totalItens;
    }
    public Short getMaximoExecucoes() {
        return maximoExecucoes;
    }
    public void setMaximoExecucoes(Short maximoItens) {
        this.maximoExecucoes = maximoItens;
    }
    public Integer getItensExecutados() {
        return itensExecutados;
    }
    public void setItensExecutados(Integer itensExecutados) {
        this.itensExecutados = itensExecutados;
    }
    public Short getItensEmExecucao() {
        return itensEmExecucao;
    }
    public void setItensEmExecucao(Short itensEmExecucao) {
        this.itensEmExecucao = itensEmExecucao;
    }
    public String getDescAtividade() {
        return descAtividade;
    }
    public void setDescAtividade(String descAtividade) {
        this.descAtividade = descAtividade;
    }
    public String toString() {
        return "ControleExecucaoTO [idControle=" + idControle + ", descAtividade=" + descAtividade + ", totalItens=" + totalItens + ", maximoExecucoes="
                + maximoExecucoes + ", itensEmExecucao=" + itensEmExecucao + ", itensExecutados=" + itensExecutados + "]";
    }
}
