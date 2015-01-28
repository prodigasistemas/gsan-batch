package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.servicos.arrecadacao.PagamentoBO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.Localidade;
import br.gov.model.cobranca.CobrancaDocumento;
import br.gov.model.cobranca.DocumentoTipo;
import br.gov.servicos.arrecadacao.DebitoAutomaticoRepositorio;
import br.gov.servicos.to.DadosBancariosTO;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo01DadosCobrancaTest {

	@TestSubject
    private ArquivoTextoTipo01DadosCobranca arquivo;
	@Mock
    private DebitoAutomaticoRepositorio debitoAutomaticoRepositorioMock;
    
	@Mock
    private PagamentoBO pagamentoBOMock;
	
	private Imovel imovel;
	private CobrancaDocumento cobrancaDocumento;
	
	@Before
    public void init(){
		imovel = new Imovel(1234567);
       
		cobrancaDocumento = new CobrancaDocumento();
        cobrancaDocumento.setEmissao(new Date());
        cobrancaDocumento.setValorDocumento(new BigDecimal(100.00));
        cobrancaDocumento.setLocalidade(new Localidade(1));
        cobrancaDocumento.setImovel(imovel);
        cobrancaDocumento.setNumeroSequenciaDocumento(1);
        cobrancaDocumento.setDocumentoTipo(DocumentoTipo.AVISO_CORTE.getId());
        
        arquivo = new ArquivoTextoTipo01DadosCobranca(imovel, cobrancaDocumento);
    }
    
    @Test
    public void buildArquivoDadosCobranca() {
    	carregarMocks();
    	
    	Map<Integer, StringBuilder> mapDados = arquivo.build();
    	String linha = getLinha(mapDados);
    	
    	System.out.println(mapDados.get(9));
    	System.out.println(mapDados.get(34));
    	System.out.println(mapDados.get(42));
    	System.out.println(mapDados.get(45));
    	assertNotNull(mapDados);
    	assertEquals(4, mapDados.keySet().size());
    	assertEquals(linha, "         333333333333333333333333333333333333333333333333BANCO DO BRASIL00000         20150123");
    }
    
    private String getLinha(Map<Integer, StringBuilder> mapDados) {
    	StringBuilder builder = new StringBuilder();
    	
    	Collection<StringBuilder> dados = mapDados.values();
    	
    	Iterator<StringBuilder> it = dados.iterator();
    	
    	while (it.hasNext()) {
    		builder.append(it.next());
    	}
    	
    	return builder.toString();
    }
    
    private void carregarMocks() {

    	DadosBancariosTO to = new DadosBancariosTO();
    	to.setCodigoAgencia("00000");
    	to.setDescricaoBanco("BANCO DO BRASIL");
    	
    	expect(debitoAutomaticoRepositorioMock.dadosBancarios(imovel.getId())).andReturn(to);
    	replay(debitoAutomaticoRepositorioMock);
    	
    	expect(pagamentoBOMock.obterCodigoBarra(anyObject())).andReturn("333333333333333333333333333333333333333333333333");
    	replay(pagamentoBOMock);
    
    }
    
}
