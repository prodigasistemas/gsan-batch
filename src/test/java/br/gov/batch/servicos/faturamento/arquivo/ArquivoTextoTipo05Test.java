package br.gov.batch.servicos.faturamento.arquivo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.CreditoTipo;
import br.gov.servicos.faturamento.FaturamentoRepositorio;
import br.gov.servicos.to.CreditoRealizadoTO;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo05Test {

	@TestSubject
	private ArquivoTextoTipo05 arquivoTextoTipo05;
	
	@Mock
	private FaturamentoRepositorio faturamentoRepositorio;
	
	private Conta conta;
	
	private CreditoRealizadoTO creditoRealizadoTO;
	
	private List<CreditoRealizadoTO> listaCreditoRealizadoTO;
	
	@Before
	public void setup(){
		arquivoTextoTipo05 = new ArquivoTextoTipo05();
		listaCreditoRealizadoTO = new ArrayList<CreditoRealizadoTO>();
		creditoRealizadoTO = new CreditoRealizadoTO();
		conta = new Conta();
		conta.setId(1);
		conta.setImovel(new Imovel(1));
		
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
		
		String linha = arquivoTextoTipo05.build(conta);
		int tamanhoLinha = linha.length();
		
		System.out.println(linha);
		System.out.println(tamanhoLinha);
		
		assertTrue(tamanhoLinha >= 121);
	}
	
	@Test
	public void arquivoTextoTipo05BuildLinha(){
		carregarMocks();
		
		assertNotNull(arquivoTextoTipo05.build(conta));
	}
	
	public void carregarMocks() {
		expect(faturamentoRepositorio.buscarCreditoRealizado(conta)).andReturn(listaCreditoRealizadoTO);
		replay(faturamentoRepositorio);
	}
	

}
