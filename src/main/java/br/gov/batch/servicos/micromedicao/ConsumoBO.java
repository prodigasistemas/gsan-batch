package br.gov.batch.servicos.micromedicao;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.cadastro.ImovelBO;
import br.gov.model.Status;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.ConsumoImovelCategoriaTO;
import br.gov.model.faturamento.ConsumoTarifaVigencia;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaCategoriaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaVigenciaRepositorio;
import br.gov.servicos.micromedicao.ConsumoMinimoAreaRepositorio;
import br.gov.servicos.to.ConsumoTarifaCategoriaTO;
import br.gov.servicos.to.ConsumoTarifaFaixaTO;
import br.gov.servicos.to.ConsumoTarifaVigenciaTO;

@Stateless
public class ConsumoBO {

	@EJB
	private SistemaParametrosRepositorio sistemaParametrosRepositorio;

	@EJB
	private ConsumoTarifaRepositorio consumoTarifaRepositorio;

	@EJB
	private ConsumoTarifaVigenciaRepositorio consumoTarifaVigenciaRepositorio;

	@EJB
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;

	@EJB
	private ConsumoTarifaCategoriaRepositorio consumoTarifaCategoriaRepositorio;

	@EJB
	private ImovelBO imovelBO;

	@EJB
	private ConsumoMinimoAreaRepositorio consumoMinimoAreaRepositorio;

	private SistemaParametros sistemaParametros;

	@PostConstruct
	public void init() {
		sistemaParametros = sistemaParametrosRepositorio.getSistemaParametros();
	}

	public int consumoNaoMedido(Integer idImovel, Integer anoMesReferencia) {
		if (sistemaParametros.getIndicadorNaoMedidoTarifa() == Status.ATIVO.getId()) {
			return this.consumoMinimoLigacao(idImovel);
		} else {
			return this.obterConsumoNaoMedidoSemTarifa(idImovel, anoMesReferencia);
		}
	}
	
	public int obterConsumoNaoMedidoSemTarifa(Integer idImovel, Integer anoMesReferencia) {
		Integer qtdEconomiasVirtuais = getQuantidadeTotalEconomias(idImovel);

		BigDecimal areaConstruida = imovelBO.verificarAreaConstruida(idImovel);

		BigDecimal areaConstruidaVirtual = areaConstruida.divide(new BigDecimal(qtdEconomiasVirtuais), 2, BigDecimal.ROUND_HALF_UP);

		Collection<ICategoria> subcategoria = imovelSubcategoriaRepositorio.buscarSubcategoria(idImovel);

		Integer consumoNaoMedido = 0;

		for (ICategoria sub : subcategoria) {
			Integer consumoMinimo = consumoMinimoAreaRepositorio.pesquisarConsumoMinimoArea(areaConstruidaVirtual, anoMesReferencia, null, sub.getId());

			if (sub.getCategoria().getFatorEconomias() != null) {
				consumoNaoMedido += consumoMinimo * sub.getCategoria().getFatorEconomias();
			} else {
				consumoNaoMedido += consumoMinimo * sub.getQuantidadeEconomias();
			}
		}

		return consumoNaoMedido;
	}

	public Integer consumoMinimoLigacao(Integer idImovel) {
		Integer idTarifa = consumoTarifaRepositorio.consumoTarifaDoImovel(idImovel);

		Collection<ICategoria> categorias = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasPorImovel(idImovel);

		return obterConsumoMinimoLigacaoCategorias(idImovel, idTarifa, categorias);
	}
	
	public BigDecimal valorMinimoTarifa(Integer idImovel) {
		Integer idTarifa = consumoTarifaRepositorio.consumoTarifaDoImovel(idImovel);
		
		Collection<ICategoria> categorias = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasPorImovel(idImovel);
		
		return obterValorMinimoTarifaCategorias(idTarifa, categorias);
	}

	private BigDecimal obterValorMinimoTarifaCategorias(Integer idTarifa, Collection<ICategoria> categorias) {
		BigDecimal valorMinimoTarifa = BigDecimal.ZERO;
		
		for (ICategoria categoria : categorias) {
			valorMinimoTarifa.add(obterValorMinimoTarifaPorCategoria(idTarifa, categoria));
		}

		return valorMinimoTarifa;
	}

