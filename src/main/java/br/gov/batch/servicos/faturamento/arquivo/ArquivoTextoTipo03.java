package br.gov.batch.servicos.faturamento.arquivo;

import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.logging.Logger;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;

@Stateless
public class ArquivoTextoTipo03 extends ArquivoTexto {
    private static Logger logger = Logger.getLogger(ArquivoTextoTipo03.class);

	@EJB
	private ConsumoHistoricoRepositorio consumoHistoricoRepositorio;

	@EJB
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorio;
	
	public ArquivoTextoTipo03() {
		super();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String build(ArquivoTextoTO to) {
//        logger.info("Construcao da linha 03");
	    
		Collection<ConsumoHistorico> colecaoConsumoHistorico = consumoHistoricoRepositorio.buscarUltimos6ConsumosAguaImovel(to.getIdImovel());

		for (ConsumoHistorico consumoHistorico : colecaoConsumoHistorico) {
			builder.append(TIPO_REGISTRO_03_CONSUMO_HISTORICO);
			builder.append(Utilitarios.completaComZerosEsquerda(9, to.getImovel().getId()));
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
			return Utilitarios.completaComZerosEsquerda(2, consumoHistorico.getConsumoAnormalidade().getId());
		} else {
			return Utilitarios.completaComZerosEsquerda(2, "0");
		}
	}

	private String getIdLeituraAnormalidadeFaturamento(ConsumoHistorico consumoHistorico) {
		Integer idLeituraAnormalidadeFaturamento = medicaoHistoricoRepositorio.buscarLeituraAnormalidadeFaturamento(consumoHistorico);
		if (idLeituraAnormalidadeFaturamento != null) {
			return Utilitarios.completaComZerosEsquerda(2, idLeituraAnormalidadeFaturamento);
		} else {
			return Utilitarios.completaComZerosEsquerda(2, "0");
		}
	}

	private String getNumeroConsumoFaturadoMes(ConsumoHistorico consumoHistorico) {
		if (consumoHistorico.getNumeroConsumoFaturadoMes() != null) {
			return Utilitarios.completaComZerosEsquerda(6, consumoHistorico.getNumeroConsumoFaturadoMes());
		} else {
			return Utilitarios.completaComZerosEsquerda(6, "0");
		}
	}
}