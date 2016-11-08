package br.gov.batch.servicos.faturamento.arquivo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.DebitoTipo;
import br.gov.servicos.faturamento.DebitoCobradoRepositorio;
import br.gov.servicos.to.DebitoCobradoNaoParceladoTO;
import br.gov.servicos.to.ParcelaDebitoCobradoTO;

public class ArquivoTextoTipo04Test {

	@InjectMocks
	private ArquivoTextoTipo04 arquivo;
	
	private int TAMANHO_LINHA = 244;
	
	@Mock
	private DebitoCobradoRepositorio debitoCobradoRepositorioMock;

	private ParcelaDebitoCobradoTO debitoCobradoParcelamentoTO;
	private DebitoCobradoNaoParceladoTO debitoCobrado;

	private List<ParcelaDebitoCobradoTO> debitosCobradosParcelamentos;
	private List<DebitoCobradoNaoParceladoTO> debitosCobrados;

	private ArquivoTextoTO to;
	
	@Before
	public void setup() {
		debitoCobrado = new DebitoCobradoNaoParceladoTO();

		DebitoTipo debitoTipo = new DebitoTipo(1);
		debitoTipo.setDescricao("AGUA TRATADA");

		debitoCobradoParcelamentoTO = new ParcelaDebitoCobradoTO();
		debitoCobradoParcelamentoTO.setCodigoConstante(null);
		debitoCobradoParcelamentoTO.setNumeroPrestacaoDebito(Short.valueOf("1"));
		debitoCobradoParcelamentoTO.setTotalParcela(Short.valueOf("10"));
		debitoCobradoParcelamentoTO.setTotalPrestacao(new BigDecimal(5));

		debitosCobradosParcelamentos = new ArrayList<ParcelaDebitoCobradoTO>();
		debitosCobradosParcelamentos.add(debitoCobradoParcelamentoTO);

		debitoCobrado.setAnoMesReferencia(201501);
		debitoCobrado.setValorPrestacao(new BigDecimal(5));
		debitoCobrado.setTotalPrestacao(Short.valueOf("10"));
		debitoCobrado.setNumeroPrestacaoDebito(Short.valueOf("1"));
		debitoCobrado.setDebitoTipo(debitoTipo.getId());
		debitoCobrado.setDescricaoTipoDebito("CONSUMO DE AGUA");

		debitosCobrados = new ArrayList<DebitoCobradoNaoParceladoTO>();
		debitosCobrados.add(debitoCobrado);
		debitosCobrados.add(debitoCobrado);

		to = new ArquivoTextoTO();
		Imovel imovel = new Imovel(1);
		to.setImovel(imovel);
		Conta conta = new Conta(10);
		conta.setImovel(imovel);
		to.setConta(conta);
		arquivo = new ArquivoTextoTipo04();
		arquivo.setArquivoTextoTO(to);
		
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void buildArquivoTextoTipo04() {
		carregarMocks();
		
		assertNotNull(arquivo.build(to));
	}

	@Test
	public void buildArquivoTextoTipo04TamanhoLinha() {
		carregarMocks();
		
		String linha = arquivo.build(to);
		assertTrue(linha.length() == TAMANHO_LINHA);
	}
	
	private void carregarMocks() {
		when(debitoCobradoRepositorioMock.pesquisarDebitoCobradoParcelamento(to.getConta().getId())).thenReturn(debitosCobradosParcelamentos);
		when(debitoCobradoRepositorioMock.pesquisarDebitoCobradoSemParcelamento(to.getConta().getId())).thenReturn(debitosCobrados);
	}

}
