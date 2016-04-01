package br.gov.batch.servicos.faturamento;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.servicos.faturamento.to.VolumeMedioAguaEsgotoTO;
import br.gov.batch.servicos.micromedicao.ConsumoBO;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaRepositorio;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;

@RunWith(EasyMockRunner.class)
public class AguaEsgotoBOTest {

	@TestSubject
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
		expect(sistemaParametrosMock.getMesesMediaConsumo()).andReturn(Short.valueOf("6")).times(3);
		expect(sistemaParametrosMock.getNumeroMesesMaximoCalculoMedia()).andReturn(Short.valueOf("24")).times(3);
		expect(sistemaParametrosMock.getIndicadorTarifaCategoria()).andReturn(Short.valueOf("1")).times(3);
		replay(sistemaParametrosMock);
	}
	
	private void mockConsumoHistoricoSemMesRetroagido() {
		expect(consumoHistoricoRepositorioMock.obterConsumoMedio(1, 201201, 201407, 1)).andReturn(getListaConsumoHistoricoSemMesRetroagido());
		replay(consumoHistoricoRepositorioMock);
	}
	
	private void mockConsumoHistoricoComMesRetroagido() {
		expect(consumoHistoricoRepositorioMock.obterConsumoMedio(1, 201201, 201407, 1)).andReturn(getListaConsumoHistoricoComMesRetroagido());
		replay(consumoHistoricoRepositorioMock);
	}
	
	private void mockConsumoHistoricoNulo() {
		expect(consumoHistoricoRepositorioMock.obterConsumoMedio(1, 201201, 201407, 1)).andReturn(null);
		replay(consumoHistoricoRepositorioMock);
	}
	
	private void mockConsumoHistoricoVazio() {
		expect(consumoHistoricoRepositorioMock.obterConsumoMedio(1, 201201, 201407, 1)).andReturn(new ArrayList<ConsumoHistorico>());
		replay(consumoHistoricoRepositorioMock);
	}
	
	private void mockCategoria() {
		expect(imovelSubcategoriaRepositorioMock.buscarQuantidadeEconomiasPorImovel(1)).andReturn(null);
		replay(imovelSubcategoriaRepositorioMock);
	}
	
	private void mockConsumoTarifa() {
		expect(consumoTarifaRepositorioMock.consumoTarifaDoImovel(1)).andReturn(1);
		replay(consumoTarifaRepositorioMock);
	}
	
	private void mockConsumoMinimoPorLigacao() {
		expect(consumoBOMock.obterConsumoMinimoLigacaoPorCategoria(1, 1, null)).andReturn(30);
		replay(consumoBOMock);
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
