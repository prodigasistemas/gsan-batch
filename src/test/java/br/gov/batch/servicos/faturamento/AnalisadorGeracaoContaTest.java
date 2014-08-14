package br.gov.batch.servicos.faturamento;

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
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.CreditoRealizar;
import br.gov.model.faturamento.DebitoCobrar;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.model.faturamento.DebitoTipo;
import br.gov.model.faturamento.FaturamentoSituacaoTipo;
import br.gov.servicos.arrecadacao.DevolucaoRepositorio;
import br.gov.servicos.arrecadacao.pagamento.PagamentoRepositorio;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.CreditoRealizarRepositorio;

@RunWith(EasyMockRunner.class)
public class AnalisadorGeracaoContaTest {

	@TestSubject
	private AnalisadorGeracaoConta analisadorGeracaoConta;
	
	private Imovel imovel;
	private LigacaoAguaSituacao aguaLigada;
	private LigacaoAguaSituacao aguaNaoLigada;
	private LigacaoEsgotoSituacao esgotoLigado;
	private LigacaoEsgotoSituacao esgotoNaoLigado;
	private boolean aguaEsgotoZerado;
	private int anoMesFaturamento;
	
	@Mock
	private DebitoCobrarBO debitoCobrarEJBMock;
	
	@Mock
	private PagamentoRepositorio pagamentoEJBMock;
	
	@Mock
	private CreditoRealizarRepositorio creditoRealizarEJBMock;
	
	@Mock
	private DevolucaoRepositorio devolucaoEJBMock;
	
	@Mock
	private SistemaParametrosRepositorio sistemaParametrosRepositorioMock;
	
	@Before
	public void setup(){
		
		anoMesFaturamento = 0;

		imovel = new Imovel();
		aguaLigada = new LigacaoAguaSituacao();
		aguaLigada.setId(LigacaoAguaSituacao.LIGADO);
		imovel.setLigacaoAguaSituacao(aguaLigada);
		aguaNaoLigada = new LigacaoAguaSituacao();
		aguaNaoLigada.setId(LigacaoAguaSituacao.POTENCIAL);
		
		esgotoLigado = new LigacaoEsgotoSituacao();
		esgotoLigado.setId(LigacaoEsgotoSituacao.LIGADO);
		imovel.setLigacaoEsgotoSituacao(esgotoLigado);
		esgotoNaoLigado = new LigacaoEsgotoSituacao();
		esgotoNaoLigado.setId(LigacaoEsgotoSituacao.POTENCIAL);

		analisadorGeracaoConta = new AnalisadorGeracaoConta();
	}
	

	@Test
	public void naoGeraContaComAguaEsgotoZeradosAguaLigadaEsgotoLigadoNaoCondominio() throws Exception {
		aguaEsgotoZerado = true;
		
		assertFalse(analisadorGeracaoConta.verificarSituacaoImovelParaGerarConta(aguaEsgotoZerado, imovel));
	}
	
	@Test
	public void geraContaSemAguaEsgotoZeradosAguaLigadaEsgotoDesligadoNaoCondominio() throws Exception {
		aguaEsgotoZerado = false;
		imovel.setLigacaoEsgotoSituacao(esgotoNaoLigado);
		
		assertTrue(analisadorGeracaoConta.verificarSituacaoImovelParaGerarConta(aguaEsgotoZerado, imovel));
	}
	
	@Test
	public void geraContaSemAguaEsgotoZeradosELigado() throws Exception {
		aguaEsgotoZerado = false;
		
		assertTrue(analisadorGeracaoConta.verificarSituacaoImovelParaGerarConta(aguaEsgotoZerado, imovel));
	}

	@Test
	public void naoGeraContaSemAguaEsgotoZeradoEDesligado() throws Exception {
		aguaEsgotoZerado = false;

		aguaLigada.setId(0);
		esgotoLigado.setId(0);
		
		assertFalse(analisadorGeracaoConta.verificarSituacaoImovelParaGerarConta(aguaEsgotoZerado, imovel));
	}
	
	@Test
	public void naoGeraContaSemAguaEsgotoZeradosDesligadoESemCondominio() throws Exception {
		aguaEsgotoZerado = false;
		
		aguaLigada.setId(0);
		esgotoLigado.setId(0);
		
		assertFalse(analisadorGeracaoConta.verificarSituacaoImovelParaGerarConta(aguaEsgotoZerado, imovel));
	}
	
	@Test
	public void naoGeraContaSemAguaEsgotoZeradosDesligadoEComCondominio() throws Exception {
		aguaEsgotoZerado = false;

		aguaLigada.setId(0);
		esgotoLigado.setId(0);
		
		imovel.setImovelCondominio(new Imovel());
		
		assertTrue(analisadorGeracaoConta.verificarSituacaoImovelParaGerarConta(aguaEsgotoZerado, imovel));
	}
	
	@Test
	public void geraContaComDebitoCobrar() throws Exception {
		mockDebitosCobrarPorImovelComPendenciaESemRevisao(null);
		mockSistemaParametrosRepositorio();
		assertFalse(analisadorGeracaoConta.verificarDebitosECreditosParaGerarConta(anoMesFaturamento, imovel));
	}
	
	@Test
	public void geraContaComParalisacaoFaturamento() throws Exception {

		Collection<DebitoCobrar> debitosCobrar = buildCollectionDebitosCobrarVazio(false);
		
		mockDebitosCobrarPorImovelComPendenciaESemRevisao(debitosCobrar);

		mockSistemaParametrosRepositorio();
		
		adicionaFaturamentoSituacaoTipoParaImovel(Status.ATIVO);
		
		assertFalse(analisadorGeracaoConta.verificarDebitosECreditosParaGerarConta(anoMesFaturamento, imovel));
	}
	
