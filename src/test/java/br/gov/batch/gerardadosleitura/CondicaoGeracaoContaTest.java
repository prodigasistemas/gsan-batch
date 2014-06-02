package br.gov.batch.gerardadosleitura;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.Status;
import br.gov.model.atendimentopublico.LigacaoAguaSituacao;
import br.gov.model.atendimentopublico.LigacaoEsgotoSituacao;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.CreditoRealizar;
import br.gov.model.faturamento.DebitoCobrar;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.model.faturamento.DebitoTipo;
import br.gov.model.faturamento.FaturamentoSituacaoTipo;
import br.gov.servicos.arrecadacao.PagamentoEJB;
import br.gov.servicos.faturamento.CreditoRealizarEJB;
import br.gov.servicos.faturamento.DebitoCobrarEJB;

@RunWith(EasyMockRunner.class)
public class CondicaoGeracaoContaTest {

	@TestSubject
	private CondicaoGeracaoConta condicaoGeracaoConta;
	
	private Imovel imovel;
	private LigacaoAguaSituacao ligacaoAguaSituacao;
	private LigacaoEsgotoSituacao ligacaoEsgotoSituacao;
	private boolean aguaEsgotoZerado;
	private int anoMesFaturamento;
	
	@Mock
	private DebitoCobrarEJB debitoCobrarEJBMock;
	
	@Mock
	private PagamentoEJB pagamentoEJBMock;
	
	@Mock
	private CreditoRealizarEJB creditoRealizarEJBMock;
	
	@Before
	public void setup(){
		condicaoGeracaoConta = new CondicaoGeracaoConta();
		
		anoMesFaturamento = 0;

		imovel = new Imovel();
		ligacaoAguaSituacao = new LigacaoAguaSituacao();
		ligacaoAguaSituacao.setId(LigacaoAguaSituacao.LIGADO);
		imovel.setLigacaoAguaSituacao(ligacaoAguaSituacao);
		
		ligacaoEsgotoSituacao = new LigacaoEsgotoSituacao();
		ligacaoEsgotoSituacao.setId(LigacaoEsgotoSituacao.LIGADO);
		imovel.setLigacaoEsgotoSituacao(ligacaoEsgotoSituacao);
	}
	

	@Test
	public void primeiraCondicaoNaoGeraContaComAguaEsgotoZerados() throws Exception {
		aguaEsgotoZerado = true;
		boolean naoGeraConta = condicaoGeracaoConta.primeiraCondicaoNaoGerarConta(imovel, aguaEsgotoZerado);
		
		assertTrue(naoGeraConta);
	}
	
	@Test
	public void primeiraCondicaoGeraContaSemAguaEsgotoZeradosELigado() throws Exception {
		aguaEsgotoZerado = false;
		boolean naoGeraConta = condicaoGeracaoConta.primeiraCondicaoNaoGerarConta(imovel, aguaEsgotoZerado);
		
		assertFalse(naoGeraConta);
	}

	@Test
	public void primeiraCondicaoNaoGeraContaSemAguaEsgotoZeradoEDesligado() throws Exception {
		aguaEsgotoZerado = false;

		ligacaoAguaSituacao.setId(0);
		ligacaoEsgotoSituacao.setId(0);
		
		boolean naoGeraConta = condicaoGeracaoConta.primeiraCondicaoNaoGerarConta(imovel, aguaEsgotoZerado);
		
		assertTrue(naoGeraConta);
	}
	
	@Test
	public void primeiraCondicaoNaoGeraContaSemAguaEsgotoZeradosDesligadoESemCondominio() throws Exception {
		aguaEsgotoZerado = false;
		
		ligacaoAguaSituacao.setId(0);
		ligacaoEsgotoSituacao.setId(0);
		
		boolean naoGeraConta = condicaoGeracaoConta.primeiraCondicaoNaoGerarConta(imovel, aguaEsgotoZerado);
		
		assertTrue(naoGeraConta);
	}
	
	@Test
	public void primeiraCondicaoNaoGeraContaSemAguaEsgotoZeradosDesligadoEComCondominio() throws Exception {
		aguaEsgotoZerado = false;

		ligacaoAguaSituacao.setId(0);
		ligacaoEsgotoSituacao.setId(0);
		
		imovel.setImovelCondominio(new Imovel());
		
		boolean naoGeraConta = condicaoGeracaoConta.primeiraCondicaoNaoGerarConta(imovel, aguaEsgotoZerado);
		
		assertFalse(naoGeraConta);
	}
	
	@Test
	public void segundaCondicaoGeraContaComDebitoCobrar() throws Exception {

		expect(debitoCobrarEJBMock.debitosCobrarPorImovelESituacao(imovel, DebitoCreditoSituacao.NORMAL, anoMesFaturamento))
			.andReturn(null);
		replay(debitoCobrarEJBMock);
		
		boolean geraConta = condicaoGeracaoConta.segundaCondicaoGerarConta(imovel, anoMesFaturamento);

		assertFalse(geraConta);
	}
	
