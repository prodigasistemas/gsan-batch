package br.gov.batch.servicos.faturamento.arquivo;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJB;

import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.DebitoCobrado;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.faturamento.DebitoCobradoRepositorio;

public class ArquivoTextoTipo04 {

	private final String TIPO_REGISTRO = "04";
	
	@EJB
	private DebitoCobradoRepositorio debitoCobradoRepositorio;
	
	public String build(Conta conta) {

		StringBuilder builder = new StringBuilder();
		int quantidadeLinhas = 0;

		if (conta != null) {

			Collection colecaoDebitoCobradoDeParcelamento = debitoCobradoRepositorio.pesquisarDebitoCobradoParcelamento(conta);

			if (colecaoDebitoCobradoDeParcelamento != null && !colecaoDebitoCobradoDeParcelamento.isEmpty()) {

				Iterator iterator = colecaoDebitoCobradoDeParcelamento.iterator();

				while (iterator.hasNext()) {

					Object[] arrayDebitoCobrado = (Object[]) iterator.next();

					quantidadeLinhas = quantidadeLinhas + 1;

					builder.append(TIPO_REGISTRO);
					builder.append(Utilitarios.completaComZerosEsquerda(9, conta.getImovel().getId().toString()));
					builder.append(Utilitarios.completaComEspacosADireita(90, "PARCELAMENTO DE DEBITOS PARCELA "
													+ Utilitarios.completaComZerosEsquerda(3, String.valueOf(arrayDebitoCobrado[0]))
													+ "/"
													+ Utilitarios.completaComZerosEsquerda(3, String.valueOf(arrayDebitoCobrado[1]))));
					builder.append(Utilitarios.completaComZerosEsquerda(14, Utilitarios.formatarBigDecimalComPonto((BigDecimal) arrayDebitoCobrado[2])));
					builder.append(Utilitarios.completaTexto(6, (arrayDebitoCobrado[3] != null ? arrayDebitoCobrado[3] : "") + ""));
					builder.append(System.getProperty("line.separator"));
				}
			}

			Collection colecaoDebitoCobradoNaoParcelamento = debitoCobradoRepositorio.pesquisarDebitoCobradoNaoParcelamento(conta);

			if (colecaoDebitoCobradoNaoParcelamento != null && !colecaoDebitoCobradoNaoParcelamento.isEmpty()) {

				Iterator iterator = colecaoDebitoCobradoNaoParcelamento.iterator();
				
				DebitoCobrado debitoCobradoAnterior = null;
				DebitoCobrado debitoCobradoAtual = null;
				BigDecimal valorPrestacaoAcumulado = BigDecimal.ZERO;
				String anoMesAcumulado = "";
				Integer qtdAnoMesDistintos = 0;

				while (iterator.hasNext()) {

					debitoCobradoAtual = (DebitoCobrado) iterator.next();

					if (debitoCobradoAnterior == null) {

						debitoCobradoAnterior = debitoCobradoAtual;

						if (debitoCobradoAnterior.getAnoMesReferenciaDebito() != null) {

							valorPrestacaoAcumulado = debitoCobradoAnterior.getValorPrestacao();
							qtdAnoMesDistintos++;
							anoMesAcumulado = Utilitarios.formatarAnoMesParaMesAno(debitoCobradoAnterior.getAnoMesReferenciaDebito());

						} else {
							quantidadeLinhas = quantidadeLinhas + 1;
							builder.append(this.obterDadosServicosParcelamentosParteII(conta, debitoCobradoAnterior, 1, "", debitoCobradoAnterior.getValorPrestacao()));
						}

					} else if (debitoCobradoAnterior != null && debitoCobradoAnterior.getDebitoTipo().getId().equals(debitoCobradoAtual.getDebitoTipo().getId())) {

						debitoCobradoAnterior = debitoCobradoAtual;

						if (debitoCobradoAnterior.getAnoMesReferenciaDebito() != null) {

							valorPrestacaoAcumulado = valorPrestacaoAcumulado.add(debitoCobradoAtual.getValorPrestacao());
							qtdAnoMesDistintos++;
							anoMesAcumulado = anoMesAcumulado + " " + Utilitarios.formatarAnoMesParaMesAno(debitoCobradoAtual.getAnoMesReferenciaDebito());

						} else {
							quantidadeLinhas = quantidadeLinhas + 1;
							builder.append(this.obterDadosServicosParcelamentosParteII(conta, debitoCobradoAnterior, 1, "", debitoCobradoAnterior.getValorPrestacao()));
						}

					} else {
						if (qtdAnoMesDistintos > 0) {
							quantidadeLinhas = quantidadeLinhas + 1;
							builder.append(this.obterDadosServicosParcelamentosParteII(conta, debitoCobradoAnterior, qtdAnoMesDistintos, anoMesAcumulado, valorPrestacaoAcumulado));
						}

						debitoCobradoAnterior = debitoCobradoAtual;
						qtdAnoMesDistintos = 0;

						if (debitoCobradoAnterior.getAnoMesReferenciaDebito() != null) {
							valorPrestacaoAcumulado = debitoCobradoAnterior.getValorPrestacao();
							qtdAnoMesDistintos = 1;
							anoMesAcumulado = Utilitarios.formatarAnoMesParaMesAno(debitoCobradoAnterior.getAnoMesReferenciaDebito());
						} else {
							quantidadeLinhas = quantidadeLinhas + 1;
							builder.append(this.obterDadosServicosParcelamentosParteII(conta, debitoCobradoAnterior, 1, "", debitoCobradoAnterior.getValorPrestacao()));
						}
					}
				}

				if (qtdAnoMesDistintos > 0) {
					quantidadeLinhas = quantidadeLinhas + 1;
					builder.append(this.obterDadosServicosParcelamentosParteII(conta, debitoCobradoAnterior, qtdAnoMesDistintos, anoMesAcumulado, valorPrestacaoAcumulado));
				}
			}
		}

		return builder.toString();
	}
	
	public StringBuilder obterDadosServicosParcelamentosParteII(Conta conta, DebitoCobrado debitoCobrado, Integer qtdAnoMesDistintos, 
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
				builder.append(Utilitarios.completaTexto(90, debitoCobrado.getDebitoTipo().getDescricao() + " " + anoMesAcumulado));
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
}