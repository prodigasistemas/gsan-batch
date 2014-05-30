package br.gov.batch.gerardadosleitura;

import java.util.Collection;
import java.util.Iterator;

import javax.ejb.Stateless;

import br.gov.model.atendimentopublico.LigacaoAguaSituacao;
import br.gov.model.atendimentopublico.LigacaoEsgotoSituacao;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.DebitoACobrar;

@Stateless
public class CondicaoGeracaoConta {

	public boolean verificarNaoGeracaoConta(Imovel imovel, boolean valoresAguaEsgotoZerados, int anoMesFaturamento) throws Exception {
		boolean primeiraCondicaoNaoGerarConta = false;

		if (valoresAguaEsgotoZerados
			|| (!valoresAguaEsgotoZerados 
				&& aguaEsgotoDesligados(imovel)
				&& !imovelPertenceACondominio(imovel))
			) {
			primeiraCondicaoNaoGerarConta = true;
		}

		boolean segundaCondicaoNaoGerarConta = false;
		Collection<DebitoACobrar> debitosACobrar = this.obterDebitoACobrarImovel(imovel.getId(), DebitoCreditoSituacao.NORMAL, anoMesFaturamento);

		if (naoHaDebitosHaCobrar(debitosACobrar)) {
			segundaCondicaoNaoGerarConta = true;
		} else if (paralisacaoFaturamento(imovel)) {
			segundaCondicaoNaoGerarConta = true;
		} else if (!existeDebitoSemPagamento(debitosACobrar)) {
			segundaCondicaoNaoGerarConta = true;
		} else {
			Collection creditosARealizar = repositorioFaturamento.pesquisarCreditoARealizar(imovel.getId(),	DebitoCreditoSituacao.NORMAL, anoMesFaturamento);

			boolean existeCreditoComDevolucao = existeCreditoComDevolucao(creditosARealizar);

			if (naoHaCreditoARealizar(creditosARealizar) || existeCreditoComDevolucao) {
				DebitoTipo debitoTipo = null;
				segundaCondicaoNaoGerarConta = true;

				for (DebitoACobrar debitoACobrar: debitosACobrar) {
					debitoTipo = repositorioFaturamento.getDebitoTipo(debitoACobrar.getDebitoTipo().getId());

					if (debitoTipo.getIndicadorGeracaoConta().shortValue() != 2) {
						segundaCondicaoNaoGerarConta = false;
					}
				}
			}
		}

		return !(primeiraCondicaoNaoGerarConta && segundaCondicaoNaoGerarConta);
	}

	private boolean naoHaCreditoARealizar(Collection creditosARealizar) {
		return creditosARealizar == null || creditosARealizar.isEmpty();
	}

	private boolean existeCreditoComDevolucao(Collection creditosARealizar) {
		boolean existeCreditoComDevolucao = false;
		if (creditosARealizar != null && !creditosARealizar.isEmpty()) {
			Iterator iteratorColecaoCreditosARealizar = creditosARealizar.iterator();
			CreditoARealizar creditoARealizar = null;

			while (iteratorColecaoCreditosARealizar.hasNext()) {
				Object[] arrayCreditosACobrar = (Object[]) iteratorColecaoCreditosARealizar.next();
				creditoARealizar = new CreditoARealizar();
				creditoARealizar.setId((Integer) arrayCreditosACobrar[0]);

				FiltroDevolucao filtroDevolucao = new FiltroDevolucao();
				filtroDevolucao.adicionarParametro(new ParametroSimples(FiltroDevolucao.CREDITO_A_REALIZAR_ID,creditoARealizar.getId()));
				Collection devolucoes = getControladorUtil().pesquisar(filtroDevolucao, Devolucao.class.getName());

				if (devolucoes != null && !devolucoes.isEmpty()) {
					existeCreditoComDevolucao = true;
					break;
				}
			}
		}
		return existeCreditoComDevolucao;
	}

	private boolean existeDebitoSemPagamento(Collection<DebitoACobrar> debitosACobrar) {
		boolean existeDebitoSemPagamento = false;
		for (DebitoACobrar debitoACobrar: debitosACobrar) {
			FiltroPagamento filtroPagamento = new FiltroPagamento();
			filtroPagamento.adicionarParametro(new ParametroSimples(FiltroPagamento.DEBITO_A_COBRAR, debitoACobrar.getId()));
			Collection colecaoPagamentos = getControladorUtil().pesquisar(filtroPagamento, Pagamento.class.getName());
			if (colecaoPagamentos == null || colecaoPagamentos.isEmpty()) {
				existeDebitoSemPagamento = true;
				break;
			}
		}
		return existeDebitoSemPagamento;
	}

	private boolean paralisacaoFaturamento(Imovel imovel) {
		return imovel.getFaturamentoSituacaoTipo()!= null && imovel.getFaturamentoSituacaoTipo().getIndicadorParalisacaoFaturamento().equals(ConstantesSistema.SIM);
	}

	private boolean naoHaDebitosHaCobrar(Collection<DebitoACobrar> colecaoDebitosACobrar) {
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
