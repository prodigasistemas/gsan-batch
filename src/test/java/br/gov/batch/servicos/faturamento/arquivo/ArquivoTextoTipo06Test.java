package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.ImpostoTipo;
import br.gov.servicos.faturamento.ContaImpostosDeduzidosRepositorio;
import br.gov.servicos.to.ContaImpostosDeduzidosTO;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo06Test {

	@TestSubject
	private ArquivoTextoTipo06 arquivoTextoTipo06;
	
	@Mock
	private  ContaImpostosDeduzidosRepositorio contaImpostosDeduzidosRepositorio;
	
	private ContaImpostosDeduzidosTO contaTO;
	
	private List<ContaImpostosDeduzidosTO> lista;
	
	private Conta conta;
	
	@Before
	public void setup(){
		arquivoTextoTipo06 = new ArquivoTextoTipo06();
		lista = new ArrayList<ContaImpostosDeduzidosTO>();
		contaTO = new ContaImpostosDeduzidosTO();
		conta = new Conta();
		conta.setId(1);
		conta.setImovel(new Imovel(1));
		
		ImpostoTipo impostoTipo = new ImpostoTipo();
		impostoTipo.setDescricaoAbreviada("TESTE DESC ABREV");
		impostoTipo.setId(1);

		contaTO.setDescricaoImposto("DESC ABREV");
		contaTO.setPercentualAliquota(BigDecimal.valueOf(1));
		contaTO.setTipoImpostoId(1);
		contaTO.setValorImposto(BigDecimal.valueOf(1));
		
		lista.add(contaTO);
	}
	
	@Test
	public void buildArquivoTextoTipo05TamanhoLinha() {
		carregarMocks();
		
		String linha = arquivoTextoTipo06.build(conta);
		int tamanhoLinha = linha.length();
		
		System.out.println(linha);
		System.out.println(tamanhoLinha);
		
		assertTrue(tamanhoLinha >= 34);
	}
	
	@Test
	public void arquivoTextoTipo05BuildLinha(){
		carregarMocks();
		
		assertNotNull(arquivoTextoTipo06.build(conta));
	}
	
	public void carregarMocks() {
		expect(contaImpostosDeduzidosRepositorio.pesquisarParmsContaImpostosDeduzidos(conta.getId())).andReturn(lista);
		replay(contaImpostosDeduzidosRepositorio);
	}
}
