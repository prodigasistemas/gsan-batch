package br.gov.batch.servicos.faturamento.tarifa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.joda.time.DateTime;
import org.joda.time.Days;

import br.gov.batch.servicos.micromedicao.ConsumoBO;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.servicos.to.ConsumoImovelCategoriaTO;
import br.gov.servicos.to.ConsumoTarifaCategoriaTO;
import br.gov.servicos.to.ConsumoTarifaFaixaTO;
import br.gov.servicos.to.TarifasVigenciaTO;

@Stateless
public class ConsumoImovelCategoriaBO {

	private List<ConsumoImovelCategoriaTO> consumoImoveisCategoriaTO;
	private ConsumoImovelCategoriaTO consumoImovelCategoriaTO;

	@EJB 
	private ConsumoBO consumoBO;
	
	public List<ConsumoImovelCategoriaTO> getConsumoImoveisCategoriaTO(ConsumoHistorico consumoHistorico, Date dataLeituraAnterior, Date dataLeituraAtual,
																		Collection<ICategoria> categorias) {
		initConsumoImoveisCategoriaTO();
		
		distribuirConsumoPorCategoria(consumoHistorico, dataLeituraAnterior, dataLeituraAtual, categorias);
		distribuirConsumoPorFaixa();
		
		return getConsumoImoveisCategoriaTO();
	}
	
	public List<ConsumoImovelCategoriaTO> getConsumoImoveisCategoriaTO(ConsumoHistorico consumoHistorico, Date dataLeituraAnterior, Date dataLeituraAtual) {
		initConsumoImoveisCategoriaTO();
		
		distribuirConsumoPorCategoria(consumoHistorico, dataLeituraAnterior, dataLeituraAtual);
		distribuirConsumoPorFaixa();
		
		return getConsumoImoveisCategoriaTO();		
	}
	
	public List<ConsumoImovelCategoriaTO> distribuirConsumoPorCategoria(ConsumoHistorico consumoHistorico, Date dataLeituraAnterior, Date dataLeituraAtual) {
		Collection<ICategoria> categorias = consumoBO.buscarQuantidadeEconomiasPorImovel(consumoHistorico.getImovel().getId());
		
		return distribuirConsumoPorCategoria(consumoHistorico, dataLeituraAnterior, dataLeituraAtual, categorias);
	}
	
	public List<ConsumoImovelCategoriaTO> distribuirConsumoPorCategoria(ConsumoHistorico consumoHistorico, Date dataLeituraAnterior, Date dataLeituraAtual, 
																		Collection<ICategoria> categorias) {
		for (ICategoria categoria : categorias) {
			addConsumoImovelCategoriaTO(consumoHistorico, dataLeituraAnterior, dataLeituraAtual, categoria);
		}	
		
		return getConsumoImoveisCategoriaTO();
	}
	
	public List<ConsumoImovelCategoriaTO> distribuirConsumoPorFaixa() {
		carregarTarifasConsumoImovelCategoria();
		
		for (ConsumoImovelCategoriaTO consumoImovel : getConsumoImoveisCategoriaTO()) {
			distribuirConsumoFaixaPorCategoria(consumoImovel);
		}
		
		return getConsumoImoveisCategoriaTO();
	}
	
	public BigDecimal getValorTotalConsumoImovel(ConsumoHistorico consumoHistorico, Date dataLeituraAnterior, Date dataLeituraAtual, Collection<ICategoria> categorias) {
		List<ConsumoImovelCategoriaTO> consumoImoveisCategoria = getConsumoImoveisCategoriaTO(consumoHistorico, dataLeituraAnterior, dataLeituraAtual, categorias);
		
		return getValorTotalConsumoImovel(consumoImoveisCategoria);
	}
	
	public BigDecimal getValorTotalConsumoImovel(ConsumoHistorico consumoHistorico, Date dataLeituraAnterior, Date dataLeituraAtual) {
		List<ConsumoImovelCategoriaTO> consumoImoveisCategoria = getConsumoImoveisCategoriaTO(consumoHistorico, dataLeituraAnterior, dataLeituraAtual);
		
		return getValorTotalConsumoImovel(consumoImoveisCategoria);
	}
	
	private BigDecimal getValorTotalConsumoImovel(List<ConsumoImovelCategoriaTO> consumoImoveisCategoria) {
		BigDecimal valorTotalConsumo = BigDecimal.ZERO;
		for (ConsumoImovelCategoriaTO consumoImovelCategoriaTO : consumoImoveisCategoria) {
			valorTotalConsumo = valorTotalConsumo.add(getValorConsumoTotal(consumoImovelCategoriaTO));
		}
		
		return valorTotalConsumo;
	}
	
