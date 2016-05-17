package br.gov.batch.servicos.faturamento;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.batch.servicos.faturamento.to.VolumeMedioAguaEsgotoTO;
import br.gov.batch.servicos.micromedicao.ConsumoBO;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaRepositorio;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;

public class AguaEsgotoBOTest {

	@InjectMocks
	private AguaEsgotoBO aguaEsgotoBO;
	
	@Mock
	private SistemaParametros sistemaParametrosMock;
	
	@Mock
	private ConsumoHistoricoRepositorio consumoHistoricoRepositorioMock;
	
	@Mock
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorioMock;
	
	@Mock
	private ConsumoBO consumoBOMock;
	
	@Mock
	private ConsumoTarifaRepositorio consumoTarifaRepositorioMock;
	
	@Before
	public void setup() {
		aguaEsgotoBO = new AguaEsgotoBO();
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void volumeMedioComConsumoHistoricoSemMesRetroagido() {
		mockParametros();
		mockConsumoHistoricoSemMesRetroagido();
		
		VolumeMedioAguaEsgotoTO volumeMedioAguaEsgotoTO = aguaEsgotoBO.obterVolumeMedioAguaEsgoto(1, 201408, 1);
		
		assertEquals(27, volumeMedioAguaEsgotoTO.getConsumoMedio().intValue());
		assertEquals(6, volumeMedioAguaEsgotoTO.getQuantidadeMesesConsiderados().intValue());
	}
	
	@Test
	public void volumeMedioComConsumoHistoricoComMesRetroagido() {
		mockParametros();
		mockConsumoHistoricoComMesRetroagido();
		
		VolumeMedioAguaEsgotoTO volumeMedioAguaEsgotoTO = aguaEsgotoBO.obterVolumeMedioAguaEsgoto(1, 201408, 1);
		
		assertEquals(27, volumeMedioAguaEsgotoTO.getConsumoMedio().intValue());
		assertEquals(4, volumeMedioAguaEsgotoTO.getQuantidadeMesesConsiderados().intValue());
	}
	
	@Test
	public void volumeMedioComListaConsumoHistoricoNula() {
		mockParametros();
		mockConsumoHistoricoNulo();
		mockCategoria();
		mockConsumoTarifa();
		mockConsumoMinimoPorLigacao();
		
		VolumeMedioAguaEsgotoTO volumeMedioAguaEsgotoTO = aguaEsgotoBO.obterVolumeMedioAguaEsgoto(1, 201408, 1);
		
		assertEquals(30, volumeMedioAguaEsgotoTO.getConsumoMedio().intValue());
		assertEquals(1, volumeMedioAguaEsgotoTO.getQuantidadeMesesConsiderados().intValue());
	}
	
	@Test
	public void volumeMedioComListaConsumoHistoricoVazia() {
		mockParametros();
		mockConsumoHistoricoVazio();
		mockCategoria();
		mockConsumoTarifa();
		mockConsumoMinimoPorLigacao();
		
		VolumeMedioAguaEsgotoTO volumeMedioAguaEsgotoTO = aguaEsgotoBO.obterVolumeMedioAguaEsgoto(1, 201408, 1);
		
		assertEquals(30, volumeMedioAguaEsgotoTO.getConsumoMedio().intValue());
		assertEquals(1, volumeMedioAguaEsgotoTO.getQuantidadeMesesConsiderados().intValue());
	}
	
	private void mockParametros() {
		when(sistemaParametrosMock.getMesesMediaConsumo()).thenReturn(Short.valueOf("6"));
		when(sistemaParametrosMock.getNumeroMesesMaximoCalculoMedia()).thenReturn(Short.valueOf("24"));
		when(sistemaParametrosMock.getIndicadorTarifaCategoria()).thenReturn(Short.valueOf("1"));
	}
	
	private void mockConsumoHistoricoSemMesRetroagido() {
		when(consumoHistoricoRepositorioMock.obterConsumoMedio(1, 201201, 201407, 1)).thenReturn(getListaConsumoHistoricoSemMesRetroagido());
	}
	
	private void mockConsumoHistoricoComMesRetroagido() {
		when(consumoHistoricoRepositorioMock.obterConsumoMedio(1, 201201, 201407, 1)).thenReturn(getListaConsumoHistoricoComMesRetroagido());
	}
	
	private void mockConsumoHistoricoNulo() {
		when(consumoHistoricoRepositorioMock.obterConsumoMedio(1, 201201, 201407, 1)).thenReturn(null);
	}
	
	private void mockConsumoHistoricoVazio() {
		when(consumoHistoricoRepositorioMock.obterConsumoMedio(1, 201201, 201407, 1)).thenReturn(new ArrayList<ConsumoHistorico>());
	}
	
	private void mockCategoria() {
		when(imovelSubcategoriaRepositorioMock.buscarQuantidadeEconomiasPorImovel(1)).thenReturn(null);
	}
	
	private void mockConsumoTarifa() {
		when(consumoTarifaRepositorioMock.consumoTarifaDoImovel(1)).thenReturn(1);
	}
	
	private void mockConsumoMinimoPorLigacao() {
		when(consumoBOMock.obterConsumoMinimoLigacaoPorCategoria(1, 1, null)).thenReturn(30);
	}
	
	private List<ConsumoHistorico> getListaConsumoHistoricoSemMesRetroagido() {
		List<ConsumoHistorico> consumos = new ArrayList<ConsumoHistorico>();
		
		ConsumoHistorico c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(25);
		c.setReferenciaFaturamento(201407);
		consumos.add(c);
		
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(30);
		c.setReferenciaFaturamento(201406);
		consumos.add(c);
		
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(27);
		c.setReferenciaFaturamento(201405);
		consumos.add(c);
		
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(28);
		c.setReferenciaFaturamento(201404);
		consumos.add(c);
		
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(27);
		c.setReferenciaFaturamento(201403);
		consumos.add(c);
		
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(29);
		c.setReferenciaFaturamento(201402);
		consumos.add(c);
		
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(30);
		c.setReferenciaFaturamento(201401);
		consumos.add(c);
		
		return consumos;
	}
	
	private List<ConsumoHistorico> getListaConsumoHistoricoComMesRetroagido() {
		List<ConsumoHistorico> consumos = new ArrayList<ConsumoHistorico>();
		
		ConsumoHistorico c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(25);
		c.setReferenciaFaturamento(201407);
		consumos.add(c);
		
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(30);
		c.setReferenciaFaturamento(201406);
		consumos.add(c);
		
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(28);
		c.setReferenciaFaturamento(201404);
		consumos.add(c);
		
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(27);
		c.setReferenciaFaturamento(201403);
		consumos.add(c);
		
		return consumos;
	}

}
