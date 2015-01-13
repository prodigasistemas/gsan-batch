package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.DebitoCobrado;
import br.gov.model.faturamento.DebitoTipo;
import br.gov.model.financeiro.FinanciamentoTipo;
import br.gov.servicos.faturamento.DebitoCobradoRepositorio;
import br.gov.servicos.to.DebitoCobradoParcelamentoTO;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo04Test {

	@TestSubject
	private ArquivoTextoTipo04 arquivoTextoTipo04;
	
	private Imovel imovel;
	private Conta conta;
	
	private DebitoCobradoParcelamentoTO debitoCobradoParcelamentoTO;
	private DebitoCobrado debitoCobrado;
	
	private ArrayList<DebitoCobradoParcelamentoTO> debitosCobradosParcelamentos;
	private ArrayList<DebitoCobrado> debitosCobrados;
	
	@Mock
	private DebitoCobradoRepositorio debitoCobradoRepositorioMock;
	
	@Before
	public void setup() {
		imovel = new Imovel(1);
		
		debitoCobrado = new DebitoCobrado();

		conta = new Conta(10);
		conta.setImovel(imovel);
		
		DebitoTipo debitoTipo = new DebitoTipo(1);
		debitoTipo.setDescricao("AGUA TRATADA");

		debitoCobradoParcelamentoTO = new DebitoCobradoParcelamentoTO();
		debitoCobradoParcelamentoTO.setCodigoConstante(null);
		debitoCobradoParcelamentoTO.setNumeroPrestacaoDebito(Short.valueOf("1"));
		debitoCobradoParcelamentoTO.setTotalParcela(Short.valueOf("10"));
		debitoCobradoParcelamentoTO.setTotalPrestacao(new BigDecimal(5));
		
		debitosCobradosParcelamentos = new ArrayList<DebitoCobradoParcelamentoTO>();
		debitosCobradosParcelamentos.add(debitoCobradoParcelamentoTO);
		
		debitoCobrado.setConta(conta);
		debitoCobrado.setFinanciamentoTipo(new FinanciamentoTipo(FinanciamentoTipo.SERVICO_NORMAL));
		debitoCobrado.setAnoMesReferenciaDebito(201501);
		debitoCobrado.setValorPrestacao(new BigDecimal(5));
		debitoCobrado.setNumeroPrestacao(Short.valueOf("10"));
		debitoCobrado.setNumeroPrestacaoDebito(Short.valueOf("1"));
		debitoCobrado.setNumeroParcelaBonus(Short.valueOf("0"));
		debitoCobrado.setDebitoTipo(debitoTipo);
		
		debitosCobrados = new ArrayList<DebitoCobrado>();
		debitosCobrados.add(debitoCobrado);
		
		arquivoTextoTipo04 = new ArquivoTextoTipo04();
	}
	
	@Test
	public void buildArquivoTextoTipo04() {
		carregarMocks();
		
		assertNotNull(arquivoTextoTipo04.build(conta));
	}
	
	@Test
	public void buildArquivoTextoTipo04TamanhoLinha() {
		carregarMocks();
		
		String linha = arquivoTextoTipo04.build(conta);
		int tamanhoLinha = linha.length();
		
		System.out.println(linha);
		System.out.println(tamanhoLinha);
		
		assertTrue(tamanhoLinha == 244);
	}
	
	private void carregarMocks() {
		expect(debitoCobradoRepositorioMock.pesquisarDebitoCobradoParcelamento(conta)).andReturn(debitosCobradosParcelamentos);
		expect(debitoCobradoRepositorioMock.pesquisarDebitoCobradoNaoParcelamento(conta)).andReturn(debitosCobrados);
		replay(debitoCobradoRepositorioMock);
	}

}
