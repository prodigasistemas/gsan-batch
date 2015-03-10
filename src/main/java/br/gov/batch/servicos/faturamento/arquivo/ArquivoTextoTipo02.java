package br.gov.batch.servicos.faturamento.arquivo;

import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.completaTexto;
import static br.gov.model.util.Utilitarios.completaComEspacosADireita;
import static br.gov.model.util.Utilitarios.quebraLinha;

import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.ICategoria;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;

@Stateless
public class ArquivoTextoTipo02 extends ArquivoTexto {
	@EJB
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;

	public ArquivoTextoTipo02() {
		super();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String build(ArquivoTextoTO to) {
		Collection<ICategoria> colecaoCategorias = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasPorImovel(to.getImovel().getId());

		for (ICategoria categoria : colecaoCategorias) {
			builder.append(TIPO_REGISTRO_02_CATEGORIAS);
			builder.append(completaComZerosEsquerda(9, to.getImovel().getId()));
			builder.append(getCodigoCategoriaOuSubcategoria(categoria));
			builder.append(getDescricaoCategoriaOuSubcategoria(categoria));
			builder.append(getCodigoSubcategoria(categoria));
			builder.append(getDescricaoSubcategoria(categoria));
			builder.append(completaComZerosEsquerda(4, categoria.getQuantidadeEconomias()));
			builder.append(completaTexto(3, categoria.getCategoriaDescricaoAbreviada()));
			builder.append(getSubcategoriaDescricaoAbreviada(categoria));
			builder.append(getFatorEconomias(categoria));
			builder.append(quebraLinha);
		}

		return builder.toString();
	}

	private String getCodigoSubcategoria(ICategoria categoria) {
		if (sistemaParametros.indicadorTarifaCategoria()) {
			return completaComZerosEsquerda(3, "0");
		} else {
			return completaComZerosEsquerda(3, categoria.getId().toString());
		}
	}

	private String getDescricaoSubcategoria(ICategoria categoria) {
		if (sistemaParametros.indicadorTarifaCategoria()) {
			return completaTexto(50, "");
		} else {
			return completaTexto(50, categoria.getSubcategoriaDescricao());
		}
	}

	private String getSubcategoriaDescricaoAbreviada(ICategoria categoria) {
		if (sistemaParametros.indicadorTarifaCategoria()) {
			return completaTexto(20, categoria.getSubcategoriaDescricaoAbreviada());
		} else {
			return completaTexto(20, categoria.getCategoria().getCategoriaDescricaoAbreviada());
		}
	}

	private String getCodigoCategoriaOuSubcategoria(ICategoria categoria) {
		if (sistemaParametros.indicadorTarifaCategoria()) {
			return completaTexto(1, categoria.getId().toString());
		} else {
			return completaTexto(1, categoria.getCategoria().getId().toString());
		}
	}

	private String getDescricaoCategoriaOuSubcategoria(ICategoria categoria) {
		if (sistemaParametros.indicadorTarifaCategoria()) {
			return completaComEspacosADireita(15, categoria.getCategoriaDescricao());
		} else {
			return completaComEspacosADireita(15, categoria.getSubcategoriaDescricao());
		}
	}

	private String getFatorEconomias(ICategoria categoria) {
		if (categoria.getFatorEconomias() != null) {
			return completaTexto(2, categoria.getFatorEconomias().toString());
		} else {
			return completaTexto(2, "");
		}
	}
}