package br.gov.batch.servicos.arrecadacao;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.servicos.arrecadacao.to.ConsultaCodigoBarrasTO;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.cobranca.DocumentoTipo;
import br.gov.model.faturamento.TipoPagamento;

@RunWith(EasyMockRunner.class)
public class PagamentoBOTest {

	@TestSubject
	private PagamentoBO pagamentoBO;
	
	@Mock
	private SistemaParametros sistemaParametrosMock;
	
	@Before
	public void setup() {
		pagamentoBO = new PagamentoBO();
	}

	@Test
	public void codigoDeBarraValidoParaConta() {
		mockParametros();
		
		String codigoDeBarra = pagamentoBO.obterCodigoBarra(getDadosParaConta());
		assertEquals("826400000004474000220027006858686014012015400034", codigoDeBarra);
	}
	
	@Test
	public void codigoDeBarraValidoParaDocumentoCobrancaImovel() {
		mockParametros();
		
		String codigoDeBarra = pagamentoBO.obterCodigoBarra(getDadosParaDocumentoCobrancaImovel());
		assertEquals("826500000003958000220185004929756007696727601150", codigoDeBarra);
	}
	
	private void mockParametros() {
		expect(sistemaParametrosMock.moduloVerificador11()).andReturn(false);
		expect(sistemaParametrosMock.getCodigoEmpresaFebraban()).andReturn(Short.valueOf("22"));
		replay(sistemaParametrosMock);
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
