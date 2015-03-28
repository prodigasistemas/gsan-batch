package br.gov.batch.to;

public class ControleExecucaoTO {
    private Integer idControle;
    
    private Integer totalItens;
    
    private Short maximoItens;
    
    private Short itensEmExecucao = (short) 0;
    
    private Integer itensExecutados = new Integer(0);
    

    public ControleExecucaoTO(Integer idControle, Integer totalItens, Short maximoItens) {
        this.idControle = idControle;
        this.totalItens = totalItens;
        this.maximoItens = maximoItens;
    }
    
    public boolean execucaoConcluida(){
        return totalItens.intValue() == itensExecutados.intValue();
    }
    
    public boolean estaNoLimite(){
        return maximoItens.shortValue() == itensEmExecucao.shortValue();
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

    public Short getMaximoItens() {
        return maximoItens;
    }

    public void setMaximoItens(Short maximoItens) {
        this.maximoItens = maximoItens;
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
}
