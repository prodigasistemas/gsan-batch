package br.gov.batch.servicos.micromedicao;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.atendimentopublico.LigacaoAguaSituacao;
import br.gov.model.atendimentopublico.LigacaoEsgotoSituacao;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.Localidade;
import br.gov.model.cadastro.Quadra;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.MovimentoRoteiroEmpresa;
import br.gov.model.micromedicao.Rota;
import br.gov.servicos.micromedicao.MovimentoRoteiroEmpresaRepositorio;

@RunWith(EasyMockRunner.class)
public class MovimentoRoteiroEmpresBOTest {

	@TestSubject
	private MovimentoRoteiroEmpresaBO bo;
	
	@Mock
	private MovimentoRoteiroEmpresaRepositorio repositorioMock;
	
	private FaturamentoGrupo grupo;
	private List<Imovel> imoveis;
	private List<Imovel> imoveisOutrosGrupos;
	private List<MovimentoRoteiroEmpresa> movimentosInseridos;
	private Rota rota;
	private Imovel imovel;
	
	@Before
	public void setUp() {
		
		bo = new MovimentoRoteiroEmpresaBO();
		
		grupo = new FaturamentoGrupo();
		grupo.setAnoMesReferencia(201501);
		
		rota = new Rota(1);
		rota.setFaturamentoGrupo(grupo);
		
		Quadra quadra = new Quadra(1);
		quadra.setRota(rota);
		
		imovel = new Imovel(1);
		imovel.setLocalidade(new Localidade(1));
		imovel.setLigacaoAguaSituacao(new LigacaoAguaSituacao(LigacaoAguaSituacao.LIGADO));
		imovel.setLigacaoEsgotoSituacao(new LigacaoEsgotoSituacao(LigacaoEsgotoSituacao.LIGADO));
		imovel.setQuadra(quadra);
		
		imoveisOutrosGrupos = new ArrayList<Imovel>();
		
		imoveis = new ArrayList<Imovel>();
		imoveis.add(imovel);
	}
	
	@Test
	public void testarGerarMovimentoRoteiroEmpresa() {
		mockImoveisGerados();
		
		List<MovimentoRoteiroEmpresa> movimentos = bo.gerarMovimentoRoteiroEmpresa(imoveis, rota);
		assertNotNull(movimentos);
	}
	
	@Test
	public void testarNaoGerarMovimentoRoteiroEmpresa() {
		mockImoveisGeradosOutrosGrupos();
		
		List<MovimentoRoteiroEmpresa> movimentos = bo.gerarMovimentoRoteiroEmpresa(imoveis, rota);
		
		assertNotNull(movimentos);
		assertTrue(movimentos.isEmpty());
	}
	
	private void mockImoveisGerados() {
		imoveisOutrosGrupos = new ArrayList<Imovel>();
		
		movimentosInseridos = new ArrayList<MovimentoRoteiroEmpresa>();
		movimentosInseridos.add(getMovimento());
		
		repositorioMock.deletarPorRota(rota);
		expect(repositorioMock.pesquisarImoveisGeradosParaOutroGrupo(imoveis, grupo)).andReturn(imoveisOutrosGrupos);
		expect(repositorioMock.criarMovimentoRoteiroEmpresa(imoveis, rota)).andReturn(movimentosInseridos);
		replay(repositorioMock);
	}

	private MovimentoRoteiroEmpresa getMovimento() {
		MovimentoRoteiroEmpresa movimento = new MovimentoRoteiroEmpresa();
		movimento.setAnoMesMovimento(rota.getFaturamentoGrupo().getAnoMesReferencia());
		movimento.setImovel(imovel);
		movimento.setFaturamentoGrupo(imovel.getQuadra().getRota().getFaturamentoGrupo());
		movimento.setLocalidade(imovel.getLocalidade());
		movimento.setGerenciaRegional(imovel.getLocalidade().getGerenciaRegional());
		movimento.setLigacaoAguaSituacao(imovel.getLigacaoAguaSituacao());
		movimento.setLigacaoEsgotoSituacao(imovel.getLigacaoEsgotoSituacao());
		movimento.setRota(imovel.getQuadra().getRota());
		return movimento;
	}
	
	private void mockImoveisGeradosOutrosGrupos() {
		imoveisOutrosGrupos = new ArrayList<Imovel>();
		imoveisOutrosGrupos.add(imovel);
		
		movimentosInseridos = new ArrayList<MovimentoRoteiroEmpresa>();
		
		repositorioMock.deletarPorRota(rota);
		expect(repositorioMock.pesquisarImoveisGeradosParaOutroGrupo(imoveis, grupo)).andReturn(imoveisOutrosGrupos);
		expect(repositorioMock.criarMovimentoRoteiroEmpresa(imoveis, rota)).andReturn(movimentosInseridos);
		replay(repositorioMock);
	}
}
