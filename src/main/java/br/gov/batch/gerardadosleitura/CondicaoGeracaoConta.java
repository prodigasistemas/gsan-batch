package br.gov.batch.gerardadosleitura;

import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.atendimentopublico.LigacaoAguaSituacao;
import br.gov.model.atendimentopublico.LigacaoEsgotoSituacao;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.CreditoRealizar;
import br.gov.model.faturamento.DebitoCobrar;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.model.faturamento.DebitoTipo;
import br.gov.servicos.faturamento.CreditoRealizarEJB;

@Stateless
public class CondicaoGeracaoConta {
	
	@EJB
	private CreditoRealizarEJB creditoRealizar;

	public boolean verificarNaoGeracaoConta(Imovel imovel, boolean valoresAguaEsgotoZerados, int anoMesFaturamento) throws Exception {
		boolean primeiraCondicaoNaoGerarConta = primeiraCondicaoNaoGerarConta(imovel, valoresAguaEsgotoZerados);

		boolean segundaCondicaoNaoGerarConta = false;
		Collection<DebitoCobrar> debitosACobrar = this.obterDebitoACobrarImovel(imovel.getId(), DebitoCreditoSituacao.NORMAL, anoMesFaturamento);

		if (naoHaDebitosHaCobrar(debitosACobrar)) {
			segundaCondicaoNaoGerarConta = true;
		} else if (paralisacaoFaturamento(imovel)) {
			segundaCondicaoNaoGerarConta = true;
		} else if (!existeDebitoSemPagamento(debitosACobrar)) {
			segundaCondicaoNaoGerarConta = true;
		} else {
			Collection<CreditoRealizar> creditosARealizar = creditoRealizar.pesquisarCreditoARealizar(imovel.getId(), DebitoCreditoSituacao.NORMAL, anoMesFaturamento);

			boolean existeCreditoComDevolucao = existeCreditoComDevolucao(creditosARealizar);

			if (naoHaCreditoARealizar(creditosARealizar) || existeCreditoComDevolucao) {
				DebitoTipo debitoTipo = null;
				segundaCondicaoNaoGerarConta = true;

				for (DebitoCobrar debitoACobrar: debitosACobrar) {
					debitoTipo = creditoRealizar.getDebitoTipo(debitoACobrar.getDebitoTipo().getId());

					if (debitoTipo.getIndicadorGeracaoConta() != 2) {
						segundaCondicaoNaoGerarConta = false;
					}
				}
			}
		}

		return !(primeiraCondicaoNaoGerarConta && segundaCondicaoNaoGerarConta);
	}

	public boolean primeiraCondicaoNaoGerarConta(Imovel imovel, boolean valoresAguaEsgotoZerados) {
		if (valoresAguaEsgotoZerados || (aguaEsgotoDesligados(imovel) && !imovelPertenceACondominio(imovel))) {
			return true;
		}else{
			return false;
		}
	}

	private Collection<DebitoCobrar> obterDebitoACobrarImovel(Long id, DebitoCreditoSituacao normal, int anoMesFaturamento) {
		return null;
	}

	private boolean naoHaCreditoARealizar(Collection<CreditoRealizar> creditosRealizar) {
		return creditosRealizar == null || creditosRealizar.isEmpty();
	}

	private boolean existeCreditoComDevolucao(Collection<CreditoRealizar> creditosARealizar) {
		return false;
	}

	private boolean existeDebitoSemPagamento(Collection<DebitoCobrar> debitosACobrar) {
		return false;
	}

	private boolean paralisacaoFaturamento(Imovel imovel) {
		return false;
	}

	private boolean naoHaDebitosHaCobrar(Collection<DebitoCobrar> colecaoDebitosACobrar) {
		return false;
	}

	private boolean imovelPertenceACondominio(Imovel imovel) {
		return imovel.getImovelCondominio() != null;
	}

	private boolean aguaEsgotoDesligados(Imovel imovel) {
		return !imovel.getLigacaoAguaSituacao().getId().equals(LigacaoAguaSituacao.LIGADO)
				 && !imovel.getLigacaoEsgotoSituacao().getId().equals(LigacaoEsgotoSituacao.LIGADO);
	}		
}
