package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.cadastro.Imovel;
import br.gov.model.cobranca.CobrancaDocumento;
import br.gov.model.cobranca.CobrancaDocumentoItem;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.ContaGeral;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cobranca.CobrancaDocumentoItemRepositorio;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo07Test {

	@TestSubject
	private ArquivoTextoTipo07 arquivoTextoTipo07;

	@Mock
	private CobrancaDocumentoItemRepositorio cobrancaDocumentoItemRepositorioMock;
	
	private Imovel imovel;
	private CobrancaDocumento cobrancaDocumento;
	
	private int TAMANHO_LINHA = 53;
	
	@Before
	public void setup() {
		imovel = new Imovel(1);
		cobrancaDocumento = new CobrancaDocumento();
		cobrancaDocumento.setId(1);
		
		arquivoTextoTipo07 = new ArquivoTextoTipo07();
	}
	
	@Test
	public void buildArquivoTextoTipo07ItensInferiorQuantidadeContas() {
		carregarMockItensInferiorQuantidadeContas();
		
		String linha = arquivoTextoTipo07.build(imovel, cobrancaDocumento, 17);
		
		assertNotNull(linha);
		assertEquals(getRegistroValidoItensInferiorQuantidadeContas(), linha);
	}
	
	@Test
	public void buildArquivoTextoTipo07VencimentoAnterior() {
		carregarMockVencimentoAnterior();
		
		String linha = arquivoTextoTipo07.build(imovel, cobrancaDocumento, 2);
		
		assertNotNull(linha);
		assertEquals(getRegistroValidoVencimentoAnterior(), linha);
	}
	
	@Test
	public void buildArquivoTextoTipo07TamanhoLinha() {
		carregarMockItensInferiorQuantidadeContas();
		
		String linha = arquivoTextoTipo07.build(imovel, cobrancaDocumento, 17);
		
		String[] linhas = linha.split(System.getProperty("line.separator"));
		
		for (int i = 0; i < linhas.length; i++) {
			assertEquals(TAMANHO_LINHA, linhas[i].length());
		}
	}
	
	private void carregarMockItensInferiorQuantidadeContas() {
		expect(cobrancaDocumentoItemRepositorioMock.buscarCobrancaDocumentoItens(1)).andReturn(getItensInferiorQuantidadeContas());
		replay(cobrancaDocumentoItemRepositorioMock);
	}

	private void carregarMockVencimentoAnterior() {
		expect(cobrancaDocumentoItemRepositorioMock.buscarCobrancaDocumentoItens(1)).andReturn(getItensVencimentoAnterior());
		replay(cobrancaDocumentoItemRepositorioMock);
	}
	
	private List<CobrancaDocumentoItem> getItensInferiorQuantidadeContas() {
		List<CobrancaDocumentoItem> lista = new ArrayList<CobrancaDocumentoItem>();
		
		CobrancaDocumentoItem item = new CobrancaDocumentoItem();
		item.setCobrancaDocumento(cobrancaDocumento);
		item.setValorItemCobrado(BigDecimal.valueOf(14.56));
		item.setValorAcrescimos(BigDecimal.valueOf(1.23));
		Conta conta = new Conta();
		conta.setReferencia(201412);
		conta.setDataVencimentoConta(Utilitarios.converterStringParaData("2015-01-05", FormatoData.ANO_MES_DIA_SEPARADO));
		ContaGeral contaGeral = new ContaGeral();
		contaGeral.setConta(conta);
		item.setContaGeral(contaGeral);
		
		lista.add(item);
		
		item = new CobrancaDocumentoItem();
		item.setCobrancaDocumento(cobrancaDocumento);
		item.setValorItemCobrado(BigDecimal.valueOf(20.12));
		item.setValorAcrescimos(BigDecimal.valueOf(3.56));
		conta = new Conta();
		conta.setReferencia(201501);
		conta.setDataVencimentoConta(Utilitarios.converterStringParaData("2015-02-07", FormatoData.ANO_MES_DIA_SEPARADO));
		contaGeral = new ContaGeral();
		contaGeral.setConta(conta);
		item.setContaGeral(contaGeral);
		
		lista.add(item);

		return lista;
	}
	
	private String getRegistroValidoItensInferiorQuantidadeContas() {
    	StringBuilder linha = new StringBuilder();
    	linha.append("0700000000120141200000000014.562015010500000000001.23");
    	linha.append(System.getProperty("line.separator"));
    	linha.append("0700000000120150100000000020.122015020700000000003.56");
    	linha.append(System.getProperty("line.separator"));
    	return linha.toString();
    }
	
	private List<CobrancaDocumentoItem> getItensVencimentoAnterior() {
		List<CobrancaDocumentoItem> lista = new ArrayList<CobrancaDocumentoItem>();
		
		CobrancaDocumentoItem item = new CobrancaDocumentoItem();
		item.setCobrancaDocumento(cobrancaDocumento);
		item.setValorItemCobrado(BigDecimal.valueOf(14.00));
		item.setValorAcrescimos(BigDecimal.valueOf(2.00));
		Conta conta = new Conta();
		conta.setReferencia(201409);
		conta.setDataVencimentoConta(Utilitarios.converterStringParaData("2014-10-08", FormatoData.ANO_MES_DIA_SEPARADO));
		ContaGeral contaGeral = new ContaGeral();
		contaGeral.setConta(conta);
		item.setContaGeral(contaGeral);
		
		lista.add(item);

		item = new CobrancaDocumentoItem();
		item.setCobrancaDocumento(cobrancaDocumento);
		item.setValorItemCobrado(BigDecimal.valueOf(13.00));
		item.setValorAcrescimos(BigDecimal.valueOf(1.50));
		conta = new Conta();
		conta.setReferencia(201410);
		conta.setDataVencimentoConta(Utilitarios.converterStringParaData("2014-11-05", FormatoData.ANO_MES_DIA_SEPARADO));
		contaGeral = new ContaGeral();
		contaGeral.setConta(conta);
		item.setContaGeral(contaGeral);
		
		lista.add(item);
		
		item = new CobrancaDocumentoItem();
		item.setCobrancaDocumento(cobrancaDocumento);
		item.setValorItemCobrado(BigDecimal.valueOf(15.00));
		item.setValorAcrescimos(BigDecimal.valueOf(2.50));
		conta = new Conta();
		conta.setReferencia(201411);
		conta.setDataVencimentoConta(Utilitarios.converterStringParaData("2014-12-05", FormatoData.ANO_MES_DIA_SEPARADO));
		contaGeral = new ContaGeral();
		contaGeral.setConta(conta);
		item.setContaGeral(contaGeral);
		
		lista.add(item);
		
		item = new CobrancaDocumentoItem();
		item.setCobrancaDocumento(cobrancaDocumento);
		item.setValorItemCobrado(BigDecimal.valueOf(16.00));
		item.setValorAcrescimos(BigDecimal.valueOf(3.00));
		conta = new Conta();
		conta.setReferencia(201412);
		conta.setDataVencimentoConta(Utilitarios.converterStringParaData("2015-01-07", FormatoData.ANO_MES_DIA_SEPARADO));
		contaGeral = new ContaGeral();
		contaGeral.setConta(conta);
		item.setContaGeral(contaGeral);
		
		lista.add(item);
		return lista;
	}
	
	private String getRegistroValidoVencimentoAnterior() {
    	StringBuilder linha = new StringBuilder();
    	
    	linha.append("07000000001DB.ATE00000000058.002015010700000000009.00");
    	linha.append(System.getProperty("line.separator"));
    	linha.append("0700000000120141200000000016.002015010700000000003.00");
    	linha.append(System.getProperty("line.separator"));
    	return linha.toString();
    }
}
