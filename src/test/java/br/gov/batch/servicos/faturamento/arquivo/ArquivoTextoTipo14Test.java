package br.gov.batch.servicos.faturamento.arquivo;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.Status;
import br.gov.model.micromedicao.LeituraAnormalidade;
import br.gov.model.micromedicao.LeituraAnormalidadeConsumo;
import br.gov.model.micromedicao.LeituraAnormalidadeLeitura;
import br.gov.servicos.micromedicao.LeituraAnormalidadeRepositorio;

public class ArquivoTextoTipo14Test {

	@InjectMocks
	private ArquivoTextoTipo14 arquivo;
	
	private int TAMANHO_LINHA = 49;

	@Mock
	private LeituraAnormalidadeRepositorio repositorio;

	private LeituraAnormalidade leituraAnormalidade;

	private List<LeituraAnormalidade> lista;

	private ArquivoTextoTO to;
	
	@Before
	public void setup() {
		arquivo = new ArquivoTextoTipo14();
		to = new ArquivoTextoTO();
		to.setIdLeituraAnormalidade(1);

		leituraAnormalidade = new LeituraAnormalidade();
		leituraAnormalidade.setId(1);
		leituraAnormalidade.setDescricao("DESC ANORMALIDADE");
		leituraAnormalidade.setIndicadorLeitura(Short.valueOf("1"));

		leituraAnormalidade.setNumeroFatorComLeitura(BigDecimal.valueOf(2.00));
		LeituraAnormalidadeConsumo l1 = new LeituraAnormalidadeConsumo();
		l1.setId(8);
		LeituraAnormalidadeConsumo l2 = new LeituraAnormalidadeConsumo();
		l2.setId(8);
		leituraAnormalidade.setLeituraAnormalidadeConsumoComLeitura(l1);
		leituraAnormalidade.setLeituraAnormalidadeConsumoSemLeitura(l2);

		LeituraAnormalidadeLeitura l3 = new LeituraAnormalidadeLeitura();
		l3.setId(9);
		LeituraAnormalidadeLeitura l4 = new LeituraAnormalidadeLeitura();
		l4.setId(9);
		leituraAnormalidade.setLeituraAnormalidadeLeituraSemLeitura(l4);
		leituraAnormalidade.setIndicadorUso(Short.valueOf("1"));
		lista = new ArrayList<LeituraAnormalidade>();
		lista.add(leituraAnormalidade);
		
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void buildLinha14() {
		carregarMocks();

		String linha = arquivo.build(to);
		assertTrue(linha.length() >= TAMANHO_LINHA);
	}

	private void carregarMocks() {
		when(repositorio.listarLeituraAnormalidadePor(1, Status.ATIVO.getId())).thenReturn(lista);
		when(repositorio.listarLeituraAnormalidadeImpressaoSimultanea(Status.ATIVO.getId())).thenReturn(lista);
	}
}
