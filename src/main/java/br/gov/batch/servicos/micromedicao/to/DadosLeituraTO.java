package br.gov.batch.servicos.micromedicao.to;

import java.io.Serializable;

public class DadosLeituraTO implements Serializable {

	private static final long serialVersionUID = 6445673308623699934L;

	private Integer idImovel;
	private Integer idRota;
	private Integer anoMesFaturamento;
	private Integer idGrupo;
	
	public DadosLeituraTO() {
		super();
	}

	public Integer getIdImovel() {
		return idImovel;
	}

	public void setIdImovel(Integer idImovel) {
		this.idImovel = idImovel;
	}

	public Integer getIdRota() {
		return idRota;
	}

	public void setIdRota(Integer idRota) {
		this.idRota = idRota;
	}

	public Integer getAnoMesFaturamento() {
		return anoMesFaturamento;
	}

	public void setAnoMesFaturamento(Integer anoMesFaturamento) {
		this.anoMesFaturamento = anoMesFaturamento;
	}

	public Integer getIdGrupo() {
		return idGrupo;
	}

	public void setIdGrupo(Integer idGrupo) {
		this.idGrupo = idGrupo;
	}
}
