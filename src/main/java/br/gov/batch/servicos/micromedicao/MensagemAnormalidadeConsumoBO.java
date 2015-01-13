package br.gov.batch.servicos.micromedicao;

import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.ICategoria;
import br.gov.model.micromedicao.ConsumoAnormalidade;
import br.gov.model.micromedicao.ConsumoAnormalidadeAcao;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;
import br.gov.servicos.to.AnormalidadeHistoricoConsumo;

@Stateless
public class MensagemAnormalidadeConsumoBO {
	
	@EJB
	private ConsumoHistoricoRepositorio consumoHistoricoRepositorio;
	
	@EJB
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;
	
	@EJB
	private ConsumoAnormalidadeAcaoBO consumoAnormalidadeAcaoBO;
	
	public String[] mensagensAnormalidadeConsumo(Integer idImovel, Integer anoMesReferencia, Integer idPerfilImovel){

		String[] mensagemConta = null;

		AnormalidadeHistoricoConsumo anormalidade = consumoHistoricoRepositorio.anormalidadeHistoricoConsumo(idImovel, LigacaoTipo.AGUA, anoMesReferencia);
		
		if (anormalidade == null){
			anormalidade = consumoHistoricoRepositorio.anormalidadeHistoricoConsumo(idImovel, LigacaoTipo.ESGOTO, anoMesReferencia);
		}
		
		if (anormalidade != null){
			if (anormalidade.getIdAnormalidade() == ConsumoAnormalidade.BAIXO_CONSUMO || 
				anormalidade.getIdAnormalidade() == ConsumoAnormalidade.ALTO_CONSUMO ||
				anormalidade.getIdAnormalidade() == ConsumoAnormalidade.ESTOURO_CONSUMO){
				
				Collection<ICategoria> colecaoCategoria = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasCategoria(idImovel);

				Integer categoriaComMaisEconomias = null;
				int maiorQuantidadeEconomia = 0;
				
				for (ICategoria categoria : colecaoCategoria) {
					int qtdEconomias = categoria.getQuantidadeEconomias().intValue();

					if (maiorQuantidadeEconomia < qtdEconomias) {
						categoriaComMaisEconomias = categoria.getId().intValue();
					}
				}
				
				ConsumoAnormalidadeAcao consumoAnormalidadeAcao = consumoAnormalidadeAcaoBO.acaoASerTomada(anormalidade.getIdAnormalidade(), categoriaComMaisEconomias, idPerfilImovel);
				
				if (consumoAnormalidadeAcao != null) {
					String mensagemContaAnormalidade = "";

					int anoMesReferenciaAnterior = Utilitarios.reduzirMeses(anoMesReferencia, 1);

					AnormalidadeHistoricoConsumo anormalidadeAnterior = consumoHistoricoRepositorio.anormalidadeHistoricoConsumo(idImovel, LigacaoTipo.AGUA, anoMesReferenciaAnterior, anormalidade.getIdAnormalidade());

					if (anormalidadeAnterior == null) {
						mensagemContaAnormalidade = consumoAnormalidadeAcao.getDescricaoContaMensagemMes1();
					} else {
						anoMesReferenciaAnterior = Utilitarios.reduzirMeses(anoMesReferencia, 1);
						anormalidadeAnterior = consumoHistoricoRepositorio.anormalidadeHistoricoConsumo(idImovel, LigacaoTipo.AGUA, anoMesReferenciaAnterior, anormalidade.getIdAnormalidade());

						if (anormalidadeAnterior == null) {
							mensagemContaAnormalidade = consumoAnormalidadeAcao.getDescricaoContaMensagemMes2();

						} else {
							mensagemContaAnormalidade = consumoAnormalidadeAcao.getDescricaoContaMensagemMes3();
						}
					}

					if (mensagemContaAnormalidade != null && !mensagemContaAnormalidade.equals("")) {
						mensagemConta = new String[3];
						int tamanho = mensagemContaAnormalidade.length();
						if (tamanho < 60) {
							mensagemConta[0] = mensagemContaAnormalidade.substring(0, tamanho);
							mensagemConta[1] = "";
							mensagemConta[2] = "";
						} else {
							mensagemConta[0] = mensagemContaAnormalidade.substring(0, 60);
							mensagemConta[1] = mensagemContaAnormalidade.substring(60, tamanho);
							mensagemConta[2] = "";
						}
					}
				}
				
			}
		}

		return mensagemConta;
	}
}
