package br.gov.batch.servicos.faturamento.arquivo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.Status;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.faturamento.ConsumoTarifaFaixaRepositorio;
import br.gov.servicos.to.ConsumoTarifaFaixaTO;

@Stateless
public class ArquivoTextoTipo10 {

	private final String TIPO_REGISTRO = "10";

	@EJB
	private ConsumoTarifaFaixaRepositorio consumoTarifaFaixaRepositorio;

	private StringBuilder builder;
	
	public ArquivoTextoTipo10() {
		builder = new StringBuilder();
	}

	public String build(List<Integer> idsConsumoTarifaCategoria, Short indicadorTarifaCategoria) {

		List<ConsumoTarifaFaixaTO> listaConsumoTarifaFaixa = consumoTarifaFaixaRepositorio.dadosConsumoTarifaFaixa(idsConsumoTarifaCategoria);

		for (ConsumoTarifaFaixaTO to : listaConsumoTarifaFaixa) {
			builder.append(TIPO_REGISTRO);
			builder.append(getIdConsumoTarifa(to.getIdConsumoTarifa()));
			builder.append(getDataVigencia(to.getDataVigencia()));
			builder.append(getIdCategoria(to.getIdCategoria()));
			builder.append(getIdSubcategoria(to.getIdSubcategoria(), indicadorTarifaCategoria));
			builder.append(getNumeroConsumoFaixaInicio(to.getNumeroConsumoFaixaInicio()));
			builder.append(getNumeroConsumoFaixaFim(to.getNumeroConsumoFaixaFim()));
			builder.append(getValorConsumoTarifa(to.getValorConsumoTarifa()));
			
			if (getQuantidadeLinhas() < listaConsumoTarifaFaixa.size()) {
				builder.append(System.getProperty("line.separator"));
			}
		}

		return builder.toString();
	}
	
	public int getQuantidadeLinhas() {
		String[] linhas = builder.toString().split(System.getProperty("line.separator"));
		return linhas.length;
	}
	
	private String getValorConsumoTarifa(BigDecimal valorConsumoTarifa) {
		if (valorConsumoTarifa != null) {
			return Utilitarios.completaComZerosEsquerda(14, Utilitarios.formatarBigDecimalComPonto(valorConsumoTarifa));
		} else {
			return Utilitarios.completaComZerosEsquerda(14, "");
		}
	}

	private String getNumeroConsumoFaixaFim(Integer numeroConsumoFaixaFim) {
		if (numeroConsumoFaixaFim != null) {
			return Utilitarios.completaComZerosEsquerda(6, numeroConsumoFaixaFim);
		} else {
			return Utilitarios.completaComZerosEsquerda(6, "");
		}
	}

	private String getNumeroConsumoFaixaInicio(Integer numeroConsumoFaixaInicio) {
		if (numeroConsumoFaixaInicio != null) {
			return Utilitarios.completaComZerosEsquerda(6, numeroConsumoFaixaInicio);
		} else {
			return Utilitarios.completaComZerosEsquerda(6, "");
		}
	}

	private String getIdSubcategoria(Integer idSubcategoria, Short indicadorTarifaCategoria) {
		if (indicadorTarifaCategoria.equals(Status.INATIVO.getId()) && idSubcategoria != null) {
			return Utilitarios.completaComZerosEsquerda(3, idSubcategoria);
		} else {
			return Utilitarios.completaTexto(3, "");
		}
	}

	private String getIdCategoria(Integer idCategoria) {
		if (idCategoria != null) {
			return Utilitarios.completaComZerosEsquerda(1, idCategoria);
		} else {
			return Utilitarios.completaComZerosEsquerda(1, "");
		}
	}

	private String getDataVigencia(Date dataVigencia) {
		if (dataVigencia != null) {
			return Utilitarios.formataData(dataVigencia, FormatoData.ANO_MES_DIA);
		} else {
			return Utilitarios.completaComZerosEsquerda(8, "");
		}
	}

	private String getIdConsumoTarifa(Integer idConsumoTarifa) {
		if (idConsumoTarifa != null) {
			return Utilitarios.completaComZerosEsquerda(2, idConsumoTarifa);
		} else {
			return Utilitarios.completaComZerosEsquerda(2, "");
		}
	}
}
