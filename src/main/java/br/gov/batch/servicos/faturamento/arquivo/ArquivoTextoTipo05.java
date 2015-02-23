package br.gov.batch.servicos.faturamento.arquivo;

import java.math.BigDecimal;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.faturamento.Conta;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.faturamento.FaturamentoRepositorio;
import br.gov.servicos.to.CreditoRealizadoTO;

@Stateless
public class ArquivoTextoTipo05 extends ArquivoTexto {
	@EJB
	private FaturamentoRepositorio faturamentoRepositorio;

	private Integer qtdAnoMesDistintos;
	private CreditoRealizadoTO creditoRealizadoAnterior;
	private String anoMesAcumulado;
	
	public ArquivoTextoTipo05() {
		super();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String build(ArquivoTextoTO to) {
		Conta conta = to.getConta();
		
		if (conta != null) {
			creditoRealizadoAnterior = null;
			BigDecimal valorCreditoAcumulado = BigDecimal.ZERO;
			anoMesAcumulado = "";
			qtdAnoMesDistintos = 0;
			
			List<CreditoRealizadoTO> creditosRealizados = faturamentoRepositorio.buscarCreditoRealizado(conta);

			for (CreditoRealizadoTO creditoRealizado : creditosRealizados) {
				if (creditoRealizadoAnterior == null || creditoRealizadoAnterior.getCreditoTipo().getId().equals(creditoRealizado.getCreditoTipo().getId())) {
					if (!carregarParametros(creditoRealizado, valorCreditoAcumulado))
						gerarDadosCreditosRealizados(conta, 1, "", creditoRealizadoAnterior.getValorCredito());
				} else {
					if (qtdAnoMesDistintos > 0)
						gerarDadosCreditosRealizados(conta, qtdAnoMesDistintos, anoMesAcumulado, valorCreditoAcumulado);

					qtdAnoMesDistintos = 0;
					carregarParametros(creditoRealizado, valorCreditoAcumulado);
				}
			}
			if (qtdAnoMesDistintos > 0)
				gerarDadosCreditosRealizados(conta, qtdAnoMesDistintos, anoMesAcumulado, valorCreditoAcumulado);
		}

		return builder.toString();
	}

	private boolean carregarParametros(CreditoRealizadoTO creditoRealizado, BigDecimal valorCreditoAcumulado) {
		creditoRealizadoAnterior = creditoRealizado;
		
		if (creditoRealizadoAnterior.getAnoMesReferenciaCredito() != null) {
			valorCreditoAcumulado = creditoRealizadoAnterior.getValorCredito();
			qtdAnoMesDistintos++;
			anoMesAcumulado = Utilitarios.formatarAnoMesParaMesAno(creditoRealizadoAnterior.getAnoMesReferenciaCredito());
			return true;
		} else {
			return false;
		}
	}

	private void gerarDadosCreditosRealizados(Conta conta, Integer qtdAnoMesDistintos, String anoMesAcumulado, BigDecimal valorCreditoAcumulado) {
		builder.append(TIPO_REGISTRO_05_CREDITO);
		builder.append(Utilitarios.completaComZerosEsquerda(9, conta.getImovel().getId()));
		
		String descricaoCredito = "";
		String valorCredito = "";
		if (qtdAnoMesDistintos > 1) {
		    descricaoCredito = creditoRealizadoAnterior.getCreditoTipo().getDescricao() + " " + anoMesAcumulado + (qtdAnoMesDistintos > 5 ? " E OUTRAS" : "");
		    valorCredito = Utilitarios.formatarBigDecimalComPonto(valorCreditoAcumulado);
		} else {
		    descricaoCredito = creditoRealizadoAnterior.getCreditoTipo().getDescricao()
                    + ( anoMesAcumulado == null || anoMesAcumulado.equals("") 
                        ? " PARCELA " + Utilitarios.completaComZerosEsquerda(2, creditoRealizadoAnterior.getNumeroPrestacaoCredito()
                          + "/" + Utilitarios.completaComZerosEsquerda(2, creditoRealizadoAnterior.getNumeroPrestacoesRestantes())) 
                        : ""
                      );
		    valorCredito = Utilitarios.formatarBigDecimalComPonto(creditoRealizadoAnterior.getValorCredito());
		}
		
		builder.append(Utilitarios.completaComEspacosADireita(90, descricaoCredito));
		builder.append(Utilitarios.completaComEspacosADireita(14, valorCredito));
		
		builder.append(Utilitarios.completaComEspacosADireita(6, creditoRealizadoAnterior.getCreditoTipo() != null ? (
				(creditoRealizadoAnterior.getCreditoTipo().getCodigoConstante() != null) ? creditoRealizadoAnterior.getCreditoTipo().getCodigoConstante() : 
					creditoRealizadoAnterior.getCreditoTipo().getId()) : null));
		builder.append(System.getProperty("line.separator"));
	}
}