	public BigDecimal obterValorMinimoTarifaPorCategoria(Integer idTarifa, ICategoria categoria) {
		BigDecimal valorMinimoTarifa = getValorMinimoTarifaPorCategoria(idTarifa, categoria);

		if (categoria.getFatorEconomias() != null) {
			valorMinimoTarifa.add(valorMinimoTarifa.multiply(new BigDecimal(categoria.getFatorEconomias())));
		} else {
			valorMinimoTarifa.add(valorMinimoTarifa.multiply(new BigDecimal(categoria.getQuantidadeEconomias())));
		}
		return valorMinimoTarifa;
	}


	public int obterConsumoMinimoLigacaoCategorias(Integer idImovel, Integer idTarifa, Collection<ICategoria> categorias) {
		int consumoMinimoLigacao = 0;

		for (ICategoria categoria : categorias) {
			consumoMinimoLigacao += obterConsumoMinimoLigacaoPorCategoria(consumoMinimoLigacao, idTarifa, categoria);
		}

		return consumoMinimoLigacao;
	}

	public int obterConsumoMinimoLigacaoPorCategoria(int consumoMinimoLigacao, Integer idTarifa, ICategoria categoria) {
		Integer consumoMinimoTarifa = getConsumoMinimoTarifaPorCategoria(idTarifa, categoria);

		if (categoria.getFatorEconomias() != null) {
			consumoMinimoLigacao += consumoMinimoTarifa * categoria.getFatorEconomias().intValue();
		} else {
			consumoMinimoLigacao += consumoMinimoTarifa * categoria.getQuantidadeEconomias();
		}
		return consumoMinimoLigacao;
	}

	public BigDecimal getValorMinimoTarifaPorCategoria(Integer idTarifa, ICategoria categoria) {
		ConsumoTarifaVigenciaTO consumoTarifaVigencia = consumoTarifaVigenciaRepositorio.maiorDataVigenciaConsumoTarifa(idTarifa);
		
		return consumoTarifaCategoriaRepositorio.valorMinimoTarifa(categoria, consumoTarifaVigencia.getIdVigencia());
	}

	public int getConsumoMinimoTarifaPorCategoria(Integer idTarifa, ICategoria categoria) {
		ConsumoTarifaVigenciaTO consumoTarifaVigencia = consumoTarifaVigenciaRepositorio.maiorDataVigenciaConsumoTarifa(idTarifa);
		
		return consumoTarifaCategoriaRepositorio.consumoMinimoTarifa(categoria, consumoTarifaVigencia.getIdVigencia());
	}

	public Collection<ICategoria> buscarQuantidadeEconomiasPorImovel(Integer imovelId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getQuantidadeTotalEconomias(Integer idImovel) {
		Collection<ICategoria> subcategorias = imovelSubcategoriaRepositorio.buscarSubcategoria(idImovel);

		Integer quantidadeEconomias = 0;
		for (ICategoria subcategoria : subcategorias) {
			quantidadeEconomias += getQuantidadeEconomiasPorCategoria(subcategoria);
		}
		
		return quantidadeEconomias;
	}

	public Integer getQuantidadeEconomiasPorCategoria(ICategoria subcategoria) {
		Integer quantidadeEconomias = 0;
		if (subcategoria.getCategoria().getFatorEconomias() != null) {
			quantidadeEconomias = subcategoria.getCategoria().getFatorEconomias().intValue();
		} else {
			quantidadeEconomias = subcategoria.getQuantidadeEconomias();
		}
		return quantidadeEconomias;
	}
	
	public List<ConsumoTarifaCategoriaTO> getConsumoTarifasCategoria(ConsumoTarifaVigencia consumoTarifaVigencia, ICategoria categoria) {
		return null;
	}

	public List<ConsumoTarifaFaixaTO> obterFaixas(ConsumoImovelCategoriaTO consumoImovelCategoriaTO, MedicaoHistorico medicaoHistorico) {
		return null;
	}
}
