package br.gov.batch.servicos.micromedicao;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaCategoriaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaVigenciaRepositorio;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;
import br.gov.servicos.to.ConsumoTarifaVigenciaTO;

@Stateless
public class ConsumoHistoricoBO {
	
	@Inject
	private SistemaParametros sistemaParametro;
	
	@EJB
	private ConsumoHistoricoRepositorio consumoHistoricoRepositorio;
	
	@EJB
	private ConsumoTarifaRepositorio consumoTarifaRepositorio;
	
	@EJB
	private ConsumoTarifaVigenciaRepositorio consumoTarifaVigenciaRepositorio;
	
	@EJB
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;
	
	@EJB
	private ConsumoTarifaCategoriaRepositorio consumoTarifaCategoriaRepositorio;
	
	public Integer obterVolumeMedioAguaEsgoto(Integer idImovel, Integer anoMesReferencia, LigacaoTipo ligacaoTipo) {
		Integer amReferenciaFinal   = Utilitarios.reduzirMeses(anoMesReferencia, 1);
		Integer amReferenciaInicial = Utilitarios.reduzirMeses(amReferenciaFinal, sistemaParametro.getMesesMediaConsumo());

		int numeroMesesMaximoCalculoMedia = sistemaParametro.getNumeroMesesMaximoCalculoMedia();
		int amReferenciaInicialMaximo = Utilitarios.reduzirMeses(amReferenciaInicial, numeroMesesMaximoCalculoMedia);
		
		List<ConsumoHistorico> medias = consumoHistoricoRepositorio.obterConsumoMedio(idImovel, amReferenciaInicialMaximo, amReferenciaFinal, ligacaoTipo.getId());
		Integer mediaConsumo = 0;
		if (medias.isEmpty()){
			mediaConsumo = consumoMinimoLigacao(idImovel);
		}else{
			mediaConsumo = calcularMediaConsumo(medias, anoMesReferencia);
		}

		return mediaConsumo;
	}
	
	private Integer consumoMinimoLigacao(Integer idImovel) {
		Integer idTarifa = consumoTarifaRepositorio.consumoTarifaDoImovel(idImovel);
		
		Collection<ICategoria> economias = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasPorImovel(idImovel);
		
		Integer consumoMinimoLigacao = this.obterConsumoMinimoLigacaoPorCategoria(idImovel, idTarifa, economias);

		return consumoMinimoLigacao;
	}

	public Integer calcularMediaConsumo(List<ConsumoHistorico> consumos, Integer anoMesReferencia){
		Collections.sort(consumos);
		
		int mesesParaMedia = sistemaParametro.getMesesMediaConsumo();
		int maximoMesesCalculoMedia = sistemaParametro.getNumeroMesesMaximoCalculoMedia();
		
		int mesesCortados = 0;
		int somaConsumo = 0;
		int mesesConsumo = 0; 
		for (int item =  0; item < consumos.size(); ) {
			ConsumoHistorico consumo = consumos.get(item);
			
			if (mesesConsumo == mesesParaMedia || mesesCortados == maximoMesesCalculoMedia){
				break;
			}
			
			if (consumo.getReferenciaFaturamento().intValue() == anoMesReferencia.intValue()){
				somaConsumo += consumo.getNumeroConsumoCalculoMedia();
				mesesConsumo++;
				item++;
			}else{
				mesesCortados++;
			}
			anoMesReferencia = Utilitarios.reduzirMeses(anoMesReferencia, 1);
		}
		
		return mesesConsumo > 0 ? somaConsumo / mesesConsumo : 0;
	}

	public int obterConsumoMinimoLigacaoPorCategoria(Integer idImovel, Integer idTarifa, Collection<ICategoria> categorias){
		
		int consumoMinimoLigacao = 0;
		
		ConsumoTarifaVigenciaTO consumoTarifaVigencia = consumoTarifaVigenciaRepositorio.maiorDataVigenciaConsumoTarifa(idTarifa);
		
		for (ICategoria economia : categorias) {
			Integer consumoMinimoTarifa = consumoTarifaCategoriaRepositorio.consumoMinimoTarifa(economia, consumoTarifaVigencia.getIdVigencia());
			
			if (economia.getFatorEconomias() != null) {
				consumoMinimoLigacao += consumoMinimoTarifa * economia.getFatorEconomias().intValue();
			} else {
				consumoMinimoLigacao += consumoMinimoTarifa * economia.getQuantidadeEconomias();
			}

		}
		
		return consumoMinimoLigacao;
	}
}
