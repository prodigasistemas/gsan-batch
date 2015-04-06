package br.gov.batch.servicos.faturamento.arquivo;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.batch.servicos.arrecadacao.PagamentoBO;
import br.gov.batch.servicos.arrecadacao.to.ConsultaCodigoBarrasTO;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cobranca.CobrancaDocumento;
import br.gov.model.cobranca.DocumentoTipo;
import br.gov.model.faturamento.TipoPagamento;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.arrecadacao.DebitoAutomaticoRepositorio;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.to.DadosBancariosTO;

@Stateless
public class ArquivoTextoTipo01DadosCobranca {

	@EJB
    private DebitoAutomaticoRepositorio debitoAutomaticoRepositorio;
	
	@EJB
    private PagamentoBO pagamentoBO;
	
	@EJB
	private ImovelRepositorio imovelRepositorio;
	
	private Map<Integer, StringBuilder> dadosCobranca;
	
	private Imovel imovel;
	
	private CobrancaDocumento cobrancaDocumento;
		
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Map<Integer, StringBuilder> build(ArquivoTextoTO to) {
	    this.imovel = to.getImovel();
	    this.cobrancaDocumento = to.getCobrancaDocumento();
	    
		dadosCobranca = new HashMap<Integer, StringBuilder>();
		
		escreverDadosBancarios();
		escreverDadosCobranca();
		escreverCodigoDebitoAutomatico();
		escreverDataEmissaoDocumentoCobranca();
		
		return dadosCobranca;
	}
	
	private void escreverDadosBancarios() {
		StringBuilder builder = new StringBuilder();
		
        DadosBancariosTO dadosBancarios = debitoAutomaticoRepositorio.dadosBancarios(imovel.getId());

        if (dadosBancarios != null) {
            builder.append(Utilitarios.completaComEspacosADireita(15, dadosBancarios.getDescricaoBanco()));
            builder.append(Utilitarios.completaComEspacosADireita(5, dadosBancarios.getCodigoAgencia()));
        } else {
            builder.append(Utilitarios.completaComEspacosADireita(20, ""));
        }
        
        dadosCobranca.put(9, builder);
    }
	
	private void escreverDadosCobranca() {
		StringBuilder builder = new StringBuilder();
		
        if (cobrancaDocumento != null){
            builder.append(Utilitarios.completaComEspacosADireita(9, cobrancaDocumento.getId()));
            
            ConsultaCodigoBarrasTO to = new ConsultaCodigoBarrasTO();
            to.setTipoPagamento(TipoPagamento.DOCUMENTO_COBRANCA_IMOVEL);
            to.setValorCodigoBarra(cobrancaDocumento.getValorDocumento());
            to.setIdLocalidade(cobrancaDocumento.getLocalidade().getId());
            to.setMatriculaImovel(cobrancaDocumento.getImovel().getId());
            to.setSequencialDocumentoCobranca(String.valueOf(cobrancaDocumento.getNumeroSequenciaDocumento()));
            to.setTipoDocumento(DocumentoTipo.parse(cobrancaDocumento.getDocumentoTipo()));
            
            builder.append(pagamentoBO.obterCodigoBarra(to));
        }else{
            builder.append(Utilitarios.completaComEspacosADireita(57, ""));
        }
        
        dadosCobranca.put(34, builder);
    }
	
	private void escreverCodigoDebitoAutomatico() {
		StringBuilder builder = new StringBuilder();
		
		if (imovel.getCodigoDebitoAutomatico() != null) {
			builder.append(Utilitarios.completaComEspacosADireita(9, imovel.getCodigoDebitoAutomatico()));
		} else {
			builder.append(Utilitarios.completaComEspacosADireita(9, ""));
		}
		
		dadosCobranca.put(42, builder);
	}
	
	private void escreverDataEmissaoDocumentoCobranca() {
		StringBuilder builder = new StringBuilder();
		
		if (cobrancaDocumento != null){
			builder.append(Utilitarios.formataData(cobrancaDocumento.getEmissao(), FormatoData.ANO_MES_DIA));
		} else {
			builder.append(Utilitarios.completaComEspacosADireita(8, ""));
		}
		
		dadosCobranca.put(45, builder);
	}
}
