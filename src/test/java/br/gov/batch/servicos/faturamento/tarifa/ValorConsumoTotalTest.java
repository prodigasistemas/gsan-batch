package br.gov.batch.servicos.faturamento.tarifa;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import br.gov.model.cadastro.Categoria;
import br.gov.model.cadastro.ICategoria;
import br.gov.servicos.to.ConsumoImovelCategoriaTO;
import br.gov.servicos.to.ConsumoTarifaCategoriaTO;
import br.gov.servicos.to.ConsumoTarifaFaixaTO;
import br.gov.servicos.to.ImovelSubcategoriaTO;
import br.gov.servicos.to.TarifasVigenciaTO;

public class ValorConsumoTotalTest {
	
	private ConsumoImovelCategoriaBO bo;
	
	private static int idSubcategoriaC1 = 5;
	
	private static int idSubcategoriaR3 = 3;
	
	private Date data_2016_01_23 = null;
	private Date data_2016_08_25 = null;
	private Date data_2016_09_26 = null;
	private int consumoFaixa = 7;
	
	@Before
	public void init(){
		bo = new ConsumoImovelCategoriaBO();
		
		data_2016_01_23 = new DateTime(2016, 1, 23, 0, 0).toDate();
		data_2016_08_25 = new DateTime(2016, 8, 25, 0, 0).toDate();
		data_2016_09_26 = new DateTime(2016, 9, 26, 0, 0).toDate();
	}
	
	@Test
	public void calcularValorConsumoCategoriaComercialUmaEconomia(){
		List<ConsumoImovelCategoriaTO> consumoImoveisCategoria = buildConsumosPorCategoria(buildConsumoComercial());
		assertEquals(new BigDecimal(94.02).setScale(2, BigDecimal.ROUND_HALF_UP), bo.getValorTotalConsumoImovel(consumoImoveisCategoria));
	}
	
	@Test
	public void calcularValorConsumoCategoriaResidencialTresEconomias(){
		List<ConsumoImovelCategoriaTO> consumoImoveisCategoria = buildConsumosPorCategoria(buildConsumoResidencial());
		assertEquals(new BigDecimal(187.32).setScale(2, BigDecimal.ROUND_HALF_UP), bo.getValorTotalConsumoImovel(consumoImoveisCategoria));
	}
	
	@Test
	public void calcularValorConsumoCategoriaResidencialEComercialUmaEcoComercialTresEcoResidencial(){
		List<ConsumoImovelCategoriaTO> consumoImoveisCategoria = buildConsumosPorCategoria(buildConsumoComercial(), buildConsumoResidencial());
		assertEquals(new BigDecimal(281.34).setScale(2, BigDecimal.ROUND_HALF_UP), bo.getValorTotalConsumoImovel(consumoImoveisCategoria));
	}
	
	private List<ConsumoImovelCategoriaTO> buildConsumosPorCategoria(ConsumoImovelCategoriaTO... consumos){
		List<ConsumoImovelCategoriaTO> lista = new ArrayList<>();
		
		for (ConsumoImovelCategoriaTO consumo : consumos) {
			lista.add(consumo);
		}
		
		return lista;
	}
	
	private ConsumoImovelCategoriaTO buildConsumoResidencial(){
		ConsumoImovelCategoriaTO consumoImovel = new ConsumoImovelCategoriaTO();
		consumoImovel.setConsumoEconomiaCategoria(10);
		consumoImovel.setConsumoExcedenteCategoria(7);
		consumoImovel.setDataAnterior(data_2016_08_25);
		consumoImovel.setDataAtual(data_2016_09_26);
		consumoImovel.setQtdEconomias(3);
		consumoImovel.setCategoria(residencialR3());
		
		consumoImovel.setConsumoTarifasCategoria(buildTarifasConsumoResidencial());
		consumoImovel.setVigencias(buildVigenciaTarifaResidencial());
		
		return consumoImovel;
	}

	private ConsumoImovelCategoriaTO buildConsumoComercial(){
		ConsumoImovelCategoriaTO consumoComercial = new ConsumoImovelCategoriaTO();
		consumoComercial.setConsumoEconomiaCategoria(10);
		consumoComercial.setConsumoExcedenteCategoria(7);
		consumoComercial.setDataAnterior(data_2016_08_25);
		consumoComercial.setDataAtual(data_2016_09_26);
		consumoComercial.setQtdEconomias(1);
		consumoComercial.setCategoria(comercialC1());
		
		consumoComercial.setConsumoTarifasCategoria(buildTarifasConsumoComercial());
		consumoComercial.setVigencias(buildVigenciaTarifaComercial());
		
		return consumoComercial;
	}
	
