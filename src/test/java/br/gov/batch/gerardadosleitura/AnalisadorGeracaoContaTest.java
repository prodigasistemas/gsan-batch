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

import br.gov.batch.servicos.faturamento.AnalisadorGeracaoConta;
import br.gov.model.Status;
import br.gov.model.atendimentopublico.LigacaoAguaSituacao;
import br.gov.model.atendimentopublico.LigacaoEsgotoSituacao;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.CreditoRealizar;
import br.gov.model.faturamento.DebitoCobrar;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.model.faturamento.DebitoTipo;
import br.gov.model.faturamento.FaturamentoSituacaoTipo;
import br.gov.servicos.arrecadacao.DevolucaoRepositorio;
import br.gov.servicos.arrecadacao.PagamentoRepositorio;
import br.gov.servicos.faturamento.CreditoRealizarRepositorio;
import br.gov.servicos.faturamento.DebitoCobrarRepositorio;

@RunWith(EasyMockRunner.class)
public class AnalisadorGeracaoContaTest {

	@TestSubject
	private AnalisadorGeracaoConta analisadorGeracaoConta;
	
	private Imovel imovel;
	private LigacaoAguaSituacao ligacaoAguaSituacao;
	private LigacaoEsgotoSituacao ligacaoEsgotoSituacao;
	private boolean aguaEsgotoZerado;
	private int anoMesFaturamento;
	
	@Mock
	private DebitoCobrarRepositorio debitoCobrarEJBMock;
	
	@Mock
	private PagamentoRepositorio pagamentoEJBMock;
	
	@Mock
	private CreditoRealizarRepositorio creditoRealizarEJBMock;
	
	@Mock
	private DevolucaoRepositorio devolucaoEJBMock;
	
	@Before
	public void setup(){
		
		anoMesFaturamento = 0;

		imovel = new Imovel();
		ligacaoAguaSituacao = new LigacaoAguaSituacao();
		ligacaoAguaSituacao.setId(LigacaoAguaSituacao.LIGADO);
		imovel.setLigacaoAguaSituacao(ligacaoAguaSituacao);
		
		ligacaoEsgotoSituacao = new LigacaoEsgotoSituacao();
		ligacaoEsgotoSituacao.setId(LigacaoEsgotoSituacao.LIGADO);
		imovel.setLigacaoEsgotoSituacao(ligacaoEsgotoSituacao);

		analisadorGeracaoConta = new AnalisadorGeracaoConta(imovel);
	}
	

	@Test
	public void naoGeraContaComAguaEsgotoZerados() throws Exception {
		aguaEsgotoZerado = true;
		
		assertFalse(analisadorGeracaoConta.verificarSituacaoImovelParaGerarConta(aguaEsgotoZerado));
	}
	
	@Test
	public void geraContaSemAguaEsgotoZeradosELigado() throws Exception {
		aguaEsgotoZerado = false;
		
		assertTrue(analisadorGeracaoConta.verificarSituacaoImovelParaGerarConta(aguaEsgotoZerado));
	}

	@Test
	public void naoGeraContaSemAguaEsgotoZeradoEDesligado() throws Exception {
		aguaEsgotoZerado = false;

		ligacaoAguaSituacao.setId(0);
		ligacaoEsgotoSituacao.setId(0);
		
		assertFalse(analisadorGeracaoConta.verificarSituacaoImovelParaGerarConta(aguaEsgotoZerado));
	}
	
	@Test
	public void naoGeraContaSemAguaEsgotoZeradosDesligadoESemCondominio() throws Exception {
		aguaEsgotoZerado = false;
		
		ligacaoAguaSituacao.setId(0);
		ligacaoEsgotoSituacao.setId(0);
		
		assertFalse(analisadorGeracaoConta.verificarSituacaoImovelParaGerarConta(aguaEsgotoZerado));
	}
	
	@Test
	public void naoGeraContaSemAguaEsgotoZeradosDesligadoEComCondominio() throws Exception {
		aguaEsgotoZerado = false;

		ligacaoAguaSituacao.setId(0);
		ligacaoEsgotoSituacao.setId(0);
		
		imovel.setImovelCondominio(new Imovel());
		
		assertTrue(analisadorGeracaoConta.verificarSituacaoImovelParaGerarConta(aguaEsgotoZerado));
	}
	
