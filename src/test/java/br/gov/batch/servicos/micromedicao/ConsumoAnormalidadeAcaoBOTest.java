package br.gov.batch.servicos.micromedicao;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.micromedicao.ConsumoAnormalidadeAcao;
import br.gov.servicos.micromedicao.ConsumoAnormalidadeAcaoRepositorio;

@RunWith(EasyMockRunner.class)
public class ConsumoAnormalidadeAcaoBOTest {

	@TestSubject
	private ConsumoAnormalidadeAcaoBO bo;

	@Mock
	private ConsumoAnormalidadeAcaoRepositorio consumoAnormalidadeAcaoRepositorioMock;

	@Before
	public void setup() {
		bo = new ConsumoAnormalidadeAcaoBO();
	}

	@Test
	public void anormalidadeAcaoComPerfil() {
		mockAnormalidadeAcaoComPerfil();

		ConsumoAnormalidadeAcao anormalidadeAcao = bo.acaoASerTomada(1, 1, 1);

		assertEquals(1, anormalidadeAcao.getId().intValue());
	}
	
	@Test
	public void anormalidadeAcaoSemPerfil() {
		mockAnormalidadeAcaoSemPerfil();

		ConsumoAnormalidadeAcao anormalidadeAcao = bo.acaoASerTomada(1, 1, 1);

		assertEquals(2, anormalidadeAcao.getId().intValue());
	}
	
	@Test
	public void anormalidadeAcaoSemPerfilESemCategoria() {
		mockAnormalidadeAcaoSemPerfilESemCategoria();

		ConsumoAnormalidadeAcao anormalidadeAcao = bo.acaoASerTomada(1, 1, 1);

		assertEquals(3, anormalidadeAcao.getId().intValue());
	}

	private void mockAnormalidadeAcaoComPerfil() {
		ConsumoAnormalidadeAcao anormalidadeAcao = new ConsumoAnormalidadeAcao();
		anormalidadeAcao.setId(1);

		expect(consumoAnormalidadeAcaoRepositorioMock.consumoAnormalidadeAcao(1, 1, 1)).andReturn(anormalidadeAcao);
		replay(consumoAnormalidadeAcaoRepositorioMock);
	}
	
	private void mockAnormalidadeAcaoSemPerfil() {
		ConsumoAnormalidadeAcao anormalidadeAcao = new ConsumoAnormalidadeAcao();
		anormalidadeAcao.setId(2);

		expect(consumoAnormalidadeAcaoRepositorioMock.consumoAnormalidadeAcao(1, 1, 1)).andReturn(null);
		expect(consumoAnormalidadeAcaoRepositorioMock.consumoAnormalidadeAcao(1, 1, null)).andReturn(anormalidadeAcao);
		replay(consumoAnormalidadeAcaoRepositorioMock);
	}
	
	private void mockAnormalidadeAcaoSemPerfilESemCategoria() {
		ConsumoAnormalidadeAcao anormalidadeAcao = new ConsumoAnormalidadeAcao();
		anormalidadeAcao.setId(3);

		expect(consumoAnormalidadeAcaoRepositorioMock.consumoAnormalidadeAcao(1, 1, 1)).andReturn(null);
		expect(consumoAnormalidadeAcaoRepositorioMock.consumoAnormalidadeAcao(1, 1, null)).andReturn(null);
		expect(consumoAnormalidadeAcaoRepositorioMock.consumoAnormalidadeAcao(1, null, null)).andReturn(anormalidadeAcao);
		replay(consumoAnormalidadeAcaoRepositorioMock);
	}
}
