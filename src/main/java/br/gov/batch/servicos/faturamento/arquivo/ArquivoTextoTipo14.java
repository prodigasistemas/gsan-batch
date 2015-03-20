package br.gov.batch.servicos.faturamento.arquivo;

import static br.gov.model.util.Utilitarios.completaComEspacosADireita;
import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.formatarBigDecimalComPonto;
import static br.gov.model.util.Utilitarios.quebraLinha;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.Status;
import br.gov.model.micromedicao.LeituraAnormalidade;
import br.gov.servicos.micromedicao.LeituraAnormalidadeRepositorio;


@Stateless
public class ArquivoTextoTipo14 extends ArquivoTexto {

	@EJB
	private LeituraAnormalidadeRepositorio repositorio;

	public ArquivoTextoTipo14() {
		super();
	}

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String build(ArquivoTextoTO to) {
	    
		List<LeituraAnormalidade> leituraAnormalidades = repositorio.listarLeituraAnormalidadeImpressaoSimultanea(Status.ATIVO.getId());

		for (LeituraAnormalidade leitura : leituraAnormalidades) {
		    builder.append(quebraLinha);
			builder.append(TIPO_REGISTRO_14_ANORMALIDADE_LEITURA);
			builder.append(completaComZerosEsquerda(3, leitura.getId()));
			builder.append(completaComEspacosADireita(25, leitura.getDescricao()));
			builder.append(completaComEspacosADireita(1, leitura.getIndicadorLeitura()));
			builder.append(completaComEspacosADireita(2, (leitura.getLeituraAnormalidadeConsumoComLeitura() != null) ?
					leitura.getLeituraAnormalidadeConsumoComLeitura().getId() : null));
			builder.append(completaComEspacosADireita(2, (leitura.getLeituraAnormalidadeConsumoSemLeitura() != null) ?
					leitura.getLeituraAnormalidadeConsumoSemLeitura().getId() : null));
			builder.append(completaComEspacosADireita(2, (leitura.getLeituraAnormalidadeLeituraComLeitura() != null) ?
					leitura.getLeituraAnormalidadeLeituraComLeitura().getId() : null));
			builder.append(completaComEspacosADireita(2, (leitura.getLeituraAnormalidadeLeituraSemLeitura() != null) ?
					leitura.getLeituraAnormalidadeLeituraSemLeitura().getId() : null));
			builder.append(completaComEspacosADireita(1, leitura.getIndicadorUso()));
			builder.append((leitura.getNumeroFatorSemLeitura() != null) ? formatarBigDecimalComPonto(leitura.getNumeroFatorSemLeitura()) :
				completaComEspacosADireita(4, leitura.getNumeroFatorSemLeitura()));
			builder.append((leitura.getNumeroFatorComLeitura() != null) ? formatarBigDecimalComPonto(leitura.getNumeroFatorComLeitura()) :
				completaComEspacosADireita(4, leitura.getNumeroFatorComLeitura()));
		}

		return builder.toString();
	}
}
