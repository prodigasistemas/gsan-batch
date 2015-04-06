package br.gov.batch.servicos.faturamento.to;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.FaturamentoAtividadeCronogramaRota;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.Rota;

public class FaturamentoImovelTO implements Serializable{
	private static final long serialVersionUID = -8120350363915598518L;
	
//	private Imovel imovel;
	private Integer idImovel;
	private Date dataVencimentoConta;
	private Rota rota;
	private FaturamentoAtividadeCronogramaRota faturamentoAtivCronRota;
	private Collection colecaoResumoFaturamento;
	private Boolean faturamentoAntecipado;
	private Integer anoMesFaturamento;
	private FaturamentoGrupo faturamentoGrupo;
	
	public Rota getRota() {
		return rota;
	}
	public void setRota(Rota rota) {
		this.rota = rota;
	}
	
	public Integer getIdImovel() {
        return idImovel;
    }
    public void setIdImovel(Integer idImovel) {
        this.idImovel = idImovel;
    }
    //	public Imovel getImovel() {
//		return imovel;
//	}
//	public void setImovel(Imovel imovel) {
//		this.imovel = imovel;
//	}
	public FaturamentoAtividadeCronogramaRota getFaturamentoAtivCronRota() {
		return faturamentoAtivCronRota;
	}
	public void setFaturamentoAtivCronRota(FaturamentoAtividadeCronogramaRota faturamentoAtivCronRota) {
		this.faturamentoAtivCronRota = faturamentoAtivCronRota;
	}
	public Collection getColecaoResumoFaturamento() {
		return colecaoResumoFaturamento;
	}
	public void setColecaoResumoFaturamento(Collection colecaoResumoFaturamento) {
		this.colecaoResumoFaturamento = colecaoResumoFaturamento;
	}
	public Boolean getFaturamentoAntecipado() {
		return faturamentoAntecipado;
	}
	public void setFaturamentoAntecipado(Boolean faturamentoAntecipado) {
		this.faturamentoAntecipado = faturamentoAntecipado;
	}
	public Integer getAnoMesFaturamento() {
		return anoMesFaturamento;
	}
	public void setAnoMesFaturamento(Integer anoMesFaturamento) {
		this.anoMesFaturamento = anoMesFaturamento;
	}
	public FaturamentoGrupo getFaturamentoGrupo() {
		return faturamentoGrupo;
	}
	public void setFaturamentoGrupo(FaturamentoGrupo faturamentoGrupo) {
		this.faturamentoGrupo = faturamentoGrupo;
	}
	public Date getDataVencimentoConta() {
		return dataVencimentoConta;
	}
	public void setDataVencimentoConta(Date dataVencimentoConta) {
		this.dataVencimentoConta = dataVencimentoConta;
	}
}
