package br.gov.batch.servicos.faturamento.arquivo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;

import br.gov.batch.servicos.faturamento.FaturamentoAtividadeCronogramaBO;
import br.gov.model.Status;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.ConsumoTarifaCategoria;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.faturamento.TarifaTipoCalculo;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaCategoriaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaVigenciaRepositorio;
import br.gov.servicos.faturamento.FaturamentoAtividadeCronogramaRepositorio;
import br.gov.servicos.faturamento.TarifaTipoCalculoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.to.ConsumoTarifaVigenciaTO;



public class ArquivoTextoTipo09 {

	@Inject
	private SistemaParametros sistemaParametro;
	
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
	
	private Imovel imovel;
	private Integer anoMesReferencia;
	private FaturamentoGrupo faturamentoGrupo;

	private StringBuilder builder;
	
	private final String TIPO_REGISTRO = "09";
	
	public ArquivoTextoTipo09(Imovel imovel, FaturamentoGrupo grupo, SistemaParametros sistemaParametros) {
		this.imovel = imovel;
		this.faturamentoGrupo = grupo;
		this.anoMesReferencia = grupo.getAnoMesReferencia();
		this.sistemaParametro = sistemaParametros;
	}
	
	public String build() {

		builder = new StringBuilder();
		int quantidadeLinhas = 0;

		List<ConsumoTarifaCategoria> colecaoDadosTarifa = obterDadosTarifa();

		if (colecaoDadosTarifa != null && !colecaoDadosTarifa.isEmpty()) {
			for (ConsumoTarifaCategoria dadosTarifa : colecaoDadosTarifa) {
				quantidadeLinhas = quantidadeLinhas + 1;
				builder.append(TIPO_REGISTRO);
				builder.append(Utilitarios.completaComZerosEsquerda(2, dadosTarifa.getConsumoTarifaVigencia().getConsumoTarifa().getId()));
				builder.append(Utilitarios.formataData(dadosTarifa.getConsumoTarifaVigencia().getDataVigencia(), FormatoData.ANO_MES_DIA));
				builder.append(Utilitarios.completaComZerosEsquerda(1, dadosTarifa.getCategoria().getId()));
				buildDadosTarifaSubcategoria(dadosTarifa);
				builder.append(Utilitarios.completaComZerosEsquerda(6, dadosTarifa.getNumeroConsumoMinimo()));
				builder.append(Utilitarios.completaComZerosEsquerda(14,Utilitarios.formatarBigDecimalComPonto((BigDecimal) dadosTarifa.getValorTarifaMinima())));
				builder.append(System.getProperty("line.separator"));
			}
		}
		return builder.toString();
	}

	private void buildDadosTarifaSubcategoria(ConsumoTarifaCategoria dadosTarifa) {
		if (sistemaParametro.getIndicadorTarifaCategoria().equals(Status.INATIVO)) {
			builder.append(Utilitarios.completaComZerosEsquerda(3, dadosTarifa.getSubCategoria().getId()));
		} else {
			builder.append(Utilitarios.completaTexto(3, ""));
		}
	}

	private List<ConsumoTarifaCategoria> obterDadosTarifa() {
		
		List<ICategoria> categorias = obterCategorias(sistemaParametro.getIndicadorTarifaCategoria());
		TarifaTipoCalculo tipoCalculoTarifa = tarifaTipoCalculoRepositorio.tarifaTipoCalculoAtiva();
		
		if (tipoCalculoTarifa != null && tipoCalculoTarifa.getId().equals(TarifaTipoCalculo.CALCULO_POR_REFERENCIA)) {
			return obterTarifasParaCalculoPorReferencia(categorias);
		} else {
			return obterTarifasParaCalculoPorReferenciaAnterior(categorias);
		}
	}
	
	private List<ICategoria> obterCategorias(Short indicadorTarifaCategoria) {
		
		Collection<ICategoria> dadosSubcategoria = imovelSubcategoriaRepositorio.buscarSubcategoria(imovel.getId());
		
		List<ICategoria> colecaoSubcategorias = new ArrayList<ICategoria>();
		
		if (indicadorTarifaCategoria.equals(Status.ATIVO)) {
			Integer idCategoriaAnterior = -1;

			for (ICategoria subcatategoria : dadosSubcategoria) {
				if (!idCategoriaAnterior.equals((Integer)subcatategoria.getCategoria().getId())) {
					idCategoriaAnterior = (Integer) subcatategoria.getCategoria().getId();
					colecaoSubcategorias.add(subcatategoria);
				}
			}
		} else {
			colecaoSubcategorias.addAll(dadosSubcategoria);
		}
		return colecaoSubcategorias;
	}

