package br.gov.batch.servicos.faturamento.tarifa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.micromedicao.ConsumoBO;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.ConsumoImovelCategoriaTO;
import br.gov.model.faturamento.tarifas.TabelaTarifas;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.servicos.to.ConsumoTarifaCategoriaTO;
import br.gov.servicos.to.ConsumoTarifaFaixaTO;

@Stateless
public class ConsumoImovelCategoriaBO {

	private List<ConsumoImovelCategoriaTO> consumoImoveisCategoriaTO;
	private ConsumoImovelCategoriaTO consumoImovelCategoriaTO;

	@EJB 
	private ConsumoBO consumoBO;
	
	public List<ConsumoImovelCategoriaTO> getConsumoImoveisCategoriaTO(ConsumoHistorico consumoHistorico, int referencia) {
		initConsumoImoveisCategoriaTO();
		
		distribuirConsumoPorCategoria(consumoHistorico, referencia);
		distribuirConsumoPorFaixa(referencia);
		
		return getConsumoImoveisCategoriaTO();		
	}
	
	public List<ConsumoImovelCategoriaTO> distribuirConsumoPorCategoria(ConsumoHistorico consumoHistorico, int referencia) {
		Collection<ICategoria> categorias = consumoBO.buscarQuantidadeEconomiasPorImovel(consumoHistorico.getImovel().getId());
		
		for (ICategoria categoria : categorias) {
			addConsumoImovelCategoriaTO(consumoHistorico, referencia, categoria);
		}	
		
		return getConsumoImoveisCategoriaTO();
	}
	
	public List<ConsumoImovelCategoriaTO> distribuirConsumoPorFaixa(int referencia) {
		carregarTarifasConsumoImovelCategoria(referencia);
		
		for (ConsumoImovelCategoriaTO consumoImovel : getConsumoImoveisCategoriaTO()) {
			distribuirConsumoFaixaPorCategoria(consumoImovel);
		}
		
		return getConsumoImoveisCategoriaTO();
	}
	
	public BigDecimal getValorTotalConsumoImovel(ConsumoHistorico consumoHistorico, MedicaoHistorico medicaoHistorico, int referencia) {
		List<ConsumoImovelCategoriaTO> consumoImoveisCategoria = getConsumoImoveisCategoriaTO(consumoHistorico, referencia);
		
		BigDecimal valorTotalConsumo = BigDecimal.ZERO;
		for (ConsumoImovelCategoriaTO consumoImovelCategoriaTO : consumoImoveisCategoria) {
			valorTotalConsumo = valorTotalConsumo.add(consumoImovelCategoriaTO.getValorConsumoTotal(medicaoHistorico));
		}
		
		return valorTotalConsumo;
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
	
	private void addConsumoImovelCategoriaTO(ConsumoHistorico consumoHistorico, int referencia,  ICategoria categoria) {
		consumoImovelCategoriaTO = new ConsumoImovelCategoriaTO();
		Imovel imovel = consumoHistorico.getImovel();
		
		List<ConsumoTarifaCategoriaTO> consumoTarifasCategoria = consumoBO.getConsumoTarifasCategoria(imovel, referencia, categoria);

		int qtdTotalEconomias = consumoBO.getQuantidadeTotalEconomias(imovel.getId());
		int consumoPorEconomia = getConsumoPorEconomia(consumoHistorico, qtdTotalEconomias);
		int qtdEconomiasCategoria = consumoBO.getQuantidadeEconomiasPorCategoria(categoria);
		
		consumoImovelCategoriaTO.setQtdEconomias(qtdEconomiasCategoria);
		consumoImovelCategoriaTO.setConsumoTarifasCategoria(consumoTarifasCategoria);

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

	private void carregarTarifasConsumoImovelCategoria(int referencia) {
		for (ConsumoImovelCategoriaTO consumoImovelCategoriaTO : getConsumoImoveisCategoriaTO()) {
			List<TabelaTarifas> faixas = consumoBO.obterFaixas(consumoImovelCategoriaTO);
			consumoImovelCategoriaTO.setTabelaTarifas(faixas);
		}
	}

	private void distribuirConsumoFaixaPorCategoria(ConsumoImovelCategoriaTO consumoImovelCategoria) {	
		Integer consumo = consumoImovelCategoria.getConsumoExcedenteCategoria();

		List<TabelaTarifas> tabelaTarifasCalculadas = new ArrayList<TabelaTarifas>();
		for (TabelaTarifas tabelaTarifas : consumoImovelCategoria.getTabelaTarifas()) {
			for (ConsumoTarifaFaixaTO faixa : tabelaTarifas.getFaixas()) {
				Integer consumoFinalFaixa = calcularConsumoFaixa(consumo, faixa);
				
				tabelaTarifas.addFaixaConsumo(faixa, consumoFinalFaixa);
				tabelaTarifasCalculadas.add(tabelaTarifas);
				
				consumo = consumo - consumoFinalFaixa;
			}
		}
		
		consumoImovelCategoria.setTabelaTarifas(tabelaTarifasCalculadas);
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