	public BigDecimal getValorConsumoTotal(ConsumoImovelCategoriaTO consumoImovelCategoriaTO) {
		BigDecimal valorConsumo = BigDecimal.ZERO;
		
		calcularDiasProporcionaisPorTarifa(consumoImovelCategoriaTO);
		
		for (TarifasVigenciaTO vigencia : consumoImovelCategoriaTO.getTabelaTarifas()) {
			BigDecimal valorConsumoMinimoTarifa = getValorConsumoMinimo(consumoImovelCategoriaTO, vigencia.getDataVigencia());

			BigDecimal valorConsumoVigencia = vigencia.getValorConsumoTotal(consumoImovelCategoriaTO.getQtdEconomias(), valorConsumoMinimoTarifa);
			
			BigDecimal percentualDias = vigencia.getPercentualDiasProporcionais(consumoImovelCategoriaTO.getQtdDiasConsumoTarifa())
												.setScale(4, RoundingMode.FLOOR);
			
			valorConsumo = valorConsumo.add(valorConsumoVigencia.multiply(percentualDias).setScale(2, RoundingMode.HALF_DOWN)); 
		}
		
		return valorConsumo;
	}
	
	private BigDecimal getValorConsumoMinimo(ConsumoImovelCategoriaTO consumoImovelCategoriaTO, Date dataVigencia) {
		BigDecimal valorConsumoMinimoTarifa = null;
		for (ConsumoTarifaCategoriaTO consumoTarifaCategoriaTO : consumoImovelCategoriaTO.getConsumoTarifasCategoria()) {
			if(consumoTarifaCategoriaTO.possuiVigencia(dataVigencia)) {
				valorConsumoMinimoTarifa = consumoTarifaCategoriaTO.getValorConsumoMinimo(); 
			}
		}
		
		return valorConsumoMinimoTarifa;
	}

	private void calcularDiasProporcionaisPorTarifa(ConsumoImovelCategoriaTO consumoImovelCategoriaTO) {
		DateTime dataLeituraAnterior = new DateTime(consumoImovelCategoriaTO.getDataAnterior());
		DateTime dataAtual = new DateTime(consumoImovelCategoriaTO.getDataAtual());
		
		List<TarifasVigenciaTO> tabelaTarifas = consumoImovelCategoriaTO.getTabelaTarifas();

		DateTime dataVigenciaAnterior = null;
		TarifasVigenciaTO tabelaAnterior = null;
		
		Collections.sort(tabelaTarifas, (o1, o2) -> o1.compareTo(o2));

		for (TarifasVigenciaTO tabela : tabelaTarifas) {
			DateTime dataVigencia = new DateTime(tabela.getDataVigencia());
			
			if(dataVigenciaAnterior != null) {
				int qtdDiasProporcionais = getQtdDiasProporcionais(dataLeituraAnterior, dataVigenciaAnterior, dataVigencia);
				tabelaAnterior.setQtdDiasProporcionais(qtdDiasProporcionais);
			}
			
			dataVigenciaAnterior = dataVigencia;
			tabelaAnterior = tabela;
		}
		
		int qtdDiasProporcionais = getQtdDiasProporcionais(dataLeituraAnterior, dataVigenciaAnterior, dataAtual);
		tabelaAnterior.setQtdDiasProporcionais(qtdDiasProporcionais);
	}

	private int getQtdDiasProporcionais(DateTime dataLeituraAnterior, DateTime dataVigenciaAnterior, DateTime dataVigencia) {
		DateTime dataInicio = dataVigenciaAnterior;
		
		if(dataVigenciaAnterior.isBefore(dataLeituraAnterior)) {
			dataInicio = dataLeituraAnterior;
		}
		
		return Days.daysBetween(dataInicio, dataVigencia).getDays();
	}

	private List<ConsumoImovelCategoriaTO> getConsumoImoveisCategoriaTO() {
		if(consumoImoveisCategoriaTO == null) {
			initConsumoImoveisCategoriaTO();
		}
		
		return consumoImoveisCategoriaTO;
	}
	
	private void initConsumoImoveisCategoriaTO() {
		consumoImoveisCategoriaTO = new ArrayList<ConsumoImovelCategoriaTO>();
	}
	
	private void addConsumoImovelCategoriaTO(ConsumoHistorico consumoHistorico, Date dataLeituraAnterior, Date dataLeituraAtual, ICategoria categoria) {
		consumoImovelCategoriaTO = new ConsumoImovelCategoriaTO();
		Imovel imovel = consumoHistorico.getImovel();
		
		List<ConsumoTarifaCategoriaTO> consumoTarifasCategoria = consumoBO.getConsumoTarifasCategoria(imovel, dataLeituraAnterior, dataLeituraAtual, categoria);

		int qtdTotalEconomias = consumoBO.getQuantidadeTotalEconomias(imovel.getId());
		int consumoPorEconomia = getConsumoPorEconomia(consumoHistorico, qtdTotalEconomias);
		int qtdEconomiasCategoria = consumoBO.getQuantidadeEconomiasPorCategoria(categoria);
		
		consumoImovelCategoriaTO.setCategoria(categoria);
		consumoImovelCategoriaTO.setQtdEconomias(qtdEconomiasCategoria);
		consumoImovelCategoriaTO.setConsumoTarifasCategoria(consumoTarifasCategoria);
		consumoImovelCategoriaTO.setDataAnterior(dataLeituraAnterior);
		consumoImovelCategoriaTO.setDataAtual(dataLeituraAtual);

		int numeroConsumoMinimo = consumoBO.getConsumoMinimoTarifaPorCategoria(consumoTarifasCategoria, categoria);
		
		setConsumoEconomiaCategoria(consumoHistorico, numeroConsumoMinimo, consumoPorEconomia);
		setConsumoExcedenteCategoria(consumoHistorico, numeroConsumoMinimo, qtdTotalEconomias, consumoPorEconomia);
		
		addConsumoImovelCategoriaTO(consumoImovelCategoriaTO);
	}

