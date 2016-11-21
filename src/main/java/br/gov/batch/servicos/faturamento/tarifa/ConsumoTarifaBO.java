package br.gov.batch.servicos.faturamento.tarifa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.cadastro.ImovelBO;
import br.gov.batch.servicos.faturamento.FaturamentoAtividadeCronogramaBO;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.ConsumoTarifaCategoria;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.faturamento.TarifaTipoCalculo;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaCategoriaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaVigenciaRepositorio;
import br.gov.servicos.faturamento.TarifaTipoCalculoRepositorio;
import br.gov.servicos.to.ConsumoTarifaCategoriaTO;
import br.gov.servicos.to.ConsumoTarifaVigenciaTO;

@Stateless
public class ConsumoTarifaBO {

	@EJB
	private SistemaParametrosRepositorio sistemaParametrosRepositorio;

	@EJB
	private TarifaTipoCalculoRepositorio tarifaTipoCalculoRepositorio;

	@EJB
	private ConsumoTarifaVigenciaRepositorio consumoTarifaVigenciaRepositorio;

	@EJB
	private ConsumoTarifaCategoriaRepositorio consumoTarifaCategoriaRepositorio;

	@EJB
	private FaturamentoAtividadeCronogramaBO faturamentoAtividadeCronogramaBO;

	@EJB
	private ImovelBO imovelBO;

	private SistemaParametros sistemaParametros;

	@PostConstruct
	public void init() {
		sistemaParametros = sistemaParametrosRepositorio.getSistemaParametros();
	}
	
	public List<ConsumoTarifaCategoriaTO> obterConsumoTarifasPorPeriodo(Imovel imovel, Date dataAnterior, Date dataAtual, ICategoria categoria) {
		
		List<ConsumoTarifaVigenciaTO> tarifaTO = consumoTarifaVigenciaRepositorio.buscarTarifasPorPeriodo(imovel.getConsumoTarifa().getId(),
				dataAnterior, dataAtual);
		
		List<ConsumoTarifaCategoria> consumoTarifasCategoria = new ArrayList<ConsumoTarifaCategoria>();

		List<ICategoria> categorias = new ArrayList<>();
		
		categorias.add(categoria);
		
		for (ConsumoTarifaVigenciaTO consumoTarifaVigenciaTO : tarifaTO) {
			consumoTarifasCategoria.addAll(getConsumoTarifaCategoriaVigenteList(categorias, consumoTarifaVigenciaTO));
		}

		return getConsumoTarifaCategoriaTOList(consumoTarifasCategoria);
	}

	public List<ConsumoTarifaCategoriaTO> obterConsumoTarifasPorReferencia(Imovel imovel, MedicaoHistorico medicaoHistorico) {
		Integer referencia = medicaoHistorico.getAnoMesReferencia();

		return obterConsumoTarifasPorReferencia(imovel, referencia);
	}

	public List<ConsumoTarifaCategoriaTO> obterConsumoTarifasPorReferencia(Imovel imovel, Integer referencia) {
		List<ConsumoTarifaCategoria> consumoTarifasCategoria = this.obterTarifasConsumoParaCalculoPorReferencia(referencia, imovel);

		return getConsumoTarifaCategoriaTOList(consumoTarifasCategoria);
	}

	public List<ConsumoTarifaCategoria> obterConsumoTarifasCategoria(Imovel imovel, SistemaParametros sistemaParametros) {

		FaturamentoGrupo faturamentoGrupo = imovelBO.pesquisarFaturamentoGrupo(imovel.getId());

		TarifaTipoCalculo tipoCalculoTarifa = tarifaTipoCalculoRepositorio.tarifaTipoCalculoAtiva();

		if (tipoCalculoTarifa != null && tipoCalculoTarifa.getId().equals(TarifaTipoCalculo.CALCULO_POR_REFERENCIA)) {
			return obterTarifasConsumoParaCalculoPorReferencia(faturamentoGrupo.getAnoMesReferencia(), imovel);
		} else {
			return obterTarifasConsumoParaCalculoPorReferenciaAnterior(imovel, faturamentoGrupo);
		}
	}

	public ConsumoTarifaCategoria obterConsumoTarifaCategoriaVigente(ConsumoTarifaVigenciaTO tarifaTO, ICategoria subcategoria) {

		ConsumoTarifaCategoria consumoTarifaCategoria = consumoTarifaCategoriaRepositorio.buscarConsumoTarifaCategoriaVigente(
				tarifaTO.getDataVigencia(), subcategoria.getCategoria().getId(), subcategoria.getSubcategoria().getId());

		if (consumoTarifaCategoria == null) {
			consumoTarifaCategoria = consumoTarifaCategoriaRepositorio.buscarConsumoTarifaCategoriaVigente(tarifaTO.getDataVigencia(),
					subcategoria.getCategoria().getId(), 0);
		}

		return consumoTarifaCategoria;
	}
	
