package br.gov.batch.servicos.faturamento.arquivo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.batch.servicos.faturamento.AguaEsgotoBO;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.batch.servicos.faturamento.to.VolumeMedioAguaEsgotoTO;
import br.gov.batch.servicos.micromedicao.FaixaLeituraBO;
import br.gov.batch.servicos.micromedicao.HidrometroBO;
import br.gov.batch.servicos.micromedicao.MedicaoHistoricoBO;
import br.gov.model.atendimentopublico.LigacaoAgua;
import br.gov.model.atendimentopublico.LigacaoAguaSituacao;
import br.gov.model.cadastro.Imovel;
import br.gov.model.micromedicao.LeituraSituacao;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.model.micromedicao.MedicaoTipo;
import br.gov.model.micromedicao.RateioTipo;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.micromedicao.to.FaixaLeituraTO;
import br.gov.servicos.to.HidrometroMedicaoHistoricoTO;

public class ArquivoTextoTipo08Test {

	@InjectMocks
	private ArquivoTextoTipo08 arquivo;

	private int TAMANHO_LINHA = 120;

	private Imovel imovel;
	private Integer referencia;
	private boolean houveInstalacaoOuSubstituicaoHidrometro;
	private LigacaoTipo ligacaoTipo = LigacaoTipo.AGUA;
	private MedicaoHistorico medicaoHistorico;
	private List<HidrometroMedicaoHistoricoTO> hidrometrosMedicaoHistoricoTO;
	private LigacaoAguaSituacao situacao = new LigacaoAguaSituacao();

	@Mock
	private MedicaoHistoricoBO medicaoHistoricoBOMock;

	@Mock
	private HidrometroBO hidrometroBOMock;

	@Mock
	private AguaEsgotoBO aguaEsgotoBOMock;

	@Mock
	private FaixaLeituraBO faixaLeituraBOMock;

	@Mock
	private MedicaoHistoricoRepositorio medicaoHistoricoMock;
	
    @Mock
    private ImovelRepositorio repositorioImovel;

	private ArquivoTextoTO to;
	
	@Before
	public void setup() {
		referencia = new Integer(201201);
		imovel = new Imovel(1);

		LigacaoAgua ligacaoAgua = new LigacaoAgua();
		ligacaoAgua.setDataLigacao(new Date());

		imovel.setLigacaoAgua(ligacaoAgua);
		houveInstalacaoOuSubstituicaoHidrometro = false;

		HidrometroMedicaoHistoricoTO hidrometroMH = new HidrometroMedicaoHistoricoTO();
		hidrometroMH.setMedicaoTipo(MedicaoTipo.LIGACAO_AGUA.getId());
		hidrometroMH.setNumero("A98N128712");
		hidrometroMH.setDataInstalacao(new Date());
		hidrometroMH.setNumeroDigitosLeitura(new Short("5"));
		hidrometroMH.setLeituraSituacaoAtual(LeituraSituacao.CONFIRMADA.getId());
		hidrometroMH.setDescricaoLocalInstalacao("JARDIM (FRENTE) CEN ");
		hidrometroMH.setRateioTipo(RateioTipo.SEM_RATEIO.getId());
		hidrometroMH.setNumeroLeituraInstalacao(0);

		hidrometrosMedicaoHistoricoTO = new ArrayList<HidrometroMedicaoHistoricoTO>();
		hidrometrosMedicaoHistoricoTO.add(hidrometroMH);

		medicaoHistorico = new MedicaoHistorico();
		medicaoHistorico.setLeituraAnteriorFaturamento(200);
		medicaoHistorico.setDataLeituraAnteriorFaturamento(new Date());
		medicaoHistorico.setLeituraAnteriorInformada(new Integer(180));
		medicaoHistorico.setDataLeituraAtualInformada(new Date());
		medicaoHistorico.setAnoMesReferencia(201112);
		
		situacao = new LigacaoAguaSituacao();
		situacao.setId(LigacaoAguaSituacao.POTENCIAL);
		imovel.setLigacaoAguaSituacao(situacao);

		to = new ArquivoTextoTO();
		to.setImovel(imovel);
		to.setAnoMesReferencia(referencia);
		arquivo = new ArquivoTextoTipo08();
		
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void buildArquivoTextoTipo08() {
		carregarMocks();

		assertNotNull(arquivo.build(to));
	}

	@Test
	public void buildArquivoTextoTipo08TamanhoLinha() {
		carregarMocks();

		String linha = arquivo.build(to);
		assertTrue(linha.length() == TAMANHO_LINHA);
	}

	private void carregarMocks() {
        when(repositorioImovel.obterPorID(imovel.getId())).thenReturn(imovel);
	    
		when(medicaoHistoricoBOMock.obterDadosTiposMedicao(imovel.getId(), referencia)).thenReturn(hidrometrosMedicaoHistoricoTO);
		when(hidrometroBOMock.houveInstalacaoOuSubstituicao(imovel.getId())).thenReturn(houveInstalacaoOuSubstituicaoHidrometro);
		when(medicaoHistoricoMock.buscarPorLigacaoAguaOuPoco(imovel.getId(),  201112)).thenReturn(medicaoHistorico);
		when(faixaLeituraBOMock.obterDadosFaixaLeitura(any(), any(), any(), any())).thenReturn(new FaixaLeituraTO(230, 250));
		when(aguaEsgotoBOMock.obterVolumeMedioAguaEsgoto(imovel.getId(), referencia, ligacaoTipo.getId())).thenReturn(new VolumeMedioAguaEsgotoTO(20, 6));
	}
}
