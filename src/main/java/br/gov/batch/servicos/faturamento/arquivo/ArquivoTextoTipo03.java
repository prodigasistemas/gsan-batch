package br.gov.batch.servicos.faturamento.arquivo;

import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.quebraLinha;

import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;

@Stateless
public class ArquivoTextoTipo03 extends ArquivoTexto {
	@EJB
	private ConsumoHistoricoRepositorio consumoHistoricoRepositorio;

	@EJB
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorio;
	
	public ArquivoTextoTipo03() {
		super();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String build(ArquivoTextoTO to) {
		Collection<ConsumoHistorico> colecaoConsumoHistorico = consumoHistoricoRepositorio.buscarUltimos6ConsumosAguaImovel(to.getImovel().getId());

		for (ConsumoHistorico consumoHistorico : colecaoConsumoHistorico) {
			builder.append(TIPO_REGISTRO_03_CONSUMO_HISTORICO);
			builder.append(completaComZerosEsquerda(9, to.getImovel().getId()));
			builder.append(String.valueOf(consumoHistorico.getLigacaoTipo()));
			builder.append(String.valueOf(consumoHistorico.getReferenciaFaturamento()));
			builder.append(completaComZerosEsquerda(6, consumoHistorico.getNumeroConsumoFaturadoMes()));
			builder.append(getIdLeituraAnormalidadeFaturamento(consumoHistorico));
			builder.append(getConsumoAnormalidade(consumoHistorico));
			builder.append(quebraLinha);
		}

		return builder.toString();
	}

	private String getConsumoAnormalidade(ConsumoHistorico consumoHistorico) {
		if (consumoHistorico.getConsumoAnormalidade() != null) {
			return completaComZerosEsquerda(2, consumoHistorico.getConsumoAnormalidade().getId());
		} else {
			return completaComZerosEsquerda(2, "0");
		}
	}

	private String getIdLeituraAnormalidadeFaturamento(ConsumoHistorico consumoHistorico) {
		Integer idLeituraAnormalidadeFaturamento = medicaoHistoricoRepositorio.buscarLeituraAnormalidadeFaturamento(consumoHistorico);
		if (idLeituraAnormalidadeFaturamento != null) {
			return completaComZerosEsquerda(2, idLeituraAnormalidadeFaturamento);
		} else {
			return completaComZerosEsquerda(2, "0");
		}
	}
}