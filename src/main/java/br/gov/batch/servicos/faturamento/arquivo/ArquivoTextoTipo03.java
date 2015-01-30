package br.gov.batch.servicos.faturamento.arquivo;

import java.util.Collection;

import javax.ejb.EJB;

import br.gov.model.cadastro.Imovel;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;

public class ArquivoTextoTipo03 extends ArquivoTexto {

	@EJB
	private ConsumoHistoricoRepositorio consumoHistoricoRepositorio;
	
	@EJB
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorio;
	
	public String build(Imovel imovel) {
		Collection<ConsumoHistorico> colecaoConsumoHistorico = consumoHistoricoRepositorio.buscarUltimos6ConsumosAguaImovel(imovel);

		for (ConsumoHistorico consumoHistorico : colecaoConsumoHistorico) {
			builder.append(TIPO_REGISTRO_03);
			builder.append(Utilitarios.completaComZerosEsquerda(9, imovel.getId().toString()));
			builder.append(String.valueOf(consumoHistorico.getLigacaoTipo()));
			builder.append(String.valueOf(consumoHistorico.getReferenciaFaturamento()));
			builder.append(getNumeroConsumoFaturadoMes(consumoHistorico));
			builder.append(getIdLeituraAnormalidadeFaturamento(consumoHistorico));
			builder.append(getConsumoAnormalidade(consumoHistorico));
			builder.append(System.getProperty("line.separator"));
		}

		return builder.toString();
	}

	private String getConsumoAnormalidade(ConsumoHistorico consumoHistorico) {
		if (consumoHistorico.getConsumoAnormalidade() != null) {
			return Utilitarios.completaComZerosEsquerda(2, consumoHistorico.getConsumoAnormalidade().getId().toString());
		} else {
			return Utilitarios.completaComZerosEsquerda(2, "0");
		}
	}

	private String getIdLeituraAnormalidadeFaturamento(ConsumoHistorico consumoHistorico) {
		Integer idLeituraAnormalidadeFaturamento = medicaoHistoricoRepositorio.buscarLeituraAnormalidadeFaturamento(consumoHistorico);
		if (idLeituraAnormalidadeFaturamento != null) {
			return Utilitarios.completaComZerosEsquerda(2, idLeituraAnormalidadeFaturamento.toString());
		} else {
			return Utilitarios.completaComZerosEsquerda(2, "0");
		}
	}

	private String getNumeroConsumoFaturadoMes(ConsumoHistorico consumoHistorico) {
		if (consumoHistorico.getNumeroConsumoFaturadoMes() != null) {
			return Utilitarios.completaComZerosEsquerda(6, consumoHistorico.getNumeroConsumoFaturadoMes().toString());
		} else {
			return Utilitarios.completaComZerosEsquerda(6, "0");
		}
	}
}