package br.gov.batch.servicos.faturamento.arquivo;

import java.util.List;

import javax.ejb.EJB;

import br.gov.model.faturamento.Conta;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.faturamento.ContaImpostosDeduzidosRepositorio;
import br.gov.servicos.to.ContaImpostosDeduzidosTO;

public class ArquivoTextoTipo06 {

	@EJB
	private ContaImpostosDeduzidosRepositorio contaImpostosDeduzidosRepositorio;
	
	private StringBuilder builder;
	
	private final String TIPO_REGISTRO = "06";
	
	public String build(Conta conta) {

		builder = new StringBuilder();
		List<ContaImpostosDeduzidosTO> contas;
		
		if(conta!=null){
			contas = contaImpostosDeduzidosRepositorio.pesquisarParmsContaImpostosDeduzidos(conta.getId());
			for (ContaImpostosDeduzidosTO contaImpostosDeduzidos : contas) {
				builder.append(TIPO_REGISTRO);
				builder.append(Utilitarios.completaComZerosEsquerda(9, conta.getImovel().getId()));
				builder.append(String.valueOf(contaImpostosDeduzidos.getTipoImpostoId()));
				builder.append(Utilitarios.completaComEspacosADireita(15, contaImpostosDeduzidos.getDescricaoImposto()));
				builder.append(Utilitarios.completaComZerosEsquerda(6, Utilitarios.formatarBigDecimalComPonto(contaImpostosDeduzidos.getPercentualAliquota())));
				builder.append(System.getProperty("line.separator"));
			}
		}
	
		return builder.toString();
	}
}
