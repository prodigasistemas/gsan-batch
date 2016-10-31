package br.gov.batch.servicos.cadastro;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.cadastro.to.AreaConstruidaTO;

@Stateless
public class ImovelBO {

	@EJB
	private ImovelRepositorio imovelRepositorio;

	@EJB
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;
	
	public FaturamentoGrupo pesquisarFaturamentoGrupo(Integer idImovel) {
		FaturamentoGrupo faturamentoGrupo = imovelRepositorio.pesquisarFaturamentoGrupoRotaAlternativa(idImovel);

		if (faturamentoGrupo == null) {
			faturamentoGrupo = imovelRepositorio.pesquisarFaturamentoGrupo(idImovel);
		}

		return faturamentoGrupo;
	}

	public BigDecimal verificarAreaConstruida(Integer idImovel) {
		BigDecimal areaConstruida = BigDecimal.ONE;

		AreaConstruidaTO to = imovelRepositorio.dadosAreaConstruida(idImovel);

		if (to.getAreaConstruida() == null && to.getMenorFaixa() != null) {
			areaConstruida = new BigDecimal(to.getMenorFaixa());
		}

		return areaConstruida;
	}
	
	public List<ICategoria> obterCategorias(Imovel imovel, SistemaParametros sistemaParametros) {

		Collection<ICategoria> dadosSubcategoria = imovelSubcategoriaRepositorio.buscarSubcategoria(imovel.getId());

		List<ICategoria> colecaoSubcategorias = new ArrayList<ICategoria>();

		if (sistemaParametros.indicadorTarifaCategoria()) {
			Integer idCategoriaAnterior = -1;

			for (ICategoria subcatategoria : dadosSubcategoria) {
				if (!idCategoriaAnterior.equals((Integer) subcatategoria.getCategoria().getId())) {
					idCategoriaAnterior = (Integer) subcatategoria.getCategoria().getId();
					colecaoSubcategorias.add(subcatategoria);
				}
			}
		} else {
			colecaoSubcategorias.addAll(dadosSubcategoria);
		}
		return colecaoSubcategorias;
	}
}