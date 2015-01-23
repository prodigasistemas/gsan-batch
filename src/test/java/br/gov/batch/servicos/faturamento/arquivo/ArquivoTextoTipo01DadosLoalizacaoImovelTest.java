package br.gov.batch.servicos.faturamento.arquivo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;

import br.gov.model.cadastro.GerenciaRegional;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.Localidade;
import br.gov.model.cadastro.Quadra;
import br.gov.model.cadastro.QuadraFace;
import br.gov.model.cadastro.SetorComercial;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.Rota;

public class ArquivoTextoTipo01DadosLoalizacaoImovelTest {

	@TestSubject
	private ArquivoTextoTipo01DadosLocalizacaoImovel arquivo;
	
	private Imovel imovel;
	private Rota rota;
	
	@Before
	public void init() {

    	imovel = new Imovel(1234567);
    	
    	GerenciaRegional gerencia = new GerenciaRegional();
    	gerencia.setNome("BELEM");
    	
    	FaturamentoGrupo grupo = new FaturamentoGrupo(1);
    	
    	rota = new Rota();
    	rota.setFaturamentoGrupo(grupo);
    	rota.setCodigo(Short.valueOf("99"));
    	
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
    	
    	arquivo = new ArquivoTextoTipo01DadosLocalizacaoImovel(imovel, rota);
	}
	
	@Test
    public void buildArquivoDadosCobranca() {
    	Map<Integer, StringBuilder> mapDados = arquivo.build();
    	
    	String linha = getLinha(mapDados);

    	StringBuilder linhaValida = new StringBuilder();
    	linhaValida.append("BELEM                    DESCRICAO DA LOCALIDADE  00100112340000000                                 ")
    		.append("                                     0010000099                                                            ")
    		.append("          33224455   000000000");

    	
    	System.out.println(mapDados.get(0));
    	System.out.println(mapDados.get(1));
    	System.out.println(mapDados.get(23));
    	System.out.println(mapDados.get(24));
    	
    	System.out.println(mapDados.get(28));
    	System.out.println(mapDados.get(4));
    	System.out.println(mapDados.get(5));
    	assertNotNull(mapDados);
    	assertEquals(8, mapDados.keySet().size());
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
}
