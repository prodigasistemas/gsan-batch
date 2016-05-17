package br.gov.batch.servicos.micromedicao;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

public class MovimentoRoteiroEmpresaBOTest {

	@InjectMocks
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
		
		MockitoAnnotations.initMocks(this);
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
		when(repositorioMock.pesquisarImoveisGeradosParaOutroGrupo(imoveis, grupo)).thenReturn(imoveisOutrosGrupos);
	}

    private void mockImoveisGerados() {
        imoveisOutrosGrupos = new ArrayList<Imovel>();
        
        movimentosInseridos = new ArrayList<MovimentoRoteiroEmpresa>();
        movimentosInseridos.add(getMovimento());
        
        repositorioMock.deletarPorRota(rota);
        when(repositorioMock.pesquisarImoveisGeradosParaOutroGrupo(imoveis, grupo)).thenReturn(imoveisOutrosGrupos);
        repositorioMock.salvar(any());
    }	
    
    private void mockBuild() {
    	CategoriaPrincipalTO categoria = new CategoriaPrincipalTO(Integer.valueOf(1), Long.valueOf(1));
    	VolumeMedioAguaEsgotoTO volumeMedioAguaEsgotoTO = new VolumeMedioAguaEsgotoTO(20, 6);
    	
    	when(imovelSubcategoriaRepositorioMock.buscarCategoriaPrincipal(imovel.getId())).thenReturn(categoria);
    	when(medicaoHistoricoBOMock.getMedicaoHistorico(imovel.getId(), 201412)).thenReturn(null);
    	when(aguaEsgotoBOMock.obterVolumeMedioAguaEsgoto(imovel.getId(), 201501, MedicaoTipo.LIGACAO_AGUA.getId())).thenReturn(volumeMedioAguaEsgotoTO);    	
    	
    	when(hidrometroInstalacaoRepositorioMock.dadosHidrometroInstaladoAgua(imovel.getId())).thenReturn(null);
    	when(faixaLeituraBOMock.obterDadosFaixaLeitura(imovel, null, volumeMedioAguaEsgotoTO.getConsumoMedio(), null)).thenReturn(new FaixaLeituraTO(0, 0));
    }
}
