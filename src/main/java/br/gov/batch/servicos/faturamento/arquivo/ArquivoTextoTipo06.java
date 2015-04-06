package br.gov.batch.servicos.faturamento.arquivo;

import static br.gov.model.util.Utilitarios.completaComEspacosADireita;
import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.quebraLinha;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.faturamento.Conta;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.faturamento.ContaImpostosDeduzidosRepositorio;
import br.gov.servicos.to.ContaImpostosDeduzidosTO;

@Stateless
public class ArquivoTextoTipo06 extends ArquivoTexto {
	@EJB
	private ContaImpostosDeduzidosRepositorio contaImpostosDeduzidosRepositorio;

	public ArquivoTextoTipo06() {
		super();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String build(ArquivoTextoTO to) {
		Conta conta = to.getConta();
		
		if (conta != null) {
			List<ContaImpostosDeduzidosTO> contas = contaImpostosDeduzidosRepositorio.pesquisarParmsContaImpostosDeduzidos(conta.getId());

			for (ContaImpostosDeduzidosTO contaImpostosDeduzidos : contas) {
				builder.append(TIPO_REGISTRO_06_IMPOSTOS);
				builder.append(completaComZerosEsquerda(9, conta.getImovel().getId()));
				builder.append(String.valueOf(contaImpostosDeduzidos.getTipoImpostoId()));
				builder.append(completaComEspacosADireita(15, contaImpostosDeduzidos.getDescricaoImposto()));
				builder.append(completaComZerosEsquerda(6, Utilitarios.formatarBigDecimalComPonto(contaImpostosDeduzidos.getPercentualAliquota())));
				builder.append(quebraLinha);
			}
		}
		return builder.toString();
	}
}
