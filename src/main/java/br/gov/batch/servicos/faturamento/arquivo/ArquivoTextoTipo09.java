package br.gov.batch.servicos.faturamento.arquivo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.batch.servicos.faturamento.FaturamentoAtividadeCronogramaBO;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
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

@Stateless
public class ArquivoTextoTipo09 extends ArquivoTexto {
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

	public ArquivoTextoTipo09() {
		super();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String build(ArquivoTextoTO to) {
		List<ConsumoTarifaCategoria> colecaoDadosTarifaCategoria = obterDadosTarifa(to.getImovel(), to.getFaturamentoGrupo());
		
		for (ConsumoTarifaCategoria dadosTarifaCategoria : colecaoDadosTarifaCategoria) {
		    if (dadosTarifaCategoria.getConsumoTarifaVigencia() == null || dadosTarifaCategoria.getConsumoTarifaVigencia().getConsumoTarifa() == null){
		        continue;
		    }
		    
			builder.append(TIPO_REGISTRO_09_TARIFA);
			builder.append(Utilitarios.completaComZerosEsquerda(2, dadosTarifaCategoria.getConsumoTarifaVigencia().getConsumoTarifa().getId()));
			builder.append(Utilitarios.formataData(dadosTarifaCategoria.getConsumoTarifaVigencia().getDataVigencia(), FormatoData.ANO_MES_DIA));
			builder.append(Utilitarios.completaComZerosEsquerda(1, dadosTarifaCategoria.getCategoria().getId()));
			buildDadosTarifaSubcategoria(dadosTarifaCategoria);
			builder.append(Utilitarios.completaComZerosEsquerda(6, dadosTarifaCategoria.getNumeroConsumoMinimo()));
			builder.append(Utilitarios.completaComZerosEsquerda(14, Utilitarios.formatarBigDecimalComPonto((BigDecimal) dadosTarifaCategoria.getValorTarifaMinima())));
			builder.append(System.getProperty("line.separator"));
			
			to.addIdsConsumoTarifaCategoria(dadosTarifaCategoria.getId());
		}
		return builder.toString();
	}

	private void buildDadosTarifaSubcategoria(ConsumoTarifaCategoria dadosTarifa) {
		if (sistemaParametros.indicadorTarifaCategoria()) {
			builder.append(Utilitarios.completaTexto(3, ""));
		} else {
			builder.append(Utilitarios.completaComZerosEsquerda(3, dadosTarifa.getSubcategoria().getId()));
		}
	}

	private List<ConsumoTarifaCategoria> obterDadosTarifa(Imovel imovel, FaturamentoGrupo faturamentoGrupo) {
		List<ICategoria> categorias = obterCategorias(imovel);
		TarifaTipoCalculo tipoCalculoTarifa = tarifaTipoCalculoRepositorio.tarifaTipoCalculoAtiva();

		if (tipoCalculoTarifa != null && tipoCalculoTarifa.getId().equals(TarifaTipoCalculo.CALCULO_POR_REFERENCIA)) {
			return obterTarifasParaCalculoPorReferencia(categorias, faturamentoGrupo.getAnoMesReferencia(), imovel);
		} else {
			return obterTarifasParaCalculoPorReferenciaAnterior(categorias, imovel, faturamentoGrupo);
		}
	}

	private List<ICategoria> obterCategorias(Imovel imovel) {

		Collection<ICategoria> dadosSubcategoria = imovelSubcategoriaRepositorio.buscarSubcategoria(imovel.getId());

		List<ICategoria> colecaoSubcategorias = new ArrayList<ICategoria>();

		if (sistemaParametros.indicadorTarifaCategoria()) {
			Integer idCategoriaAnterior = -1;

			for (ICategoria subcatategoria : dadosSubcategoria) {
				if (!idCategoriaAnterior.equals((Integer) subcatategoria.getCategoria().getId())) {
					idCategoriaAnterior = (Integer) subcatategoria.getCategoria().getId();
					colecaoSubcategorias.add(subcatategoria);
				}
			}
		} else {
			colecaoSubcategorias.addAll(dadosSubcategoria);
		}
		return colecaoSubcategorias;
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
	    
	    if (tarifa == null){
	        Integer idSub = subcategoria.getSubcategoria() != null ? subcategoria.getSubcategoria().getId() : null;
	        Integer idCat = subcategoria.getCategoria() != null ? subcategoria.getCategoria().getId() : null;
	    }
	    
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