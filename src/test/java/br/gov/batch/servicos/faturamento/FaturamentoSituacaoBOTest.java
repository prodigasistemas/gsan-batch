package br.gov.batch.servicos.faturamento;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

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
public class FaturamentoSituacaoBOTest {

	@TestSubject
	private FaturamentoSituacaoBO bo;
	
	@Mock
	private FaturamentoSituacaoRepositorio faturamentoSituacaoRepositorioMock;
	
	private Imovel imovel;
	private FaturamentoSituacaoHistorico faturamentoSituacaoHistorico;
	private FaturamentoSituacaoTipo faturamentoSituacaoTipo;
	private Integer referencia;
	
	@Before
	public void setUp() {
		bo = new FaturamentoSituacaoBO();
		
		imovel = new Imovel();
		
		faturamentoSituacaoHistorico = new FaturamentoSituacaoHistorico();
		faturamentoSituacaoHistorico.setAnoMesFaturamentoSituacaoInicio(201401);
		faturamentoSituacaoHistorico.setAnoMesFaturamentoSituacaoFim(201501);
		
		faturamentoSituacaoTipo = new FaturamentoSituacaoTipo();
	}
	
	@Test
	public void paralisacaoFaturamentoAguaAtivo() {
		faturamentoSituacaoTipo.setValidoAgua(Status.ATIVO.getId());
		faturamentoSituacaoTipo.setParalisacaoFaturamento(Status.ATIVO.getId());
		
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
		referencia = 201406;
		
		carregarMocks(faturamentoSituacaoHistorico);
		
		assertEquals(Status.ATIVO, bo.verificarParalisacaoFaturamentoAgua(imovel, referencia));
	}
	
	@Test
	public void paralisacaoFaturamentoAguaIntivo() {
		faturamentoSituacaoTipo.setValidoAgua(Status.INATIVO.getId());
		faturamentoSituacaoTipo.setParalisacaoFaturamento(Status.INATIVO.getId());
		
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
		referencia = 201406;
		
		carregarMocks(faturamentoSituacaoHistorico);
		
		assertEquals(Status.INATIVO, bo.verificarParalisacaoFaturamentoAgua(imovel, referencia));
	}
	
	@Test
	public void paralisacaoFaturamentoAguaForaVigencia() {
		faturamentoSituacaoTipo.setValidoAgua(Status.ATIVO.getId());
		faturamentoSituacaoTipo.setParalisacaoFaturamento(Status.ATIVO.getId());
		
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
		referencia = 201502;
		
		carregarMocks(faturamentoSituacaoHistorico);
		
		assertEquals(Status.INATIVO, bo.verificarParalisacaoFaturamentoAgua(imovel, referencia));
	}
	
	@Test
	public void paralisacaoFaturamentoEsgotoAtivo() {
		faturamentoSituacaoTipo.setValidoEsgoto(Status.ATIVO.getId());
		faturamentoSituacaoTipo.setParalisacaoFaturamento(Status.ATIVO.getId());
		
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
		referencia = 201406;
		
		carregarMocks(faturamentoSituacaoHistorico);
		
		assertEquals(Status.ATIVO, bo.verificarParalisacaoFaturamentoEsgoto(imovel, referencia));
	}
	
	@Test
	public void paralisacaoFaturamentoEsgotoInativo() {
		faturamentoSituacaoTipo.setValidoEsgoto(Status.INATIVO.getId());
		faturamentoSituacaoTipo.setParalisacaoFaturamento(Status.INATIVO.getId());
		
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
		referencia = 201406;
		
		carregarMocks(faturamentoSituacaoHistorico);
		
		assertEquals(Status.INATIVO, bo.verificarParalisacaoFaturamentoEsgoto(imovel, referencia));
	}
	
	@Test
	public void paralisacaoFaturamentoEsgotoForaVigencia() {
		faturamentoSituacaoTipo.setValidoEsgoto(Status.ATIVO.getId());
		faturamentoSituacaoTipo.setParalisacaoFaturamento(Status.ATIVO.getId());
		
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
		referencia = 201502;
		
		carregarMocks(faturamentoSituacaoHistorico);
		
		assertEquals(Status.INATIVO, bo.verificarParalisacaoFaturamentoEsgoto(imovel, referencia));
	}
	
	private void carregarMocks(FaturamentoSituacaoHistorico faturamentoSituacaoHistorico ) {
		List<FaturamentoSituacaoHistorico> list = Arrays.asList(faturamentoSituacaoHistorico);
		expect(faturamentoSituacaoRepositorioMock.faturamentosHistoricoVigentesPorImovel(imovel.getId())).andReturn(list);
		
		replay(faturamentoSituacaoRepositorioMock);
	}
}
