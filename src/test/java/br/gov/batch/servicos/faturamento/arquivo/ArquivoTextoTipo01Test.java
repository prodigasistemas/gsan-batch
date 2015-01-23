package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.servicos.arrecadacao.PagamentoBO;
import br.gov.batch.servicos.faturamento.AguaEsgotoBO;
import br.gov.batch.servicos.faturamento.EsgotoBO;
import br.gov.batch.servicos.faturamento.ExtratoQuitacaoBO;
import br.gov.batch.servicos.faturamento.FaturamentoAtividadeCronogramaBO;
import br.gov.batch.servicos.faturamento.FaturamentoSituacaoBO;
import br.gov.batch.servicos.faturamento.MensagemContaBO;
import br.gov.batch.servicos.faturamento.to.VolumeMedioAguaEsgotoTO;
import br.gov.batch.servicos.micromedicao.ConsumoBO;
import br.gov.batch.servicos.micromedicao.HidrometroBO;
import br.gov.model.Status;
import br.gov.model.atendimentopublico.LigacaoAgua;
import br.gov.model.atendimentopublico.LigacaoAguaSituacao;
import br.gov.model.atendimentopublico.LigacaoEsgotoSituacao;
import br.gov.model.cadastro.Bairro;
import br.gov.model.cadastro.Categoria;
import br.gov.model.cadastro.Cliente;
import br.gov.model.cadastro.ClienteImovel;
import br.gov.model.cadastro.ClienteRelacaoTipo;
import br.gov.model.cadastro.GerenciaRegional;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.ImovelContaEnvio;
import br.gov.model.cadastro.ImovelPerfil;
import br.gov.model.cadastro.Localidade;
import br.gov.model.cadastro.Logradouro;
import br.gov.model.cadastro.LogradouroBairro;
import br.gov.model.cadastro.Quadra;
import br.gov.model.cadastro.QuadraFace;
import br.gov.model.cadastro.SetorComercial;
import br.gov.model.cadastro.Subcategoria;
import br.gov.model.cadastro.endereco.Cep;
import br.gov.model.cadastro.endereco.LogradouroCep;
import br.gov.model.cadastro.endereco.LogradouroTipo;
import br.gov.model.cadastro.endereco.LogradouroTitulo;
import br.gov.model.cadastro.endereco.Municipio;
import br.gov.model.cobranca.CobrancaDocumento;
import br.gov.model.cobranca.DocumentoTipo;
import br.gov.model.faturamento.ConsumoTarifa;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.FaturamentoAtividade;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.faturamento.FaturamentoParametro.NOME_PARAMETRO_FATURAMENTO;
import br.gov.model.faturamento.QualidadeAgua;
import br.gov.model.faturamento.QualidadeAguaPadrao;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.model.micromedicao.Rota;
import br.gov.model.operacional.FonteCaptacao;
import br.gov.servicos.arrecadacao.DebitoAutomaticoRepositorio;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.faturamento.FaturamentoParametroRepositorio;
import br.gov.servicos.faturamento.QuadraFaceRepositorio;
import br.gov.servicos.faturamento.QualidadeAguaPadraoRepositorio;
import br.gov.servicos.faturamento.QualidadeAguaRepositorio;
import br.gov.servicos.to.DadosBancariosTO;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo01Test {
    
    @TestSubject
    private ArquivoTextoTipo01 arquivoTextoTipo01;
    
    @Mock
    private FaturamentoParametroRepositorio repositorioParametros;
    
    @Mock
    private DebitoAutomaticoRepositorio debitoAutomaticoRepositorioMock;
    
    @Mock
    private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorioMock;
    
    @Mock
    private QualidadeAguaPadraoRepositorio qualidadeAguaPadraoRepositorioMock;
    
    @Mock
    private QualidadeAguaRepositorio qualidadeAguaRepositorioMock;
    
    @Mock
    private QuadraFaceRepositorio quadraFaceRepositorioMock;
    
    @Mock
    private HidrometroBO hidrometroBOMock;

    @Mock
    private AguaEsgotoBO aguaEsgotoBOMock;
    
    @Mock
    private EsgotoBO esgotoBOMock;
    
    @Mock
    private MensagemContaBO mensagemContaBOMock;
    
    @Mock
    private ExtratoQuitacaoBO extratoQuitacaoBOMock;
    
    @Mock
    private ConsumoBO consumoBOMock;
    
    @Mock
    private FaturamentoAtividadeCronogramaBO faturamentoAtividadeCronogramaBOMock;
    
    @Mock
    private FaturamentoSituacaoBO faturamentoSituacaoBOMock;
    
    @Mock
    private PagamentoBO pagamentoBOMock;
    
	private Imovel imovel;
    private FaturamentoGrupo faturamentoGrupo;
    private GerenciaRegional gerencia;
    private Localidade localidade;
    private ClienteImovel clienteImovelUsuario;
    private ClienteImovel clienteImovelResponsavel;
    private Cliente clienteUsuario;
    private Cliente clienteResponsavel;
    private Rota rota;
    private QuadraFace quadraFace;
    private Conta conta;
    private LigacaoAguaSituacao ligacaoAguaSituacao;
    private LigacaoEsgotoSituacao ligacaoEsgotoSituacao;
    private CobrancaDocumento cobrancaDocumento;
    
    @Before
    public void init(){
        arquivoTextoTipo01 = new ArquivoTextoTipo01();
        
        imovelSetUp();
        
        rota = new Rota();
        rota.setCodigo(Short.valueOf("1"));
        
        faturamentoGrupo = new FaturamentoGrupo(1);
        faturamentoGrupo.setAnoMesReferencia(201501);
        
        conta = new Conta();
        conta.setDataVencimentoConta(new Date());
        conta.setDataValidadeConta(new Date());
        conta.setLigacaoAguaSituacao(ligacaoAguaSituacao);
        conta.setLigacaoEsgotoSituacao(ligacaoEsgotoSituacao);
        conta.setReferencia(201501);
        conta.setId(999999999);
        conta.setDigitoVerificadorConta(Short.valueOf("1"));
        
        cobrancaDocumento = new CobrancaDocumento();
        cobrancaDocumento.setEmissao(new Date());
        cobrancaDocumento.setValorDocumento(new BigDecimal(100.00));
        cobrancaDocumento.setLocalidade(localidade);
        cobrancaDocumento.setImovel(imovel);
        cobrancaDocumento.setNumeroSequenciaDocumento(1);
        cobrancaDocumento.setDocumentoTipo(DocumentoTipo.AVISO_CORTE.getId());
        
        arquivoTextoTipo01.setImovel(imovel);
        arquivoTextoTipo01.setFaturamentoGrupo(faturamentoGrupo);
        arquivoTextoTipo01.setRota(rota);
        arquivoTextoTipo01.setConta(conta);
        arquivoTextoTipo01.setCobrancaDocumento(cobrancaDocumento);
    }
    
    public void imovelSetUp() {
    	imovel = new Imovel(1234567);
    	imovel.setIndicadorImovelCondominio(Status.INATIVO.getId());
    	imovel.setImovelPerfil(new ImovelPerfil(1));
    	imovel.setLote(Short.valueOf("1234"));
    	imovel.setSubLote(Short.valueOf("123"));
    	preencherEnderecoImovel();
    	
    	clienteUsuario = new Cliente();
    	clienteUsuario.setNome("MARIA JOSÉ DA SILVA");
    	clienteUsuario.setCpf("11111111111");
    	
    	clienteResponsavel = new Cliente();
    	clienteResponsavel.setNome("JOÃO ROBERTO SOUZA");
    	clienteResponsavel.setCpf("222.222.222-22");

    	clienteImovelUsuario = new ClienteImovel();
    	clienteImovelUsuario.setCliente(clienteUsuario);
    	clienteImovelUsuario.setImovel(imovel);
    	clienteImovelUsuario.setClienteRelacaoTipo(new ClienteRelacaoTipo(ClienteRelacaoTipo.USUARIO));

    	clienteImovelResponsavel = new ClienteImovel();
    	clienteImovelResponsavel.setCliente(clienteResponsavel);
    	clienteImovelResponsavel.setImovel(imovel);
    	clienteImovelResponsavel.setClienteRelacaoTipo(new ClienteRelacaoTipo(ClienteRelacaoTipo.RESPONSAVEL));
    	
    	List<ClienteImovel> clientesImovel = new ArrayList<ClienteImovel>();
    	clientesImovel.add(clienteImovelUsuario);
    	clientesImovel.add(clienteImovelResponsavel);
    	imovel.setClienteImoveis(clientesImovel);
    	
    	
    	gerencia = new GerenciaRegional();
    	gerencia.setNome("BELEM");
    	
    	localidade = new Localidade(1);
    	localidade.setGerenciaRegional(gerencia);
    	localidade.setDescricao("DESCRICAO DA LOCALIDADE");
    	localidade.setLogradouroCep(imovel.getLogradouroCep());
    	localidade.setLogradouroBairro(imovel.getLogradouroBairro());
    	localidade.setNumeroImovel("10");
    	localidade.setFone("33224455");
    	
    	imovel.setLocalidade(localidade);

    	quadraFace = new QuadraFace(1);
    	
    	Quadra quadra = new Quadra(1);
    	quadra.setNumeroQuadra(1234);
    	imovel.setQuadra(quadra);
    	imovel.setQuadraFace(quadraFace);
    	
    	imovel.setSetorComercial(new SetorComercial(1));
    	
    	ligacaoAguaSituacao = new LigacaoAguaSituacao(LigacaoAguaSituacao.LIGADO);
    	ligacaoAguaSituacao.setSituacaoFaturamento(Status.ATIVO.getId());
    	ligacaoAguaSituacao.setIndicadorAbastecimento(Status.ATIVO.getId());
    	imovel.setLigacaoAguaSituacao(ligacaoAguaSituacao);
    	
    	ligacaoEsgotoSituacao = new LigacaoEsgotoSituacao(LigacaoEsgotoSituacao.POTENCIAL);
    	ligacaoEsgotoSituacao.setSituacaoFaturamento(Status.INATIVO.getId());
    	imovel.setLigacaoEsgotoSituacao(ligacaoEsgotoSituacao);
    	
    	imovel.setConsumoTarifa(new ConsumoTarifa(1));
    	
    	imovel.setImovelContaEnvio(1);
    	
    	LigacaoAgua ligacaoAgua = new LigacaoAgua();
    	ligacaoAgua.setConsumoMinimoAgua(10);
    	imovel.setLigacaoAgua(ligacaoAgua);
    	imovel.setCodigoDebitoAutomatico(888888888);
    	
    	
    }
    
    private List<ICategoria> categoriasSetUp() {
    	Categoria categoria = new Categoria();
    	
    	categoria.setConsumoAlto(50);
    	categoria.setConsumoEstouro(50);
    	categoria.setNumeroConsumoMaximoEc(500);
    	categoria.setMediaBaixoConsumo(30);
    	categoria.setQuantidadeEconomias(1);
    	categoria.setVezesMediaAltoConsumo(new BigDecimal("2.0"));
    	categoria.setVezesMediaEstouro(new BigDecimal("3.0"));
    	categoria.setPorcentagemMediaBaixoConsumo(new BigDecimal("50.0"));
    	
    	List<ICategoria> categorias = new ArrayList<ICategoria>();
    	categorias.add(categoria);
    	
    	return categorias;
    }
    
    private List<ICategoria> subcategoriasSetUp() {
    	Subcategoria subcategoria = new Subcategoria();
    	
    	subcategoria.setIndicadorSazonalidade(Status.INATIVO.getId());

    	List<ICategoria> subcategorias = new ArrayList<ICategoria>();
    	subcategorias.add(subcategoria);
    	
    	return subcategorias;
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
    
    private void preencherEnderecoImovel() {
    	Cep cep = new Cep();
    	cep.setCodigo(66093906);
    	
    	LogradouroCep logradouroCep = new LogradouroCep();
    	Logradouro logradouro = new Logradouro();
    	logradouro.setNome("BARROSO");
    	logradouroCep.setCep(cep);
    	
    	LogradouroTipo logradouroTipo = new LogradouroTipo();
    	logradouroTipo.setDescricaoAbreviada("AV");
    	logradouroTipo.setDescricao("AVENIDA");
    	
    	LogradouroTitulo logradouroTitulo = new LogradouroTitulo();
    	logradouroTitulo.setDescricaoAbreviada("ALM");
    	
    	logradouro.setLogradouroTipo(logradouroTipo);
    	logradouro.setLogradouroTitulo(logradouroTitulo);
    	logradouroCep.setLogradouro(logradouro);
    	
    	Municipio municipio = new Municipio();
    	municipio.setId(1);
    	municipio.setNome("BELEM");
    	municipio.setDdd(Short.valueOf("091"));
    	
    	Bairro bairro = new Bairro();
    	bairro.setId(1);
    	bairro.setNome("MARCO");
    	bairro.setMunicipio(municipio);
    	
    	LogradouroBairro logradouroBairro = new LogradouroBairro();
    	logradouroBairro.setBairro(bairro);
    	
    	imovel.setNumeroImovel("25");
    	imovel.setLogradouroCep(logradouroCep);
    	imovel.setLogradouroBairro(logradouroBairro);
    }
    
    @Test
    public void emitirContaFebrabanCosanpa(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("false");
        replay(repositorioParametros);
        
        
        imovel = new Imovel();
        
        assertFalse(arquivoTextoTipo01.naoEmitirConta(imovel.getImovelContaEnvio()));
    }
    
    @Test
    public void emitirContaJuazeiro(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("true");
        replay(repositorioParametros);
        
        
        imovel = new Imovel();
        imovel.setImovelContaEnvio(ImovelContaEnvio.ENVIAR_IMOVEL.getId());
        
        assertFalse(arquivoTextoTipo01.naoEmitirConta(imovel.getImovelContaEnvio()));
    }
    
    @Test
    public void naoEmitirContaFebrabanCosanpaClienteReponsavelGrupo(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("false");
        replay(repositorioParametros);
        
        imovel = new Imovel();
        imovel.setImovelContaEnvio(ImovelContaEnvio.ENVIAR_CLIENTE_RESPONSAVEL_FINAL_GRUPO.getId());
        
        assertTrue(arquivoTextoTipo01.naoEmitirConta(imovel.getImovelContaEnvio()));
    }
    
    @Test
    public void emitirConta(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("true");
        replay(repositorioParametros);        
        
        imovel = new Imovel();
        
        assertFalse(arquivoTextoTipo01.naoEmitirConta(imovel.getImovelContaEnvio()));
    }
    
    @Test
    public void naoEmitirContaBraille(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("true");
        replay(repositorioParametros);
        
        imovel = new Imovel();
        imovel.setImovelContaEnvio(ImovelContaEnvio.ENVIAR_CONTA_BRAILLE.getId());
        
        assertTrue(arquivoTextoTipo01.naoEmitirConta(imovel.getImovelContaEnvio()));
    }
    
    
    
    @Test
    public void naoEmitirContaFebrabanCosanpa(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("false");
        replay(repositorioParametros);
        
        imovel = new Imovel();
        
        assertTrue(arquivoTextoTipo01.emitirConta(imovel.getImovelContaEnvio()));
    }
    
    @Test
    public void naoEmitirContaJuazeiro(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("true");
        replay(repositorioParametros);
        
        imovel = new Imovel();
        imovel.setImovelContaEnvio(ImovelContaEnvio.ENVIAR_IMOVEL.getId());
        
        assertTrue(arquivoTextoTipo01.emitirConta(imovel.getImovelContaEnvio()));
    }
    
    @Test
    public void emitirContaFebrabanCosanpaClienteReponsavelGrupo(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("false");
        replay(repositorioParametros);

        imovel = new Imovel();
        imovel.setImovelContaEnvio(ImovelContaEnvio.ENVIAR_CLIENTE_RESPONSAVEL_FINAL_GRUPO.getId());
        
        assertFalse(arquivoTextoTipo01.emitirConta(imovel.getImovelContaEnvio()));
    }
    
    @Test
    public void naoEmitirConta(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("true");
        replay(repositorioParametros);
        
        imovel = new Imovel();
        
        assertTrue(arquivoTextoTipo01.emitirConta(imovel.getImovelContaEnvio()));
    }
    
    @Test
    public void emitirContaBraille(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("true");
        replay(repositorioParametros);
        
        imovel = new Imovel();
        imovel.setImovelContaEnvio(ImovelContaEnvio.ENVIAR_CONTA_BRAILLE.getId());
        
        assertFalse(arquivoTextoTipo01.emitirConta(imovel.getImovelContaEnvio()));
    }  
    
    @Test
    public void buildArquivoTextoTipo01() {
    	carregarMocks();
    	
    	assertNotNull(arquivoTextoTipo01.build());
    }
    
    @Test
	public void buildArquivoTextoTipo01TamanhoLinha() {
		carregarMocks();
		
		String linha = arquivoTextoTipo01.build();
		int tamanhoLinha = linha.length();
		
		System.out.println(linha);
		System.out.println(tamanhoLinha);
		
		 assertTrue(tamanhoLinha == 1526);
	}
    
    public void carregarMocks() {
    	DadosBancariosTO to = new DadosBancariosTO();
    	to.setCodigoAgencia("00000");
    	to.setDescricaoBanco("BANCO DO BRASIL");

    	VolumeMedioAguaEsgotoTO volumeMedioTO = new VolumeMedioAguaEsgotoTO(20, 6);
    	
    	boolean instalacaoOuSubstituicaoHidrometro = false;
    	BigDecimal percentualEsgotoAlternativo = new BigDecimal("30");
    	
    	List<ICategoria> categorias = categoriasSetUp(); 
    	List<ICategoria> subcategorias = subcategoriasSetUp();
    	String[] mensagemConta = obterMensagem();
    	
    	expect(debitoAutomaticoRepositorioMock.dadosBancarios(imovel.getId())).andReturn(to);
    	replay(debitoAutomaticoRepositorioMock);
    	
    	expect(hidrometroBOMock.houveInstalacaoOuSubstituicao(imovel.getId())).andReturn(instalacaoOuSubstituicaoHidrometro);
    	replay(hidrometroBOMock);
    	
    	expect(aguaEsgotoBOMock.obterVolumeMedioAguaEsgoto(imovel.getId(),faturamentoGrupo.getAnoMesReferencia(), LigacaoTipo.AGUA.getId(), instalacaoOuSubstituicaoHidrometro))
    		.andReturn(volumeMedioTO);
    	replay(aguaEsgotoBOMock);
    	
    	expect(esgotoBOMock.percentualEsgotoAlternativo(imovel)).andReturn(percentualEsgotoAlternativo);
    	replay(esgotoBOMock);
    	
    	expect(imovelSubcategoriaRepositorioMock.buscarQuantidadeEconomiasCategoria(imovel.getId())).andReturn(categorias);
    	expect(imovelSubcategoriaRepositorioMock.buscarQuantidadeEconomiasSubcategoria(imovel.getId())).andReturn(subcategorias);
    	replay(imovelSubcategoriaRepositorioMock);
    	
    	expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.ESCREVER_MENSAGEM_CONTA_TRES_PARTES)).andReturn("true");
    	expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("false");
    	expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_COMPESA)).andReturn("false");
    	replay(repositorioParametros);
    	
    	expect(mensagemContaBOMock.obterMensagemConta3Partes(imovel, null, null)).andReturn(mensagemConta);
    	replay(mensagemContaBOMock);
    	
    	expect(extratoQuitacaoBOMock.obterMsgQuitacaoDebitos(imovel.getId(), null)).andReturn("MENSAGEM QUITACAO ANUAL DE DEBITOS");
    	replay(extratoQuitacaoBOMock);
    	
    	expect(qualidadeAguaPadraoRepositorioMock.obterLista()).andReturn(obterQualidadeAguaPadrao());
    	replay(qualidadeAguaPadraoRepositorioMock);
    	
    	expect(quadraFaceRepositorioMock.obterPorID(1)).andReturn(quadraFace);
    	replay(quadraFaceRepositorioMock);

    	expect(qualidadeAguaRepositorioMock.buscarPorAnoMesELocalidadeESetorComFonteCaptacao(anyObject(), anyObject(), anyObject())).andReturn(obterQualidadeAgua());
    	replay(qualidadeAguaRepositorioMock);
    	
    	expect(consumoBOMock.consumoMinimoLigacao(imovel.getId())).andReturn(10);
    	expect(consumoBOMock.consumoNaoMedido(imovel.getId(), null)).andReturn(10);
    	replay(consumoBOMock);
    	
    	expect(faturamentoAtividadeCronogramaBOMock.obterDataPrevistaDoCronogramaAnterior(faturamentoGrupo, FaturamentoAtividade.EFETUAR_LEITURA)).andReturn(new Date());
    	replay(faturamentoAtividadeCronogramaBOMock);
    	
    	expect(faturamentoSituacaoBOMock.verificarParalisacaoFaturamentoAgua(imovel, null)).andReturn(Status.ATIVO);
    	expect(faturamentoSituacaoBOMock.verificarParalisacaoFaturamentoEsgoto(imovel, null)).andReturn(Status.INATIVO);
    	replay(faturamentoSituacaoBOMock);
    	
    	expect(pagamentoBOMock.obterCodigoBarra(anyObject())).andReturn("333333333333333333333333333333333333333333333333");
    	replay(pagamentoBOMock);
    }
}