	@Test
	public void segundaCondicaoGeraContaComParalisacaoFaturamento() throws Exception {

		Collection<DebitoCobrar> debitosCobrar = new ArrayList<DebitoCobrar>();
		DebitoCobrar debitoCobrar = new DebitoCobrar();
		debitosCobrar.add(debitoCobrar);
		
		expect(debitoCobrarEJBMock.debitosCobrarPorImovelESituacao(imovel, DebitoCreditoSituacao.NORMAL, anoMesFaturamento))
			.andReturn(debitosCobrar);
		replay(debitoCobrarEJBMock);
		
		FaturamentoSituacaoTipo faturamentoSituacaoTipo = new FaturamentoSituacaoTipo();
		faturamentoSituacaoTipo.setParalisacaoFaturamento(Status.ATIVO);
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
		
		boolean geraConta = condicaoGeracaoConta.segundaCondicaoGerarConta(imovel, anoMesFaturamento);

		assertFalse(geraConta);
	}
	
	@Test
	public void segundaCondicaoGeraContaQuandoHaDebitoSemPagamento() throws Exception {

		Collection<DebitoCobrar> debitosCobrar = new ArrayList<DebitoCobrar>();
		DebitoCobrar debitoCobrar = new DebitoCobrar();
		debitosCobrar.add(debitoCobrar);
		
		expect(debitoCobrarEJBMock.debitosCobrarPorImovelESituacao(imovel, DebitoCreditoSituacao.NORMAL, anoMesFaturamento))
			.andReturn(debitosCobrar);
		replay(debitoCobrarEJBMock);
		
		expect(pagamentoEJBMock.existeDebitoSemPagamento(debitosCobrar))
			.andReturn(false);
		replay(pagamentoEJBMock);
		
		FaturamentoSituacaoTipo faturamentoSituacaoTipo = new FaturamentoSituacaoTipo();
		faturamentoSituacaoTipo.setParalisacaoFaturamento(Status.INATIVO);
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
		
		boolean geraConta = condicaoGeracaoConta.segundaCondicaoGerarConta(imovel, anoMesFaturamento);

		assertFalse(geraConta);
	}
	
	@Test
	public void segundaCondicaoNaoGeraContaQuandoNaoHaDebitosCobrarAtivosENaoHaCreditosRealizar() throws Exception {

		Collection<DebitoCobrar> debitosCobrar = new ArrayList<DebitoCobrar>();
		
		DebitoTipo debitoTipo = new DebitoTipo();
		debitoTipo.setIndicadorGeracaoConta(Status.INATIVO);
		
		DebitoCobrar debitoCobrar = new DebitoCobrar();
		debitoCobrar.setDebitoTipo(debitoTipo);
		debitosCobrar.add(debitoCobrar);
		
		expect(debitoCobrarEJBMock.debitosCobrarPorImovelESituacao(imovel, DebitoCreditoSituacao.NORMAL, anoMesFaturamento))
			.andReturn(debitosCobrar);
		replay(debitoCobrarEJBMock);
		
		expect(pagamentoEJBMock.existeDebitoSemPagamento(debitosCobrar))
			.andReturn(true);
		replay(pagamentoEJBMock);
		
		expect(creditoRealizarEJBMock.pesquisarCreditoARealizar(imovel.getId(), DebitoCreditoSituacao.NORMAL, anoMesFaturamento))
			.andReturn(null);
		replay(creditoRealizarEJBMock);
		
		FaturamentoSituacaoTipo faturamentoSituacaoTipo = new FaturamentoSituacaoTipo();
		faturamentoSituacaoTipo.setParalisacaoFaturamento(Status.INATIVO);
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
		
		boolean geraConta = condicaoGeracaoConta.segundaCondicaoGerarConta(imovel, anoMesFaturamento);

		assertFalse(geraConta);
	}
	
	@Test
	public void segundaCondicaoGeraContaQuandoHaDebitosCobrarAtivosENaoHaCreditosRealizar() throws Exception {

		Collection<DebitoCobrar> debitosCobrar = new ArrayList<DebitoCobrar>();
		
		DebitoTipo debitoTipo = new DebitoTipo();
		debitoTipo.setIndicadorGeracaoConta(Status.ATIVO);
		
		DebitoCobrar debitoCobrar = new DebitoCobrar();
		debitoCobrar.setDebitoTipo(debitoTipo);
		debitosCobrar.add(debitoCobrar);
		
		expect(debitoCobrarEJBMock.debitosCobrarPorImovelESituacao(imovel, DebitoCreditoSituacao.NORMAL, anoMesFaturamento))
			.andReturn(debitosCobrar);
		replay(debitoCobrarEJBMock);
		
		expect(pagamentoEJBMock.existeDebitoSemPagamento(debitosCobrar))
			.andReturn(true);
		replay(pagamentoEJBMock);
		
		expect(creditoRealizarEJBMock.pesquisarCreditoARealizar(imovel.getId(), DebitoCreditoSituacao.NORMAL, anoMesFaturamento))
			.andReturn(null);
		replay(creditoRealizarEJBMock);
		
		FaturamentoSituacaoTipo faturamentoSituacaoTipo = new FaturamentoSituacaoTipo();
		faturamentoSituacaoTipo.setParalisacaoFaturamento(Status.INATIVO);
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
		
		boolean geraConta = condicaoGeracaoConta.segundaCondicaoGerarConta(imovel, anoMesFaturamento);

		assertTrue(geraConta);
	}
	
