package br.gov.batch.servicos.faturamento.arquivo;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJB;

import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.DebitoCobrado;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.faturamento.DebitoCobradoRepositorio;
import br.gov.servicos.to.DebitoCobradoParcelamentoTO;

public class ArquivoTextoTipo04 {

	private final String TIPO_REGISTRO = "04";
	
	@EJB
	private DebitoCobradoRepositorio debitoCobradoRepositorio;
	
	private BigDecimal valorPrestacaoAcumulado;
	private String anoMesAcumulado;
	private Integer qtdAnoMesDistintos;
	private StringBuilder builder;
	
	public String build(Conta conta) {

		builder = new StringBuilder();
		int quantidadeLinhas = 0;

		if (conta != null) {

			Collection<DebitoCobradoParcelamentoTO> colecaoDebitoCobradoDeParcelamento = debitoCobradoRepositorio.pesquisarDebitoCobradoParcelamento(conta);

			if (colecaoDebitoCobradoDeParcelamento != null && !colecaoDebitoCobradoDeParcelamento.isEmpty()) {

				for (DebitoCobradoParcelamentoTO debitoParcelamento : colecaoDebitoCobradoDeParcelamento) {
					
					quantidadeLinhas = quantidadeLinhas + 1;
					
					builder.append(TIPO_REGISTRO);
					builder.append(Utilitarios.completaComZerosEsquerda(9, conta.getImovel().getId().toString()));
					builder.append(getDescricaoServicoParcelamento(debitoParcelamento));
					builder.append(Utilitarios.completaComZerosEsquerda(14, Utilitarios.formatarBigDecimalComPonto(debitoParcelamento.getTotalPrestacao())));
					builder.append(Utilitarios.completaTexto(6, (debitoParcelamento.getCodigoConstante() != null ? debitoParcelamento.getCodigoConstante() : "") + ""));
					builder.append(System.getProperty("line.separator"));
				}
			}

			Collection<DebitoCobrado> colecaoDebitoCobradoNaoParcelamento = debitoCobradoRepositorio.pesquisarDebitoCobradoNaoParcelamento(conta);

			if (colecaoDebitoCobradoNaoParcelamento != null && !colecaoDebitoCobradoNaoParcelamento.isEmpty()) {

				DebitoCobrado debitoCobradoAnterior = null;
				
				valorPrestacaoAcumulado = BigDecimal.ZERO;
				anoMesAcumulado = "";
				qtdAnoMesDistintos = 0;

				for (DebitoCobrado debitoCobradoAtual : colecaoDebitoCobradoNaoParcelamento) {

					if (debitoTipoAnteriorIgualAtual(debitoCobradoAnterior, debitoCobradoAtual)) {
						quantidadeLinhas = buildLinhaOuAtualizaParametros(conta, quantidadeLinhas, debitoCobradoAnterior, debitoCobradoAtual, qtdAnoMesDistintos++);
					} else {
						if (qtdAnoMesDistintos > 0) {
							quantidadeLinhas = adicionaLinha(conta, quantidadeLinhas, debitoCobradoAnterior, qtdAnoMesDistintos, anoMesAcumulado, valorPrestacaoAcumulado);
						}

						qtdAnoMesDistintos = 0;

						quantidadeLinhas = buildLinhaOuAtualizaParametros(conta, quantidadeLinhas, debitoCobradoAnterior, debitoCobradoAtual, 1);
					}
					
					debitoCobradoAnterior = debitoCobradoAtual;
				}

				if (qtdAnoMesDistintos > 0) {
					quantidadeLinhas = adicionaLinha(conta, quantidadeLinhas, debitoCobradoAnterior, qtdAnoMesDistintos, anoMesAcumulado, valorPrestacaoAcumulado);
				}
			}
		}

		return builder.toString();
	}

	private int buildLinhaOuAtualizaParametros(Conta conta, int quantidadeLinhas, DebitoCobrado debitoCobradoAnterior, DebitoCobrado debitoCobradoAtual, int qtdAnoMesDistintos) {
		if (debitoCobradoAtual.getAnoMesReferenciaDebito() != null) {
			atualizaParametros(calculaValorPrestacao(debitoCobradoAnterior, debitoCobradoAtual), 
							   calculaAnoMes(debitoCobradoAnterior, debitoCobradoAtual),
							   qtdAnoMesDistintos);
		} else {
			quantidadeLinhas = adicionaLinha(conta, quantidadeLinhas, debitoCobradoAtual, 1, "", debitoCobradoAtual.getValorPrestacao());
		}
		
		return quantidadeLinhas;
	}
	
	private BigDecimal calculaValorPrestacao(DebitoCobrado debitoCobradoAnterior, DebitoCobrado debitoCobradoAtual) {
		if(debitoTipoAnteriorIgualAtual(debitoCobradoAnterior, debitoCobradoAtual)) {
			return valorPrestacaoAcumulado.add(debitoCobradoAtual.getValorPrestacao()); 
		} 
		
		return debitoCobradoAtual.getValorPrestacao();
	}
	