	@Test
	public void geraContaQuandoHaDebitoSemPagamento() throws Exception {

		Collection<DebitoCobrar> debitosCobrar = buildCollectionDebitosCobrarVazio(false);
		
		mockDebitosCobrarPorImovelComPendenciaESemRevisao(debitosCobrar);
		mockExisteDebitoSemPagamento(debitosCobrar, false);
		mockSistemaParametrosRepositorio();
		
		adicionaFaturamentoSituacaoTipoParaImovel(Status.INATIVO);
		
		assertTrue(analisadorGeracaoConta.verificarDebitosECreditosParaGerarConta(anoMesFaturamento, imovel));
	}
	
	@Test
	public void naoGeraContaQuandoNaoHaDebitosCobrarAtivosENaoHaCreditosRealizar() throws Exception {

		Collection<DebitoCobrar> debitosCobrar = buildCollectionDebitosCobrarComDebitoTipo(Status.INATIVO);
		
		mockDebitosCobrarPorImovelComPendenciaESemRevisao(debitosCobrar);
		mockExisteDebitoSemPagamento(debitosCobrar, true);
		mockPesquisarCreditoARealizar(null);
		mockSistemaParametrosRepositorio();
		
		adicionaFaturamentoSituacaoTipoParaImovel(Status.INATIVO);
		
		assertFalse(analisadorGeracaoConta.verificarDebitosECreditosParaGerarConta(anoMesFaturamento, imovel));
	}
	
	@Test
	public void geraContaQuandoHaDebitosCobrarAtivosENaoHaCreditosRealizar() throws Exception {

		Collection<DebitoCobrar> debitosCobrar = buildCollectionDebitosCobrarComDebitoTipo(Status.ATIVO);
		
		mockDebitosCobrarPorImovelComPendenciaESemRevisao(debitosCobrar);
		mockExisteDebitoSemPagamento(debitosCobrar, true);
		mockPesquisarCreditoARealizar(null);
		mockSistemaParametrosRepositorio();
		
		adicionaFaturamentoSituacaoTipoParaImovel(Status.INATIVO);
		
		assertTrue(analisadorGeracaoConta.verificarDebitosECreditosParaGerarConta(anoMesFaturamento, imovel));
	}
	
	@Test
	public void naoGeraContaQuandoNaoHaDebitosCobrarAtivosEHaCreditosRealizarComDevolucao() throws Exception {

		Collection<DebitoCobrar> debitosCobrar = buildCollectionDebitosCobrarComDebitoTipo(Status.INATIVO);
		
		mockDebitosCobrarPorImovelComPendenciaESemRevisao(debitosCobrar);
		mockExisteDebitoSemPagamento(debitosCobrar, true);
		
		Collection<CreditoRealizar> creditosRealizar = buildCollectionCreditosRealizar();
		mockPesquisarCreditoARealizar(creditosRealizar);
		mockExisteCreditoComDevolucao(creditosRealizar, true);
		mockSistemaParametrosRepositorio();
		replay(creditoRealizarEJBMock);
		
		adicionaFaturamentoSituacaoTipoParaImovel(Status.INATIVO);
		
		assertFalse(analisadorGeracaoConta.verificarDebitosECreditosParaGerarConta(anoMesFaturamento, imovel));
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
		mockSistemaParametrosRepositorio();
		
		adicionaFaturamentoSituacaoTipoParaImovel(Status.INATIVO);
		
		assertTrue(analisadorGeracaoConta.verificarDebitosECreditosParaGerarConta(anoMesFaturamento, imovel));
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
		mockSistemaParametrosRepositorio();
		
		adicionaFaturamentoSituacaoTipoParaImovel(Status.INATIVO);
		
		assertTrue(analisadorGeracaoConta.verificarDebitosECreditosParaGerarConta(anoMesFaturamento, imovel));
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
		replay(devolucaoEJBMock);
	}
	
	private void mockExisteDebitoSemPagamento(Collection<DebitoCobrar> debitosCobrar, boolean retorno) {
		expect(pagamentoEJBMock.existeDebitoSemPagamento(debitosCobrar))
			.andReturn(retorno);
		replay(pagamentoEJBMock);
	}
	
	private void adicionaFaturamentoSituacaoTipoParaImovel(Status status) {
		FaturamentoSituacaoTipo faturamentoSituacaoTipo = new FaturamentoSituacaoTipo();
		faturamentoSituacaoTipo.setParalisacaoFaturamento(status.getId());
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
	}


	private void mockDebitosCobrarPorImovelComPendenciaESemRevisao(Collection<DebitoCobrar> debitosCobrar) {
		expect(debitoCobrarEJBMock.debitosCobrarSemPagamentos(imovel))
			.andReturn(debitosCobrar);
		replay(debitoCobrarEJBMock);
	}
	
	private void mockSistemaParametrosRepositorio() {
		SistemaParametros sistemaParametros = new SistemaParametros();
		sistemaParametros.setAnoMesFaturamento(anoMesFaturamento);
		expect(sistemaParametrosRepositorioMock.getSistemaParametros())
		.andReturn(sistemaParametros);
		replay(sistemaParametrosRepositorioMock);
	}
	
	private Collection<DebitoCobrar> buildCollectionDebitosCobrarVazio(boolean vazio){
		Collection<DebitoCobrar> debitosCobrar = new ArrayList<DebitoCobrar>();
		
		if(vazio == false){
			DebitoCobrar debitoCobrar = new DebitoCobrar();
			DebitoTipo debitoTipo = new DebitoTipo();
			debitoTipo.setIndicadorGeracaoConta(Status.ATIVO);
			debitoCobrar.setDebitoTipo(debitoTipo);
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