	@Test
	public void segundaCondicaoNaoGeraContaQuandoNaoHaDebitosCobrarAtivosEHaCreditosRealizarComDevolucao() throws Exception {

		Collection<DebitoCobrar> debitosCobrar = new ArrayList<DebitoCobrar>();
		
		DebitoTipo debitoTipo = new DebitoTipo();
		debitoTipo.setIndicadorGeracaoConta(Status.INATIVO);
		
		DebitoCobrar debitoCobrar = new DebitoCobrar();
		debitoCobrar.setDebitoTipo(debitoTipo);
		debitosCobrar.add(debitoCobrar);
		
		expect(debitoCobrarEJBMock.debitosCobrarPorImovelESituacao(imovel, DebitoCreditoSituacao.NORMAL, anoMesFaturamento))
			.andReturn(debitosCobrar);
		replay(debitoCobrarEJBMock);
		
		expect(pagamentoEJBMock.existeDebitoSemPagamento(debitosCobrar))
			.andReturn(true);
		replay(pagamentoEJBMock);
		
		Collection<CreditoRealizar> creditosRealizar = new ArrayList<CreditoRealizar>();
		CreditoRealizar creditoRealizar = new CreditoRealizar();
		creditosRealizar.add(creditoRealizar);
		
		expect(creditoRealizarEJBMock.pesquisarCreditoARealizar(imovel.getId(), DebitoCreditoSituacao.NORMAL, anoMesFaturamento))
			.andReturn(creditosRealizar);
		expect(creditoRealizarEJBMock.existeCreditoComDevolucao(creditosRealizar))
			.andReturn(true);
		replay(creditoRealizarEJBMock);
		
		FaturamentoSituacaoTipo faturamentoSituacaoTipo = new FaturamentoSituacaoTipo();
		faturamentoSituacaoTipo.setParalisacaoFaturamento(Status.INATIVO);
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
		
		boolean geraConta = condicaoGeracaoConta.segundaCondicaoGerarConta(imovel, anoMesFaturamento);

		assertFalse(geraConta);
	}
	
	@Test
	public void segundaCondicaoGeraContaQuandoHaDebitosCobrarAtivosEHaCreditosRealizarComDevolucao() throws Exception {

		Collection<DebitoCobrar> debitosCobrar = new ArrayList<DebitoCobrar>();
		
		DebitoTipo debitoTipo = new DebitoTipo();
		debitoTipo.setIndicadorGeracaoConta(Status.ATIVO);
		
		DebitoCobrar debitoCobrar = new DebitoCobrar();
		debitoCobrar.setDebitoTipo(debitoTipo);
		debitosCobrar.add(debitoCobrar);
		
		expect(debitoCobrarEJBMock.debitosCobrarPorImovelESituacao(imovel, DebitoCreditoSituacao.NORMAL, anoMesFaturamento))
			.andReturn(debitosCobrar);
		replay(debitoCobrarEJBMock);
		
		expect(pagamentoEJBMock.existeDebitoSemPagamento(debitosCobrar))
			.andReturn(true);
		replay(pagamentoEJBMock);
		
		Collection<CreditoRealizar> creditosRealizar = new ArrayList<CreditoRealizar>();
		CreditoRealizar creditoRealizar = new CreditoRealizar();
		creditosRealizar.add(creditoRealizar);
		
		expect(creditoRealizarEJBMock.pesquisarCreditoARealizar(imovel.getId(), DebitoCreditoSituacao.NORMAL, anoMesFaturamento))
			.andReturn(creditosRealizar);
		expect(creditoRealizarEJBMock.existeCreditoComDevolucao(creditosRealizar))
			.andReturn(true);
		replay(creditoRealizarEJBMock);
		
		FaturamentoSituacaoTipo faturamentoSituacaoTipo = new FaturamentoSituacaoTipo();
		faturamentoSituacaoTipo.setParalisacaoFaturamento(Status.INATIVO);
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
		
		boolean geraConta = condicaoGeracaoConta.segundaCondicaoGerarConta(imovel, anoMesFaturamento);

		assertTrue(geraConta);
	}
}