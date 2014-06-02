package br.gov.batch.gerardadosleitura;

import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.Status;
import br.gov.model.atendimentopublico.LigacaoAguaSituacao;
import br.gov.model.atendimentopublico.LigacaoEsgotoSituacao;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.CreditoRealizar;
import br.gov.model.faturamento.DebitoCobrar;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.servicos.arrecadacao.PagamentoEJB;
import br.gov.servicos.faturamento.CreditoRealizarEJB;
import br.gov.servicos.faturamento.DebitoCobrarEJB;

@Stateless
public class AnalisadorGeracaoConta {
	
	@EJB
	private CreditoRealizarEJB creditoRealizar;
	
	@EJB
	private DebitoCobrarEJB debitoCobrar;
	
	@EJB
	private PagamentoEJB pagamentoEJB;
	
	private Imovel imovel;
	
	public AnalisadorGeracaoConta(Imovel imovel){
		this.imovel = imovel;
	}

	public boolean verificarNaoGeracaoConta(boolean valoresAguaEsgotoZerados, int anoMesFaturamento) throws Exception {
		return !(verificarSituacaoImovelParaGerarConta(valoresAguaEsgotoZerados) && verificarDebitosECreditosParaGerarConta(anoMesFaturamento));
	}

	public boolean verificarDebitosECreditosParaGerarConta(int anoMesFaturamento) {
		
		Collection<DebitoCobrar> debitosACobrar = debitoCobrar.debitosCobrarPorImovelESituacao(imovel, DebitoCreditoSituacao.NORMAL, anoMesFaturamento);
		if (naoHaDebitosACobrar(debitosACobrar) || paralisacaoFaturamento() || !pagamentoEJB.existeDebitoSemPagamento(debitosACobrar)) {
			return false;
		}
		
		boolean segundaCondicaoGerarConta = true;
		Collection<CreditoRealizar> creditosARealizar = creditoRealizar.pesquisarCreditoARealizar(imovel.getId(), DebitoCreditoSituacao.NORMAL, anoMesFaturamento);

		if (naoHaCreditoARealizar(creditosARealizar) || creditoRealizar.existeCreditoComDevolucao(creditosARealizar)) {
			segundaCondicaoGerarConta = haDebitosCobrarAtivos(debitosACobrar);
		}

		return segundaCondicaoGerarConta;
	}

	private boolean haDebitosCobrarAtivos(Collection<DebitoCobrar> debitosACobrar) {
		boolean haDebitosCobrarAtivos = false;
		for (DebitoCobrar debitoACobrar: debitosACobrar) {
			if (debitoACobrar.getDebitoTipo().getIndicadorGeracaoConta() == Status.ATIVO) {
				haDebitosCobrarAtivos = true;
				break;
			}
		}
		return haDebitosCobrarAtivos;
	}

	public boolean verificarSituacaoImovelParaGerarConta(boolean valoresAguaEsgotoZerados) {
		return !valoresAguaEsgotoZerados && (!aguaEsgotoDesligados() || imovelPertenceACondominio());
	}

	private boolean naoHaCreditoARealizar(Collection<CreditoRealizar> creditosRealizar) {
		return creditosRealizar == null || creditosRealizar.isEmpty();
	}

	private boolean paralisacaoFaturamento() {
		return imovel.getFaturamentoSituacaoTipo() != null && imovel.getFaturamentoSituacaoTipo().getParalisacaoFaturamento() == Status.ATIVO;
	}

	private boolean naoHaDebitosACobrar(Collection<DebitoCobrar> colecaoDebitosACobrar) {
		return colecaoDebitosACobrar == null || colecaoDebitosACobrar.isEmpty();
	}

	private boolean imovelPertenceACondominio() {
		return imovel.getImovelCondominio() != null;
	}

	private boolean aguaEsgotoDesligados() {
		return !imovel.getLigacaoAguaSituacao().getId().equals(LigacaoAguaSituacao.LIGADO)
				 && !imovel.getLigacaoEsgotoSituacao().getId().equals(LigacaoEsgotoSituacao.LIGADO);
	}		
}
