package br.gov.batch.servicos.faturamento.arquivo;

import static br.gov.model.util.Utilitarios.completaComEspacosADireita;
import static br.gov.model.util.Utilitarios.completaTexto;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.batch.servicos.faturamento.ExtratoQuitacaoBO;
import br.gov.batch.servicos.faturamento.MensagemContaBO;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.Status;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.Localidade;
import br.gov.model.cadastro.QuadraFace;
import br.gov.model.cadastro.SetorComercial;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.faturamento.FaturamentoParametro.NOME_PARAMETRO_FATURAMENTO;
import br.gov.model.faturamento.QualidadeAgua;
import br.gov.model.faturamento.QualidadeAguaPadrao;
import br.gov.model.operacional.FonteCaptacao;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.faturamento.FaturamentoParametroRepositorio;
import br.gov.servicos.faturamento.QuadraFaceRepositorio;
import br.gov.servicos.faturamento.QualidadeAguaPadraoRepositorio;
import br.gov.servicos.faturamento.QualidadeAguaRepositorio;

public class ArquivoTextoTipo01DadosContaTest {

	@InjectMocks
	private ArquivoTextoTipo01DadosConta arquivo;

	@Mock
	private ExtratoQuitacaoBO extratoQuitacaoBOMock;

	@Mock
	private MensagemContaBO mensagemContaBOMock;

	@Mock
	private FaturamentoParametroRepositorio repositorioParametros;

	@Mock
	private QualidadeAguaPadraoRepositorio qualidadeAguaPadraoRepositorioMock;

	@Mock
	private QualidadeAguaRepositorio qualidadeAguaRepositorioMock;

	@Mock
	private QuadraFaceRepositorio quadraFaceRepositorioMock;
	
    @Mock
    private ImovelRepositorio repositorioImovel;

	private Imovel imovel;
	private QuadraFace quadraFace;
	private Conta conta;
	private FaturamentoGrupo faturamentoGrupo;
	
	private ArquivoTextoTO arquivoTextoTO;

	@Before
	public void setup() {
		quadraFace = new QuadraFace(1);

		faturamentoGrupo = new FaturamentoGrupo(1);
		faturamentoGrupo.setAnoMesReferencia(201501);

		conta = new Conta();
		Date data = Utilitarios.converterStringParaData("2015-01-23", FormatoData.ANO_MES_DIA_SEPARADO);
		conta.setDataVencimentoConta(data);
		conta.setDataValidadeConta(data);
		conta.setFaturamentoGrupo(faturamentoGrupo);
		conta.setReferencia(201501);
		conta.setId(999999999);
		conta.setDigitoVerificadorConta(Short.valueOf("1"));

		imovel = new Imovel(1234567);
		imovel.setIndicadorImovelCondominio(Status.INATIVO.getId());
		imovel.setLocalidade(new Localidade(1));
		imovel.setQuadraFace(quadraFace);
		imovel.setSetorComercial(new SetorComercial(1));

		arquivo = new ArquivoTextoTipo01DadosConta();
		
		arquivoTextoTO = new ArquivoTextoTO();
		arquivoTextoTO.setConta(conta);
        arquivoTextoTO.setImovel(imovel);
		arquivoTextoTO.setFaturamentoGrupo(faturamentoGrupo);
		arquivoTextoTO.setAnoMesReferencia(201501);
		
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void buildArquivoDadosCobranca() {
		carregarMocks();

		Map<Integer, StringBuilder> mapDados = arquivo.build(arquivoTextoTO);

		StringBuilder mensagem = new StringBuilder();

		mensagem.append(completaComEspacosADireita(100, "MENSAGEM EM CONTA - 1"))
		    .append(completaComEspacosADireita(100, "MENSAGEM EM CONTA - 2"))
		    .append(completaComEspacosADireita(100, "MENSAGEM EM CONTA - 3"));

		assertNotNull(mapDados);
		assertEquals(7, mapDados.keySet().size());
		assertEquals("2015012320150123", mapDados.get(3).toString());
		assertEquals("2015011", mapDados.get(6).toString());
		assertEquals("999999999", mapDados.get(24).toString());
		assertEquals(mensagem.toString(), mapDados.get(28).toString());
		assertEquals(completaComEspacosADireita(120, "MENSAGEM QUITACAO ANUAL DE DEBITOS"), mapDados.get(29).toString());
		assertEquals(completaTexto(200, ""), mapDados.get(30).toString());
		assertEquals(completaTexto(212, ""), mapDados.get(31).toString());
	}

	private String[] obterMensagem() {
		String[] mensagemConta = new String[3];

		mensagemConta[0] = "MENSAGEM EM CONTA - 1";
		mensagemConta[1] = "MENSAGEM EM CONTA - 2";
		mensagemConta[2] = "MENSAGEM EM CONTA - 3";

		return mensagemConta;
	}

	private List<QualidadeAguaPadrao> obterQualidadeAguaPadrao() {
		QualidadeAguaPadrao qualidade = new QualidadeAguaPadrao();

		List<QualidadeAguaPadrao> qualidades = new ArrayList<QualidadeAguaPadrao>();

		qualidades.add(qualidade);
		return qualidades;
	}

	private QualidadeAgua obterQualidadeAgua() {
		FonteCaptacao fonte = new FonteCaptacao();

		QualidadeAgua qualidade = new QualidadeAgua();
		qualidade.setFonteCaptacao(fonte);

		return qualidade;
	}

	public void carregarMocks() {
		String[] mensagemConta = obterMensagem();

		when(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.ESCREVER_MENSAGEM_CONTA_TRES_PARTES)).thenReturn("true");
		when(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).thenReturn("false");
		when(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.REFERENCIA_ANTERIOR_PARA_QUALIDADE_AGUA)).thenReturn("false");

		when(mensagemContaBOMock.obterMensagemConta3Partes(imovel, faturamentoGrupo.getAnoMesReferencia(), faturamentoGrupo.getId())).thenReturn(mensagemConta);

		when(extratoQuitacaoBOMock.obterMsgQuitacaoDebitos(imovel.getId(), faturamentoGrupo.getAnoMesReferencia())).thenReturn("MENSAGEM QUITACAO ANUAL DE DEBITOS");

		when(qualidadeAguaPadraoRepositorioMock.obterLista()).thenReturn(obterQualidadeAguaPadrao());

		when(quadraFaceRepositorioMock.obterPorID(1)).thenReturn(quadraFace);

		when(qualidadeAguaRepositorioMock.buscarSemFonteCaptacao(any(), any(), any())).thenReturn(obterQualidadeAgua());
		
        when(repositorioImovel.obterPorID(imovel.getId())).thenReturn(imovel);
	}
}
