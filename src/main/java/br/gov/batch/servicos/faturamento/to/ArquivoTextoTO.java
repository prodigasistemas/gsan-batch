package br.gov.batch.servicos.faturamento.to;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.gov.model.cadastro.Imovel;
import br.gov.model.cobranca.CobrancaDocumento;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.Rota;

public class ArquivoTextoTO implements Serializable {

	private static final long serialVersionUID = 6607302754718865649L;

	private Imovel imovel;
	
	//TODO: Deixar apenas o id do imovel
	private Integer idImovel;

	private Conta conta;

	private Integer anoMesReferencia;

	private FaturamentoGrupo faturamentoGrupo;
	
	private Rota rota;
	
	private CobrancaDocumento cobrancaDocumento;
	
	private List<Integer> idsConsumoTarifaCategoria;
	
	private Integer sequenciaRota;
	
	private Integer idLeituraAnormalidade;

	public ArquivoTextoTO() {
		super();
	}

	public ArquivoTextoTO(
			Imovel imovel,
			Conta conta,
			Integer anoMesReferencia,
			FaturamentoGrupo faturamentoGrupo,
			Rota rota,
			CobrancaDocumento cobrancaDocumento) {
		super();
		this.imovel = imovel;
		this.conta = conta;
		this.anoMesReferencia = anoMesReferencia;
		this.faturamentoGrupo = faturamentoGrupo;
		this.rota = rota;
		this.cobrancaDocumento = cobrancaDocumento;
		this.idsConsumoTarifaCategoria = new ArrayList<Integer>();
	}

	public Imovel getImovel() {
		return imovel;
	}

	public void setImovel(Imovel imovel) {
		this.imovel = imovel;
	}

	public Conta getConta() {
		return conta;
	}

	public void setConta(Conta conta) {
		this.conta = conta;
	}

	public Integer getAnoMesReferencia() {
		return anoMesReferencia;
	}

	public void setAnoMesReferencia(Integer anoMesReferencia) {
		this.anoMesReferencia = anoMesReferencia;
	}

	public FaturamentoGrupo getFaturamentoGrupo() {
		return faturamentoGrupo;
	}

	public void setFaturamentoGrupo(FaturamentoGrupo faturamentoGrupo) {
		this.faturamentoGrupo = faturamentoGrupo;
	}

	public Rota getRota() {
		return rota;
	}

	public void setRota(Rota rota) {
		this.rota = rota;
	}

	public CobrancaDocumento getCobrancaDocumento() {
		return cobrancaDocumento;
	}

	public void setCobrancaDocumento(CobrancaDocumento cobrancaDocumento) {
		this.cobrancaDocumento = cobrancaDocumento;
	}

	public List<Integer> getIdsConsumoTarifaCategoria() {
		return idsConsumoTarifaCategoria;
	}

	public void addIdsConsumoTarifaCategoria(Integer id) {
	    if (this.idsConsumoTarifaCategoria == null){
	        this.idsConsumoTarifaCategoria = new ArrayList<Integer>();
	    }
		this.idsConsumoTarifaCategoria.add(id);
	}

	public Integer getSequenciaRota() {
		return sequenciaRota;
	}

	public void setSequenciaRota(Integer sequenciaRota) {
		this.sequenciaRota = sequenciaRota;
	}

	public Integer getIdLeituraAnormalidade() {
		return idLeituraAnormalidade;
	}

	public void setIdLeituraAnormalidade(Integer idLeituraAnormalidade) {
		this.idLeituraAnormalidade = idLeituraAnormalidade;
	}

    public Integer getIdImovel() {
        return idImovel;
    }

    public void setIdImovel(Integer idImovel) {
        this.idImovel = idImovel;
    }
}