package br.gov.batch.servicos.faturamento.arquivo;

import static br.gov.model.util.Utilitarios.completaComEspacosADireita;
import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.completaTexto;
import static br.gov.model.util.Utilitarios.formatarAnoMesParaMesAno;
import static br.gov.model.util.Utilitarios.formatarBigDecimalComPonto;

import java.math.BigDecimal;
import java.util.Collection;

import javax.ejb.EJB;

import br.gov.model.faturamento.Conta;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.faturamento.DebitoCobradoRepositorio;
import br.gov.servicos.to.DebitoCobradoNaoParceladoTO;
import br.gov.servicos.to.ParcelaDebitoCobradoTO;


public class ArquivoTextoTipo04 extends ArquivoTexto {

	private final String TIPO_REGISTRO = "04";
	
	@EJB
	private DebitoCobradoRepositorio debitoCobradoRepositorio;
	
	private BigDecimal valorPrestacaoAcumulado;
	private String anoMesAcumulado;
	private Integer qtdAnoMesDistintos;
	
	public String build(Conta conta) {

		if (conta != null) {

			Collection<ParcelaDebitoCobradoTO> colecaoDebitoCobradoDeParcelamento = debitoCobradoRepositorio.pesquisarDebitoCobradoParcelamento(conta.getId());

			if (colecaoDebitoCobradoDeParcelamento != null && !colecaoDebitoCobradoDeParcelamento.isEmpty()) {

				for (ParcelaDebitoCobradoTO debitoParcelamento : colecaoDebitoCobradoDeParcelamento) {
					builder.append(TIPO_REGISTRO);
					builder.append(Utilitarios.completaComZerosEsquerda(9, conta.getImovel().getId().toString()));
					builder.append(getDescricaoServicoParcelamento(debitoParcelamento));
					builder.append(Utilitarios.completaComZerosEsquerda(14, Utilitarios.formatarBigDecimalComPonto(debitoParcelamento.getTotalPrestacao())));
					builder.append(Utilitarios.completaTexto(6, (debitoParcelamento.getCodigoConstante() != null ? debitoParcelamento.getCodigoConstante() : "") + ""));
					builder.append(System.getProperty("line.separator"));
				}
			}

			Collection<DebitoCobradoNaoParceladoTO> debitosCobradosNaoParcelados = debitoCobradoRepositorio.pesquisarDebitoCobradoSemParcelamento(conta.getId());

			DebitoCobradoNaoParceladoTO debitoCobradoAnterior = null;
			
			valorPrestacaoAcumulado = BigDecimal.ZERO;
			anoMesAcumulado = "";
			qtdAnoMesDistintos = 0;

			for (DebitoCobradoNaoParceladoTO atual : debitosCobradosNaoParcelados) {

				if (atual.possuiMesmoTipoDebitoAnterior(debitoCobradoAnterior)) {
					buildLinhaOuAtualizaParametros(conta, debitoCobradoAnterior, atual, qtdAnoMesDistintos++);
				} else {
					if (qtdAnoMesDistintos > 0) {
						adicionaLinha(conta, debitoCobradoAnterior, qtdAnoMesDistintos, anoMesAcumulado, valorPrestacaoAcumulado);
					}

					qtdAnoMesDistintos = 0;

					buildLinhaOuAtualizaParametros(conta, debitoCobradoAnterior, atual, 1);
				}
				
				debitoCobradoAnterior = atual;
			}

			if (qtdAnoMesDistintos > 0) {
				adicionaLinha(conta, debitoCobradoAnterior, qtdAnoMesDistintos, anoMesAcumulado, valorPrestacaoAcumulado);
			}
		}

		return builder.toString();
	}