	private int calculoExcessoImovel(ConsumoHistorico consumoHistorico) {
		Imovel imovel = consumoHistorico.getImovel(); 
		
		int consumoMinimoImovel = consumoBO.consumoMinimoLigacao(imovel.getId());
		
		int calculoExcessoImovel = consumoHistorico.getNumeroConsumoFaturadoMes() - consumoMinimoImovel;
		return calculoExcessoImovel;
	}
	
	private void setConsumoExcedenteCategoria(ConsumoHistorico consumoHistorico, int numeroConsumoMinimo, int qtdTotalEconomias, int consumoPorEconomia) {
		if(calculoExcessoImovel(consumoHistorico) > 0) {
			consumoImovelCategoriaTO.setConsumoExcedenteCategoria(calculoExcessoEconomia(consumoHistorico, qtdTotalEconomias));
		} else {
			int valor = consumoPorEconomia - numeroConsumoMinimo;
			
			consumoImovelCategoriaTO.setConsumoExcedenteCategoria(valor > 0 ? valor : 0);	
		}
	}

	private void setConsumoEconomiaCategoria(ConsumoHistorico consumoHistorico, int numeroConsumoMinimo, int consumoPorEconomia) {
		if(calculoExcessoImovel(consumoHistorico) > 0 || consumoPorEconomia > numeroConsumoMinimo) {
			consumoImovelCategoriaTO.setConsumoEconomiaCategoria(numeroConsumoMinimo);
		} else {
			consumoImovelCategoriaTO.setConsumoEconomiaCategoria(consumoPorEconomia);
		}
	}
	
	private int calculoExcessoEconomia(ConsumoHistorico consumoHistorico, int qtdTotalEconomias) {
		return calculoExcessoImovel(consumoHistorico) / qtdTotalEconomias;
	}

	private int getConsumoPorEconomia(ConsumoHistorico consumoHistorico, int qtdTotalEconomias) {
		int consumoPorEconomia = 0;
		
		if(qtdTotalEconomias != 0) {
			consumoPorEconomia = consumoHistorico.getNumeroConsumoFaturadoMes() / qtdTotalEconomias;
		}
		
		return consumoPorEconomia;
	}
	
	private void addConsumoImovelCategoriaTO(ConsumoImovelCategoriaTO consumoImovelTO) {
		getConsumoImoveisCategoriaTO().add(consumoImovelTO);
	}

	private void carregarTarifasConsumoImovelCategoria() {
		for (ConsumoImovelCategoriaTO consumoImovelCategoriaTO : getConsumoImoveisCategoriaTO()) {
			List<TarifasVigenciaTO> faixas = consumoBO.obterFaixas(consumoImovelCategoriaTO);
			consumoImovelCategoriaTO.setTabelaTarifas(faixas);
		}
	}

	private void distribuirConsumoFaixaPorCategoria(ConsumoImovelCategoriaTO consumoImovelCategoria) {	
		Integer consumo = consumoImovelCategoria.getConsumoExcedenteCategoria();

		for (TarifasVigenciaTO vigencia : consumoImovelCategoria.getTabelaTarifas()) {
			Collection<ConsumoTarifaFaixaTO> faixas = vigencia.getFaixas().values();
			
			for (ConsumoTarifaFaixaTO faixa : faixas) {
				Integer consumoFinalFaixa = calcularConsumoFaixa(consumo, faixa);
				
				faixa.setConsumo(consumoFinalFaixa);
				
				consumo = consumo - consumoFinalFaixa;
			}
		}
	}

	private Integer calcularConsumoFaixa(Integer consumo, ConsumoTarifaFaixaTO faixa) {
		Integer consumoMaximoFaixa = getValorConsumoFaixa(consumo, faixa);
		
		Integer consumofaixa = 0;
		if(consumo <= consumoMaximoFaixa) {
			consumofaixa = consumo;
		} else {
			consumofaixa = consumo - consumoMaximoFaixa;
		}
		return consumofaixa;
	}

	private int getValorConsumoFaixa(Integer consumo, ConsumoTarifaFaixaTO faixa) {
		int valorConsumoFaixa;
		if(consumo < faixa.getNumeroConsumoFaixaFim()) {
			valorConsumoFaixa = faixa.getConsumoTotalFaixa();
		} else {
			valorConsumoFaixa = faixa.getNumeroConsumoFaixaFim();
		}
		return valorConsumoFaixa;
	}
}
