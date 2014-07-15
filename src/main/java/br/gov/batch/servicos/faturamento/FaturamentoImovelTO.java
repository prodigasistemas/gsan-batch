package br.gov.batch.servicos.faturamento;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.FaturamentoAtividadeCronogramaRota;
import br.gov.model.faturamento.FaturamentoGrupo;

public class FaturamentoImovelTO implements Serializable{
	private static final long serialVersionUID = -8120350363915598518L;
	
	private Imovel imovel;
	private Boolean gerarAtividadeGrupoFaturamento;
	private Date dataVencimentoConta;
	private FaturamentoAtividadeCronogramaRota faturamentoAtivCronRota;
	private Collection colecaoResumoFaturamento;
	private Boolean faturamentoAntecipado;
	private Integer anoMesFaturamento;
	private FaturamentoGrupo faturamentoGrupo;
	
	public Imovel getImovel() {
		return imovel;
	}
	public void setImovel(Imovel imovel) {
		this.imovel = imovel;
	}
	public Boolean getGerarAtividadeGrupoFaturamento() {
		return gerarAtividadeGrupoFaturamento;
	}
	public void setGerarAtividadeGrupoFaturamento(Boolean gerarAtividadeGrupoFaturamento) {
		this.gerarAtividadeGrupoFaturamento = gerarAtividadeGrupoFaturamento;
	}
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