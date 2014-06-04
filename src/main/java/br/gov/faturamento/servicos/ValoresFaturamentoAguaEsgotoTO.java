package br.gov.faturamento.servicos;

import java.math.BigDecimal;

public class ValoresFaturamentoAguaEsgotoTO {
	
	private BigDecimal valorTotalAgua;
	
	private BigDecimal valorTotalEsgoto;
	
	public ValoresFaturamentoAguaEsgotoTO(){}

	public BigDecimal getValorTotalAgua() {
		return valorTotalAgua;
	}

	public void setValorTotalAgua(BigDecimal valorTotalAgua) {
		this.valorTotalAgua = valorTotalAgua;
	}

	public BigDecimal getValorTotalEsgoto() {
		return valorTotalEsgoto;
	}

	public void setValorTotalEsgoto(BigDecimal valorTotalEsgoto) {
		this.valorTotalEsgoto = valorTotalEsgoto;
	}
}