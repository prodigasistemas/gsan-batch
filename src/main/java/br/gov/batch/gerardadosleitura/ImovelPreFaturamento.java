package br.gov.batch.gerardadosleitura;

import java.io.Serializable;

public class ImovelPreFaturamento implements Serializable {
	private static final long serialVersionUID = 1812860262352781388L;
	
	private Long idImovel;
	
	private Integer anoMesFaturamento;
	
	public ImovelPreFaturamento() {
	}

	public Long getIdImovel() {
		return idImovel;
	}

	public void setIdImovel(Long idImovel) {
		this.idImovel = idImovel;
	}

	public Integer getAnoMesFaturamento() {
		return anoMesFaturamento;
	}

	public void setAnoMesFaturamento(Integer anoMesFaturamento) {
		this.anoMesFaturamento = anoMesFaturamento;
	}
}
