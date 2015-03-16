package br.gov.batch.servicos.micromedicao;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
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

import br.gov.batch.servicos.faturamento.AguaEsgotoBO;
import br.gov.batch.servicos.faturamento.to.VolumeMedioAguaEsgotoTO;
import br.gov.model.atendimentopublico.LigacaoAguaSituacao;
import br.gov.model.atendimentopublico.LigacaoEsgotoSituacao;
import br.gov.model.cadastro.Bairro;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.Localidade;
import br.gov.model.cadastro.Logradouro;
import br.gov.model.cadastro.LogradouroBairro;
import br.gov.model.cadastro.Quadra;
import br.gov.model.cadastro.QuadraFace;
import br.gov.model.cadastro.SetorComercial;
import br.gov.model.cadastro.endereco.LogradouroCep;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.Hidrometro;
import br.gov.model.micromedicao.HidrometroInstalacaoHistorico;
import br.gov.model.micromedicao.MedicaoTipo;
import br.gov.model.micromedicao.MovimentoRoteiroEmpresa;
import br.gov.model.micromedicao.Rota;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.micromedicao.HidrometroInstalacaoHistoricoRepositorio;
import br.gov.servicos.micromedicao.MovimentoRoteiroEmpresaRepositorio;
import br.gov.servicos.micromedicao.to.FaixaLeituraTO;
import br.gov.servicos.to.CategoriaPrincipalTO;

@RunWith(EasyMockRunner.class)
public class MovimentoRoteiroEmpresaBOTest {

	@TestSubject
	private MovimentoRoteiroEmpresaBO bo;
	
	@Mock
	private MovimentoRoteiroEmpresaRepositorio repositorioMock;
	
	@Mock
	ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorioMock;
	
	@Mock
	MedicaoHistoricoBO medicaoHistoricoBOMock;
	
	@Mock
	AguaEsgotoBO aguaEsgotoBOMock;
	
	@Mock
	HidrometroInstalacaoHistoricoRepositorio hidrometroInstalacaoRepositorioMock;
	
	@Mock
	FaixaLeituraBO faixaLeituraBOMock;
	
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
		
		SetorComercial setor = new SetorComercial();
		setor.setCodigo(1);
		rota = new Rota(1);
		rota.setFaturamentoGrupo(grupo);
		rota.setSetorComercial(setor);
		
		Quadra quadra = new Quadra(1);
		quadra.setRota(rota);
		quadra.setNumeroQuadra(1);
		
		imovel = new Imovel(1);
		imovel.setSetorComercial(setor);
		imovel.setNumeroMorador(Short.valueOf("2"));
		imovel.setLocalidade(new Localidade(1));
		imovel.setLigacaoAguaSituacao(new LigacaoAguaSituacao(LigacaoAguaSituacao.LIGADO));
		imovel.setLigacaoEsgotoSituacao(new LigacaoEsgotoSituacao(LigacaoEsgotoSituacao.LIGADO));
		imovel.setQuadra(quadra);
		
		QuadraFace quadraFace = new QuadraFace();
		quadraFace.setNumeroQuadraFace(1);
		imovel.setQuadraFace(quadraFace);
		
		HidrometroInstalacaoHistorico hidrometroInstalacaoHistorico = new HidrometroInstalacaoHistorico();
		Hidrometro hidrometro = new Hidrometro();
		hidrometro.setNumero("");
		hidrometroInstalacaoHistorico.setHidrometro(hidrometro);
		imovel.setHidrometroInstalacaoHistorico(hidrometroInstalacaoHistorico);
		
		LogradouroCep logradouroCep = new LogradouroCep();
		Logradouro logradouro = new Logradouro();
		logradouro.setNome("Almirante Barroso");
		logradouroCep.setLogradouro(logradouro);
		
		imovel.setLogradouroCep(logradouroCep);
		
		LogradouroBairro logradouroBairro = new LogradouroBairro();
		Bairro bairro = new Bairro();
		bairro.setNome("");
		logradouroBairro.setBairro(bairro);
		imovel.setLogradouroBairro(logradouroBairro);
		
		imoveisOutrosGrupos = new ArrayList<Imovel>();
		
		imoveis = new ArrayList<Imovel>();
		imoveis.add(imovel);
	}
	
	@Test
	public void testarGerarMovimentoRoteiroEmpresa() {
		mockImoveisGerados();
		mockBuild();
		
		List<MovimentoRoteiroEmpresa> movimentos = bo.gerarMovimento(imoveis, rota);
		assertNotNull(movimentos);
	}
	
	@Test
	public void testarNaoGerarMovimentoRoteiroEmpresa() {
		mockImoveisGeradosOutrosGrupos();
		
		List<MovimentoRoteiroEmpresa> movimentos = bo.gerarMovimento(imoveis, rota);
		
		assertNotNull(movimentos);
		assertTrue(movimentos.isEmpty());
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
		replay(repositorioMock);
	}

    private void mockImoveisGerados() {
        imoveisOutrosGrupos = new ArrayList<Imovel>();
        
        movimentosInseridos = new ArrayList<MovimentoRoteiroEmpresa>();
        movimentosInseridos.add(getMovimento());
        
        repositorioMock.deletarPorRota(rota);
        expect(repositorioMock.pesquisarImoveisGeradosParaOutroGrupo(imoveis, grupo)).andReturn(imoveisOutrosGrupos);
        repositorioMock.salvar(anyObject());
        expectLastCall().times(1);
        replay(repositorioMock);
    }	
    
    private void mockBuild() {
    	CategoriaPrincipalTO categoria = new CategoriaPrincipalTO(Integer.valueOf(1), Long.valueOf(1));
    	VolumeMedioAguaEsgotoTO volumeMedioAguaEsgotoTO = new VolumeMedioAguaEsgotoTO(20, 6);
    	
    	expect(imovelSubcategoriaRepositorioMock.buscarCategoriaPrincipal(imovel.getId())).andReturn(categoria);
    	expect(medicaoHistoricoBOMock.getMedicaoHistorico(imovel.getId(), 201412)).andReturn(null);
    	expect(aguaEsgotoBOMock.obterVolumeMedioAguaEsgoto(imovel.getId(), 201501, MedicaoTipo.LIGACAO_AGUA.getId())).andReturn(volumeMedioAguaEsgotoTO);    	
    	
    	expect(hidrometroInstalacaoRepositorioMock.dadosHidrometroInstaladoAgua(imovel.getId())).andReturn(null);
    	expect(faixaLeituraBOMock.obterDadosFaixaLeitura(imovel, null, volumeMedioAguaEsgotoTO.getConsumoMedio(), null)).andReturn(new FaixaLeituraTO(0, 0));
    	
    	replay(imovelSubcategoriaRepositorioMock);
    	replay(medicaoHistoricoBOMock);
    	replay(aguaEsgotoBOMock);
    	replay(hidrometroInstalacaoRepositorioMock);
    	replay(faixaLeituraBOMock);
    }
}
