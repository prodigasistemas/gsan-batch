package br.gov.batch.servicos.faturamento;

import java.util.Collection;
import java.util.List;

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
import br.gov.model.micromedicao.LigacaoTipo;
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
	private SistemaParametrosRepositorio sistemaParametrosRepositorio; 
	
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
	
	public VolumeMedioAguaEsgotoTO obterVolumeMedioAguaEsgoto(Integer idImovel,
			Integer anoMesReferencia, LigacaoTipo ligacaoTipo, boolean houveInstalacaoHidrometro){
		
		SistemaParametros sistemaParametros = sistemaParametrosRepositorio.getSistemaParametros();
		List<ConsumoHistorico> dadosConsumo;
		Integer dataInicio;
		Integer dataFim;

		dataFim = Utilitarios.reduzirMeses(anoMesReferencia, 1);
		dataInicio = Utilitarios.reduzirMeses(dataFim, sistemaParametros.getMesesMediaConsumo());
		dadosConsumo = consumoHistoricoRepositorio.obterVolumeMedioAguaOuEsgoto(idImovel, dataInicio, dataFim, ligacaoTipo);
		
		if(dadosConsumo!=null && !dadosConsumo.isEmpty()){
			return gerarVolumeMedioComConsumoHistorico(sistemaParametros, dadosConsumo, dataInicio, dataFim);
		}else{
			return gerarVolumeMedioSemConsumoHistorico(idImovel);
		}
	}

	private VolumeMedioAguaEsgotoTO gerarVolumeMedioComConsumoHistorico(SistemaParametros sistemaParametros,
			List<ConsumoHistorico> dadosConsumo, Integer dataInicio,Integer dataFim){
		ConsumoHistorico dados = dadosConsumo.iterator().next();
		Integer referencia = dados.getReferenciaFaturamento();
		Integer maximoDeMesesParaCalcularMedia;
		Integer novaDataInicio;
		Integer quantidadeDeMeses;
		Integer quantidadeDeMesesConsiderados = 0;
		Integer quantidadeDeMesesRetroagidos = 0;
		Integer consumo = 0;
		Integer mediaConsumo = 0;
		
		maximoDeMesesParaCalcularMedia = sistemaParametros.getNumeroMesesMaximoCalculoMedia().intValue();
		novaDataInicio = Utilitarios.reduzirMeses(dataInicio, maximoDeMesesParaCalcularMedia);
		quantidadeDeMeses = Utilitarios.obterQuantidadeMeses(dataFim, novaDataInicio);
		
		while (quantidadeDeMesesRetroagidos <= maximoDeMesesParaCalcularMedia
				&& quantidadeDeMesesConsiderados < quantidadeDeMeses) {
			if (dataFim.equals(referencia)) {
				consumo += dados.getNumeroConsumoCalculoMedia();
				quantidadeDeMesesConsiderados++;

				dados = dadosConsumo.iterator().next();
				if(dados==null)break;
				
				referencia = dados.getNumeroConsumoCalculoMedia();
				dataFim = Utilitarios.reduzirMeses(dataFim, 1);
			}else{
				quantidadeDeMesesRetroagidos++;
			}
		}
		if (quantidadeDeMesesConsiderados > 0) {
			mediaConsumo = (consumo/quantidadeDeMesesConsiderados);
		}
		return new VolumeMedioAguaEsgotoTO(mediaConsumo, quantidadeDeMesesConsiderados);
	}

	private VolumeMedioAguaEsgotoTO gerarVolumeMedioSemConsumoHistorico(Integer idImovel) {
		Imovel imovel = this.imovelRepositorio.buscarPeloId(idImovel);

		imovel.setConsumoTarifa(new ConsumoTarifa());
		imovel.getConsumoTarifa().setId(imovel.getId());

		Collection<ICategoria> categoria;
		
		SistemaParametros sistemaParametros = this.sistemaParametrosRepositorio.getSistemaParametros();

		if (sistemaParametros.getIndicadorTarifaCategoria().equals(
				SistemaParametros.INDICADOR_TARIFA_CATEGORIA)) {
			categoria = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasCategoria(imovel.getId());
		} else {
			categoria = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasSubcategoria(imovel.getId());
		}
		return new VolumeMedioAguaEsgotoTO(consumoBO.obterConsumoMinimoLigacaoPorCategoria(idImovel, 
				consumoTarifaRepositorio.consumoTarifaDoImovel(imovel.getId()), categoria),1);
	}
}






