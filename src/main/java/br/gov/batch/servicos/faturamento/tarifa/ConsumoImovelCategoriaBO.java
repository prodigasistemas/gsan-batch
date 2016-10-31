package br.gov.batch.servicos.faturamento.tarifa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.micromedicao.ConsumoBO;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.ConsumoImovelCategoriaTO;
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
	
	public List<ConsumoImovelCategoriaTO> getConsumoImoveisCategoriaTO(ConsumoHistorico consumoHistorico, MedicaoHistorico medicaoHistorico) {
		initConsumoImoveisCategoriaTO();
		
		distribuirConsumoPorCategoria(consumoHistorico, medicaoHistorico);
		distribuirConsumoPorFaixa(medicaoHistorico);
		
		return getConsumoImoveisCategoriaTO();		
	}
	
	public List<ConsumoImovelCategoriaTO> distribuirConsumoPorCategoria(ConsumoHistorico consumoHistorico, MedicaoHistorico medicaoHistorico) {
		Collection<ICategoria> categorias = consumoBO.buscarQuantidadeEconomiasPorImovel(consumoHistorico.getImovel().getId());
		
		for (ICategoria categoria : categorias) {
			addConsumoImovelCategoriaTO(consumoHistorico, medicaoHistorico, categoria);
		}	
		
		return getConsumoImoveisCategoriaTO();
	}
	
	public List<ConsumoImovelCategoriaTO> distribuirConsumoPorFaixa(MedicaoHistorico medicaoHistorico) {
		carregarTarifasConsumoImovelCategoria(medicaoHistorico);
		
		for (ConsumoImovelCategoriaTO consumoImovel : getConsumoImoveisCategoriaTO()) {
			distribuirConsumoFaixaPorCategoria(consumoImovel, medicaoHistorico);
		}
		
		return getConsumoImoveisCategoriaTO();
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
	
	private void addConsumoImovelCategoriaTO(ConsumoHistorico consumoHistorico, MedicaoHistorico medicaoHistorico,  ICategoria categoria) {
		consumoImovelCategoriaTO = new ConsumoImovelCategoriaTO();
		Imovel imovel = consumoHistorico.getImovel();
		
		List<ConsumoTarifaCategoriaTO> consumoTarifasCategoria = consumoBO.getConsumoTarifasCategoria(imovel, medicaoHistorico, categoria);

		int qtdTotalEconomias = consumoBO.getQuantidadeTotalEconomias(imovel.getId());
		int consumoPorEconomia = getConsumoPorEconomia(consumoHistorico, qtdTotalEconomias);
		int numeroConsumoMinimo = consumoBO.getConsumoMinimoTarifaPorCategoria(consumoTarifasCategoria.get(0).getConsumotarifaVigencia().getId(), categoria);
		
		int qtdEconomiasCategoria = consumoBO.getQuantidadeEconomiasPorCategoria(categoria);
		
		consumoImovelCategoriaTO.setQtdEconomias(qtdEconomiasCategoria);
		consumoImovelCategoriaTO.setConsumoTarifasCategoria(consumoTarifasCategoria);
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

	private void carregarTarifasConsumoImovelCategoria(MedicaoHistorico medicaoHistorico) {
		for (ConsumoImovelCategoriaTO consumoImovelCategoriaTO : getConsumoImoveisCategoriaTO()) {
			List<ConsumoTarifaFaixaTO> faixas = consumoBO.obterFaixas(consumoImovelCategoriaTO, medicaoHistorico);
			consumoImovelCategoriaTO.setFaixas(faixas);
		}
	}

	private void distribuirConsumoFaixaPorCategoria(ConsumoImovelCategoriaTO consumoImovelCategoria, MedicaoHistorico medicaoHistorico) {	
		Integer consumo = consumoImovelCategoria.getConsumoExcedenteCategoria();
		for (ConsumoTarifaFaixaTO faixa : consumoImovelCategoria.getFaixas()) {
			Integer consumoFinalFaixa = calcularConsumoFaixa(consumo, faixa);
			
			consumoImovelCategoria.addFaixaConsumo(faixa, consumoFinalFaixa);
			consumo = consumo - consumoFinalFaixa;
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
