package br.gov.batch.servicos.faturamento;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

public class AnalisadorGeracaoContaTest {

	@InjectMocks
	private AnalisadorGeracaoConta analisadorGeracaoConta;
	
	private Imovel imovel;
	private LigacaoAguaSituacao aguaLigada;
	private LigacaoAguaSituacao aguaDesligada;
	private LigacaoEsgotoSituacao esgotoLigado;
	private LigacaoEsgotoSituacao esgotoDesligado;
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
	
	private boolean aguaEsgotoZerados;
	
	@Before
	public void setup(){
		
		anoMesFaturamento = 0;

		imovel = new Imovel();
		aguaLigada = new LigacaoAguaSituacao();
		aguaLigada.setId(LigacaoAguaSituacao.LIGADO);
		imovel.setLigacaoAguaSituacao(aguaLigada);
		aguaDesligada = new LigacaoAguaSituacao();
		aguaDesligada.setId(LigacaoAguaSituacao.POTENCIAL);
		
		esgotoLigado = new LigacaoEsgotoSituacao();
		esgotoLigado.setId(LigacaoEsgotoSituacao.LIGADO);
		imovel.setLigacaoEsgotoSituacao(esgotoLigado);
		esgotoDesligado = new LigacaoEsgotoSituacao();
		esgotoDesligado.setId(LigacaoEsgotoSituacao.POTENCIAL);

		analisadorGeracaoConta = new AnalisadorGeracaoConta();
		
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void naoGeraContaSemConsumo_AguaLigada_EsgotoLigado_NaoPertenceACondominio() throws Exception {
		aguaEsgotoZerados = true;
		
		assertFalse(analisadorGeracaoConta.verificarSituacaoImovelParaGerarConta(aguaEsgotoZerados, imovel));
	}
	
	@Test
	public void naoGeraContaSemConsumo_AguaDesligada_EsgotoDesligado_NaoPertenceACondominio() throws Exception {
        aguaEsgotoZerados = true;
        imovel.setLigacaoAguaSituacao(aguaLigada);
        imovel.setLigacaoEsgotoSituacao(esgotoDesligado);
		
		assertFalse(analisadorGeracaoConta.verificarSituacaoImovelParaGerarConta(aguaEsgotoZerados, imovel));
	}
	
	@Test
	public void geraContaComConsumoDeAguaEEsgoto_AguaLigada_EsgotoDesligado_NaoPertenceACondominio() throws Exception {
		aguaEsgotoZerados = false;
		imovel.setLigacaoEsgotoSituacao(esgotoDesligado);
		
		assertTrue(analisadorGeracaoConta.verificarSituacaoImovelParaGerarConta(aguaEsgotoZerados, imovel));
	}

   @Test
    public void geraContaComConsumoDeAguaEEsgoto_AguaLigada() throws Exception {
        aguaEsgotoZerados = false;
        
        assertTrue(analisadorGeracaoConta.verificarSituacaoImovelParaGerarConta(aguaEsgotoZerados, imovel));
    }

	@Test
	public void naoGeraContaComConsumoDeAguaEsgoto_AguaDesligada_EsgotoDesligado() throws Exception {
		aguaEsgotoZerados = false;
		
        imovel.setLigacaoAguaSituacao(aguaDesligada);
        imovel.setLigacaoEsgotoSituacao(esgotoDesligado);
		
		assertFalse(analisadorGeracaoConta.verificarSituacaoImovelParaGerarConta(aguaEsgotoZerados, imovel));
	}
	
	@Test
	public void naoGeraContaComConsumoDeAguaEsgoto_AguaEsgotoDesligados_ESemCondominio() throws Exception {
        aguaEsgotoZerados = false;
        
        imovel.setLigacaoAguaSituacao(aguaDesligada);
        imovel.setLigacaoEsgotoSituacao(esgotoDesligado);
		
		assertFalse(analisadorGeracaoConta.verificarSituacaoImovelParaGerarConta(aguaEsgotoZerados, imovel));
	}
	
	@Test
	public void geraContaSemConsumo_AguaDesligada_EsgotoDesligado_EPerterceACondominio() throws Exception {
		aguaEsgotoZerados = true;
		
        imovel.setLigacaoAguaSituacao(aguaDesligada);
        imovel.setLigacaoEsgotoSituacao(esgotoDesligado);
        
        imovel.setImovelCondominio(new Imovel());
		
		assertTrue(analisadorGeracaoConta.verificarSituacaoDeCondominio(aguaEsgotoZerados, imovel));
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
		when(creditoRealizarEJBMock.buscarCreditoRealizarPorImovel(imovel.getId(), DebitoCreditoSituacao.NORMAL, anoMesFaturamento))
			.thenReturn(retorno);
	}
	
	private void mockExisteCreditoComDevolucao(Collection<CreditoRealizar> creditosRealizar, boolean retorno) {
		when(devolucaoEJBMock.existeCreditoComDevolucao(creditosRealizar))
			.thenReturn(retorno);
	}
	
	private void mockExisteDebitoSemPagamento(Collection<DebitoCobrar> debitosCobrar, boolean retorno) {
		when(pagamentoEJBMock.existeDebitoSemPagamento(debitosCobrar))
			.thenReturn(retorno);
	}
	
	private void adicionaFaturamentoSituacaoTipoParaImovel(Status status) {
		FaturamentoSituacaoTipo faturamentoSituacaoTipo = new FaturamentoSituacaoTipo();
		faturamentoSituacaoTipo.setParalisacaoFaturamento(status.getId());
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
	}


	private void mockDebitosCobrarPorImovelComPendenciaESemRevisao(Collection<DebitoCobrar> debitosCobrar) {
		when(debitoCobrarEJBMock.debitosCobrarSemPagamentos(imovel.getId()))
			.thenReturn(debitosCobrar);
	}
	
	private void mockSistemaParametrosRepositorio() {
		SistemaParametros sistemaParametros = new SistemaParametros();
		sistemaParametros.setAnoMesFaturamento(anoMesFaturamento);
		when(sistemaParametrosRepositorioMock.getSistemaParametros())
		.thenReturn(sistemaParametros);
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