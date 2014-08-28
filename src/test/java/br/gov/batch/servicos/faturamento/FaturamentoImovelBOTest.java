package br.gov.batch.servicos.faturamento;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.Status;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.FaturamentoSituacaoHistorico;
import br.gov.model.faturamento.FaturamentoSituacaoTipo;
import br.gov.servicos.faturamento.FaturamentoSituacaoRepositorio;

@RunWith(EasyMockRunner.class)
public class FaturamentoImovelBOTest {

	private Imovel imovel;
	private Integer anoMesFaturamento;
	
	@TestSubject
	private FaturamentoImovelBO faturamentoImovelBO;
	
	private FaturamentoSituacaoHistorico faturamentoSituacaoHistorico;
	
	@Mock
	private FaturamentoSituacaoRepositorio faturamentoSituacaoRepositorioMock;
	
	private FaturamentoSituacaoTipo faturamentoSituacaoTipo;
	
	@Before
	public void setup(){
		anoMesFaturamento = 201402;
		
		imovel = new Imovel();
		faturamentoImovelBO = new FaturamentoImovelBO();
		faturamentoSituacaoHistorico = new FaturamentoSituacaoHistorico();
	}
	
	@Test
	public void verificarFaturarEmSituacaoEspecialFaturamentoComFaturamentoSituacaoTipoNull() {
		faturamentoSituacaoTipo = null;
		inicializaFaturamentoSituacaoHistorico(201401, 201403);
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
		
		mockFaturamentoSituacaoRepositorio(faturamentoSituacaoHistorico);
		
		assertTrue(faturamentoImovelBO.verificarFaturarEmSituacaoEspecialFaturamento(imovel, anoMesFaturamento));
	}
	
	@Test
	public void verificarFaturarEmSituacaoEspecialFaturamentoComParalisacaoFaturamentoInativo() {
		faturamentoSituacaoTipo = new FaturamentoSituacaoTipo();
		faturamentoSituacaoTipo.setParalisacaoFaturamento(Status.INATIVO.getId());
		faturamentoSituacaoTipo.setValidoAgua(Status.ATIVO.getId());
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
		
		inicializaFaturamentoSituacaoHistorico(201401, 201403);
		mockFaturamentoSituacaoRepositorio(faturamentoSituacaoHistorico);
		
		assertTrue(faturamentoImovelBO.verificarFaturarEmSituacaoEspecialFaturamento(imovel, anoMesFaturamento));
	}
	
	@Test
	public void verificarFaturarEmSituacaoEspecialFaturamentoComParalisacaoFaturamentoAtivo() {
		faturamentoSituacaoTipo = new FaturamentoSituacaoTipo();
		faturamentoSituacaoTipo.setParalisacaoFaturamento(Status.ATIVO.getId());
		faturamentoSituacaoTipo.setValidoAgua(Status.ATIVO.getId());
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
		
		inicializaFaturamentoSituacaoHistorico(201401, 201403);
		mockFaturamentoSituacaoRepositorio(faturamentoSituacaoHistorico);
		
		assertFalse(faturamentoImovelBO.verificarFaturarEmSituacaoEspecialFaturamento(imovel, anoMesFaturamento));
	}
	
	@Test
	public void verificarFaturarEmSituacaoEspecialFaturamentoComAguaInvalidoAtivo() {
		faturamentoSituacaoTipo = new FaturamentoSituacaoTipo();
		faturamentoSituacaoTipo.setParalisacaoFaturamento(Status.ATIVO.getId());
		faturamentoSituacaoTipo.setValidoAgua(Status.INATIVO.getId());
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
		
		inicializaFaturamentoSituacaoHistorico(201401, 201403);
		mockFaturamentoSituacaoRepositorio(faturamentoSituacaoHistorico);
		
		assertTrue(faturamentoImovelBO.verificarFaturarEmSituacaoEspecialFaturamento(imovel, anoMesFaturamento));
	}
	
	@Test
	public void possuiSituacaoEspecialFaturamentoComFaturamentoSituacaoHistoricoNull() {
		mockFaturamentoSituacaoRepositorio(null);
		assertFalse(faturamentoImovelBO.possuiSituacaoEspecialFaturamento(imovel, anoMesFaturamento));
	}
	
	@Test
	public void possuiSituacaoEspecialFaturamentoComFaturamentoSituacaoHistoricoIntervaloAnterior() {
		inicializaFaturamentoSituacaoHistorico(201312, 201401);
		mockFaturamentoSituacaoRepositorio(faturamentoSituacaoHistorico);
		assertFalse(faturamentoImovelBO.possuiSituacaoEspecialFaturamento(imovel, anoMesFaturamento));
	}
	
	@Test
	public void possuiSituacaoEspecialFaturamentoComFaturamentoSituacaoHistoricoIntervaloPosterior() {
		inicializaFaturamentoSituacaoHistorico(201403, 201404);
		mockFaturamentoSituacaoRepositorio(faturamentoSituacaoHistorico);
		assertFalse(faturamentoImovelBO.possuiSituacaoEspecialFaturamento(imovel, anoMesFaturamento));
	}
	
	@Test
	public void possuiSituacaoEspecialFaturamentoComFaturamentoSituacaoHistoricoIntervaloCorreto() {
		inicializaFaturamentoSituacaoHistorico(201401, 201403);
		mockFaturamentoSituacaoRepositorio(faturamentoSituacaoHistorico);
		assertTrue(faturamentoImovelBO.possuiSituacaoEspecialFaturamento(imovel, anoMesFaturamento));
	}
	
	private void inicializaFaturamentoSituacaoHistorico(int anoMesInicio, int anoMesFinal) {
		faturamentoSituacaoHistorico.setAnoMesFaturamentoSituacaoInicio(anoMesInicio);
		faturamentoSituacaoHistorico.setAnoMesFaturamentoSituacaoFim(anoMesFinal);
	}
	
	private void mockFaturamentoSituacaoRepositorio(FaturamentoSituacaoHistorico faturamentoSituacaoHistorico) {
		List<FaturamentoSituacaoHistorico> list = Arrays.asList(faturamentoSituacaoHistorico);
		expect(faturamentoSituacaoRepositorioMock.faturamentosHistoricoVigentesPorImovel(imovel.getId())).andReturn(list);
		replay(faturamentoSituacaoRepositorioMock);
	}
}