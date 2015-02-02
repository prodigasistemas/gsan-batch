package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.micromedicao.ConsumoAnormalidade;
import br.gov.servicos.micromedicao.ConsumoAnormalidadeRepositorio;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo13Test {

	@TestSubject
	private ArquivoTextoTipo13 arquivoTextoTipo13;
	
	@Mock
	private ConsumoAnormalidadeRepositorio repositorio;
	
	private ConsumoAnormalidade consumo;
	
	private List<ConsumoAnormalidade> lista;
	
	@Before
	public void init(){
		arquivoTextoTipo13 = new ArquivoTextoTipo13();
		
		consumo = new ConsumoAnormalidade();
		consumo.setId(22);
		consumo.setMensagemConta("MENSAGEM DE CONSUMO ANORMALIDADE MOCK TESTE");
		
		lista = new ArrayList<ConsumoAnormalidade>();
		lista.add(consumo);
	}
	
	@Test
	public void buildLinha13(){
		expect(repositorio.listarConsumoAnormalidadePor(Short.valueOf("1"))).andReturn(lista);
		replay(repositorio);
		
		String linha = arquivoTextoTipo13.build();
		System.out.println(linha);
		System.out.println(linha.length());
		
		assertTrue(linha.length() >= 125);
	}
}


