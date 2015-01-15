package br.gov.batch.servicos.faturamento.to;

import java.io.Serializable;

public class VolumeMedioAguaEsgotoTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3360457013333261380L;

	private Integer consumoMedio;
	private Integer quantidadeMesesConsiderados;
	
	public VolumeMedioAguaEsgotoTO(Integer consumoMedio,
			Integer quantidadeMesesConsiderados) {
		super();
		this.consumoMedio = consumoMedio;
		this.quantidadeMesesConsiderados = quantidadeMesesConsiderados;
	}
	
	public Integer getConsumoMedio() {
		return consumoMedio;
	}
	public void setConsumoMedio(Integer consumoMedio) {
		this.consumoMedio = consumoMedio;
	}
	public Integer getQuantidadeMesesConsiderados() {
		return quantidadeMesesConsiderados;
	}
	public void setQuantidadeMesesConsiderados(Integer quantidadeMesesConsiderados) {
		this.quantidadeMesesConsiderados = quantidadeMesesConsiderados;
	}
	
	
}
