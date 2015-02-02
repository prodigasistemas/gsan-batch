package br.gov.batch.servicos.faturamento.arquivo;

import java.util.List;

import javax.ejb.EJB;

import br.gov.model.faturamento.Conta;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.faturamento.ContaImpostosDeduzidosRepositorio;
import br.gov.servicos.to.ContaImpostosDeduzidosTO;

public class ArquivoTextoTipo06 extends ArquivoTexto {

	@EJB
	private ContaImpostosDeduzidosRepositorio contaImpostosDeduzidosRepositorio;
	
	public String build(Conta conta) {
		if (conta != null) {
			List<ContaImpostosDeduzidosTO> contas = contaImpostosDeduzidosRepositorio.pesquisarParmsContaImpostosDeduzidos(conta.getId());
			
			for (ContaImpostosDeduzidosTO contaImpostosDeduzidos : contas) {
				builder.append(TIPO_REGISTRO_06);
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
