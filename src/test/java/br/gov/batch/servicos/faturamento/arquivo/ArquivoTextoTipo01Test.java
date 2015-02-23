package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.Imovel;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo01Test {

	@TestSubject
	private ArquivoTextoTipo01 arquivo;
	
	private int TAMANHO_LINHA = 1526;

	@Mock
	ArquivoTextoTipo01DadosCliente dadosClienteMock;

	@Mock
	ArquivoTextoTipo01DadosCobranca dadosCobrancaMock;

	@Mock
	ArquivoTextoTipo01DadosConsumo dadosConsumoMock;

	@Mock
	ArquivoTextoTipo01DadosConta dadosContaMock;

	@Mock
	ArquivoTextoTipo01DadosFaturamento dadosFaturamentoMock;

	@Mock
	ArquivoTextoTipo01DadosLocalizacaoImovel dadosLocalizacaoImovelMock;
	
	private ArquivoTextoTO to;

	@Before
	public void setup() {
		arquivo = new ArquivoTextoTipo01();
		arquivo.setDadosCliente(dadosClienteMock);
		arquivo.setDadosCobranca(dadosCobrancaMock);
		arquivo.setDadosConsumo(dadosConsumoMock);
		arquivo.setDadosConta(dadosContaMock);
		arquivo.setDadosFaturamento(dadosFaturamentoMock);
		arquivo.setDadosLocalizacaoImovel(dadosLocalizacaoImovelMock);
		
		to = new ArquivoTextoTO();
		to.setImovel(new Imovel(1234567));
	}

	@Test
	public void buildArquivoTextoTipo01() {
		carregarMocks();

		String linha = arquivo.build(to);

		assertNotNull(linha);
		assertEquals(getLinhaValida(), linha);
	}

	@Test
	public void buildArquivoTextoTipo01TamanhoLinha() {
		carregarMocks();

		String linha = arquivo.build(to);
		assertTrue(linha.length() == TAMANHO_LINHA);
	}

	public void carregarMocks() {
		expect(dadosClienteMock.build(to)).andReturn(getMapCliente());
		replay(dadosClienteMock);

		expect(dadosCobrancaMock.build(to)).andReturn(getMapCobranca());
		replay(dadosCobrancaMock);

		expect(dadosConsumoMock.build(to)).andReturn(getMapConsumo());
		replay(dadosConsumoMock);

		expect(dadosContaMock.build(to)).andReturn(getMapConta());
		replay(dadosContaMock);

		expect(dadosFaturamentoMock.build(to)).andReturn(getMapFaturamento());
		replay(dadosFaturamentoMock);

		expect(dadosLocalizacaoImovelMock.build(to)).andReturn(getMapLocalizacao());
		replay(dadosLocalizacaoImovelMock);

	}

	private String getLinhaValida() {
		StringBuilder linha = new StringBuilder();

		linha.append("01001234567BELEM                    DESCRICAO DA LOCALIDADE  MARIA JOSÉ DA SILVA           201501222015012200100112341234123AV ALM BARROSO25 - MARCO BELEM 66093-906                              2015011                                                                                                             31BANCO DO BRASIL00000         201000020121000010      000000030.00 010000500000500000303.0 2.0 050.00000500001000000199999999900AVENIDAALMBARROSO10 - MARCO BELEM 66093-906                           9133224455 000000000MENSAGEM EM CONTA - 1                                                                               MENSAGEM EM CONTA - 2                                                                               MENSAGEM EM CONTA - 3                                                                               MENSAGEM QUITACAO ANUAL DE DEBITOS                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  000010000010         33333333333333333333333333333333333333333333333311111111111                                           201501221212888888888            20150122");
		linha.append(System.getProperty("line.separator"));
		return linha.toString();
	}

	private Map<Integer, StringBuilder> getMapCliente() {
		Map<Integer, StringBuilder> mapClientes = new HashMap<Integer, StringBuilder>();

		mapClientes.put(2, new StringBuilder("MARIA JOSÉ DA SILVA           "));
		mapClientes.put(7, new StringBuilder("                                                                                                             "));
		mapClientes.put(15, new StringBuilder("1"));
		mapClientes.put(35, new StringBuilder("11111111111       "));

		return mapClientes;
	}

	private Map<Integer, StringBuilder> getMapCobranca() {
		Map<Integer, StringBuilder> mapCobranca = new HashMap<Integer, StringBuilder>();

		mapCobranca.put(9, new StringBuilder("BANCO DO BRASIL00000"));
		mapCobranca.put(34, new StringBuilder("         333333333333333333333333333333333333333333333333"));
		mapCobranca.put(42, new StringBuilder("888888888"));
		mapCobranca.put(45, new StringBuilder("20150122"));

		return mapCobranca;
	}

	private Map<Integer, StringBuilder> getMapConsumo() {
		Map<Integer, StringBuilder> mapConsumo = new HashMap<Integer, StringBuilder>();

		mapConsumo.put(12, new StringBuilder("000020"));
		mapConsumo.put(15, new StringBuilder("000010"));
		mapConsumo.put(16, new StringBuilder("      "));
		mapConsumo.put(17, new StringBuilder("000000"));

		mapConsumo.put(18, new StringBuilder("030.00"));
		mapConsumo.put(20, new StringBuilder("01"));
		mapConsumo.put(21, new StringBuilder("0000500000500000303.0 2.0 050.00000500"));
		mapConsumo.put(43, new StringBuilder("      "));

		mapConsumo.put(44, new StringBuilder("      "));
		mapConsumo.put(32, new StringBuilder("000010"));
		mapConsumo.put(33, new StringBuilder("000010"));
		mapConsumo.put(25, new StringBuilder("00"));

		return mapConsumo;
	}

	private Map<Integer, StringBuilder> getMapConta() {
		Map<Integer, StringBuilder> mapConta = new HashMap<Integer, StringBuilder>();

		mapConta.put(3, new StringBuilder("2015012220150122"));
		mapConta.put(6, new StringBuilder("2015011"));
		mapConta.put(24, new StringBuilder("999999999"));
		mapConta.put(28, getMensagemConta());

		mapConta.put(14, new StringBuilder("1"));
		mapConta.put(30, new StringBuilder("                                                                                                                                                                                                                    "));
		mapConta.put(31, new StringBuilder("                                                                                                                                                                                                        "));
		mapConta.put(29, new StringBuilder("MENSAGEM QUITACAO ANUAL DE DEBITOS                                                                                      "));

		return mapConta;
	}

	private Map<Integer, StringBuilder> getMapFaturamento() {
		Map<Integer, StringBuilder> mapFatuamento = new HashMap<Integer, StringBuilder>();

		mapFatuamento.put(8, new StringBuilder("31"));
		mapFatuamento.put(10, new StringBuilder("         2"));
		mapFatuamento.put(13, new StringBuilder("12"));
		mapFatuamento.put(36, new StringBuilder("                                    "));

		mapFatuamento.put(38, new StringBuilder("1"));
		mapFatuamento.put(11, new StringBuilder("01"));
		mapFatuamento.put(19, new StringBuilder(" "));
		mapFatuamento.put(37, new StringBuilder("20150122"));

		mapFatuamento.put(39, new StringBuilder("2"));
		mapFatuamento.put(40, new StringBuilder("1"));
		mapFatuamento.put(41, new StringBuilder("2"));

		return mapFatuamento;
	}

	private Map<Integer, StringBuilder> getMapLocalizacao() {
		Map<Integer, StringBuilder> mapLocalizacao = new HashMap<Integer, StringBuilder>();

		mapLocalizacao.put(0, new StringBuilder("BELEM                    "));
		mapLocalizacao.put(1, new StringBuilder("DESCRICAO DA LOCALIDADE  "));
		mapLocalizacao.put(22, new StringBuilder("001"));
		mapLocalizacao.put(23, new StringBuilder("0000001"));
		mapLocalizacao.put(27, new StringBuilder("000000000"));
		mapLocalizacao.put(4, new StringBuilder("00100112341234123"));
		mapLocalizacao.put(5, new StringBuilder("AV ALM BARROSO25 - MARCO BELEM 66093-906                              "));
		mapLocalizacao.put(26, new StringBuilder("AVENIDAALMBARROSO10 - MARCO BELEM 66093-906                           9133224455 "));
		return mapLocalizacao;
	}

	private StringBuilder getMensagemConta() {
		StringBuilder mensagem = new StringBuilder();

		mensagem.append("MENSAGEM EM CONTA - 1                                                                               ")
				.append("MENSAGEM EM CONTA - 2                                                                               ")
				.append("MENSAGEM EM CONTA - 3                                                                               ");

		return mensagem;
	}
}