	@Test
	public void geraContaComDebitoCobrar() throws Exception {

		mockDebitosCobrarPorImovelComPendenciaESemRevisao(null);
		
		assertFalse(analisadorGeracaoConta.verificarDebitosECreditosParaGerarConta(anoMesFaturamento));
	}
	
	@Test
	public void geraContaComParalisacaoFaturamento() throws Exception {

		Collection<DebitoCobrar> debitosCobrar = buildCollectionDebitosCobrarVazio(false);
		
		mockDebitosCobrarPorImovelComPendenciaESemRevisao(debitosCobrar);
		
		adicionaFaturamentoSituacaoTipoParaImovel(Status.ATIVO);
		
		assertFalse(analisadorGeracaoConta.verificarDebitosECreditosParaGerarConta(anoMesFaturamento));
	}
	
	@Test
	public void geraContaQuandoHaDebitoSemPagamento() throws Exception {

		Collection<DebitoCobrar> debitosCobrar = buildCollectionDebitosCobrarVazio(false);
		
		mockDebitosCobrarPorImovelComPendenciaESemRevisao(debitosCobrar);
		mockExisteDebitoSemPagamento(debitosCobrar, false);
		
		adicionaFaturamentoSituacaoTipoParaImovel(Status.INATIVO);
		
		assertFalse(analisadorGeracaoConta.verificarDebitosECreditosParaGerarConta(anoMesFaturamento));
	}
	
	@Test
	public void naoGeraContaQuandoNaoHaDebitosCobrarAtivosENaoHaCreditosRealizar() throws Exception {

		Collection<DebitoCobrar> debitosCobrar = buildCollectionDebitosCobrarComDebitoTipo(Status.INATIVO);
		
		mockDebitosCobrarPorImovelComPendenciaESemRevisao(debitosCobrar);
		mockExisteDebitoSemPagamento(debitosCobrar, true);
		mockPesquisarCreditoARealizar(null);
		
		adicionaFaturamentoSituacaoTipoParaImovel(Status.INATIVO);
		
		assertFalse(analisadorGeracaoConta.verificarDebitosECreditosParaGerarConta(anoMesFaturamento));
	}
	
	@Test
	public void geraContaQuandoHaDebitosCobrarAtivosENaoHaCreditosRealizar() throws Exception {

		Collection<DebitoCobrar> debitosCobrar = buildCollectionDebitosCobrarComDebitoTipo(Status.ATIVO);
		
		mockDebitosCobrarPorImovelComPendenciaESemRevisao(debitosCobrar);
		mockExisteDebitoSemPagamento(debitosCobrar, true);
		mockPesquisarCreditoARealizar(null);
		
		adicionaFaturamentoSituacaoTipoParaImovel(Status.INATIVO);
		
		assertTrue(analisadorGeracaoConta.verificarDebitosECreditosParaGerarConta(anoMesFaturamento));
	}
	
	@Test
	public void naoGeraContaQuandoNaoHaDebitosCobrarAtivosEHaCreditosRealizarComDevolucao() throws Exception {

		Collection<DebitoCobrar> debitosCobrar = buildCollectionDebitosCobrarComDebitoTipo(Status.INATIVO);
		
		mockDebitosCobrarPorImovelComPendenciaESemRevisao(debitosCobrar);
		mockExisteDebitoSemPagamento(debitosCobrar, true);
		
		Collection<CreditoRealizar> creditosRealizar = buildCollectionCreditosRealizar();
		mockPesquisarCreditoARealizar(creditosRealizar);
		mockExisteCreditoComDevolucao(creditosRealizar, true);
		replay(creditoRealizarEJBMock);
		replay(devolucaoEJBMock);
		
		adicionaFaturamentoSituacaoTipoParaImovel(Status.INATIVO);
		
		assertFalse(analisadorGeracaoConta.verificarDebitosECreditosParaGerarConta(anoMesFaturamento));
	}
	
