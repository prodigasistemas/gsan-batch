package br.gov.batch.servicos.faturamento;

import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.faturamento.to.VolumeMedioAguaEsgotoTO;
import br.gov.batch.servicos.micromedicao.ConsumoBO;
import br.gov.batch.servicos.micromedicao.ConsumoHistoricoBO;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.ConsumoTarifa;
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
			int idLigacaoTipo, boolean houveInstalacaoHidrometro) {

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
		
		ConsumoHistorico consumoHistorico = listaConsumoHistorico.iterator().next();
		Integer referencia = consumoHistorico.getReferenciaFaturamento();
		Integer maximoDeMesesParaCalcularMedia = sistemaParametros.getNumeroMesesMaximoCalculoMedia().intValue();
		Integer novaDataInicio = Utilitarios.reduzirMeses(dataInicio, maximoDeMesesParaCalcularMedia);
		Integer quantidadeDeMeses = Utilitarios.obterQuantidadeMeses(dataFim, novaDataInicio);

		Integer quantidadeDeMesesConsiderados = 0;
		Integer quantidadeDeMesesRetroagidos = 0;
		Integer consumo = 0;
		Integer mediaConsumo = 0;
		while (quantidadeDeMesesRetroagidos <= maximoDeMesesParaCalcularMedia && quantidadeDeMesesConsiderados < quantidadeDeMeses) {
			if (dataFim.equals(referencia)) {
				consumo += consumoHistorico.getNumeroConsumoCalculoMedia();
				quantidadeDeMesesConsiderados++;

				consumoHistorico = listaConsumoHistorico.iterator().next();
				if (consumoHistorico == null)
					break;

				referencia = consumoHistorico.getNumeroConsumoCalculoMedia();
				dataFim = Utilitarios.reduzirMeses(dataFim, 1);
			} else {
				quantidadeDeMesesRetroagidos++;
			}
		}
		
		if (quantidadeDeMesesConsiderados > 0) {
			mediaConsumo = (consumo / quantidadeDeMesesConsiderados);
		}
		
		return new VolumeMedioAguaEsgotoTO(mediaConsumo, quantidadeDeMesesConsiderados);
	}

	private VolumeMedioAguaEsgotoTO gerarVolumeMedioSemConsumoHistorico(Integer idImovel) {
		Imovel imovel = imovelRepositorio.buscarPeloId(idImovel);

		imovel.setConsumoTarifa(new ConsumoTarifa());
		imovel.getConsumoTarifa().setId(imovel.getId());

		Collection<ICategoria> categoria;

		categoria = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasPorImovel(imovel.getId());
		
		return new VolumeMedioAguaEsgotoTO(consumoBO.obterConsumoMinimoLigacaoPorCategoria(idImovel,
				consumoTarifaRepositorio.consumoTarifaDoImovel(imovel.getId()), categoria), 1);
	}
}
