package br.gov.batch.servicos.faturamento.tarifa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;

import br.gov.batch.servicos.cadastro.ImovelBO;
import br.gov.batch.servicos.faturamento.FaturamentoAtividadeCronogramaBO;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.ConsumoTarifaCategoria;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.faturamento.TarifaTipoCalculo;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaCategoriaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaVigenciaRepositorio;
import br.gov.servicos.faturamento.FaturamentoAtividadeCronogramaRepositorio;
import br.gov.servicos.faturamento.TarifaTipoCalculoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.to.ConsumoTarifaCategoriaTO;
import br.gov.servicos.to.ConsumoTarifaVigenciaTO;

public class ConsumoTarifaBO {

	@EJB
	private TarifaTipoCalculoRepositorio tarifaTipoCalculoRepositorio;

	@EJB
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;

	@EJB
	private ConsumoTarifaVigenciaRepositorio consumoTarifaVigenciaRepositorio;

	@EJB
	private ConsumoTarifaCategoriaRepositorio consumoTarifaCategoriaRepositorio;

	@EJB
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorio;

	@EJB
	private FaturamentoAtividadeCronogramaRepositorio faturamentoAtividadeCronogramaRepositorio;

	@EJB
	private FaturamentoAtividadeCronogramaBO faturamentoAtividadeCronogramaBO;

	@EJB 
	private ImovelBO imovelBO;
	
	public List<ConsumoTarifaCategoriaTO> obterTarifasPorReferencia(Imovel imovel,MedicaoHistorico medicaoHistorico, SistemaParametros sistemaParametros) {
		
		Integer referencia = medicaoHistorico.getAnoMesReferencia();
		
		List<ICategoria> categorias = imovelBO.obterCategorias(imovel, sistemaParametros);
		
		List<ConsumoTarifaCategoria> consumoTarifasCategoria = this.obterTarifasParaCalculoPorReferencia(categorias, referencia, imovel);
		List<ConsumoTarifaCategoriaTO> tos = new ArrayList<ConsumoTarifaCategoriaTO>();
		
		for (ConsumoTarifaCategoria consumoTarifaCategoria : consumoTarifasCategoria) {
			ConsumoTarifaCategoriaTO to = new ConsumoTarifaCategoriaTO(consumoTarifaCategoria);
			
			tos.add(to);
		}
		
		return tos;
	}
	public List<ConsumoTarifaCategoria> obterDadosTarifa(Imovel imovel, SistemaParametros sistemaParametros) {
		
		FaturamentoGrupo faturamentoGrupo = imovelBO.pesquisarFaturamentoGrupo(imovel.getId());
		
		List<ICategoria> categorias = imovelBO.obterCategorias(imovel, sistemaParametros);
		TarifaTipoCalculo tipoCalculoTarifa = tarifaTipoCalculoRepositorio.tarifaTipoCalculoAtiva();

		if (tipoCalculoTarifa != null && tipoCalculoTarifa.getId().equals(TarifaTipoCalculo.CALCULO_POR_REFERENCIA)) {
			return obterTarifasParaCalculoPorReferencia(categorias, faturamentoGrupo.getAnoMesReferencia(), imovel);
		} else {
			return obterTarifasParaCalculoPorReferenciaAnterior(categorias, imovel, faturamentoGrupo);
		}
	}
	
	private List<ConsumoTarifaCategoria> obterTarifasParaCalculoPorReferencia(List<ICategoria> colecaoCatSub, Integer anoMesReferencia, Imovel imovel) {

		List<ConsumoTarifaCategoria> tarifasVigentes = new ArrayList<ConsumoTarifaCategoria>();

		Date dataFaturamento = Utilitarios.criarData(1, Utilitarios.extrairMes(anoMesReferencia), Utilitarios.extrairAno(anoMesReferencia));

		ConsumoTarifaVigenciaTO tarifaTO = consumoTarifaVigenciaRepositorio.maiorDataVigenciaConsumoTarifaPorData(
				imovel.getConsumoTarifa().getId(), dataFaturamento);

		for (ICategoria subcategoria : colecaoCatSub) {
            obterTarifaCategoriaVigente(tarifasVigentes, subcategoria, tarifaTO);
		}
		return tarifasVigentes;
	}

