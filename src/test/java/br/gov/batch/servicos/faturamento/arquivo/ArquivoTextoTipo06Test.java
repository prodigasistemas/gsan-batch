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
import br.gov.model.faturamento.ImpostoTipo;
import br.gov.servicos.faturamento.ContaImpostosDeduzidosRepositorio;
import br.gov.servicos.to.ContaImpostosDeduzidosTO;

public class ArquivoTextoTipo06Test {

	@InjectMocks
	private ArquivoTextoTipo06 arquivo;

	private int TAMANHO_LINHA = 34;

	@Mock
	private ContaImpostosDeduzidosRepositorio contaImpostosDeduzidosRepositorio;

	private ContaImpostosDeduzidosTO contaTO;
	private List<ContaImpostosDeduzidosTO> lista;
	
	private ArquivoTextoTO to;

	@Before
	public void setup() {
		Conta conta = new Conta();
		conta.setId(1);
		conta.setImovel(new Imovel(1));
		to = new ArquivoTextoTO();
		to.setConta(conta);
		arquivo = new ArquivoTextoTipo06();
		arquivo.setArquivoTextoTO(to);

		lista = new ArrayList<ContaImpostosDeduzidosTO>();
		contaTO = new ContaImpostosDeduzidosTO();

		ImpostoTipo impostoTipo = new ImpostoTipo();
		impostoTipo.setDescricaoAbreviada("TESTE DESC ABREV");
		impostoTipo.setId(1);

		contaTO.setDescricaoImposto("DESC ABREV");
		contaTO.setPercentualAliquota(BigDecimal.valueOf(1));
		contaTO.setTipoImpostoId(1);
		contaTO.setValorImposto(BigDecimal.valueOf(1));

		lista.add(contaTO);
		
		MockitoAnnotations.initMocks(this);
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

	public void carregarMocks() {
		when(contaImpostosDeduzidosRepositorio.pesquisarParmsContaImpostosDeduzidos(to.getConta().getId())).thenReturn(lista);
	}
}
