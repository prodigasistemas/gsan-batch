package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.faturamento.QualidadeAgua;
import br.gov.model.faturamento.QualidadeAguaPadrao;
import br.gov.model.micromedicao.Rota;
import br.gov.model.operacional.FonteCaptacao;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo01Test {
    
    @TestSubject
    private ArquivoTextoTipo01 arquivoTextoTipo01;
    
    @Mock
    ArquivoTextoTipo01DadosCliente dadosClienteMock;

    @Mock
    ArquivoTextoTipo01DadosCobranca dadosCobrancaMock;
    
    @Mock
    ArquivoTextoTipo01DadosConsumo dadosConsumoMock;

    @Mock
    ArquivoTextoTipo01DadosConta dadosContaMock;

    @Mock
    ArquivoTextoTipo01DadosFaturamento dadosFaturamentoMock;

    @Mock
    ArquivoTextoTipo01DadosLocalizacaoImovel dadosLocalizacaoImovelMock;
    
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
        conta.setFaturamentoGrupo(faturamentoGrupo);
        
        cobrancaDocumento = new CobrancaDocumento();
        cobrancaDocumento.setEmissao(new Date());
        cobrancaDocumento.setValorDocumento(new BigDecimal(100.00));
        cobrancaDocumento.setLocalidade(localidade);
        cobrancaDocumento.setImovel(imovel);
        cobrancaDocumento.setNumeroSequenciaDocumento(1);
        cobrancaDocumento.setDocumentoTipo(DocumentoTipo.AVISO_CORTE.getId());
        
//        ArquivoTextoTipo01DadosCobranca dadosCobranca = new ArquivoTextoTipo01DadosCobranca(imovel, cobrancaDocumento);
//        ArquivoTextoTipo01DadosCliente dadosCliente = new ArquivoTextoTipo01DadosCliente(imovel);
//        ArquivoTextoTipo01DadosConsumo dadosConsumo = new ArquivoTextoTipo01DadosConsumo(imovel, faturamentoGrupo);
//        ArquivoTextoTipo01DadosConta dadosConta = new ArquivoTextoTipo01DadosConta(imovel, conta);
//        ArquivoTextoTipo01DadosFaturamento dadosFaturamento = new ArquivoTextoTipo01DadosFaturamento(imovel, conta);
//        ArquivoTextoTipo01DadosLocalizacaoImovel dadosLocalizacaoImovel = new ArquivoTextoTipo01DadosLocalizacaoImovel(imovel, rota);
        
        arquivoTextoTipo01.setDadosCliente(dadosClienteMock);
        arquivoTextoTipo01.setDadosCobranca(dadosCobrancaMock);
        arquivoTextoTipo01.setDadosConsumo(dadosConsumoMock);
        arquivoTextoTipo01.setDadosConta(dadosContaMock);
        arquivoTextoTipo01.setDadosFaturamento(dadosFaturamentoMock);
        arquivoTextoTipo01.setDadosLocalizacaoImovel(dadosLocalizacaoImovelMock);
        
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
    public void buildArquivoTextoTipo01() {
    	carregarMocks();
    	
    	String linha01 = arquivoTextoTipo01.build();
    	
    	assertNotNull(linha01);
    	assertEquals(getLinhaValida(), linha01);
    }
    
//    @Test
//	public void buildArquivoTextoTipo01TamanhoLinha() {
//		carregarMocks();
//		
//		String linha = arquivoTextoTipo01.build();
//		int tamanhoLinha = linha.length();
//		
//		System.out.println(linha);
//		System.out.println(tamanhoLinha);
//		
//		 assertTrue(tamanhoLinha == 1526);
//	}
    
    public void carregarMocks() {
    	expect(dadosClienteMock.build()).andReturn(getMapCliente());
    	replay(dadosClienteMock);
    	
    	expect(dadosCobrancaMock.build()).andReturn(getMapCobranca());
    	replay(dadosCobrancaMock);
    	
    	expect(dadosConsumoMock.build()).andReturn(getMapConsumo());
    	replay(dadosConsumoMock);
    	
    	expect(dadosContaMock.build()).andReturn(getMapConta());
    	replay(dadosContaMock);
    	
    	expect(dadosFaturamentoMock.build()).andReturn(getMapFaturamento());
    	replay(dadosFaturamentoMock);
    	
    	expect(dadosLocalizacaoImovelMock.build()).andReturn(getMapLocalizacao());
    	replay(dadosLocalizacaoImovelMock);
    	
    }
    
    private String getLinhaValida() {
    	StringBuilder linha = new StringBuilder();
    	
    	linha.append("01001234567BELEM                    DESCRICAO DA LOCALIDADE  MARIA JOSÉ DA SILVA           201501222")
    		.append("015012200100112341234123AV ALM BARROSO25 - MARCO BELEM 66093-906                              201501")
    		.append("1                                                                                                   ")
    		.append("          31BANCO DO BRASIL00000         201000020121000010      000000030.00 010000500000500000303.")
    		.append("0 2.0 050.00000500001000000199999999900AVENIDAALMBARROSO10 - MARCO BELEM 66093-906                  ")
    		.append("         9133224455 000000000MENSAGEM EM CONTA - 1                                                  ")
    		.append("                             MENSAGEM EM CONTA - 2                                                  ")
    		.append("                             MENSAGEM EM CONTA - 3                                                  ")
    		.append("                             MENSAGEM QUITACAO ANUAL DE DEBITOS                                     ")
    		.append("                                                                                                    ")
    		.append("                                                                                                    ")
    		.append("                                                                                                    ")
    		.append("                                                                                                    ")
    		.append("                                                             000010000010         333333333333333333")
    		.append("33333333333333333333333333333311111111111                                           201501221212888888888            20150122");
    	
 //"01001234567BELEM                    DESCRICAO DA LOCALIDADE  MARIA JOSÉ DA SILVA           201501222015012200100112341234123AV ALM BARROSO25 - MARCO BELEM 66093-906                              2015011                                                                                                             31BANCO DO BRASIL00000         201000020121000010      000000030.00 010000500000500000303.0 2.0 050.00000500001000000199999999900AVENIDAALMBARROSO10 - MARCO BELEM 66093-906                           9133224455 000000000MENSAGEM EM CONTA - 1                                                                               MENSAGEM EM CONTA - 2                                                                               MENSAGEM EM CONTA - 3                                                                               MENSAGEM QUITACAO ANUAL DE DEBITOS                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  000010000010         33333333333333333333333333333333333333333333333311111111111                                           201501221212888888888            20150122"
    	return linha.toString();
    }
    
   // public String getLinhaCompleta(String [] args) {
    public static void main (String [] args) {
    	int tamanhoLinha = 90;
    	
    	String linhaCompleta = "01001234567BELEM                    DESCRICAO DA LOCALIDADE  MARIA JOSÉ DA SILVA           201501222015012200100112341234123AV ALM BARROSO25 - MARCO BELEM 66093-906                              2015011                                                                                                             31BANCO DO BRASIL00000         201000020121000010      000000030.00 010000500000500000303.0 2.0 050.00000500001000000199999999900           AVENIDAALMBARROSO10 - MARCO BELEM 66093-906                           9133224455 000000000MENSAGEM EM CONTA - 1                                                                               MENSAGEM EM CONTA - 2                                                                               MENSAGEM EM CONTA - 3                                                                               MENSAGEM QUITACAO ANUAL DE DEBITOS                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  000010000010         33333333333333333333333333333333333333333333333311111111111                                           201501221212888888888            20150122";
    	String linha1 = linhaCompleta.substring(0, 100);
    	String linha2 = linhaCompleta.substring(100, 200);
    	String linha3 = linhaCompleta.substring(200, 300);
    	String linha4 = linhaCompleta.substring(300, 400);
    	String linha5 = linhaCompleta.substring(400, 500);
    	String linha6 = linhaCompleta.substring(500, 600);
    	String linha7 = linhaCompleta.substring(600, 700);
    	String linha8 = linhaCompleta.substring(700, 800);
    	String linha9 = linhaCompleta.substring(800, 900);
    	String linha10 = linhaCompleta.substring(900, 1000);
    	String linha11 = linhaCompleta.substring(1000, 1100);
    	String linha12 = linhaCompleta.substring(1100, 1200);
    	String linha13 = linhaCompleta.substring(1200, 1300);
    	String linha14 = linhaCompleta.substring(1300, 1400);
    	String linha15 = linhaCompleta.substring(1400, linhaCompleta.length());
    	System.out.println(linha1);
    	System.out.println(linha2);
    	System.out.println(linha3);
    	System.out.println(linha4);
    	System.out.println(linha5);
    	System.out.println(linha6);
    	System.out.println(linha7);
    	System.out.println(linha8);
    	System.out.println(linha9);
    	System.out.println(linha10);
    	System.out.println(linha11);
    	System.out.println(linha12);
    	System.out.println(linha13);
    	System.out.println(linha14);
    	System.out.println(linha15);
    }
    
    private Map<Integer, StringBuilder> getMapCliente() {
    	Map<Integer, StringBuilder> mapClientes = new HashMap<Integer, StringBuilder>();
    	
    	mapClientes.put(2, new StringBuilder("MARIA JOSÉ DA SILVA           "));
    	mapClientes.put(7, new StringBuilder("                                                                                                             "));
    	mapClientes.put(15, new StringBuilder("1"));
    	mapClientes.put(35, new StringBuilder("11111111111       "));
    	
    	return mapClientes;
    }
    
    private Map<Integer, StringBuilder> getMapCobranca() {
    	Map<Integer, StringBuilder> mapCobranca = new HashMap<Integer, StringBuilder>();
    	
    	mapCobranca.put(9, new StringBuilder("BANCO DO BRASIL00000"));
    	mapCobranca.put(34, new StringBuilder("         333333333333333333333333333333333333333333333333"));
    	mapCobranca.put(42, new StringBuilder("         "));
    	mapCobranca.put(45, new StringBuilder("20150123"));
    	
    	return mapCobranca;
    }
    
    private Map<Integer, StringBuilder> getMapConsumo() {
    	Map<Integer, StringBuilder> mapConsumo = new HashMap<Integer, StringBuilder>();
    	
    	mapConsumo.put(12, new StringBuilder("000020"));
    	mapConsumo.put(15, new StringBuilder("000010"));
    	mapConsumo.put(16, new StringBuilder("      "));
    	mapConsumo.put(17, new StringBuilder("000000"));
    	
    	mapConsumo.put(18, new StringBuilder("030.00"));
    	mapConsumo.put(20, new StringBuilder("01"));
    	mapConsumo.put(21, new StringBuilder("0000500000500000303.0 2.0 050.00000500"));
    	mapConsumo.put(43, new StringBuilder("      "));
    	
    	mapConsumo.put(44, new StringBuilder("      "));
    	mapConsumo.put(32, new StringBuilder("000010"));
    	mapConsumo.put(33, new StringBuilder("000010"));
    	mapConsumo.put(25, new StringBuilder("00"));
    	
    	return mapConsumo;
    }
    
    private Map<Integer, StringBuilder> getMapConta() {
    	Map<Integer, StringBuilder> mapConta = new HashMap<Integer, StringBuilder>();
    	
    	mapConta.put(3, new StringBuilder("2015012320150123"));
    	mapConta.put(6, new StringBuilder("2015011"));
    	mapConta.put(24, new StringBuilder("999999999"));
    	mapConta.put(28, getMensagemConta());
    	
    	mapConta.put(14, new StringBuilder("1"));
    	mapConta.put(30, new StringBuilder("                                                                                                                                                                                                                    "));
    	mapConta.put(31, new StringBuilder("                                                                                                                                                                                                        "));
    	mapConta.put(29, new StringBuilder("MENSAGEM QUITACAO ANUAL DE DEBITOS                                                                                      "));
    	
    	return mapConta;
    }
    
    private Map<Integer, StringBuilder> getMapFaturamento() {
    	Map<Integer, StringBuilder> mapFatuamento = new HashMap<Integer, StringBuilder>();
    	
    	mapFatuamento.put(8, new StringBuilder("31"));
    	mapFatuamento.put(10, new StringBuilder("         2"));
    	mapFatuamento.put(13, new StringBuilder("12"));
    	mapFatuamento.put(36, new StringBuilder("                                    "));
    	
    	mapFatuamento.put(38, new StringBuilder("1"));
    	mapFatuamento.put(11, new StringBuilder("01"));
    	mapFatuamento.put(19, new StringBuilder(" "));
    	mapFatuamento.put(37, new StringBuilder("20150123"));
    	
    	mapFatuamento.put(39, new StringBuilder(""));
    	mapFatuamento.put(40, new StringBuilder("1"));
    	mapFatuamento.put(41, new StringBuilder("2"));
    	
    	return mapFatuamento;
    }
    
    private Map<Integer, StringBuilder> getMapLocalizacao() {
    	Map<Integer, StringBuilder> mapLocalizacao = new HashMap<Integer, StringBuilder>();
    	
    	mapLocalizacao.put(0, new StringBuilder("BELEM                    "));
    	mapLocalizacao.put(1, new StringBuilder("DESCRICAO DA LOCALIDADE  "));
    	mapLocalizacao.put(22, new StringBuilder("001"));
    	mapLocalizacao.put(23, new StringBuilder("0000001"));
    	
    	mapLocalizacao.put(27, new StringBuilder("000000000"));
    	mapLocalizacao.put(4, new StringBuilder("00100112341234123"));
    	mapLocalizacao.put(5, new StringBuilder("AV ALM BARROSO25 - MARCO BELEM 66093-906                              "));
    	mapLocalizacao.put(26, new StringBuilder("           "));
    	return mapLocalizacao;
    }
    
    private StringBuilder getMensagemConta() {
    	
    	StringBuilder mensagem = new StringBuilder();
    	
    	mensagem.append("MENSAGEM EM CONTA - 1                                                                               ")
    			.append("MENSAGEM EM CONTA - 2                                                                               ")
    			.append("MENSAGEM EM CONTA - 3                                                                               ");
    	
    	return mensagem;
    }
}
