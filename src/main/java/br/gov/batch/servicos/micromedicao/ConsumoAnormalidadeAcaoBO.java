package br.gov.batch.servicos.micromedicao;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.micromedicao.ConsumoAnormalidadeAcao;
import br.gov.servicos.micromedicao.ConsumoAnormalidadeAcaoRepositorio;

@Stateless
public class ConsumoAnormalidadeAcaoBO {
	
	@EJB
	private ConsumoAnormalidadeAcaoRepositorio consumoAnormalidadeAcaoRepositorio;
	
	public ConsumoAnormalidadeAcao acaoASerTomada(Integer idConsumoAnormalidade, Integer idCategoria, Integer idPerfilImovel) {
		ConsumoAnormalidadeAcao anormalidadeAcao = consumoAnormalidadeAcaoRepositorio.consumoAnormalidadeAcao(idConsumoAnormalidade, idCategoria, idPerfilImovel);
		
		if (anormalidadeAcao == null){
			anormalidadeAcao = consumoAnormalidadeAcaoRepositorio.consumoAnormalidadeAcao(idConsumoAnormalidade, idCategoria, null);
		}
		
		if (anormalidadeAcao == null){
			anormalidadeAcao = consumoAnormalidadeAcaoRepositorio.consumoAnormalidadeAcao(idConsumoAnormalidade, null, null);
		}
		
		return anormalidadeAcao;
	}
}
