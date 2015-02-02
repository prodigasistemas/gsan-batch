package br.gov.batch.servicos.cobranca.to;

import java.math.BigDecimal;
import java.util.Date;

import br.gov.model.cobranca.CobrancaDocumentoItem;

public class VencimentoAnteriorTO {

	private BigDecimal valorAnterior;
	private BigDecimal valorAcrescimosAnterior;
	private Date dataVencimentoAnterior;
	private CobrancaDocumentoItem cobrancaDocumentoItemNaoAnterior;
	
	public VencimentoAnteriorTO(){
		valorAnterior = BigDecimal.valueOf(0.00);
		valorAcrescimosAnterior = BigDecimal.valueOf(0.00);
	}
	
	public BigDecimal getValorAnterior() {
		return valorAnterior;
	}

	public void addValorAnterior(BigDecimal valorItemCobrado) {
		if (valorItemCobrado != null) {
			valorAnterior = valorAnterior.add(valorItemCobrado);
		}
	}

	public BigDecimal getValorAcrescimosAnterior() {
		return valorAcrescimosAnterior;
	}
	
	public void addValorAcrescimosAnterior(BigDecimal valorAcrescimos) {
		if (valorAcrescimos != null) {
			valorAcrescimosAnterior = valorAcrescimosAnterior.add(valorAcrescimos);
		}
	}
	
	public Date getDataVencimentoAnterior() {
		return dataVencimentoAnterior;
	}

	public void setDataVencimentoAnterior(Date dataVencimentoAnterior) {
		this.dataVencimentoAnterior = dataVencimentoAnterior;
	}

	public CobrancaDocumentoItem getCobrancaDocumentoItemNaoAnterior() {
		return cobrancaDocumentoItemNaoAnterior;
	}

	public void setCobrancaDocumentoItemNaoAnterior(CobrancaDocumentoItem cobrancaDocumentoItemNaoAnterior) {
		this.cobrancaDocumentoItemNaoAnterior = cobrancaDocumentoItemNaoAnterior;
	}
}
