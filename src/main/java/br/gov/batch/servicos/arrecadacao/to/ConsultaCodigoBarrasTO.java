package br.gov.batch.servicos.arrecadacao.to;

import java.io.Serializable;
import java.math.BigDecimal;

import br.gov.model.cobranca.DocumentoTipo;
import br.gov.model.faturamento.TipoPagamento;

public class ConsultaCodigoBarrasTO implements Serializable{
    private static final long serialVersionUID = -7065413769080782311L;
    
    private TipoPagamento tipoPagamento;
    private BigDecimal valorCodigoBarra;
    private Integer idLocalidade;
    private Integer matriculaImovel;
    private String mesAnoReferenciaConta;
    private Integer digitoVerificadorRefContaModulo10;
    private Integer idTipoDebito;
    private String anoEmissaoGuiaPagamento;
    private String sequencialDocumentoCobranca;
    private DocumentoTipo tipoDocumento;
    private Integer idCliente;
    private Integer seqFaturaClienteResponsavel;
    private String idGuiaPagamento;
    
    public TipoPagamento getTipoPagamento() {
        return tipoPagamento;
    }
    public void setTipoPagamento(TipoPagamento tipoPagamento) {
        this.tipoPagamento = tipoPagamento;
    }
    public BigDecimal getValorCodigoBarra() {
        return valorCodigoBarra;
    }
    public void setValorCodigoBarra(BigDecimal valorCodigoBarra) {
        this.valorCodigoBarra = valorCodigoBarra;
    }
    public Integer getIdLocalidade() {
        return idLocalidade;
    }
    public void setIdLocalidade(Integer idLocalidade) {
        this.idLocalidade = idLocalidade;
    }
    public Integer getMatriculaImovel() {
        return matriculaImovel;
    }
    public void setMatriculaImovel(Integer matriculaImovel) {
        this.matriculaImovel = matriculaImovel;
    }
    public String getMesAnoReferenciaConta() {
        return mesAnoReferenciaConta;
    }
    public void setMesAnoReferenciaConta(String mesAnoReferenciaConta) {
        this.mesAnoReferenciaConta = mesAnoReferenciaConta;
    }
    public Integer getDigitoVerificadorRefContaModulo10() {
        return digitoVerificadorRefContaModulo10;
    }
    public void setDigitoVerificadorRefContaModulo10(Integer digitoVerificadorRefContaModulo10) {
        this.digitoVerificadorRefContaModulo10 = digitoVerificadorRefContaModulo10;
    }
    public Integer getIdTipoDebito() {
        return idTipoDebito;
    }
    public void setIdTipoDebito(Integer idTipoDebito) {
        this.idTipoDebito = idTipoDebito;
    }
    public String getAnoEmissaoGuiaPagamento() {
        return anoEmissaoGuiaPagamento;
    }
    public void setAnoEmissaoGuiaPagamento(String anoEmissaoGuiaPagamento) {
        this.anoEmissaoGuiaPagamento = anoEmissaoGuiaPagamento;
    }
    public String getSequencialDocumentoCobranca() {
        return sequencialDocumentoCobranca;
    }
    public void setSequencialDocumentoCobranca(String sequencialDocumentoCobranca) {
        this.sequencialDocumentoCobranca = sequencialDocumentoCobranca;
    }
    public DocumentoTipo getTipoDocumento() {
        return tipoDocumento;
    }
    public void setTipoDocumento(DocumentoTipo tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }
    public Integer getIdCliente() {
        return idCliente;
    }
    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }
    public Integer getSeqFaturaClienteResponsavel() {
        return seqFaturaClienteResponsavel;
    }
    public void setSeqFaturaClienteResponsavel(Integer seqFaturaClienteResponsavel) {
        this.seqFaturaClienteResponsavel = seqFaturaClienteResponsavel;
    }
    public String getIdGuiaPagamento() {
        return idGuiaPagamento;
    }
    public void setIdGuiaPagamento(String idGuiaPagamento) {
        this.idGuiaPagamento = idGuiaPagamento;
    }
}
