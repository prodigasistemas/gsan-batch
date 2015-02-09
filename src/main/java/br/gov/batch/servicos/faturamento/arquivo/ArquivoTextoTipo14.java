package br.gov.batch.servicos.faturamento.arquivo;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.Status;
import br.gov.model.micromedicao.LeituraAnormalidade;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.micromedicao.LeituraAnormalidadeRepositorio;

@Stateless
public class ArquivoTextoTipo14 extends ArquivoTexto {

	@EJB
	private LeituraAnormalidadeRepositorio repositorio;

	public ArquivoTextoTipo14() {
		super();
	}

	public String build(ArquivoTextoTO to) {
		List<LeituraAnormalidade> leituraAnormalidades = repositorio.listarLeituraAnormalidadePor(to.getIdLeituraAnormalidade(), Status.ATIVO.getId());

		builder.append(TIPO_REGISTRO_14);
		for (LeituraAnormalidade leituraAnormalidade : leituraAnormalidades) {
			builder.append(Utilitarios.completaComZerosEsquerda(3, leituraAnormalidade.getId()));
			builder.append(Utilitarios.completaComEspacosADireita(25, leituraAnormalidade.getDescricao()));
			builder.append(Utilitarios.completaComEspacosADireita(1, leituraAnormalidade.getIndicadorLeitura()));
			builder.append(Utilitarios.completaComEspacosADireita(2, (leituraAnormalidade.getLeituraAnormalidadeConsumoComLeitura() != null) ?
					leituraAnormalidade.getLeituraAnormalidadeConsumoComLeitura().getId() : null));
			builder.append(Utilitarios.completaComEspacosADireita(2, (leituraAnormalidade.getLeituraAnormalidadeConsumoSemLeitura() != null) ?
					leituraAnormalidade.getLeituraAnormalidadeConsumoSemLeitura().getId() : null));
			builder.append(Utilitarios.completaComEspacosADireita(2, (leituraAnormalidade.getLeituraAnormalidadeLeituraComLeitura() != null) ?
					leituraAnormalidade.getLeituraAnormalidadeLeituraComLeitura().getId() : null));
			builder.append(Utilitarios.completaComEspacosADireita(2, (leituraAnormalidade.getLeituraAnormalidadeLeituraSemLeitura() != null) ?
					leituraAnormalidade.getLeituraAnormalidadeLeituraSemLeitura().getId() : null));
			builder.append(Utilitarios.completaComEspacosADireita(1, leituraAnormalidade.getIndicadorUso()));
			builder.append((leituraAnormalidade.getNumeroFatorSemLeitura() != null) ? Utilitarios.formatarBigDecimalComPonto(leituraAnormalidade.getNumeroFatorSemLeitura()) :
				Utilitarios.completaComEspacosADireita(4, leituraAnormalidade.getNumeroFatorSemLeitura()));
			builder.append((leituraAnormalidade.getNumeroFatorComLeitura() != null) ? Utilitarios.formatarBigDecimalComPonto(leituraAnormalidade.getNumeroFatorComLeitura()) :
				Utilitarios.completaComEspacosADireita(4, leituraAnormalidade.getNumeroFatorComLeitura()));
			builder.append(System.getProperty("line.separator"));
		}

		return builder.toString();
	}
}