	private List<ConsumoTarifaCategoria> obterTarifasParaCalculoPorReferencia(List<ICategoria> colecaoCatSub) {
		
		List<ConsumoTarifaCategoria> colecaoConsumoTarifaCategoriaVigente = new ArrayList<ConsumoTarifaCategoria>();
		
		Date dataFaturamento = Utilitarios.criarData(1, Utilitarios.extrairMes(anoMesReferencia), Utilitarios.extrairAno(anoMesReferencia));
		
		ConsumoTarifaVigenciaTO consumoTarifaVigente = consumoTarifaVigenciaRepositorio.maiorDataVigenciaConsumoTarifaPorData(
				imovel.getConsumoTarifa().getId(), 
				dataFaturamento);
				
		if (consumoTarifaVigente != null) {
			for (ICategoria subcategoria : colecaoCatSub) {
				obterTarifaCategoriaVigente(colecaoConsumoTarifaCategoriaVigente, subcategoria, consumoTarifaVigente);
			}
		}
		return colecaoConsumoTarifaCategoriaVigente;
	}

	private List<ConsumoTarifaCategoria> obterTarifasParaCalculoPorReferenciaAnterior(List<ICategoria> subcategorias) {
		
		List<ConsumoTarifaCategoria> colecaoConsumoTarifaCategoria = new ArrayList<ConsumoTarifaCategoria>();
		
		Date dataLeituraAnterior = faturamentoAtividadeCronogramaBO.obterDataLeituraAnteriorCronograma(imovel, faturamentoGrupo);
		
		boolean dataVigenciaIgualAnterior = false;

		for (ICategoria subcategoria : subcategorias) {
			
			List<ConsumoTarifaCategoria> colecaoDadosTarifaCategoria = consumoTarifaCategoriaRepositorio.buscarConsumoTarifaCategoriaVigentePelaDataLeitura(
									dataLeituraAnterior, 
									imovel.getConsumoTarifa().getId(), 
									subcategoria.getCategoria().getId(), 
									subcategoria.getSubcategoria().getId());
			
			if (!colecaoDadosTarifaCategoria.isEmpty()) {

				for (ConsumoTarifaCategoria tarifaCategoria : colecaoDadosTarifaCategoria) {

					Date dataVigencia = tarifaCategoria.getConsumoTarifaVigencia().getDataVigencia();
					
					if (dataVigencia != null && Utilitarios.datasIguais(dataLeituraAnterior, dataVigencia)) {
						dataVigenciaIgualAnterior = true;
						break;
					}
				}

				if (!dataVigenciaIgualAnterior) {
					ConsumoTarifaVigenciaTO colecaoDadosMaiorTarifa = consumoTarifaVigenciaRepositorio.maiorDataVigenciaConsumoTarifa(imovel.getConsumoTarifa().getId());
					obterTarifaCategoriaVigente(colecaoConsumoTarifaCategoria, subcategoria, colecaoDadosMaiorTarifa);
				}

				colecaoConsumoTarifaCategoria.addAll(colecaoDadosTarifaCategoria);
			} else {
				ConsumoTarifaVigenciaTO consumoTarifaVigencia = consumoTarifaVigenciaRepositorio.maiorDataVigenciaConsumoTarifaPorData(
						imovel.getConsumoTarifa().getId(),
						dataLeituraAnterior);

				obterTarifaCategoriaVigente(colecaoConsumoTarifaCategoria, subcategoria, consumoTarifaVigencia);
			}
		}
		
		return colecaoConsumoTarifaCategoria;
	}

	private void obterTarifaCategoriaVigente(List<ConsumoTarifaCategoria> colecaoConsumoTarifaCategoriaVigente, ICategoria subcategoria, ConsumoTarifaVigenciaTO consumoTarifaVigente) {
		
		if (consumoTarifaVigente != null) {
			
			ConsumoTarifaCategoria tarifaCategoriaVigente = obterTarifaCategoriaVigente(consumoTarifaVigente, subcategoria);
			boolean consumoTarifaCategoriaDiferente = true;
			if (colecaoConsumoTarifaCategoriaVigente != null) {
				
				for (ConsumoTarifaCategoria consumoTarifaCategoria : colecaoConsumoTarifaCategoriaVigente) {
					if (tarifaCategoriaVigente != null && consumoTarifaCategoria.getId().equals(tarifaCategoriaVigente.getId())) {
						consumoTarifaCategoriaDiferente = false;
						break;
					}
				}
			}
			if (consumoTarifaCategoriaDiferente) {
				colecaoConsumoTarifaCategoriaVigente.add(tarifaCategoriaVigente);
			}

		}
	}
	
	private ConsumoTarifaCategoria obterTarifaCategoriaVigente(ConsumoTarifaVigenciaTO consumoTarifaVigente, ICategoria subcategoria) {
		ConsumoTarifaCategoria consumoTarifaCategoria = consumoTarifaCategoriaRepositorio.buscarConsumoTarifaCategoriaVigente(
				consumoTarifaVigente.getDataVigencia(),
				consumoTarifaVigente.getIdVigencia(),
				subcategoria.getCategoria().getId(),
				subcategoria.getSubcategoria().getId());
		
		if (consumoTarifaCategoria == null) {
			consumoTarifaCategoria = consumoTarifaCategoriaRepositorio.buscarConsumoTarifaCategoriaVigente(
					consumoTarifaVigente.getDataVigencia(),
					consumoTarifaVigente.getIdVigencia(),
					subcategoria.getCategoria().getId(), 
					0);
		}
		
		return consumoTarifaCategoria;
	}
}