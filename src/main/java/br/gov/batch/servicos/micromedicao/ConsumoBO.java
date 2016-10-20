package br.gov.batch.servicos.micromedicao;

import java.math.BigDecimal;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.cadastro.EconomiasBO;
import br.gov.batch.servicos.cadastro.ImovelBO;
import br.gov.model.Status;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaCategoriaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaVigenciaRepositorio;
import br.gov.servicos.micromedicao.ConsumoMinimoAreaRepositorio;
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
	private EconomiasBO economiasBO;

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
		Integer qtdEconomiasVirtuais = economiasBO.getQuantidadeTotalEconomias(idImovel);

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
	
	public Integer valorMinimoTarifa(Integer idImovel) {
		Integer idTarifa = consumoTarifaRepositorio.consumoTarifaDoImovel(idImovel);
		
		Collection<ICategoria> categorias = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasPorImovel(idImovel);
		
		return obterValorMinimoTarifaCategorias(idTarifa, categorias);
	}

	private Integer obterValorMinimoTarifaCategorias(Integer idTarifa, Collection<ICategoria> categorias) {
		int valorMinimoTarifa = 0;
		
		for (ICategoria categoria : categorias) {
			valorMinimoTarifa += obterValorMinimoTarifaPorCategoria(idTarifa, categoria);
		}

		return valorMinimoTarifa;
	}

	public int obterValorMinimoTarifaPorCategoria(Integer idTarifa, ICategoria categoria) {
		int valorMinimoTarifa = getValorMinimoTarifaPorCategoria(idTarifa, categoria);

		if (categoria.getFatorEconomias() != null) {
			valorMinimoTarifa += valorMinimoTarifa * categoria.getFatorEconomias().intValue();
		} else {
			valorMinimoTarifa += valorMinimoTarifa * categoria.getQuantidadeEconomias();
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

	public int getValorMinimoTarifaPorCategoria(Integer idTarifa, ICategoria categoria) {
		ConsumoTarifaVigenciaTO consumoTarifaVigencia = consumoTarifaVigenciaRepositorio.maiorDataVigenciaConsumoTarifa(idTarifa);
		
		return consumoTarifaCategoriaRepositorio.valorMinimoTarifa(categoria, consumoTarifaVigencia.getIdVigencia());
	}

	public int getConsumoMinimoTarifaPorCategoria(Integer idTarifa, ICategoria categoria) {
		ConsumoTarifaVigenciaTO consumoTarifaVigencia = consumoTarifaVigenciaRepositorio.maiorDataVigenciaConsumoTarifa(idTarifa);
		
		return consumoTarifaCategoriaRepositorio.consumoMinimoTarifa(categoria, consumoTarifaVigencia.getIdVigencia());
	}

}