	private List<ConsumoTarifaCategoriaTO> buildTarifasConsumoComercial(){
		List<ConsumoTarifaCategoriaTO> tarifas = new ArrayList<>();
		ConsumoTarifaCategoriaTO tarifa = new ConsumoTarifaCategoriaTO();
		tarifa.setConsumoMinimo(10);
		tarifa.setDataVigencia(data_2016_01_23);
		tarifa.setDescricaoCategoria(comercial().getCategoriaDescricao());
		tarifa.setIdCategoria(comercial().getCategoria().getId());
		tarifa.setValorTarifaMinima(new BigDecimal(50.20));
		tarifa.setIdTarifa(1);
		tarifa.setIdVigencia(8);
		
		tarifas.add(tarifa);
		
		return tarifas;
	}

	private List<ConsumoTarifaCategoriaTO> buildTarifasConsumoResidencial(){
		List<ConsumoTarifaCategoriaTO> tarifas = new ArrayList<>();
		ConsumoTarifaCategoriaTO tarifa = new ConsumoTarifaCategoriaTO();
		tarifa.setConsumoMinimo(10);
		tarifa.setDataVigencia(data_2016_01_23);
		tarifa.setDescricaoCategoria(comercial().getCategoriaDescricao());
		tarifa.setIdCategoria(comercial().getCategoria().getId());
		tarifa.setValorTarifaMinima(new BigDecimal(16.80));
		tarifa.setIdTarifa(1);
		tarifa.setIdVigencia(8);
		
		tarifas.add(tarifa);
		
		return tarifas;
	}

	private List<TarifasVigenciaTO> buildVigenciaTarifaComercial(){
		List<TarifasVigenciaTO> vigencias = new ArrayList<>();
		
		TarifasVigenciaTO vigencia = new TarifasVigenciaTO(data_2016_01_23, buildFaixasConsumoComercial());
		
		vigencias.add(vigencia);
		
		return vigencias;
	}
	
	private List<ConsumoTarifaFaixaTO> buildFaixasConsumoComercial(){
		List<ConsumoTarifaFaixaTO> faixas = new ArrayList<>();
		
		ConsumoTarifaFaixaTO faixa = new ConsumoTarifaFaixaTO();
		faixa.setNumeroConsumoFaixaFim(999999);
		faixa.setNumeroConsumoFaixaInicio(11);
		faixa.setValorTarifa(new BigDecimal(6.26));
		faixa.setIdConsumoTarifa(1);
		faixa.setConsumo(consumoFaixa);
		
		faixas.add(faixa);
		
		return faixas;
	}
	
	private List<TarifasVigenciaTO> buildVigenciaTarifaResidencial(){
		List<TarifasVigenciaTO> vigencias = new ArrayList<>();
		
		TarifasVigenciaTO vigencia = new TarifasVigenciaTO(data_2016_01_23, buildFaixasConsumoResidencial());
		
		vigencias.add(vigencia);
		
		return vigencias;
	}
	
	private List<ConsumoTarifaFaixaTO> buildFaixasConsumoResidencial(){
		List<ConsumoTarifaFaixaTO> faixas = new ArrayList<>();
		
		ConsumoTarifaFaixaTO faixa = new ConsumoTarifaFaixaTO();
		faixa.setNumeroConsumoFaixaFim(999999);
		faixa.setNumeroConsumoFaixaInicio(51);
		faixa.setValorTarifa(new BigDecimal(6.52));
		faixa.setIdConsumoTarifa(1);
		faixa.setConsumo(consumoFaixa);
		
		faixas.add(faixa);
		
		return faixas;
	}
	
	private ICategoria comercial(){
		Categoria comercial = new Categoria();
		comercial.setId(2);
		comercial.setDescricao("COMERCIAL");

		return comercial;
	}
	
	private ICategoria residencial(){
		Categoria comercial = new Categoria();
		comercial.setId(1);
		comercial.setDescricao("RESIDENCIAL");
		
		return comercial;
	}
	
	private ICategoria comercialC1(){
		ImovelSubcategoriaTO c1 = new ImovelSubcategoriaTO(comercial().getId(), idSubcategoriaC1);
		c1.setCategoriaDescricao(comercial().getCategoriaDescricao());
		c1.setSubcategoriaDescricao("C1");
		c1.setSubcategoriaCodigo(idSubcategoriaC1);
		c1.setSubcategoriaQuantidadeEconomias(1L);
		
		return c1;
	}
	
	private ICategoria residencialR3(){
		ImovelSubcategoriaTO c1 = new ImovelSubcategoriaTO(residencial().getId(), idSubcategoriaR3);
		c1.setCategoriaDescricao(residencial().getCategoriaDescricao());
		c1.setSubcategoriaDescricao("R3");
		c1.setSubcategoriaCodigo(idSubcategoriaR3);
		c1.setSubcategoriaQuantidadeEconomias(3L);
		
		return c1;
	}	
}
