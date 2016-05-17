package br.gov.batch.servicos.faturamento.arquivo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.batch.servicos.arrecadacao.PagamentoBO;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.Localidade;
import br.gov.model.cobranca.CobrancaDocumento;
import br.gov.model.cobranca.DocumentoTipo;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.arrecadacao.DebitoAutomaticoRepositorio;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.to.DadosBancariosTO;

public class ArquivoTextoTipo01DadosCobrancaTest {

	@InjectMocks
	private ArquivoTextoTipo01DadosCobranca arquivo;
	
	@Mock
	private DebitoAutomaticoRepositorio debitoAutomaticoRepositorioMock;

	@Mock
	private PagamentoBO pagamentoBOMock;
	
    @Mock
    private ImovelRepositorio repositorioImovel;

	private Imovel imovel;
	private CobrancaDocumento cobrancaDocumento;
	private ArquivoTextoTO arquivoTextoTO;

	@Before
	public void init() {
	    
		imovel = new Imovel(1234567);

		cobrancaDocumento = new CobrancaDocumento();
		cobrancaDocumento.setEmissao(Utilitarios.converterStringParaData("2015-01-23", FormatoData.ANO_MES_DIA_SEPARADO));
		cobrancaDocumento.setValorDocumento(new BigDecimal(100.00));
		cobrancaDocumento.setLocalidade(new Localidade(1));
		cobrancaDocumento.setImovel(imovel);
		cobrancaDocumento.setNumeroSequenciaDocumento(1);
		cobrancaDocumento.setDocumentoTipo(DocumentoTipo.AVISO_CORTE.getId());

		arquivo = new ArquivoTextoTipo01DadosCobranca();
		
		arquivoTextoTO = new ArquivoTextoTO();
        arquivoTextoTO.setImovel(imovel);
		arquivoTextoTO.setCobrancaDocumento(cobrancaDocumento);
		
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void buildArquivoDadosCobranca() {
		carregarMocks();

		Map<Integer, StringBuilder> mapDados = arquivo.build(arquivoTextoTO);
		String linha = getLinha(mapDados);

		assertNotNull(mapDados);
		assertEquals(4, mapDados.keySet().size());
		assertEquals(linha, "         333333333333333333333333333333333333333333333333BANCO DO BRASIL00000         20150123");
	}

	private String getLinha(Map<Integer, StringBuilder> mapDados) {
		StringBuilder builder = new StringBuilder();

		Collection<StringBuilder> dados = mapDados.values();

		Iterator<StringBuilder> iterator = dados.iterator();
		while (iterator.hasNext()) {
			builder.append(iterator.next());
		}

		return builder.toString();
	}

	private void carregarMocks() {
        when(repositorioImovel.obterPorID(imovel.getId())).thenReturn(imovel);
	    
		DadosBancariosTO to = new DadosBancariosTO();
		to.setCodigoAgencia("00000");
		to.setDescricaoBanco("BANCO DO BRASIL");

		when(debitoAutomaticoRepositorioMock.dadosBancarios(imovel.getId())).thenReturn(to);

		when(pagamentoBOMock.obterCodigoBarra(any())).thenReturn("333333333333333333333333333333333333333333333333");
	}
}
