package br.gov.batch.servicos.faturamento.arquivo;

import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;

@Stateless
public class ArquivoTextoTipo02 extends ArquivoTexto {
	
	private final String TIPO_REGISTRO = "02";
	
	@Inject
	private SistemaParametros sistemaParametro;

	@EJB
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;

	public String build(Imovel imovel){
		Collection<ICategoria> colecaoCategorias = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasPorImovel(imovel.getId());
		
		for (ICategoria categoria : colecaoCategorias) {
			builder.append(TIPO_REGISTRO);
			builder.append(Utilitarios.completaComZerosEsquerda(9, imovel.getId()));
			builder.append(getCodigoCategoriaOuSubcategoria(categoria));
			builder.append(getDescricaoCategoriaOuSubcategoria(categoria));
			builder.append(getCodigoSubcategoria(categoria));
			builder.append(getDescricaoSubcategoria(categoria));
			builder.append(Utilitarios.completaComZerosEsquerda(4, categoria.getQuantidadeEconomias()));
			builder.append(Utilitarios.completaTexto(3, categoria.getCategoriaDescricaoAbreviada()));
			builder.append(getSubcategoriaDescricaoAbreviada(categoria));
			builder.append(getFatorEconomias(categoria));
			builder.append(System.getProperty("line.separator"));
		}
		
		return builder.toString();
	}

	private String getCodigoSubcategoria(ICategoria iCategoria) {
		if (sistemaParametro.indicadorTarifaCategoria()) {
			return Utilitarios.completaComZerosEsquerda(3, "0");
		} else {
			return Utilitarios.completaComZerosEsquerda(3, iCategoria.getId().toString());
		}
	}
	
	private String getDescricaoSubcategoria(ICategoria iCategoria) {
		if (sistemaParametro.indicadorTarifaCategoria()) {
			return Utilitarios.completaTexto(50, "");
		} else {
			return Utilitarios.completaTexto(50, iCategoria.getSubcategoriaDescricao());
		}
	}
	
	private String getSubcategoriaDescricaoAbreviada(ICategoria iCategoria) {
		if (sistemaParametro.indicadorTarifaCategoria()) {
			return Utilitarios.completaTexto(3, iCategoria.getSubcategoriaDescricaoAbreviada());
		} else {
			return Utilitarios.completaTexto(3, iCategoria.getCategoria().getCategoriaDescricaoAbreviada());
		}
	}

	private String getCodigoCategoriaOuSubcategoria(ICategoria iCategoria) {
		if (sistemaParametro.indicadorTarifaCategoria()) {
			return iCategoria.getId().toString();
		} else {
			return iCategoria.getCategoria().getId().toString();
		}
	}
	
	private String getDescricaoCategoriaOuSubcategoria(ICategoria iCategoria) {
		if (sistemaParametro.indicadorTarifaCategoria()) {
			return Utilitarios.completaTexto(15, iCategoria.getCategoriaDescricao());
		} else {
			return Utilitarios.completaTexto(15, iCategoria.getSubcategoriaDescricao());
		}
	}

	private String getFatorEconomias(ICategoria iCategoria) {
		if (iCategoria.getFatorEconomias() != null) {
			return Utilitarios.completaTexto(2, iCategoria.getFatorEconomias().toString());
		} else {
			return Utilitarios.completaTexto(2, "");
		}
	}
}