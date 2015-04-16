package br.gov.batch.servicos.micromedicao;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collection;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.cadastro.Categoria;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.micromedicao.ConsumoAnormalidade;
import br.gov.model.micromedicao.ConsumoAnormalidadeAcao;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;
import br.gov.servicos.to.AnormalidadeHistoricoConsumoTO;

@RunWith(EasyMockRunner.class)
public class MensagemAnormalidadeConsumoBOTest {

	@TestSubject
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
		expect(consumoHistoricoRepositorioMock.anormalidadeHistoricoConsumo(1, LigacaoTipo.AGUA, 201504)).andReturn(anormalidade);
		replay(consumoHistoricoRepositorioMock);
	}
	
	private void mockAnormalidadeHistoricoConsumoMensagemAgua() {
		AnormalidadeHistoricoConsumoTO anormalidade = new AnormalidadeHistoricoConsumoTO(null, ConsumoAnormalidade.BAIXO_CONSUMO, null, null);
		expect(consumoHistoricoRepositorioMock.anormalidadeHistoricoConsumo(1, LigacaoTipo.AGUA, 201504)).andReturn(anormalidade).once();
		expect(consumoHistoricoRepositorioMock.anormalidadeHistoricoConsumo(1, LigacaoTipo.AGUA, 201503, ConsumoAnormalidade.BAIXO_CONSUMO)).andReturn(null);
		replay(consumoHistoricoRepositorioMock);
	}

	private void mockCategorias() {
		expect(imovelSubcategoriaRepositorioMock.buscarCategoria(1)).andReturn(categorias);
		replay(imovelSubcategoriaRepositorioMock);
	}

	private void mockAcaoASerTomadaMes1() {
		ConsumoAnormalidadeAcao acao = new ConsumoAnormalidadeAcao();
		acao.setDescricaoContaMensagemMes1("TESTE MES 1");

		expect(consumoAnormalidadeAcaoBOMock.acaoASerTomada(ConsumoAnormalidade.BAIXO_CONSUMO, 2, 1)).andReturn(acao);
		replay(consumoAnormalidadeAcaoBOMock);
	}
}
