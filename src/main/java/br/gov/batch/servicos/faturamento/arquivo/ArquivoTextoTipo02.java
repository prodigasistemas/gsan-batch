package br.gov.batch.servicos.faturamento.arquivo;

import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;

@Stateless
public class ArquivoTextoTipo02 {
	
	@EJB
	private SistemaParametros sistemaParametro;

	@EJB
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;

	private Imovel imovel;
	private StringBuilder builder;
	private final String TIPO_REGISTRO = "02";

	public String build(Imovel imovel){
		this.imovel = imovel;
		builder = new StringBuilder();

		int quantidadeLinhas = 0;

		Collection<ICategoria> colecaoCategorias = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasPorImovel(imovel.getId());

		if (colecaoCategorias != null && !colecaoCategorias.isEmpty()) {
			for (ICategoria categoria : colecaoCategorias) {
				quantidadeLinhas = quantidadeLinhas + 1;
				
				buildTipo02(categoria);
			}
		}
		
		return builder.toString();
	}
	
	private void buildTipo02(ICategoria iCategoria) {
		builder.append(TIPO_REGISTRO);
		builder.append(Utilitarios.completaComZerosEsquerda(9, imovel.getId().toString()));
		builder.append(getCodigoCategoriaOuSubcategoria(iCategoria));
		builder.append(getDescricaoCategoriaOuSubcategoria(iCategoria));
		builder.append(getCodigoSubcategoria(iCategoria));
		builder.append(getDescricaoSubcategoria(iCategoria));
		builder.append(Utilitarios.completaComZerosEsquerda(4, "" + iCategoria.getQuantidadeEconomias()));
		builder.append(Utilitarios.completaTexto(3, iCategoria.getCategoriaDescricaoAbreviada()));
		builder.append(getSubcategoriaDescricaoAbreviada(iCategoria));
		builder.append(getFatorEconomias(iCategoria));
		builder.append(System.getProperty("line.separator"));
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
			return Utilitarios.completaTexto(50, iCategoria.getDescricao());
		}
	}
	
	private String getSubcategoriaDescricaoAbreviada(ICategoria iCategoria) {
		if (sistemaParametro.indicadorTarifaCategoria()) {
			return Utilitarios.completaTexto(50, iCategoria.getDescricaoAbreviada());
		} else {
			return Utilitarios.completaTexto(3, iCategoria.getCategoria().getDescricaoAbreviada());
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
		if (iCategoria.getCategoriaFatorEconomias() != null) {
			return Utilitarios.completaTexto(2, iCategoria.getCategoriaFatorEconomias().toString());
		} else {
			return Utilitarios.completaTexto(2, "");
		}
	}
}