package br.gov.batch.servicos.cadastro;

import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.ICategoria;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;

@Stateless
public class EconomiasBO {

	@EJB
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;

	public Integer getQuantidadeTotalEconomias(Integer idImovel) {
		Collection<ICategoria> subcategorias = imovelSubcategoriaRepositorio.buscarSubcategoria(idImovel);

		Integer quantidadeEconomias = 0;
		for (ICategoria subcategoria : subcategorias) {
			if (subcategoria.getCategoria().getFatorEconomias() != null) {
				quantidadeEconomias += subcategoria.getCategoria().getFatorEconomias();
			} else {
				quantidadeEconomias += subcategoria.getQuantidadeEconomias();
			}
		}
		
		return quantidadeEconomias;
	}
}