package br.gov.batch.servicos.faturamento.arquivo;

import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.completaTexto;
import static br.gov.model.util.Utilitarios.quebraLinha;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.micromedicao.ConsumoAnormalidadeAcao;
import br.gov.servicos.micromedicao.ConsumoAnormalidadeAcaoRepositorio;

@Stateless
public class ArquivoTextoTipo12 extends ArquivoTexto {
	@EJB
	private ConsumoAnormalidadeAcaoRepositorio consumoAnormalidadeAcaoRepositorio;

	public ArquivoTextoTipo12() {
		super();
	}

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String build(ArquivoTextoTO to) {
		List<ConsumoAnormalidadeAcao> listaAcoes = consumoAnormalidadeAcaoRepositorio.consumoAnormalidadeAcaoAtivo();

		for (ConsumoAnormalidadeAcao acao : listaAcoes) {
			builder.append(quebraLinha);
			builder.append(TIPO_REGISTRO_12_ACAO_ANORMALIDADE);
			builder.append(completaComZerosEsquerda(2, acao.getConsumoAnormalidade().getId()));
			builder.append(completaComZerosEsquerda(2, acao.getCategoria().getId()));
			builder.append(completaComZerosEsquerda(2, acao.getImovelPerfil().getId()));
			builder.append(completaComZerosEsquerda(2, acao.getLeituraAnormalidadeConsumoMes1().getId()));
			builder.append(completaComZerosEsquerda(2, acao.getLeituraAnormalidadeConsumoMes2().getId()));
			builder.append(completaComZerosEsquerda(2, acao.getLeituraAnormalidadeConsumoMes3().getId()));
			builder.append(completaComZerosEsquerda(4, acao.getNumerofatorConsumoMes1()));
			builder.append(completaComZerosEsquerda(4, acao.getNumerofatorConsumoMes2()));
			builder.append(completaComZerosEsquerda(4, acao.getNumerofatorConsumoMes3()));
			builder.append(completaTexto(120, acao.getDescricaoContaMensagemMes1()));
			builder.append(completaTexto(120, acao.getDescricaoContaMensagemMes2()));
			builder.append(completaTexto(120, acao.getDescricaoContaMensagemMes3()));
		}

		return builder.toString();
	}
}