	@Test
	public void geraContaQuandoHaDebitosCobrarAtivosEHaCreditosRealizarComDevolucao() throws Exception {

		Collection<DebitoCobrar> debitosCobrar = buildCollectionDebitosCobrarComDebitoTipo(Status.ATIVO);
		
		mockDebitosCobrarPorImovelComPendenciaESemRevisao(debitosCobrar);
		mockExisteDebitoSemPagamento(debitosCobrar, true);
		
		Collection<CreditoRealizar> creditosRealizar = buildCollectionCreditosRealizar();
		mockPesquisarCreditoARealizar(creditosRealizar);
		
		mockExisteCreditoComDevolucao(creditosRealizar, true);
		replay(creditoRealizarEJBMock);
		
		adicionaFaturamentoSituacaoTipoParaImovel(Status.INATIVO);
		
		assertTrue(analisadorGeracaoConta.verificarDebitosECreditosParaGerarConta(anoMesFaturamento));
	}
	
	@Test
	public void geraContaQuandoHaCreditosRealizarENaoHaCreditosComDevolucao() throws Exception {

		Collection<DebitoCobrar> debitosCobrar = buildCollectionDebitosCobrarVazio(false);
		
		mockDebitosCobrarPorImovelComPendenciaESemRevisao(debitosCobrar);
		mockExisteDebitoSemPagamento(debitosCobrar, true);
		
		Collection<CreditoRealizar> creditosRealizar = buildCollectionCreditosRealizar();
		
		mockPesquisarCreditoARealizar(creditosRealizar);
		mockExisteCreditoComDevolucao(creditosRealizar, false);
		replay(creditoRealizarEJBMock);
		
		adicionaFaturamentoSituacaoTipoParaImovel(Status.INATIVO);
		
		assertTrue(analisadorGeracaoConta.verificarDebitosECreditosParaGerarConta(anoMesFaturamento));
	}
	
	private Collection<CreditoRealizar> buildCollectionCreditosRealizar() {
		Collection<CreditoRealizar> creditosRealizar = new ArrayList<CreditoRealizar>();
		CreditoRealizar creditoRealizar = new CreditoRealizar();
		creditosRealizar.add(creditoRealizar);
		return creditosRealizar;
	}
	
	private void mockPesquisarCreditoARealizar(Collection<CreditoRealizar> retorno) {
		expect(creditoRealizarEJBMock.buscarCreditoRealizarPorImovel(imovel.getId(), DebitoCreditoSituacao.NORMAL, anoMesFaturamento))
			.andReturn(retorno);
	}
	
	private void mockExisteCreditoComDevolucao(Collection<CreditoRealizar> creditosRealizar, boolean retorno) {
		expect(devolucaoEJBMock.existeCreditoComDevolucao(creditosRealizar))
			.andReturn(retorno);
	}
	
	private void mockExisteDebitoSemPagamento(Collection<DebitoCobrar> debitosCobrar, boolean retorno) {
		expect(pagamentoEJBMock.existeDebitoSemPagamento(debitosCobrar))
			.andReturn(retorno);
		replay(pagamentoEJBMock);
	}
	
	private void adicionaFaturamentoSituacaoTipoParaImovel(Status status) {
		FaturamentoSituacaoTipo faturamentoSituacaoTipo = new FaturamentoSituacaoTipo();
		faturamentoSituacaoTipo.setParalisacaoFaturamento(status);
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
	}


	private void mockDebitosCobrarPorImovelComPendenciaESemRevisao(Collection<DebitoCobrar> debitosCobrar) {
		expect(debitoCobrarEJBMock.debitosCobrarPorImovelComPendenciaESemRevisao(imovel))
			.andReturn(debitosCobrar);
		replay(debitoCobrarEJBMock);
	}
	
	private Collection<DebitoCobrar> buildCollectionDebitosCobrarVazio(boolean vazio){
		Collection<DebitoCobrar> debitosCobrar = new ArrayList<DebitoCobrar>();
		
		if(vazio == false){
			DebitoCobrar debitoCobrar = new DebitoCobrar();
			debitosCobrar.add(debitoCobrar);
		}
		
		return debitosCobrar;
	}
	
	private Collection<DebitoCobrar> buildCollectionDebitosCobrarComDebitoTipo(Status status){
		Collection<DebitoCobrar> debitosCobrar = new ArrayList<DebitoCobrar>();
		
		DebitoTipo debitoTipo = new DebitoTipo();
		debitoTipo.setIndicadorGeracaoConta(status);
		
		DebitoCobrar debitoCobrar = new DebitoCobrar();
		debitoCobrar.setDebitoTipo(debitoTipo);
		debitosCobrar.add(debitoCobrar);
		
		return debitosCobrar;
	}
}