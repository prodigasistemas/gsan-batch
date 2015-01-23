package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.servicos.faturamento.ExtratoQuitacaoBO;
import br.gov.batch.servicos.faturamento.MensagemContaBO;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo01DadosConta;
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
import br.gov.servicos.faturamento.FaturamentoParametroRepositorio;
import br.gov.servicos.faturamento.QuadraFaceRepositorio;
import br.gov.servicos.faturamento.QualidadeAguaPadraoRepositorio;
import br.gov.servicos.faturamento.QualidadeAguaRepositorio;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo01DadosContaTest {

	@TestSubject
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
    
    private Imovel imovel;
    private QuadraFace quadraFace;
    private Conta conta;
    private FaturamentoGrupo faturamentoGrupo;
    
    @Before
    public void init() {
    	quadraFace = new QuadraFace(1);
        
        faturamentoGrupo = new FaturamentoGrupo(1);
        faturamentoGrupo.setAnoMesReferencia(201501);
        
        conta = new Conta();
        conta.setDataVencimentoConta(new Date());
        conta.setDataValidadeConta(new Date());
        conta.setFaturamentoGrupo(faturamentoGrupo);
        conta.setReferencia(201501);
        conta.setId(999999999);
        conta.setDigitoVerificadorConta(Short.valueOf("1"));
        
    	imovel = new Imovel(1234567);
    	imovel.setIndicadorImovelCondominio(Status.INATIVO.getId());
    	imovel.setLocalidade(new Localidade(1));
    	imovel.setQuadraFace(quadraFace);
    	imovel.setSetorComercial(new SetorComercial(1));
    	
    	arquivo = new ArquivoTextoTipo01DadosConta(imovel, conta);
    }
    
    @Test
    public void buildArquivoDadosCobranca() {
    	carregarMocks();
    	
    	Map<Integer, StringBuilder> mapDados = arquivo.build();
    	
    	StringBuilder linhaValida = new StringBuilder();
    	
    	linhaValida.append("20150123201501232015011999999999MENSAGEM EM CONTA - 1                                              ")
    		.append("                                 MENSAGEM EM CONTA - 2                                                    ")
    		.append("                           MENSAGEM EM CONTA - 3                                                          ")
    		.append("                     MENSAGEM QUITACAO ANUAL DE DEBITOS                                                       ")
    		.append("                                                                                                              ")
    		.append("                                                                                                              ")
    		.append("                                                                                                              ")
    		.append("                                                                                                                 ");
    	
    	System.out.println(mapDados.get(3));
    	System.out.println(mapDados.get(6));
    	System.out.println(mapDados.get(25));
    	System.out.println(mapDados.get(28));
    	
    	System.out.println(mapDados.get(31));
    	System.out.println(mapDados.get(30));
    	System.out.println(mapDados.get(29));
    	String linha = getLinha(mapDados);
    	
    	assertNotNull(mapDados);
    	assertEquals(7, mapDados.keySet().size());
    	assertEquals(linha, linhaValida.toString());
    }
    
    private String getLinha(Map<Integer, StringBuilder> mapDados) {
    	StringBuilder builder = new StringBuilder();
    	
    	Collection<StringBuilder> dados = mapDados.values();
    	
    	Iterator<StringBuilder> it = dados.iterator();
    	
    	while (it.hasNext()) {
    		builder.append(it.next());
    	}
    	
    	return builder.toString();
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
    	
    	expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.ESCREVER_MENSAGEM_CONTA_TRES_PARTES)).andReturn("true");
    	expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("false");
    	expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_COMPESA)).andReturn("false");
    	replay(repositorioParametros);
    	
    	expect(mensagemContaBOMock.obterMensagemConta3Partes(imovel, faturamentoGrupo.getAnoMesReferencia(), faturamentoGrupo.getId())).andReturn(mensagemConta);
    	replay(mensagemContaBOMock);
    	
    	expect(extratoQuitacaoBOMock.obterMsgQuitacaoDebitos(imovel.getId(), faturamentoGrupo.getAnoMesReferencia())).andReturn("MENSAGEM QUITACAO ANUAL DE DEBITOS");
    	replay(extratoQuitacaoBOMock);
    	
    	expect(qualidadeAguaPadraoRepositorioMock.obterLista()).andReturn(obterQualidadeAguaPadrao());
    	replay(qualidadeAguaPadraoRepositorioMock);
    	
    	expect(quadraFaceRepositorioMock.obterPorID(1)).andReturn(quadraFace);
    	replay(quadraFaceRepositorioMock);

    	expect(qualidadeAguaRepositorioMock.buscarPorAnoMesELocalidadeESetorComFonteCaptacao(anyObject(), anyObject(), anyObject())).andReturn(obterQualidadeAgua());
    	replay(qualidadeAguaRepositorioMock);
    	
    }
	
}
