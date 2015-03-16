package br.gov.batch.servicos.faturamento.arquivo;

import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.completaTexto;
import static br.gov.model.util.Utilitarios.formataData;
import static br.gov.model.util.Utilitarios.formatarBigDecimalComPonto;
import static br.gov.model.util.Utilitarios.obterQuantidadeLinhasTexto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.exception.CategoriasTarifaInexistente;
import br.gov.model.util.FormatoData;
import br.gov.servicos.faturamento.ConsumoTarifaFaixaRepositorio;
import br.gov.servicos.to.ConsumoTarifaFaixaTO;

@Stateless
public class ArquivoTextoTipo10 extends ArquivoTexto {
	@EJB
	private ConsumoTarifaFaixaRepositorio consumoTarifaFaixaRepositorio;

	public ArquivoTextoTipo10() {
		super();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String build(ArquivoTextoTO to) {
		if (to.getIdsConsumoTarifaCategoria().isEmpty()){
		    throw new CategoriasTarifaInexistente(to.getImovel().getId());
		}
		
        List<ConsumoTarifaFaixaTO> listaConsumoTarifaFaixa = consumoTarifaFaixaRepositorio.dadosConsumoTarifaFaixa(to.getIdsConsumoTarifaCategoria());

		for (ConsumoTarifaFaixaTO faixaTO : listaConsumoTarifaFaixa) {
			builder.append(TIPO_REGISTRO_10_FAIXA_CONSUMO);
			builder.append(completaComZerosEsquerda(2, faixaTO.getIdConsumoTarifa()));
			builder.append(getDataVigencia(faixaTO.getDataVigencia()));
			builder.append(completaComZerosEsquerda(1, faixaTO.getIdCategoria()));
			builder.append(getIdSubcategoria(faixaTO.getIdSubcategoria()));
			builder.append(completaComZerosEsquerda(6, faixaTO.getNumeroConsumoFaixaInicio()));
			builder.append(completaComZerosEsquerda(6, faixaTO.getNumeroConsumoFaixaFim()));
			builder.append(getValorConsumoTarifa(faixaTO.getValorConsumoTarifa()));
			
			if (obterQuantidadeLinhasTexto(builder) < listaConsumoTarifaFaixa.size()) {
				builder.append(System.getProperty("line.separator"));
			}
		}

		return builder.toString();
	}
	
	private String getValorConsumoTarifa(BigDecimal valorConsumoTarifa) {
		if (valorConsumoTarifa != null) {
			return completaComZerosEsquerda(14, formatarBigDecimalComPonto(valorConsumoTarifa));
		} else {
			return completaComZerosEsquerda(14, "");
		}
	}

	private String getIdSubcategoria(Integer idSubcategoria) {
		if (!sistemaParametros.indicadorTarifaCategoria() && idSubcategoria != null) {
			return completaTexto(3, idSubcategoria);
		} else {
			return completaTexto(3, "");
		}
	}

	private String getDataVigencia(Date dataVigencia) {
		if (dataVigencia != null) {
			return formataData(dataVigencia, FormatoData.ANO_MES_DIA);
		} else {
			return completaComZerosEsquerda(8, "");
		}
	}
}
