package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.servicos.faturamento.AguaEsgotoBO;
import br.gov.batch.servicos.faturamento.to.VolumeMedioAguaEsgotoTO;
import br.gov.batch.servicos.micromedicao.FaixaLeituraBO;
import br.gov.batch.servicos.micromedicao.HidrometroBO;
import br.gov.batch.servicos.micromedicao.MedicaoHistoricoBO;
import br.gov.model.atendimentopublico.LigacaoAgua;
import br.gov.model.cadastro.Imovel;
import br.gov.model.micromedicao.LeituraSituacao;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.model.micromedicao.MedicaoTipo;
import br.gov.model.micromedicao.RateioTipo;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.micromedicao.to.FaixaLeituraTO;
import br.gov.servicos.to.HidrometroMedicaoHistoricoTO;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo08Test {



	@TestSubject
	private ArquivoTextoTipo08 arquivoTextoTipo08;
	
	private Imovel imovel;
	private Integer referencia;
	private boolean houveInstalacaoOuSubstituicaoHidrometro;
	private LigacaoTipo ligacaoTipo = LigacaoTipo.AGUA;
	private MedicaoHistorico medicaoHistorico;
	
	private List<HidrometroMedicaoHistoricoTO> hidrometrosMedicaoHistoricoTO;
	
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
		
		arquivoTextoTipo08 = new ArquivoTextoTipo08();
	}
	
	@Test
	public void buildArquivoTextoTipo08() {
		carregarMocks();
		
		assertNotNull(arquivoTextoTipo08.build(imovel, referencia));
	}
	
	@Test
	public void buildArquivoTextoTipo08TamanhoLinha() {
		carregarMocks();
		
		String linha = arquivoTextoTipo08.build(imovel, referencia);
		int tamanhoLinha = linha.length();
		
		System.out.println(linha);
		System.out.println(tamanhoLinha);
		
		assertTrue(tamanhoLinha == 120);
	}
	
	private void carregarMocks() {
		expect(medicaoHistoricoBOMock.obterDadosTiposMedicao(imovel.getId(), referencia)).andReturn(hidrometrosMedicaoHistoricoTO).times(1);
		expect(hidrometroBOMock.houveInstalacaoOuSubstituicao(imovel.getId())).andReturn(houveInstalacaoOuSubstituicaoHidrometro).times(1);
		expect(medicaoHistoricoMock.buscarPorLigacaoAguaOuPoco(imovel.getId(), referencia)).andReturn(medicaoHistorico).times(1);
		
		expect(faixaLeituraBOMock.obterDadosFaixaLeitura(anyObject(), anyObject(), anyObject(), anyObject())).andReturn(new FaixaLeituraTO(230, 250));
		
		expect(aguaEsgotoBOMock.obterVolumeMedioAguaEsgoto(imovel.getId(), referencia, ligacaoTipo, houveInstalacaoOuSubstituicaoHidrometro))
		.andReturn(new VolumeMedioAguaEsgotoTO(20, 6)).times(1);
		
		replay(medicaoHistoricoBOMock);
		replay(hidrometroBOMock);
		replay(aguaEsgotoBOMock);
		replay(medicaoHistoricoMock);
		replay(faixaLeituraBOMock);
	}

}
