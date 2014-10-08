package br.gov.batch.servicos.faturamento;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.batch.servicos.arrecadacao.DebitoAutomaticoMovimentoBO;
import br.gov.batch.servicos.cadastro.ClienteContaBO;
import br.gov.model.Status;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.FaturamentoSituacaoHistorico;
import br.gov.servicos.faturamento.FaturamentoSituacaoRepositorio;
import br.gov.servicos.to.CreditosContaTO;
import br.gov.servicos.to.DebitosContaTO;
import br.gov.servicos.to.ImpostosDeduzidosContaTO;

@Stateless
public class FaturamentoImovelBO {
	
	@EJB
	private AnalisadorGeracaoConta analisadorGeracaoConta;
	
	@EJB
	private DebitosContaBO debitosContaBO;
	
	@EJB
	private CreditosContaBO creditosContaBO;
	
	@EJB
	private ImpostosContaBO impostosContaBO;
	
	@EJB
	private ContaBO contaBO;
	
	@EJB
	private ClienteContaBO clienteContaBO;
	
	@EJB
	private ContaCategoriaBO contaCategoriaBO;
	
	@EJB
	private ContaImpostosDeduzidosBO contaImpostosDeduzidosBO;
	
	@EJB
	private DebitoCobradoBO debitoCobradoBO;
	
	@EJB
	private DebitoCobrarBO debitoCobrarBO;

	@EJB
	private CreditoRealizadoBO creditoRealizadoBO;
	
	@EJB
	private CreditoRealizarBO creditoRealizarBO;
	
	@EJB
	private DebitoAutomaticoMovimentoBO debitoAutomaticoMovimentoBO;

	@EJB
	private FaturamentoSituacaoRepositorio faturamentoSituacaoRepositorio;
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void preDeterminarFaturamentoImovel(FaturamentoImovelTO faturamentoTO) throws Exception {
		Imovel imovel = faturamentoTO.getImovel();
		Integer anoMesFaturamento = faturamentoTO.getAnoMesFaturamento();
		
		boolean valoresAguaEsgotoZerados = false;
		if (imovel.possuiLigacaoAguaAtiva() || imovel.possuiLigacaoEsgotoAtiva() || imovel.existeHidrometro()) {
			valoresAguaEsgotoZerados = !deveFaturar(imovel, anoMesFaturamento);
		}
		
		if (analisadorGeracaoConta.verificarGeracaoConta(valoresAguaEsgotoZerados, anoMesFaturamento, imovel)) {
			DebitosContaTO debitosContaTO = debitosContaBO.gerarDebitosConta(imovel, anoMesFaturamento);
			CreditosContaTO creditosContaTO = creditosContaBO.gerarCreditosConta(imovel, anoMesFaturamento);
			ImpostosDeduzidosContaTO impostosDeduzidosContaTO = impostosContaBO.gerarImpostosDeduzidosConta(imovel, anoMesFaturamento, 
																				debitosContaTO.getValorTotalDebito(), creditosContaTO.getValorTotalCreditos());

			Conta conta = contaBO.gerarConta(faturamentoTO, debitosContaTO, creditosContaTO, impostosDeduzidosContaTO);

			contaCategoriaBO.inserirContasCategoriaValoresZerados(imovel.getId(), conta);
			clienteContaBO.inserirClienteContaComImoveisAtivos(imovel, conta);
			contaImpostosDeduzidosBO.inserirContaImpostosDeduzidos(conta, impostosDeduzidosContaTO);

			debitoCobradoBO.inserirDebitoCobrado(debitosContaTO, conta);
			debitoCobrarBO.atualizarDebitoCobrar(debitosContaTO.getDebitosCobrar());
			
			creditoRealizadoBO.inserirCreditoRealizado(creditosContaTO, conta);
			creditoRealizarBO.atualizarCreditoRealizar(creditosContaTO.getCreditosRealizar());

			if (imovel.getIndicadorDebitoConta().equals(Status.ATIVO) && conta.getContaMotivoRevisao() == null) {
				debitoAutomaticoMovimentoBO.gerarMovimentoDebitoAutomatico(imovel, conta, faturamentoTO.getFaturamentoGrupo());
			}
		}
	}
	
	public boolean verificarFaturarEmSituacaoEspecialFaturamento(Imovel imovel, Integer anoMesFaturamento) {
		boolean faturar = true;

		if (imovel.paralisacaoFaturamento()
				&& possuiSituacaoEspecialFaturamento(imovel, anoMesFaturamento) 
				&& imovel.faturamentoAguaValido()) {
			faturar = false;
		}

		return faturar;
	}

	public boolean possuiSituacaoEspecialFaturamento(Imovel imovel, Integer anoMesFaturamento) {
		List<FaturamentoSituacaoHistorico> faturamentosSituacaoHistorico = faturamentoSituacaoRepositorio.faturamentosHistoricoVigentesPorImovel(imovel.getId());
		FaturamentoSituacaoHistorico faturamentoSituacaoHistorico = faturamentosSituacaoHistorico.get(0);
		
		return faturamentoSituacaoHistorico != null 
				&& anoMesFaturamento >= faturamentoSituacaoHistorico.getAnoMesFaturamentoSituacaoInicio() 
				&& anoMesFaturamento <= faturamentoSituacaoHistorico.getAnoMesFaturamentoSituacaoFim();
	}
	
	private boolean deveFaturar(Imovel imovel, Integer anoMesFaturamento) {
		boolean faturar = true;

		if (imovel.getFaturamentoSituacaoTipo() != null) {

			List<FaturamentoSituacaoHistorico> faturamentosSituacaoHistorico = faturamentoSituacaoRepositorio.faturamentosHistoricoVigentesPorImovel(imovel.getId());
			FaturamentoSituacaoHistorico faturamentoSituacaoHistorico = faturamentosSituacaoHistorico.get(0);

			if ((faturamentoSituacaoHistorico != null 
					&& anoMesFaturamento >= faturamentoSituacaoHistorico.getAnoMesFaturamentoSituacaoInicio() 
					&& anoMesFaturamento <= faturamentoSituacaoHistorico.getAnoMesFaturamentoSituacaoFim())
					&& imovel.paralisacaoFaturamento() 
					&& imovel.faturamentoAguaValido()) {
				faturar = false;
			}
		}

		return faturar;
	}
	
}