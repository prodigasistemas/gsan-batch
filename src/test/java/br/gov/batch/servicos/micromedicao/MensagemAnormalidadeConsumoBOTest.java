package br.gov.batch.servicos.micromedicao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.model.cadastro.Categoria;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.micromedicao.ConsumoAnormalidade;
import br.gov.model.micromedicao.ConsumoAnormalidadeAcao;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;
import br.gov.servicos.to.AnormalidadeHistoricoConsumoTO;

public class MensagemAnormalidadeConsumoBOTest {

	@InjectMocks
	private MensagemAnormalidadeConsumoBO bo;

	@Mock
	private ConsumoHistoricoRepositorio consumoHistoricoRepositorioMock;

	@Mock
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorioMock;

	@Mock
	private ConsumoAnormalidadeAcaoBO consumoAnormalidadeAcaoBOMock;

	private Collection<ICategoria> categorias;

	@Before
	public void setup() {
		bo = new MensagemAnormalidadeConsumoBO();

		categorias = new ArrayList<ICategoria>();
		Categoria categoria = new Categoria(1);
		categoria.setQuantidadeEconomias(2);
		categorias.add(categoria);

		categoria = new Categoria(2);
		categoria.setQuantidadeEconomias(5);
		categorias.add(categoria);
		
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void mensagemNula() {
		mockAnormalidadeHistoricoConsumoAguaMensagemNula();

		assertNull(bo.mensagensAnormalidadeConsumo(1, 201504, 1));
	}

	@Test
	public void mensagemAgua() {
		mockAnormalidadeHistoricoConsumoMensagemAgua();
		mockCategorias();
		mockAcaoASerTomadaMes1();

		String[] mensagens = bo.mensagensAnormalidadeConsumo(1, 201504, 1);
		
		assertEquals("TESTE MES 1", mensagens[0]);
		assertEquals("", mensagens[1]);
		assertEquals("", mensagens[2]);
	}

	private void mockAnormalidadeHistoricoConsumoAguaMensagemNula() {
		AnormalidadeHistoricoConsumoTO anormalidade = new AnormalidadeHistoricoConsumoTO(null, ConsumoAnormalidade.CONSUMO_ALTERADO, null, null);
		when(consumoHistoricoRepositorioMock.anormalidadeHistoricoConsumo(1, LigacaoTipo.AGUA, 201504)).thenReturn(anormalidade);
	}
	
	private void mockAnormalidadeHistoricoConsumoMensagemAgua() {
		AnormalidadeHistoricoConsumoTO anormalidade = new AnormalidadeHistoricoConsumoTO(null, ConsumoAnormalidade.BAIXO_CONSUMO, null, null);
		when(consumoHistoricoRepositorioMock.anormalidadeHistoricoConsumo(1, LigacaoTipo.AGUA, 201504)).thenReturn(anormalidade);
		when(consumoHistoricoRepositorioMock.anormalidadeHistoricoConsumo(1, LigacaoTipo.AGUA, 201503, ConsumoAnormalidade.BAIXO_CONSUMO)).thenReturn(null);
	}

	private void mockCategorias() {
		when(imovelSubcategoriaRepositorioMock.buscarCategoria(1)).thenReturn(categorias);
	}

	private void mockAcaoASerTomadaMes1() {
		ConsumoAnormalidadeAcao acao = new ConsumoAnormalidadeAcao();
		acao.setDescricaoContaMensagemMes1("TESTE MES 1");

		when(consumoAnormalidadeAcaoBOMock.acaoASerTomada(ConsumoAnormalidade.BAIXO_CONSUMO, 2, 1)).thenReturn(acao);
	}
}
