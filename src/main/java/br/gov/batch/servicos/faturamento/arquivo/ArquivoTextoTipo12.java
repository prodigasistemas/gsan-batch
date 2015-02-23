package br.gov.batch.servicos.faturamento.arquivo;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.micromedicao.ConsumoAnormalidadeAcao;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.micromedicao.ConsumoAnormalidadeAcaoRepositorio;

@Stateless
public class ArquivoTextoTipo12 extends ArquivoTexto {

	@EJB
	private ConsumoAnormalidadeAcaoRepositorio consumoAnormalidadeAcaoRepositorio;

	public ArquivoTextoTipo12() {
		super();
	}

	public String build(ArquivoTextoTO to) {
		List<ConsumoAnormalidadeAcao> listaAcoes = consumoAnormalidadeAcaoRepositorio.consumoAnormalidadeAcaoAtivo();

		for (ConsumoAnormalidadeAcao acao : listaAcoes) {
			builder.append(System.getProperty("line.separator"));
			builder.append(TIPO_REGISTRO_12_ACAO_ANORMALIDADE);
			builder.append(Utilitarios.completaComZerosEsquerda(2, acao.getConsumoAnormalidade().getId()));
			builder.append(Utilitarios.completaComZerosEsquerda(2, acao.getCategoria().getId()));
			builder.append(Utilitarios.completaComZerosEsquerda(2, acao.getImovelPerfil().getId()));
			builder.append(Utilitarios.completaComZerosEsquerda(2, acao.getLeituraAnormalidadeConsumoMes1().getId()));
			builder.append(Utilitarios.completaComZerosEsquerda(2, acao.getLeituraAnormalidadeConsumoMes2().getId()));
			builder.append(Utilitarios.completaComZerosEsquerda(2, acao.getLeituraAnormalidadeConsumoMes3().getId()));
			builder.append(Utilitarios.completaComZerosEsquerda(4, acao.getNumerofatorConsumoMes1()));
			builder.append(Utilitarios.completaComZerosEsquerda(4, acao.getNumerofatorConsumoMes2()));
			builder.append(Utilitarios.completaComZerosEsquerda(4, acao.getNumerofatorConsumoMes3()));
			builder.append(Utilitarios.completaTexto(120, acao.getDescricaoContaMensagemMes1()));
			builder.append(Utilitarios.completaTexto(120, acao.getDescricaoContaMensagemMes2()));
			builder.append(Utilitarios.completaTexto(120, acao.getDescricaoContaMensagemMes3()));

			if (Utilitarios.obterQuantidadeLinhasTexto(builder) < listaAcoes.size()) {
				builder.append(System.getProperty("line.separator"));
			}
		}

		return builder.toString();
	}
}
