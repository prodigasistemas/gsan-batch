package br.gov.batch.servicos.faturamento.arquivo;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.logging.Logger;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.Status;
import br.gov.model.micromedicao.ConsumoAnormalidade;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.micromedicao.ConsumoAnormalidadeRepositorio;

@Stateless
public class ArquivoTextoTipo13 extends ArquivoTexto {
    private static Logger logger = Logger.getLogger(ArquivoTextoTipo13.class);

	@EJB
	private ConsumoAnormalidadeRepositorio consumoAnormalidadeRepositorio;

	public ArquivoTextoTipo13() {
		super();
	}

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String build(ArquivoTextoTO to) {
//        logger.info("Construcao da linha 13");
	    
		List<ConsumoAnormalidade> listaConsumoAnormalidade = consumoAnormalidadeRepositorio.listarConsumoAnormalidadePor(Status.ATIVO.getId());

		builder.append(System.getProperty("line.separator"));
		builder.append(TIPO_REGISTRO_13_ANORMALIDADE_CONSUMO);
		for (ConsumoAnormalidade consumoAnormalidade : listaConsumoAnormalidade) {
			builder.append(Utilitarios.completaComZerosEsquerda(2, consumoAnormalidade.getId()));
			builder.append(Utilitarios.completaComEspacosADireita(120, consumoAnormalidade.getMensagemConta()));
			builder.append(System.getProperty("line.separator"));
		}

		return builder.toString();
	}
}
