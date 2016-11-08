package br.gov.batch.servicos.arrecadacao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.batch.servicos.arrecadacao.to.ConsultaCodigoBarrasTO;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.cobranca.DocumentoTipo;
import br.gov.model.faturamento.TipoPagamento;

public class PagamentoBOTest {

	@InjectMocks
	private PagamentoBO pagamentoBO;
	
	@Mock
	private SistemaParametros sistemaParametrosMock;
	
	@Before
	public void setup() {
		pagamentoBO = new PagamentoBO();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void codigoDeBarraQuantidadeDeDigitos() {
		mockParametros();
		
		String codigoDeBarra = pagamentoBO.obterCodigoBarra(getDadosParaConta());
		assertTrue(codigoDeBarra.length() == 48);
	}
	
	@Test
	public void codigoDeBarraValidoParaConta() {
		mockParametros();
		
		String codigoDeBarra = pagamentoBO.obterCodigoBarra(getDadosParaConta());
		assertEquals("826500000003474000220027068586860006012015400034", codigoDeBarra);
	}
	
	@Test
	public void codigoDeBarraValidoParaDocumentoCobrancaImovel() {
		mockParametros();
		
		String codigoDeBarra = pagamentoBO.obterCodigoBarra(getDadosParaDocumentoCobrancaImovel());
		assertEquals("826900000009958000220185049297560069967276010055", codigoDeBarra);
	}
	
	private void mockParametros() {
		when(sistemaParametrosMock.moduloVerificador11()).thenReturn(false);
		when(sistemaParametrosMock.getCodigoEmpresaFebraban()).thenReturn(Short.valueOf("22"));
	}
	
	private ConsultaCodigoBarrasTO getDadosParaConta() {
		ConsultaCodigoBarrasTO to = new ConsultaCodigoBarrasTO();
		to.setTipoPagamento(TipoPagamento.CONTA);
        to.setValorCodigoBarra(BigDecimal.valueOf(47.40));
        to.setMesAnoReferenciaConta("012015");
        to.setDigitoVerificadorRefContaModulo10(4);
        to.setIdLocalidade(2);
        to.setMatriculaImovel(6858686);
        to.setTipoDocumento(DocumentoTipo.parse(DocumentoTipo.CONTA.getId()));
        
        return to;
	}
	
	private ConsultaCodigoBarrasTO getDadosParaDocumentoCobrancaImovel() {
		ConsultaCodigoBarrasTO to = new ConsultaCodigoBarrasTO();
		to.setTipoPagamento(TipoPagamento.DOCUMENTO_COBRANCA_IMOVEL);
        to.setValorCodigoBarra(BigDecimal.valueOf(95.80));
        to.setIdLocalidade(18);
        to.setMatriculaImovel(4929756);
        to.setSequencialDocumentoCobranca("6967276");
        to.setTipoDocumento(DocumentoTipo.parse(DocumentoTipo.CONTA.getId()));
        
        return to;
	}
}
