package br.gov.batch.servicos.faturamento.arquivo;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.logging.Logger;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.faturamento.Conta;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.faturamento.ContaImpostosDeduzidosRepositorio;
import br.gov.servicos.to.ContaImpostosDeduzidosTO;

@Stateless
public class ArquivoTextoTipo06 extends ArquivoTexto {
    private static Logger logger = Logger.getLogger(ArquivoTextoTipo06.class);

	@EJB
	private ContaImpostosDeduzidosRepositorio contaImpostosDeduzidosRepositorio;

	public ArquivoTextoTipo06() {
		super();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String build(ArquivoTextoTO to) {
//        logger.info("Construcao da linha 06");
 
		Conta conta = to.getConta();
		
		if (conta != null) {
			List<ContaImpostosDeduzidosTO> contas = contaImpostosDeduzidosRepositorio.pesquisarParmsContaImpostosDeduzidos(conta.getId());

			for (ContaImpostosDeduzidosTO contaImpostosDeduzidos : contas) {
				builder.append(TIPO_REGISTRO_06_IMPOSTOS);
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
