package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.GerenciaRegional;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.Localidade;
import br.gov.model.cadastro.Quadra;
import br.gov.model.cadastro.QuadraFace;
import br.gov.model.cadastro.SetorComercial;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.Rota;
import br.gov.servicos.cadastro.ImovelRepositorio;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo01DadosLoalizacaoImovelTest {

	@TestSubject
	private ArquivoTextoTipo01DadosLocalizacaoImovel arquivo;
	
	@Mock
	private ImovelRepositorio repositorioImovel;
	
	private Imovel imovel;
	private Rota rota;
	
    private ArquivoTextoTO arquivoTextoTO;

	@Before
	public void setup() {

    	imovel = new Imovel(1234567);
    	
    	GerenciaRegional gerencia = new GerenciaRegional();
    	gerencia.setNome("BELEM");
    	
    	FaturamentoGrupo grupo = new FaturamentoGrupo(1);
    	
    	rota = new Rota();
    	rota.setFaturamentoGrupo(grupo);
    	rota.setCodigo(Short.valueOf("1"));
    	
    	Localidade localidade = new Localidade(1);
    	localidade.setGerenciaRegional(gerencia);
    	localidade.setDescricao("DESCRICAO DA LOCALIDADE");
    	localidade.setNumeroImovel("10");
    	localidade.setFone("33224455");
    	
    	imovel.setLocalidade(localidade);

    	Quadra quadra = new Quadra(1);
    	quadra.setNumeroQuadra(1234);
    	imovel.setQuadra(quadra);
    	imovel.setQuadraFace(new QuadraFace(1));
    	
    	imovel.setSetorComercial(new SetorComercial(1));
    	
    	arquivo = new ArquivoTextoTipo01DadosLocalizacaoImovel();
    	
    	arquivoTextoTO = new ArquivoTextoTO();
    	arquivoTextoTO.setRota(rota);
    	arquivoTextoTO.setImovel(imovel);
	}
	
	@Test
    public void buildArquivoDadosCobranca() {
	    carregarMocks();
	    
    	Map<Integer, StringBuilder> mapDados = arquivo.build(arquivoTextoTO);
    	
    	String linha = getLinha(mapDados);

    	StringBuilder linhaValida = new StringBuilder();
    	linhaValida.append("BELEM                    DESCRICAO DA LOCALIDADE  00100012340000000                                 ")
    		       .append("                                     0010000001                                                            ")
    		       .append("          33224455   000000000");

    	
    	assertNotNull(mapDados);
    	assertEquals(8, mapDados.keySet().size());
    	assertEquals(linhaValida.toString(), linha);
    }
    
    private String getLinha(Map<Integer, StringBuilder> mapDados) {
    	StringBuilder builder = new StringBuilder();
    	
    	Collection<StringBuilder> dados = mapDados.values();
    	
    	Iterator<StringBuilder> iterator = dados.iterator();
    	while (iterator.hasNext()) {
    		builder.append(iterator.next());
    	}
    	
    	return builder.toString();
    }
    
    public void carregarMocks() {
        expect(repositorioImovel.obterPorID(imovel.getId())).andReturn(imovel);
        replay(repositorioImovel);
    }
}
