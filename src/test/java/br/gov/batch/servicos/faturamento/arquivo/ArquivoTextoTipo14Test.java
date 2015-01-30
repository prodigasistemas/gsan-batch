package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.Status;
import br.gov.model.micromedicao.LeituraAnormalidade;
import br.gov.model.micromedicao.LeituraAnormalidadeConsumo;
import br.gov.servicos.micromedicao.LeituraAnormalidadeRepositorio;


@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo14Test {

	@Inject
	private ArquivoTextoTipo14 arquivoTextoTipo14;
	
	@Mock
	private LeituraAnormalidadeRepositorio repositorio;
	
	private LeituraAnormalidade leituraAnormalidade;
	
	private List<LeituraAnormalidade> lista;
	
	@Before
	public void init(){
		arquivoTextoTipo14 = new ArquivoTextoTipo14();
		
		leituraAnormalidade = new LeituraAnormalidade();
		leituraAnormalidade.setId(1);
		leituraAnormalidade.setDescricao("DESC ANORMALIDADE");
		leituraAnormalidade.setIndicadorLeitura(Short.valueOf("1"));
		LeituraAnormalidadeConsumo l = new LeituraAnormalidadeConsumo();
		l.setId(8);
		leituraAnormalidade.setLeituraAnormalidadeConsumoComLeitura(l);
		
		lista = new ArrayList<LeituraAnormalidade>();
		lista.add(leituraAnormalidade);
	}
	
	@Test
	public void buildLinha14(){
		loadMocks();
		
		String linha = arquivoTextoTipo14.build(Integer.valueOf("1"));
		System.out.println(linha);
		System.out.println(linha.length());
		
		assertTrue(linha.length() >= 49);
	}

	private void loadMocks() {
		expect(repositorio.listarLeituraAnormalidadePor(1, Status.ATIVO.getId())).andReturn(lista);
		replay(repositorio);
	}
	
	
}
