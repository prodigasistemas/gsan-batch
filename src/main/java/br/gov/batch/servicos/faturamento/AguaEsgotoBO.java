package br.gov.batch.servicos.faturamento;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.faturamento.to.VolumeMedioAguaEsgotoTO;
import br.gov.batch.servicos.micromedicao.ConsumoBO;
import br.gov.batch.servicos.micromedicao.ConsumoHistoricoBO;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaRepositorio;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;

@Stateless
public class AguaEsgotoBO {

	@EJB
	private ConsumoHistoricoRepositorio consumoHistoricoRepositorio;

	@EJB
	private ImovelRepositorio imovelRepositorio;

	@EJB
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;

	@EJB
	private ConsumoHistoricoBO consumoHistoricoBO;

	@EJB
	private ConsumoBO consumoBO;

	@EJB
	private ConsumoTarifaRepositorio consumoTarifaRepositorio;

	@EJB
	private SistemaParametrosRepositorio sistemaParametrosRepositorio;
	
	private SistemaParametros sistemaParametros;
	
	@PostConstruct
	private void init(){
		sistemaParametros = sistemaParametrosRepositorio.getSistemaParametros();
	}
	
	public VolumeMedioAguaEsgotoTO obterVolumeMedioAguaEsgoto(Integer idImovel, Integer anoMesReferencia,
			int idLigacaoTipo) {

		Integer dataFim = Utilitarios.reduzirMeses(anoMesReferencia, 1);
		Integer dataInicio = Utilitarios.reduzirMeses(dataFim, sistemaParametros.getMesesMediaConsumo());
		List<ConsumoHistorico> listaConsumoHistorico = consumoHistoricoRepositorio.obterConsumoMedio(
				idImovel, dataInicio, dataFim, idLigacaoTipo);

		if (listaConsumoHistorico != null && !listaConsumoHistorico.isEmpty()) {
			return gerarVolumeMedioComConsumoHistorico(listaConsumoHistorico, dataInicio, dataFim);
		} else {
			return gerarVolumeMedioSemConsumoHistorico(idImovel);
		}
	}

	private VolumeMedioAguaEsgotoTO gerarVolumeMedioComConsumoHistorico(List<ConsumoHistorico> listaConsumoHistorico,
			Integer dataInicio, Integer dataFim) {
		
		ListIterator<ConsumoHistorico> iterator = listaConsumoHistorico.listIterator();
		ConsumoHistorico consumoHistorico = iterator.next();
		int referencia = consumoHistorico.getReferenciaFaturamento();
		int maximoDeMesesParaCalcularMedia = sistemaParametros.getNumeroMesesMaximoCalculoMedia().intValue();
		int novaDataInicio = Utilitarios.reduzirMeses(dataInicio, maximoDeMesesParaCalcularMedia);
		int quantidadeDeMeses = Utilitarios.obterQuantidadeMeses(dataFim, novaDataInicio);

		int quantidadeDeMesesConsiderados = 0;
		int quantidadeDeMesesRetroagidos = 0;
		int consumo = 0;
		int mediaConsumo = 0;
		
		while (quantidadeDeMesesRetroagidos <= maximoDeMesesParaCalcularMedia && quantidadeDeMesesConsiderados < quantidadeDeMeses) {
			if (dataFim.equals(referencia)) {
				consumo += consumoHistorico.getNumeroConsumoCalculoMedia();
				quantidadeDeMesesConsiderados++;

				if (iterator.hasNext()) {
					consumoHistorico = iterator.next();
					referencia = consumoHistorico.getReferenciaFaturamento();
				} else {
					break;
				}
			} else {
				quantidadeDeMesesRetroagidos++;
			}
			
			if (quantidadeDeMesesRetroagidos < maximoDeMesesParaCalcularMedia) {
				dataFim = Utilitarios.reduzirMeses(dataFim, 1);
			}
		}
		
		if (quantidadeDeMesesConsiderados > 0) {
			mediaConsumo = (consumo / quantidadeDeMesesConsiderados);
		}
		
		return new VolumeMedioAguaEsgotoTO(mediaConsumo, quantidadeDeMesesConsiderados);
	}

	private VolumeMedioAguaEsgotoTO gerarVolumeMedioSemConsumoHistorico(Integer idImovel) {
		Collection<ICategoria> categorias = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasPorImovel(idImovel);
		int idTarifa = consumoTarifaRepositorio.consumoTarifaDoImovel(idImovel);
		int consumoMinimo = consumoBO.obterConsumoMinimoLigacaoPorCategoria(idImovel, idTarifa, categorias);
		
		return new VolumeMedioAguaEsgotoTO(consumoMinimo, 1);
	}
}
