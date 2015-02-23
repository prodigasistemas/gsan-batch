package br.gov.batch.servicos.faturamento.arquivo;

import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;

@Stateless
public class ArquivoTextoTipo02 extends ArquivoTexto {

	@EJB
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;

	public ArquivoTextoTipo02() {
		super();
	}

	public String build(ArquivoTextoTO to) {
		Collection<ICategoria> colecaoCategorias = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasPorImovel(to.getImovel().getId());

		for (ICategoria categoria : colecaoCategorias) {
			builder.append(TIPO_REGISTRO_02_CATEGORIAS);
			builder.append(Utilitarios.completaComZerosEsquerda(9, to.getImovel().getId()));
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

	private String getCodigoSubcategoria(ICategoria categoria) {
		if (sistemaParametros.indicadorTarifaCategoria()) {
			return Utilitarios.completaComZerosEsquerda(3, "0");
		} else {
			return Utilitarios.completaComZerosEsquerda(3, categoria.getId().toString());
		}
	}

	private String getDescricaoSubcategoria(ICategoria categoria) {
		if (sistemaParametros.indicadorTarifaCategoria()) {
			return Utilitarios.completaTexto(50, "");
		} else {
			return Utilitarios.completaTexto(50, categoria.getSubcategoriaDescricao());
		}
	}

	private String getSubcategoriaDescricaoAbreviada(ICategoria categoria) {
		if (sistemaParametros.indicadorTarifaCategoria()) {
			return Utilitarios.completaTexto(3, categoria.getSubcategoriaDescricaoAbreviada());
		} else {
			return Utilitarios.completaTexto(3, categoria.getCategoria().getCategoriaDescricaoAbreviada());
		}
	}

	private String getCodigoCategoriaOuSubcategoria(ICategoria categoria) {
		if (sistemaParametros.indicadorTarifaCategoria()) {
			return categoria.getId().toString();
		} else {
			return categoria.getCategoria().getId().toString();
		}
	}

	private String getDescricaoCategoriaOuSubcategoria(ICategoria categoria) {
		if (sistemaParametros.indicadorTarifaCategoria()) {
			return Utilitarios.completaTexto(15, categoria.getCategoriaDescricao());
		} else {
			return Utilitarios.completaTexto(15, categoria.getSubcategoriaDescricao());
		}
	}

	private String getFatorEconomias(ICategoria categoria) {
		if (categoria.getFatorEconomias() != null) {
			return Utilitarios.completaTexto(2, categoria.getFatorEconomias().toString());
		} else {
			return Utilitarios.completaTexto(2, "");
		}
	}
}