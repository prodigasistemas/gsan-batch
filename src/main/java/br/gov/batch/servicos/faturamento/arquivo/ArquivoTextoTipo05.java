package br.gov.batch.servicos.faturamento.arquivo;

import java.math.BigDecimal;
import java.util.List;

import javax.ejb.EJB;

import br.gov.model.faturamento.Conta;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.faturamento.FaturamentoRepositorio;
import br.gov.servicos.to.CreditoRealizadoTO;

public class ArquivoTextoTipo05 {
	@EJB
	private FaturamentoRepositorio faturamentoRepositorio;
	
	private StringBuilder builder;
	
	private Integer quantidadeLinhas;
	
	private final String TIPO_REGISTRO = "05";
	
	public String build(Conta conta) {

		builder = new StringBuilder();
		quantidadeLinhas = 0;
		
		if(conta!=null){
		
			CreditoRealizadoTO creditoRealizadoAnterior = null;
			BigDecimal valorCreditoAcumulado = BigDecimal.ZERO;
			String anoMesAcumulado = "";
			Integer qtdAnoMesDistintos = 0;
			List<CreditoRealizadoTO> creditosRealizados = faturamentoRepositorio.buscarCreditoRealizado(conta);
			
			for (CreditoRealizadoTO creditoRealizado : creditosRealizados) {
				if(creditoRealizadoAnterior==null ||
						creditoRealizadoAnterior.getCreditoTipo().getId().equals(
								creditoRealizado.getCreditoTipo().getId())){
					if(!carregarParametros(creditoRealizadoAnterior, creditoRealizado, 
							valorCreditoAcumulado, qtdAnoMesDistintos, anoMesAcumulado))
						gerarDadosCreditosRealizados(conta,creditoRealizadoAnterior, 1, "",
										creditoRealizadoAnterior.getValorCredito());
				}else{
					if (qtdAnoMesDistintos > 0) gerarDadosCreditosRealizados(conta,creditoRealizadoAnterior,
										qtdAnoMesDistintos,anoMesAcumulado,valorCreditoAcumulado);
					
					qtdAnoMesDistintos = 0;
					carregarParametros(creditoRealizadoAnterior, creditoRealizado, 
							valorCreditoAcumulado, qtdAnoMesDistintos, anoMesAcumulado);
				}
			}
			if (qtdAnoMesDistintos > 0) gerarDadosCreditosRealizados(conta,creditoRealizadoAnterior,
						qtdAnoMesDistintos,anoMesAcumulado,valorCreditoAcumulado);
		}
	
		return builder.toString();
	}

	private boolean carregarParametros(CreditoRealizadoTO creditoRealizadoAnterior,CreditoRealizadoTO creditoRealizado, 
			BigDecimal valorCreditoAcumulado, Integer qtdAnoMesDistintos, String anoMesAcumulado){
		
		creditoRealizadoAnterior = creditoRealizado;
		if(creditoRealizadoAnterior.getAnoMesReferenciaCredito()!=null){
			valorCreditoAcumulado = creditoRealizadoAnterior.getValorCredito();
			qtdAnoMesDistintos++;
			anoMesAcumulado = Utilitarios.formatarAnoMesParaMesAno(creditoRealizadoAnterior.getAnoMesReferenciaCredito());
			return true;
		}else{
			return false;
		}
	}
	
	private void gerarDadosCreditosRealizados(Conta conta,
			CreditoRealizadoTO creditoRealizado, Integer qtdAnoMesDistintos,
			String anoMesAcumulado, BigDecimal valorCreditoAcumulado) {
		
		quantidadeLinhas++;
		builder.append(TIPO_REGISTRO);
		builder.append(Utilitarios.completaComZerosEsquerda(9,conta.getImovel().getId()));
		if(qtdAnoMesDistintos>1){
			builder.append(Utilitarios.completaTexto(90,creditoRealizado.getCreditoTipo().getDescricao()+" "
					+anoMesAcumulado+((qtdAnoMesDistintos>5)?" E OUTRAS":"")));
			builder.append(Utilitarios.completaComZerosEsquerda(14, Utilitarios.formatarBigDecimalComPonto(valorCreditoAcumulado)));
		}else{
			builder.append(Utilitarios.completaTexto(90,creditoRealizado.getCreditoTipo().getDescricao()+
					((anoMesAcumulado == null || anoMesAcumulado.equals(""))?
					" PARCELA "+Utilitarios.completaComZerosEsquerda(2, creditoRealizado.getNumeroPrestacaoCredito()+"/"
					+Utilitarios.completaComZerosEsquerda(2, creditoRealizado.getNumeroPrestacoesRestantes())):"")));
			builder.append(Utilitarios.completaComZerosEsquerda(14, Utilitarios.formatarBigDecimalComPonto(creditoRealizado.getValorCredito())));
			
		}
		builder.append(Utilitarios.completaTexto(6,creditoRealizado.getCreditoTipo()!=null?
				((creditoRealizado.getCreditoTipo().getCodigoConstante()!=null)?
						creditoRealizado.getCreditoTipo().getCodigoConstante():creditoRealizado.getCreditoTipo().getId()):""));
		builder.append(System.getProperty("line.separator"));
	}
}













