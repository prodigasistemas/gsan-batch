package br.gov.batch.servicos.cadastro;

import java.math.BigDecimal;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.cadastro.to.AreaConstruidaTO;

@Stateless
public class ImovelBO {

	@EJB
	private ImovelRepositorio imovelRepositorio;

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
}