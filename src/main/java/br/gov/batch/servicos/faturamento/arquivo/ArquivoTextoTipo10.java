package br.gov.batch.servicos.faturamento.arquivo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.logging.Logger;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.exception.CategoriasTarifaInexistente;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.faturamento.ConsumoTarifaFaixaRepositorio;
import br.gov.servicos.to.ConsumoTarifaFaixaTO;

@Stateless
public class ArquivoTextoTipo10 extends ArquivoTexto {
    private static Logger logger = Logger.getLogger(ArquivoTextoTipo10.class);

	@EJB
	private ConsumoTarifaFaixaRepositorio consumoTarifaFaixaRepositorio;

	public ArquivoTextoTipo10() {
		super();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String build(ArquivoTextoTO to) {
//        logger.info("Construcao da linha 10");
	    
		if (to.getIdsConsumoTarifaCategoria().isEmpty()){
		    throw new CategoriasTarifaInexistente(to.getImovel().getId());
		}
		
        List<ConsumoTarifaFaixaTO> listaConsumoTarifaFaixa = consumoTarifaFaixaRepositorio.dadosConsumoTarifaFaixa(to.getIdsConsumoTarifaCategoria());

		for (ConsumoTarifaFaixaTO faixaTO : listaConsumoTarifaFaixa) {
			builder.append(TIPO_REGISTRO_10_FAIXA_CONSUMO);
			builder.append(getIdConsumoTarifa(faixaTO.getIdConsumoTarifa()));
			builder.append(getDataVigencia(faixaTO.getDataVigencia()));
			builder.append(getIdCategoria(faixaTO.getIdCategoria()));
			builder.append(getIdSubcategoria(faixaTO.getIdSubcategoria()));
			builder.append(getNumeroConsumoFaixaInicio(faixaTO.getNumeroConsumoFaixaInicio()));
			builder.append(getNumeroConsumoFaixaFim(faixaTO.getNumeroConsumoFaixaFim()));
			builder.append(getValorConsumoTarifa(faixaTO.getValorConsumoTarifa()));
			
			if (Utilitarios.obterQuantidadeLinhasTexto(builder) < listaConsumoTarifaFaixa.size()) {
				builder.append(System.getProperty("line.separator"));
			}
		}

		return builder.toString();
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

	private String getIdSubcategoria(Integer idSubcategoria) {
		if (!sistemaParametros.indicadorTarifaCategoria() && idSubcategoria != null) {
			return Utilitarios.completaTexto(3, idSubcategoria);
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
