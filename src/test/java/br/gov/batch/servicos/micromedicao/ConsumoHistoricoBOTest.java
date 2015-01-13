package br.gov.batch.servicos.micromedicao;

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

import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.micromedicao.ConsumoHistorico;


@RunWith(EasyMockRunner.class)
public class ConsumoHistoricoBOTest {
	
	@TestSubject
	private ConsumoHistoricoBO consumoHistoricoBO;
	
	@Mock
	private SistemaParametros sistemaParametrosMock;
	
	@Before
	public void setup(){
		consumoHistoricoBO = new ConsumoHistoricoBO();
	}
	
	@Test
	public void mediaConsumoSemMesesCortados(){
		mockParametros();
				
		assertEquals(45, consumoHistoricoBO.calcularMediaConsumo(consumosSemCortes(), 201407).intValue());
	}

	@Test
	public void mediaConsumoComMesesCortados(){
		mockParametros();
		
		assertEquals(41, consumoHistoricoBO.calcularMediaConsumo(consumosComCortes(), 201407).intValue());
	}
	
	@Test
	public void mediaConsumoComMesesCortadosMaiorTempo(){
		mockParametros();
		
		assertEquals(19, consumoHistoricoBO.calcularMediaConsumo(consumosComCortesTempoMaior(), 201407).intValue());
	}
	
	@Test
	public void semConseguirCalcularMediaConsumo(){
		mockParametros();
		
		assertEquals(0, consumoHistoricoBO.calcularMediaConsumo(consumosComCortesMuitoTempo(), 201407).intValue());
	}
	
	private void mockParametros() {
		expect(sistemaParametrosMock.getNumeroMesesMaximoCalculoMedia()).andReturn(Short.valueOf("24"));
		expect(sistemaParametrosMock.getMesesMediaConsumo()).andReturn(Short.valueOf("6"));
		replay(sistemaParametrosMock);
	}
	
	private List<ConsumoHistorico> consumosSemCortes(){
		List<ConsumoHistorico> consumos = new ArrayList<ConsumoHistorico>();
		
		ConsumoHistorico c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(20);
		c.setReferenciaFaturamento(201407);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(30);
		c.setReferenciaFaturamento(201406);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(40);
		c.setReferenciaFaturamento(201405);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(50);
		c.setReferenciaFaturamento(201404);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(60);
		c.setReferenciaFaturamento(201403);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(70);
		c.setReferenciaFaturamento(201402);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(80);
		c.setReferenciaFaturamento(201401);
		consumos.add(c);
		
		return consumos;
	}
	
	private List<ConsumoHistorico> consumosComCortes(){
		List<ConsumoHistorico> consumos = new ArrayList<ConsumoHistorico>();
		
		ConsumoHistorico c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(75);
		c.setReferenciaFaturamento(201403);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(25);
		c.setReferenciaFaturamento(201402);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(80);
		c.setReferenciaFaturamento(201401);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(35);
		c.setReferenciaFaturamento(201312);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(20);
		c.setReferenciaFaturamento(201311);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(15);
		c.setReferenciaFaturamento(201310);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(35);
		c.setReferenciaFaturamento(201309);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(15);
		c.setReferenciaFaturamento(201308);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(345);
		c.setReferenciaFaturamento(201307);
		consumos.add(c);
		
		return consumos;
	}
	
	private List<ConsumoHistorico> consumosComCortesTempoMaior(){
		List<ConsumoHistorico> consumos = new ArrayList<ConsumoHistorico>();
		
		ConsumoHistorico c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(13);
		c.setReferenciaFaturamento(201208);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(26);
		c.setReferenciaFaturamento(201207);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(20);
		c.setReferenciaFaturamento(201204);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(15);
		c.setReferenciaFaturamento(201203);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(35);
		c.setReferenciaFaturamento(201202);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(15);
		c.setReferenciaFaturamento(201201);
		consumos.add(c);
		c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(345);
		c.setReferenciaFaturamento(201112);
		consumos.add(c);
		
		return consumos;
	}
	
	private List<ConsumoHistorico> consumosComCortesMuitoTempo(){
		List<ConsumoHistorico> consumos = new ArrayList<ConsumoHistorico>();
		
		ConsumoHistorico c = new ConsumoHistorico();
		c.setNumeroConsumoCalculoMedia(345);
		c.setReferenciaFaturamento(201012);
		consumos.add(c);
		
		return consumos;
	}	

}
