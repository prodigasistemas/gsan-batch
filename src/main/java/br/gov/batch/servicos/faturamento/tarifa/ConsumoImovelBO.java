package br.gov.batch.servicos.faturamento.tarifa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.cadastro.EconomiasBO;
import br.gov.batch.servicos.micromedicao.ConsumoBO;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.ConsumoImovelTO;
import br.gov.model.faturamento.ConsumoTarifaVigencia;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;

@Stateless
public class ConsumoImovelBO {

	private List<ConsumoImovelTO> consumoImoveisTO;
	private ConsumoImovelTO consumoImovelTO;

	@EJB 
	private ConsumoBO consumoBO;
	
	@EJB
	private EconomiasBO economiasBO;
	
	@EJB
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;
	
	private Integer consumoPorEconomia;
	
	public List<ConsumoImovelTO> distribuirConsumoPorCategoria(ConsumoHistorico consumoHistorico, ConsumoTarifaVigencia consumoTarifaVigencia) {
		Collection<ICategoria> categorias = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasPorImovel(consumoHistorico.getImovel().getId());
		
		initConsumoImoveisTOList();
		
		for (ICategoria categoria : categorias) {
			addConsumoImovelTO(consumoHistorico, consumoTarifaVigencia, categoria);
		}	
		
		return getConsumoImoveisTO();
	}
	
	public void addConsumoImovelTO(ConsumoHistorico consumoHistorico, ConsumoTarifaVigencia consumoTarifaVigencia, ICategoria categoria) {
		consumoImovelTO = new ConsumoImovelTO();
		Imovel imovel = consumoHistorico.getImovel();
		
		int qtdTotalEconomias = economiasBO.getQuantidadeTotalEconomias(imovel.getId());
		int consumoPorEconomia = getConsumoPorEconomia(consumoHistorico, qtdTotalEconomias);
		
		int numeroConsumoMinimo = consumoBO.getConsumoMinimoTarifaPorCategoria(consumoTarifaVigencia.getId(), categoria);
		
		setConsumoEconomiaCategoria(consumoHistorico, numeroConsumoMinimo, consumoPorEconomia);
		setConsumoExcedenteCategoria(consumoHistorico, numeroConsumoMinimo, qtdTotalEconomias, consumoPorEconomia);
		
		addConsumoImovelTO(consumoImovelTO);
	}

	public int calculoExcessoImovel(ConsumoHistorico consumoHistorico) {
		Imovel imovel = consumoHistorico.getImovel(); 
		
		int consumoMinimoImovel = consumoBO.consumoMinimoLigacao(imovel.getId());
		
		int calculoExcessoImovel = consumoHistorico.getNumeroConsumoFaturadoMes() - consumoMinimoImovel;
		return calculoExcessoImovel;
	}
	
	public List<ConsumoImovelTO> getConsumoImoveisTO() {
		if(consumoImoveisTO == null) {
			initConsumoImoveisTOList();
		}
		
		return consumoImoveisTO;
	}
	
	private void setConsumoExcedenteCategoria(ConsumoHistorico consumoHistorico, int numeroConsumoMinimo, int qtdTotalEconomias, int consumoPorEconomia) {
		if(calculoExcessoImovel(consumoHistorico) > 0) {
			consumoImovelTO.setConsumoExcedenteCategoria(calculoExcessoEconomia(consumoHistorico, qtdTotalEconomias));
		} else {
			consumoImovelTO.setConsumoExcedenteCategoria(consumoPorEconomia - numeroConsumoMinimo);	
		}
	}

	private void setConsumoEconomiaCategoria(ConsumoHistorico consumoHistorico, int numeroConsumoMinimo, int consumoPorEconomia) {
		if(calculoExcessoImovel(consumoHistorico) > 0 || consumoPorEconomia > numeroConsumoMinimo) {
			consumoImovelTO.setConsumoEconomiaCategoria(numeroConsumoMinimo);
		} else {
			consumoImovelTO.setConsumoEconomiaCategoria(consumoPorEconomia);
		}
	}
	
	private int calculoExcessoEconomia(ConsumoHistorico consumoHistorico, int qtdTotalEconomias) {
		return calculoExcessoImovel(consumoHistorico) / qtdTotalEconomias;
	}

	private int getConsumoPorEconomia(ConsumoHistorico consumoHistorico, int qtdTotalEconomias) {
		this.consumoPorEconomia = 0;
		
		if(qtdTotalEconomias != 0) {
			this.consumoPorEconomia = consumoHistorico.getNumeroConsumoFaturadoMes() / qtdTotalEconomias;
		}
		
		return this.consumoPorEconomia;
	}
	
	public void initConsumoImoveisTOList() {
		consumoImoveisTO = new ArrayList<ConsumoImovelTO>();
	}
	
	private void addConsumoImovelTO(ConsumoImovelTO consumoImovelTO) {
		getConsumoImoveisTO().add(consumoImovelTO);
	}
}
