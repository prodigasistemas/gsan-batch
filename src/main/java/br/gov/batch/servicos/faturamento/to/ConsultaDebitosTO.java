package br.gov.batch.servicos.faturamento.to;

import java.io.Serializable;

import br.gov.model.faturamento.IndicadorDebito;

public class ConsultaDebitosTO implements Serializable{
    private static final long serialVersionUID = -7165662077201688983L;
    
    private IndicadorDebito indicadorDebito;

    public IndicadorDebito getIndicadorDebito() {
        return indicadorDebito;
    }

    public void setIndicadorDebito(IndicadorDebito indicadorDebito) {
        this.indicadorDebito = indicadorDebito;
    }
}
