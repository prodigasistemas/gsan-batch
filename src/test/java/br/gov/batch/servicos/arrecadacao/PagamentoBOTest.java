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
	
	private ConsultaCodigoBarrasTO to;
	
	@Before
	public void setup() {
		pagamentoBO = new PagamentoBO();
		
		to = new ConsultaCodigoBarrasTO();
		to.setTipoPagamento(TipoPagamento.CONTA);
        to.setValorCodigoBarra(BigDecimal.valueOf(14.00));
        to.setMesAnoReferenciaConta("072014");
        to.setDigitoVerificadorRefContaModulo10(10);
        to.setIdLocalidade(17);
        to.setMatriculaImovel(1101625);
        to.setTipoDocumento(DocumentoTipo.parse(1));
	}
	
	@Test
	public void codigoDeBarraValido() {
		mockParametros();
		
		String codigoDeBarra = pagamentoBO.obterCodigoBarra(to);
		
		assertEquals("826600000000140000220172001101625018072014400039", codigoDeBarra);
	}
	
	private void mockParametros() {
		expect(sistemaParametrosMock.getNumeroModuloDigitoVerificador()).andReturn(Short.valueOf("10"));
		expect(sistemaParametrosMock.getCodigoEmpresaFebraban()).andReturn(Short.valueOf("22")).times(2);
		expect(sistemaParametrosMock.moduloVerificador11()).andReturn(false);
		replay(sistemaParametrosMock);
	}
}
