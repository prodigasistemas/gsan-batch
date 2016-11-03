package br.gov.batch.servicos.faturamento.arquivo;

import java.math.BigDecimal;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.batch.servicos.faturamento.tarifa.ConsumoTarifaBO;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.faturamento.ConsumoTarifaCategoria;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;

@Stateless
public class ArquivoTextoTipo09 extends ArquivoTexto {
	
	@EJB
	private ConsumoTarifaBO consumoTarifaBO;
	
	public ArquivoTextoTipo09() {
		super();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String build(ArquivoTextoTO to) {
		List<ConsumoTarifaCategoria> colecaoDadosTarifaCategoria = consumoTarifaBO.obterConsumoTarifasCategoria(to.getImovel(), sistemaParametros);
		
		for (ConsumoTarifaCategoria dadosTarifaCategoria : colecaoDadosTarifaCategoria) {
		    if (dadosTarifaCategoria.getConsumoTarifaVigencia() == null || dadosTarifaCategoria.getConsumoTarifaVigencia().getConsumoTarifa() == null){
		        continue;
		    }
		    
			builder.append(TIPO_REGISTRO_09_TARIFA);
			builder.append(Utilitarios.completaComZerosEsquerda(2, dadosTarifaCategoria.getConsumoTarifaVigencia().getConsumoTarifa().getId()));
			builder.append(Utilitarios.formataData(dadosTarifaCategoria.getConsumoTarifaVigencia().getDataVigencia(), FormatoData.ANO_MES_DIA));
			builder.append(Utilitarios.completaComZerosEsquerda(1, dadosTarifaCategoria.getCategoria().getId()));
			buildDadosTarifaSubcategoria(dadosTarifaCategoria);
			builder.append(Utilitarios.completaComZerosEsquerda(6, dadosTarifaCategoria.getNumeroConsumoMinimo()));
			builder.append(Utilitarios.completaComZerosEsquerda(14, Utilitarios.formatarBigDecimalComPonto((BigDecimal) dadosTarifaCategoria.getValorTarifaMinima())));
			builder.append(System.getProperty("line.separator"));
			
			to.addIdsConsumoTarifaCategoria(dadosTarifaCategoria.getId());
		}
		return builder.toString();
	}

	private void buildDadosTarifaSubcategoria(ConsumoTarifaCategoria dadosTarifa) {
		if (sistemaParametros.indicadorTarifaCategoria()) {
			builder.append(Utilitarios.completaTexto(3, ""));
		} else {
			builder.append(Utilitarios.completaTexto(3, dadosTarifa.getSubcategoria().getId()));
		}
	}
	
}