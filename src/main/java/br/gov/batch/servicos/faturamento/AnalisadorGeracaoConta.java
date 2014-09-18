package br.gov.batch.servicos.faturamento;

import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.Status;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.CreditoRealizar;
import br.gov.model.faturamento.DebitoCobrar;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.servicos.arrecadacao.DevolucaoRepositorio;
import br.gov.servicos.arrecadacao.pagamento.PagamentoRepositorio;
import br.gov.servicos.faturamento.CreditoRealizarRepositorio;

@Stateless
public class AnalisadorGeracaoConta {
	
	@EJB
	private CreditoRealizarRepositorio creditoRealizarRepositorio;
	
	@EJB
	private DebitoCobrarBO debitoCobrarBO;
	
	@EJB
	private PagamentoRepositorio pagamentoRepositorio;
	
	@EJB
	private DevolucaoRepositorio devolucaoRepositorio;
	
	public AnalisadorGeracaoConta(){}
	
	public boolean verificarGeracaoConta(boolean aguaEsgotoZerados, int anoMesFaturamento, Imovel imovel) throws Exception {
		return verificarSituacaoImovelParaGerarConta(aguaEsgotoZerados, imovel) || verificarDebitosECreditosParaGerarConta(anoMesFaturamento, imovel);
	}

	public boolean verificarDebitosECreditosParaGerarConta(int anoMesFaturamento, Imovel imovel) {
		
		Collection<DebitoCobrar> debitosACobrar = debitoCobrarBO.debitosCobrarSemPagamentos(imovel);
		if (naoHaDebitosACobrar(debitosACobrar) || imovel.paralisacaoFaturamento()) {
			return false;
		}
		
		boolean segundaCondicaoGerarConta = true;
		Collection<CreditoRealizar> creditosARealizar = creditoRealizarRepositorio.buscarCreditoRealizarPorImovel(imovel.getId(), 
																													DebitoCreditoSituacao.NORMAL, 
																													anoMesFaturamento);

		if (naoHaCreditoARealizar(creditosARealizar) || devolucaoRepositorio.existeCreditoComDevolucao(creditosARealizar)) {
			segundaCondicaoGerarConta = haDebitosCobrarAtivos(debitosACobrar);
		}

		return segundaCondicaoGerarConta;
	}

	public boolean verificarSituacaoImovelParaGerarConta(boolean valoresAguaEsgotoZerados, Imovel imovel) {
		return !(valoresAguaEsgotoZerados || (!valoresAguaEsgotoZerados && !imovel.aguaLigada() && !imovel.esgotoLigado() && !imovel.pertenceACondominio()));
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

	private boolean naoHaCreditoARealizar(Collection<CreditoRealizar> creditosRealizar) {
		return creditosRealizar == null || creditosRealizar.isEmpty();
	}

	private boolean naoHaDebitosACobrar(Collection<DebitoCobrar> debitosACobrar) {
		return debitosACobrar == null || debitosACobrar.isEmpty();
	}

}