	private List<ConsumoTarifaCategoria> obterTarifasParaCalculoPorReferenciaAnterior(List<ICategoria> subcategorias, Imovel imovel, FaturamentoGrupo faturamentoGrupo) {

		List<ConsumoTarifaCategoria> tarifasCategoriaConsumo = new ArrayList<ConsumoTarifaCategoria>();

		Date dataLeituraAnterior = faturamentoAtividadeCronogramaBO.obterDataLeituraAnterior(imovel, faturamentoGrupo);

		boolean dataVigenciaIgualAnterior = false;

		for (ICategoria subcategoria : subcategorias) {

			List<ConsumoTarifaCategoria> colecaoDadosTarifaCategoria = consumoTarifaCategoriaRepositorio.buscarConsumoTarifaCategoriaVigentePelaDataLeitura(
					dataLeituraAnterior, imovel.getConsumoTarifa().getId(), subcategoria.getCategoria().getId(), subcategoria.getSubcategoria().getId());

			if (!colecaoDadosTarifaCategoria.isEmpty()) {

				for (ConsumoTarifaCategoria tarifaCategoria : colecaoDadosTarifaCategoria) {

					Date dataVigencia = tarifaCategoria.getConsumoTarifaVigencia().getDataVigencia();

					if (dataVigencia != null && Utilitarios.datasIguais(dataLeituraAnterior, dataVigencia)) {
						dataVigenciaIgualAnterior = true;
						break;
					}
				}

				if (!dataVigenciaIgualAnterior) {
					ConsumoTarifaVigenciaTO tarifaTO = consumoTarifaVigenciaRepositorio.maiorDataVigenciaConsumoTarifa(
							imovel.getConsumoTarifa().getId());
					
					obterTarifaCategoriaVigente(tarifasCategoriaConsumo, subcategoria, tarifaTO);
				}

				tarifasCategoriaConsumo.addAll(colecaoDadosTarifaCategoria);
			} else {
				ConsumoTarifaVigenciaTO tarifaTO = consumoTarifaVigenciaRepositorio.maiorDataVigenciaConsumoTarifaPorData(
						imovel.getConsumoTarifa().getId(), dataLeituraAnterior);
				
				obterTarifaCategoriaVigente(tarifasCategoriaConsumo, subcategoria, tarifaTO);
			}
		}

		return tarifasCategoriaConsumo;
	}
	
	private void obterTarifaCategoriaVigente(List<ConsumoTarifaCategoria> tarifasVigentes, ICategoria subcategoria, ConsumoTarifaVigenciaTO tarifaTO) {
	    
	    ConsumoTarifaCategoria tarifa = obterTarifaCategoriaVigente(tarifaTO, subcategoria);
	    
	    if (tarifa != null){
	        boolean tarifaDiferente = true;
	        
	        for (ConsumoTarifaCategoria consumoTarifaCategoria : tarifasVigentes) {
	            if (consumoTarifaCategoria.getId().equals(tarifa.getId())) {
	                tarifaDiferente = false;
	                break;
	            }
	        }
	        
	        if (tarifaDiferente) {
	            tarifasVigentes.add(tarifa);
	        }
	    }
	}
	
	private ConsumoTarifaCategoria obterTarifaCategoriaVigente(ConsumoTarifaVigenciaTO tarifaTO, ICategoria subcategoria) {
		
		ConsumoTarifaCategoria consumoTarifaCategoria = consumoTarifaCategoriaRepositorio.buscarConsumoTarifaCategoriaVigente(
				tarifaTO.getDataVigencia(), tarifaTO.getIdVigencia(), subcategoria.getCategoria().getId(),
				subcategoria.getSubcategoria().getId());

		if (consumoTarifaCategoria == null) {
			consumoTarifaCategoria = consumoTarifaCategoriaRepositorio.buscarConsumoTarifaCategoriaVigente(tarifaTO.getDataVigencia(),
					tarifaTO.getIdVigencia(), subcategoria.getCategoria().getId(), 0);
		}

		return consumoTarifaCategoria;
	}
}