	//TODO Refatorar esse método, muito extenso.
	private List<ConsumoTarifaCategoria> obterTarifasConsumoParaCalculoPorReferenciaAnterior(Imovel imovel, FaturamentoGrupo faturamentoGrupo) {

		List<ICategoria> subcategorias = imovelBO.obterCategorias(imovel, sistemaParametros);

		List<ConsumoTarifaCategoria> tarifasCategoriaConsumo = new ArrayList<ConsumoTarifaCategoria>();

		Date dataLeituraAnterior = faturamentoAtividadeCronogramaBO.obterDataLeituraAnterior(imovel, faturamentoGrupo);

		boolean dataVigenciaIgualAnterior = false;

		for (ICategoria subcategoria : subcategorias) {

			List<ConsumoTarifaCategoria> colecaoDadosTarifaCategoria = consumoTarifaCategoriaRepositorio
					.buscarConsumoTarifaCategoriaVigentePelaDataLeitura(dataLeituraAnterior, imovel.getConsumoTarifa().getId(),
							subcategoria.getCategoria().getId(), subcategoria.getSubcategoria().getId());

			if (!colecaoDadosTarifaCategoria.isEmpty()) {

				for (ConsumoTarifaCategoria tarifaCategoria : colecaoDadosTarifaCategoria) {

					Date dataVigencia = tarifaCategoria.getConsumoTarifaVigencia().getDataVigencia();

					if (dataVigencia != null && Utilitarios.datasIguais(dataLeituraAnterior, dataVigencia)) {
						dataVigenciaIgualAnterior = true;
						break;
					}
				}

				if (!dataVigenciaIgualAnterior) {
					ConsumoTarifaVigenciaTO tarifaTO = consumoTarifaVigenciaRepositorio
							.buscarConsumoTarifaVigenciaAtualPelaTarifa(imovel.getConsumoTarifa().getId());

					ConsumoTarifaCategoria tarifa = obterConsumoTarifaCategoriaVigente(tarifaTO, subcategoria);
					adicionarTarifaCategoriaVigente(tarifasCategoriaConsumo, tarifa);
				}

				tarifasCategoriaConsumo.addAll(colecaoDadosTarifaCategoria);
			} else {
				ConsumoTarifaVigenciaTO tarifaTO = consumoTarifaVigenciaRepositorio
						.buscarConsumoTarifaVigenciaRecentePorData(imovel.getConsumoTarifa().getId(), dataLeituraAnterior);

				ConsumoTarifaCategoria tarifa = obterConsumoTarifaCategoriaVigente(tarifaTO, subcategoria);
				adicionarTarifaCategoriaVigente(tarifasCategoriaConsumo, tarifa);
			}
		}

		return tarifasCategoriaConsumo;
	}

	private List<ConsumoTarifaCategoria> obterTarifasConsumoParaCalculoPorReferencia(Integer anoMesReferencia, Imovel imovel) {
		Date dataReferencia = Utilitarios.criarData(1, Utilitarios.extrairMes(anoMesReferencia), Utilitarios.extrairAno(anoMesReferencia));

		List<ICategoria> categorias = imovelBO.obterCategorias(imovel, sistemaParametros);

		ConsumoTarifaVigenciaTO tarifaTO = consumoTarifaVigenciaRepositorio
				.buscarConsumoTarifaVigenciaRecentePorData(imovel.getConsumoTarifa().getId(), dataReferencia);
		
		return getConsumoTarifaCategoriaVigenteList(categorias, tarifaTO);
	}
	
	private List<ConsumoTarifaCategoriaTO> getConsumoTarifaCategoriaTOList(List<ConsumoTarifaCategoria> consumoTarifasCategoria) {
		List<ConsumoTarifaCategoriaTO> tos = new ArrayList<ConsumoTarifaCategoriaTO>();
		for (ConsumoTarifaCategoria consumoTarifaCategoria : consumoTarifasCategoria) {
			tos.add(new ConsumoTarifaCategoriaTO(consumoTarifaCategoria));
		}

		return tos;
	}

	private List<ConsumoTarifaCategoria> getConsumoTarifaCategoriaVigenteList(List<ICategoria> categoriasSubcategorias, 
																				ConsumoTarifaVigenciaTO consumoTarifaVigenciaTO) {
		
		List<ConsumoTarifaCategoria> tarifasVigentes = new ArrayList<ConsumoTarifaCategoria>();
		for (ICategoria subcategoria : categoriasSubcategorias) {
			ConsumoTarifaCategoria tarifa = obterConsumoTarifaCategoriaVigente(consumoTarifaVigenciaTO, subcategoria);
			adicionarTarifaCategoriaVigente(tarifasVigentes, tarifa);
		}

		return tarifasVigentes;
	}
	
	private void adicionarTarifaCategoriaVigente(List<ConsumoTarifaCategoria> tarifasVigentes, ConsumoTarifaCategoria tarifa) {
		if(!tarifasVigentes.contains(tarifa)) {
			tarifasVigentes.add(tarifa);
		}
	}
}
