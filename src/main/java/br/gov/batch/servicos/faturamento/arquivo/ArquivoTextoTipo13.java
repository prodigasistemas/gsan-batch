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
import br.gov.model.Status;
import br.gov.model.micromedicao.ConsumoAnormalidade;
import br.gov.servicos.micromedicao.ConsumoAnormalidadeRepositorio;
@Stateless
public class ArquivoTextoTipo13 extends ArquivoTexto {

	@EJB
	private ConsumoAnormalidadeRepositorio consumoAnormalidadeRepositorio;

	public ArquivoTextoTipo13() {
		super();
	}

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String build(ArquivoTextoTO to) {
	    
		List<ConsumoAnormalidade> listaConsumoAnormalidade = consumoAnormalidadeRepositorio.listarConsumoAnormalidadePor(Status.ATIVO.getId());

		for (ConsumoAnormalidade consumoAnormalidade : listaConsumoAnormalidade) {
		    builder.append(quebraLinha);
			builder.append(TIPO_REGISTRO_13_ANORMALIDADE_CONSUMO);
			builder.append(completaComZerosEsquerda(2, consumoAnormalidade.getId()));
			builder.append(completaComEspacosADireita(120, consumoAnormalidade.getMensagemConta()));
		}

		return builder.toString();
	}
}
