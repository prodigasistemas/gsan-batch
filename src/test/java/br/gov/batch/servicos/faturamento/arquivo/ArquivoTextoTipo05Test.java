package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.CreditoTipo;
import br.gov.servicos.faturamento.FaturamentoRepositorio;
import br.gov.servicos.to.CreditoRealizadoTO;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo05Test {

	@TestSubject
	private ArquivoTextoTipo05 arquivo;
	
	private int TAMANHO_LINHA = 121;

	@Mock
	private FaturamentoRepositorio faturamentoRepositorio;

	private CreditoRealizadoTO creditoRealizadoTO;
	private List<CreditoRealizadoTO> listaCreditoRealizadoTO;
	
	private ArquivoTextoTO to;

	@Before
	public void setup() {
		Conta conta = new Conta();
		conta.setId(1);
		conta.setImovel(new Imovel(1));
		to = new ArquivoTextoTO();
		to.setConta(conta);
		arquivo = new ArquivoTextoTipo05();
		arquivo.setArquivoTextoTO(to);

		listaCreditoRealizadoTO = new ArrayList<CreditoRealizadoTO>();
		creditoRealizadoTO = new CreditoRealizadoTO();

		creditoRealizadoTO.setAnoMesReferenciaCredito(201501);
		creditoRealizadoTO.setNumeroPrestacaoCredito(Short.valueOf("1"));
		creditoRealizadoTO.setNumeroPrestacoesRestantes(Long.valueOf("1"));
		creditoRealizadoTO.setValorCredito(BigDecimal.valueOf(1));
		CreditoTipo creditoTipo = new CreditoTipo();
		creditoTipo.setId(2);
		creditoTipo.setDescricao("DEV PAGTOS DUPLICIDADE");
		creditoTipo.setCodigoConstante(1);
		creditoRealizadoTO.setCreditoTipo(creditoTipo);

		listaCreditoRealizadoTO.add(creditoRealizadoTO);
		listaCreditoRealizadoTO.add(creditoRealizadoTO);
	}

	@Test
	public void buildArquivoTextoTipo05TamanhoLinha() {
		carregarMocks();

		String linha = arquivo.build(to);
		assertTrue(linha.length() >= TAMANHO_LINHA);
	}

	@Test
	public void arquivoTextoTipo05BuildLinha() {
		carregarMocks();

		assertNotNull(arquivo.build(to));
	}
	
	@Test
	public void arquivoTextoTipo05Linha() {
		carregarMocks();
		
		String linha = arquivo.build(to);
		
		StringBuilder linhaCorreta = new StringBuilder("05000000001DEV PAGTOS DUPLICIDADE 01/2015 01/2015                                                    00000000002.001     ");
		linhaCorreta.append(System.getProperty("line.separator"));
		
		assertNotNull(linha);
		assertEquals(linhaCorreta.toString(), linha);
	}
	
	     

	public void carregarMocks() {
		expect(faturamentoRepositorio.buscarCreditoRealizado(to.getConta())).andReturn(listaCreditoRealizadoTO);
		replay(faturamentoRepositorio);
	}

}
