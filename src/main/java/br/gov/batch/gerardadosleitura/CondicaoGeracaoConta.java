package br.gov.batch.gerardadosleitura;

import io.undertow.attribute.ConstantExchangeAttribute;

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
import br.gov.model.faturamento.DebitoTipo;
import br.gov.servicos.arrecadacao.PagamentoEJB;
import br.gov.servicos.faturamento.CreditoRealizarEJB;
import br.gov.servicos.faturamento.DebitoCobrarEJB;

@Stateless
public class CondicaoGeracaoConta {
	
	@EJB
	private CreditoRealizarEJB creditoRealizar;
	
	@EJB
	private DebitoCobrarEJB debitoCobrar;
	
	@EJB
	private PagamentoEJB pagamentoEJB;
	

	public boolean verificarNaoGeracaoConta(Imovel imovel, boolean valoresAguaEsgotoZerados, int anoMesFaturamento) throws Exception {
		boolean primeiraCondicaoNaoGerarConta = primeiraCondicaoNaoGerarConta(imovel, valoresAguaEsgotoZerados);
		
		boolean segundaCondicaoGerarConta = segundaCondicaoGerarConta(imovel, anoMesFaturamento);

		return !(primeiraCondicaoNaoGerarConta && !segundaCondicaoGerarConta);
	}

	public boolean segundaCondicaoGerarConta(Imovel imovel, int anoMesFaturamento) {
		
		boolean segundaCondicaoGerarConta = true;
		Collection<DebitoCobrar> debitosACobrar = debitoCobrar.debitosCobrarPorImovelESituacao(imovel, DebitoCreditoSituacao.NORMAL, anoMesFaturamento);
		
		if (naoHaDebitosACobrar(debitosACobrar)) {
			segundaCondicaoGerarConta = false;
		} else if (paralisacaoFaturamento(imovel)) {
			segundaCondicaoGerarConta = false;
		} else if (!pagamentoEJB.existeDebitoSemPagamento(debitosACobrar)) {
			segundaCondicaoGerarConta = false;
		} else {
			Collection<CreditoRealizar> creditosARealizar = creditoRealizar.pesquisarCreditoARealizar(imovel.getId(), DebitoCreditoSituacao.NORMAL, anoMesFaturamento);

			if (naoHaCreditoARealizar(creditosARealizar) || creditoRealizar.existeCreditoComDevolucao(creditosARealizar)) {
				segundaCondicaoGerarConta = false;

				for (DebitoCobrar debitoACobrar: debitosACobrar) {
					if (debitoACobrar.getDebitoTipo().getIndicadorGeracaoConta() == Status.ATIVO) {
						segundaCondicaoGerarConta = true;
					}
				}
			}
		}
		return segundaCondicaoGerarConta;
	}

	public boolean primeiraCondicaoNaoGerarConta(Imovel imovel, boolean valoresAguaEsgotoZerados) {
		return valoresAguaEsgotoZerados || (aguaEsgotoDesligados(imovel) && !imovelPertenceACondominio(imovel));
	}

	private boolean naoHaCreditoARealizar(Collection<CreditoRealizar> creditosRealizar) {
		return creditosRealizar == null || creditosRealizar.isEmpty();
	}

	private boolean paralisacaoFaturamento(Imovel imovel) {
		return imovel.getFaturamentoSituacaoTipo() != null && imovel.getFaturamentoSituacaoTipo().getParalisacaoFaturamento() == Status.ATIVO;
	}

	private boolean naoHaDebitosACobrar(Collection<DebitoCobrar> colecaoDebitosACobrar) {
		return colecaoDebitosACobrar == null || colecaoDebitosACobrar.isEmpty();
	}

	private boolean imovelPertenceACondominio(Imovel imovel) {
		return imovel.getImovelCondominio() != null;
	}

	private boolean aguaEsgotoDesligados(Imovel imovel) {
		return !imovel.getLigacaoAguaSituacao().getId().equals(LigacaoAguaSituacao.LIGADO)
				 && !imovel.getLigacaoEsgotoSituacao().getId().equals(LigacaoEsgotoSituacao.LIGADO);
	}		
}
