package br.gov.batch.servicos.faturamento;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.faturamento.FaturamentoAtividade;
import br.gov.model.faturamento.FaturamentoAtividadeCronograma;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.Rota;
import br.gov.servicos.faturamento.FaturamentoAtividadeCronogramaRepositorio;

@RunWith(EasyMockRunner.class)
public class FaturamentoAtividadeCronogramaBOTest {

	@TestSubject
	private FaturamentoAtividadeCronogramaBO bo;
	
	@Mock
	private FaturamentoAtividadeCronogramaRepositorio mock;
	
	private FaturamentoAtividadeCronograma faturamentoAtividadeCronograma;
	private FaturamentoGrupo faturamentoGrupo;
	private Rota rota;
	
	private Date dataAtual;
	private Date dataAnterior;
	private Date dataPrevista;
	
	@Before
	public void setUp() {
		faturamentoAtividadeCronograma = new FaturamentoAtividadeCronograma();

		faturamentoGrupo = new FaturamentoGrupo();
		faturamentoGrupo.setAnoMesReferencia(201501);
		faturamentoGrupo.setId(1);
		
		rota = new Rota();
		rota.setFaturamentoGrupo(faturamentoGrupo);
	    
		bo = new FaturamentoAtividadeCronogramaBO();
	}
	
	@Test
	public void testaComDataPrevista() {
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.YEAR, 2015);
		cal.set(Calendar.MONTH, 01);
		cal.set(Calendar.DAY_OF_MONTH, 30);
		
		dataPrevista = cal.getTime();
		faturamentoAtividadeCronograma.setDataPrevista(dataPrevista);

		carregarMocks();
		
		assertNotNull(bo.obterDataPrevistaDoCronogramaAnterior(faturamentoGrupo, FaturamentoAtividade.EFETUAR_LEITURA));	
		assertEquals(dataPrevista, bo.obterDataPrevistaDoCronogramaAnterior(faturamentoGrupo, FaturamentoAtividade.EFETUAR_LEITURA));
	}
	
	@Test
	public void testaSemDataPrevista() {
		Date data = new Date();
		
		faturamentoAtividadeCronograma.setDataPrevista(null);

		carregarMocks();
		
		assertNotNull(bo.obterDataPrevistaDoCronogramaAnterior(faturamentoGrupo, FaturamentoAtividade.EFETUAR_LEITURA));	
		assertTrue(data.after(bo.obterDataPrevistaDoCronogramaAnterior(faturamentoGrupo, FaturamentoAtividade.EFETUAR_LEITURA)));
	}
	
	@Test
	public void obterDiferencaDiasCronogramasValida() {
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.YEAR, 2015);
		cal.set(Calendar.MONTH, 01);
		cal.set(Calendar.DAY_OF_MONTH, 30);
		dataAtual = cal.getTime();
		cal.set(Calendar.YEAR, 2014);
		cal.set(Calendar.MONTH, 12);
		cal.set(Calendar.DAY_OF_MONTH, 30);
		dataAnterior = cal.getTime();
		
		rota.getFaturamentoGrupo().setAnoMesReferencia(201501);
		carregarMocks2();
		
		long result = bo.obterDiferencaDiasCronogramas(rota, 1);
		assertNotNull(result);	
		assertEquals( 31, result);
	}
	
	private void carregarMocks() {
		expect(mock.buscarPorGrupoEAtividadeEReferencia(anyObject(), anyObject(), anyObject())).andReturn(faturamentoAtividadeCronograma).times(4);
		replay(mock);
	}
	
	private void carregarMocks2() {
		expect(mock.pesquisarFaturamentoAtividadeCronogramaDataPrevista(1, 1, 201501)).andReturn(dataAtual);
		expect(mock.pesquisarFaturamentoAtividadeCronogramaDataPrevista(1, 1, 201412)).andReturn(dataAnterior);
		replay(mock);
	}
}
