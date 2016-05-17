package br.gov.batch.servicos.faturamento.arquivo;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.micromedicao.ConsumoAnormalidade;
import br.gov.servicos.micromedicao.ConsumoAnormalidadeRepositorio;

public class ArquivoTextoTipo13Test {

	@InjectMocks
	private ArquivoTextoTipo13 arquivo;
	
	private int TAMANHO_LINHA = 125;

	@Mock
	private ConsumoAnormalidadeRepositorio repositorio;

	private ConsumoAnormalidade consumo;

	private List<ConsumoAnormalidade> lista;

	@Before
	public void init() {
		arquivo = new ArquivoTextoTipo13();

		consumo = new ConsumoAnormalidade();
		consumo.setId(22);
		consumo.setMensagemConta("MENSAGEM DE CONSUMO ANORMALIDADE MOCK TESTE");
		lista = new ArrayList<ConsumoAnormalidade>();
		lista.add(consumo);
		
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void buildLinhaTamanhoValido() {
		when(repositorio.listarConsumoAnormalidadePor(Short.valueOf("1"))).thenReturn(lista);

		String linha = arquivo.build(new ArquivoTextoTO());
		assertTrue(linha.length() >= TAMANHO_LINHA);
	}
}
