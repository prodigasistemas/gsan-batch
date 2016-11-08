package br.gov.batch.servicos.micromedicao;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.model.micromedicao.ConsumoAnormalidadeAcao;
import br.gov.servicos.micromedicao.ConsumoAnormalidadeAcaoRepositorio;

public class ConsumoAnormalidadeAcaoBOTest {

	@InjectMocks
	private ConsumoAnormalidadeAcaoBO bo;

	@Mock
	private ConsumoAnormalidadeAcaoRepositorio consumoAnormalidadeAcaoRepositorioMock;

	@Before
	public void setup() {
		bo = new ConsumoAnormalidadeAcaoBO();
		
		MockitoAnnotations.initMocks(this);
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

		when(consumoAnormalidadeAcaoRepositorioMock.consumoAnormalidadeAcao(1, 1, 1)).thenReturn(anormalidadeAcao);
	}
	
	private void mockAnormalidadeAcaoSemPerfil() {
		ConsumoAnormalidadeAcao anormalidadeAcao = new ConsumoAnormalidadeAcao();
		anormalidadeAcao.setId(2);

		when(consumoAnormalidadeAcaoRepositorioMock.consumoAnormalidadeAcao(1, 1, 1)).thenReturn(null);
		when(consumoAnormalidadeAcaoRepositorioMock.consumoAnormalidadeAcao(1, 1, null)).thenReturn(anormalidadeAcao);
	}
	
	private void mockAnormalidadeAcaoSemPerfilESemCategoria() {
		ConsumoAnormalidadeAcao anormalidadeAcao = new ConsumoAnormalidadeAcao();
		anormalidadeAcao.setId(3);

		when(consumoAnormalidadeAcaoRepositorioMock.consumoAnormalidadeAcao(1, 1, 1)).thenReturn(null);
		when(consumoAnormalidadeAcaoRepositorioMock.consumoAnormalidadeAcao(1, 1, null)).thenReturn(null);
		when(consumoAnormalidadeAcaoRepositorioMock.consumoAnormalidadeAcao(1, null, null)).thenReturn(anormalidadeAcao);
	}
}
