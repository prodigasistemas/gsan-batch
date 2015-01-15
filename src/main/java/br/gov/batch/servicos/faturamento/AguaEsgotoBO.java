package br.gov.batch.servicos.faturamento;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.faturamento.to.VolumeMedioAguaEsgotoTO;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;

@Stateless
public class AguaEsgotoBO {

	@EJB
	private ConsumoHistoricoRepositorio consumoHistoricoRepositorio; 
	
	@EJB
	private SistemaParametrosRepositorio sistemaParametrosRepositorio; 
	
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
			return gerarVolumeMedioSemConsumoHistorico();
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

	private VolumeMedioAguaEsgotoTO gerarVolumeMedioSemConsumoHistorico() {
		// TODO Auto-generated method stub
		return null;
	}
}