	private void buildLinhaOuAtualizaParametros(Conta conta, DebitoCobradoNaoParceladoTO anterior, DebitoCobradoNaoParceladoTO atual, int qtdAnoMesDistintos) {
		if (atual.getAnoMesReferencia() != null) {
			atualizaParametros(calculaValorPrestacao(anterior, atual), 
							   calculaAnoMes(anterior, atual),
							   qtdAnoMesDistintos);
		} else {
			adicionaLinha(conta, atual, 1, "", atual.getValorPrestacao());
		}
	}
	
	private BigDecimal calculaValorPrestacao(DebitoCobradoNaoParceladoTO anterior, DebitoCobradoNaoParceladoTO atual) {
		if(atual.possuiMesmoTipoDebitoAnterior(anterior)) {
			return valorPrestacaoAcumulado.add(atual.getValorPrestacao()); 
		} 
		
		return atual.getValorPrestacao();
	}
	
	private String calculaAnoMes(DebitoCobradoNaoParceladoTO anterior, DebitoCobradoNaoParceladoTO atual) {
	    String anoMes = formatarAnoMesParaMesAno(atual.getAnoMesReferencia());
        return atual.possuiMesmoTipoDebitoAnterior(anterior) ? anoMesAcumulado + " " + anoMes: anoMes;  
	}

	private void atualizaParametros(BigDecimal valorPrestacao, String anoMes, int qtdAnoMes) {
		valorPrestacaoAcumulado = valorPrestacao;
		qtdAnoMesDistintos = qtdAnoMes;
		anoMesAcumulado = anoMes;
	}

	private void adicionaLinha(Conta conta, DebitoCobradoNaoParceladoTO debito, int qtdMeses, String anoMesAcumulado, BigDecimal valorPrestacao) {
		builder.append(this.obterDadosServicosParcelamentos(conta, debito, qtdMeses, anoMesAcumulado, valorPrestacao));
	}
	
	public StringBuilder obterDadosServicosParcelamentos(Conta conta, DebitoCobradoNaoParceladoTO debito, Integer qtdAnoMesDistintos, 
																String anoMesAcumulado, BigDecimal valorPrestacaoAcumulado) {
		StringBuilder builder = new StringBuilder();

		builder.append(TIPO_REGISTRO);
		builder.append(completaComZerosEsquerda(9, conta.getImovel().getId().toString()));
		
		if (qtdAnoMesDistintos > 1) {
		    builder.append(completaTexto(90, debito.getDescricaoTipoDebito() + " " + anoMesAcumulado));
			if (qtdAnoMesDistintos > 5) {
				builder.append(" E OUTRAS");
			}
			
			builder.append(completaComZerosEsquerda(14, formatarBigDecimalComPonto(valorPrestacaoAcumulado)));
		} else {
			if (anoMesAcumulado == null || anoMesAcumulado.equals("")) {
				builder.append(completaTexto(90, debito.getDescricaoTipoDebito() + " PARCELA "
								+ completaComZerosEsquerda(3, String.valueOf(debito.getNumeroPrestacaoDebito()))
								+ "/"
								+ completaComZerosEsquerda(3, String.valueOf(debito.getTotalPrestacao()))));
			} else {
				builder.append(completaComEspacosADireita(90, debito.getDescricaoTipoDebito() + " " + anoMesAcumulado));
			}

			builder.append(completaComZerosEsquerda(14, formatarBigDecimalComPonto(debito.getValorPrestacao())));
		}
		
		builder.append(completaTexto(6, debito.getConstanteTipoDebito() + ""));
		builder.append(System.getProperty("line.separator"));

		return builder;
	}
	
	private String getDescricaoServicoParcelamento(ParcelaDebitoCobradoTO debito) {
		StringBuilder descricao = new StringBuilder();
		
		descricao.append("PARCELAMENTO DE DEBITOS PARCELA ")
				.append(completaComZerosEsquerda(3, String.valueOf(debito.getNumeroPrestacaoDebito())))
				.append("/")
				.append(completaComZerosEsquerda(3, String.valueOf(debito.getTotalParcela())));
		
		return completaComEspacosADireita(90, descricao.toString());
	}
}