	private String calculaAnoMes(DebitoCobrado debitoCobradoAnterior, DebitoCobrado debitoCobradoAtual) {
		if(debitoTipoAnteriorIgualAtual(debitoCobradoAnterior, debitoCobradoAtual)) {
			return anoMesAcumulado + " " + Utilitarios.formatarAnoMesParaMesAno(debitoCobradoAtual.getAnoMesReferenciaDebito()); 
		} 
		
		return Utilitarios.formatarAnoMesParaMesAno(debitoCobradoAtual.getAnoMesReferenciaDebito());
	}

	private boolean debitoTipoAnteriorIgualAtual(DebitoCobrado debitoCobradoAnterior, DebitoCobrado debitoCobradoAtual) {
		return debitoCobradoAnterior != null && debitoCobradoAnterior.getDebitoTipo().getId().equals(debitoCobradoAtual.getDebitoTipo().getId());
	}

	private void atualizaParametros(BigDecimal valorPrestacao, String anoMes, int qtdAnoMes) {
		valorPrestacaoAcumulado = valorPrestacao;
		qtdAnoMesDistintos = qtdAnoMes;
		anoMesAcumulado = anoMes;
	}

	private int adicionaLinha(Conta conta, int quantidadeLinhas, DebitoCobrado debitoCobradoAtual, int qtdMeses, String anoMesAcumulado, BigDecimal valorPrestacao) {
		quantidadeLinhas = quantidadeLinhas + 1;
		builder.append(this.obterDadosServicosParcelamentos(conta, debitoCobradoAtual, qtdMeses, anoMesAcumulado, valorPrestacao));
		return quantidadeLinhas;
	}
	
	public StringBuilder obterDadosServicosParcelamentos(Conta conta, DebitoCobrado debitoCobrado, Integer qtdAnoMesDistintos, 
																String anoMesAcumulado, BigDecimal valorPrestacaoAcumulado) {
		StringBuilder builder = new StringBuilder();

		builder.append(TIPO_REGISTRO);
		builder.append(Utilitarios.completaComZerosEsquerda(9, conta.getImovel().getId().toString()));
		
		if (qtdAnoMesDistintos > 1) {
			if (qtdAnoMesDistintos > 5) {
				builder.append(Utilitarios.completaTexto(90, debitoCobrado.getDebitoTipo().getDescricao() + " " + anoMesAcumulado + " E OUTRAS"));
			} else {
				builder.append(Utilitarios.completaTexto(90, debitoCobrado.getDebitoTipo().getDescricao() + " " + anoMesAcumulado));
			}
			
			builder.append(Utilitarios.completaComZerosEsquerda(14, Utilitarios.formatarBigDecimalComPonto(valorPrestacaoAcumulado)));
			
		} else {
			if (anoMesAcumulado == null || anoMesAcumulado.equals("")) {
				builder.append(Utilitarios.completaTexto(90, debitoCobrado.getDebitoTipo().getDescricao() + " PARCELA "
								+ Utilitarios.completaComZerosEsquerda(3, String.valueOf(debitoCobrado.getNumeroPrestacaoDebito()))
								+ "/"
								+ Utilitarios.completaComZerosEsquerda(3, String.valueOf(debitoCobrado.getNumeroPrestacao()))));
			} else {
				builder.append(Utilitarios.completaComEspacosADireita(90, debitoCobrado.getDebitoTipo().getDescricao() + " " + anoMesAcumulado));
			}

			builder.append(Utilitarios.completaComZerosEsquerda(14, Utilitarios.formatarBigDecimalComPonto(debitoCobrado.getValorPrestacao())));
		}
		
		builder.append(getCodigoCostanteDebitoTipo(debitoCobrado, builder));
		builder.append(System.getProperty("line.separator"));

		return builder;
	}

	private String getCodigoCostanteDebitoTipo(DebitoCobrado debitoCobrado, StringBuilder builder) {
		if (debitoCobrado.getDebitoTipo() != null && debitoCobrado.getDebitoTipo().getCodigoConstante() != null) {
			return Utilitarios.completaTexto(6, debitoCobrado.getDebitoTipo().getCodigoConstante() + "");
		} else {
			return Utilitarios.completaTexto(6, "");
		}
	}
	
	private String getDescricaoServicoParcelamento(DebitoCobradoParcelamentoTO debito) {
		StringBuilder descricao = new StringBuilder();
		
		descricao.append("PARCELAMENTO DE DEBITOS PARCELA ")
				.append(Utilitarios.completaComZerosEsquerda(3, String.valueOf(debito.getNumeroPrestacaoDebito())))
				.append("/")
				.append(Utilitarios.completaComZerosEsquerda(3, String.valueOf(debito.getTotalParcela())));
		
		return Utilitarios.completaComEspacosADireita(90, descricao.toString());
	}
}