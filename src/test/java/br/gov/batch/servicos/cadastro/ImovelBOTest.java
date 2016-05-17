package br.gov.batch.servicos.cadastro;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.cadastro.to.AreaConstruidaTO;

public class ImovelBOTest {

	@InjectMocks
	private ImovelBO imovelBO;

	@Mock
	private ImovelRepositorio imovelRepositorioMock;

	@Before
	public void setup() {
		imovelBO = new ImovelBO();
		
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void pesquisarFaturamentoGrupo() {
		mockFaturamentoGrupo(null, new FaturamentoGrupo(301));

		FaturamentoGrupo grupo = imovelBO.pesquisarFaturamentoGrupo(1);

		assertEquals(301, grupo.getId().intValue());
	}

	@Test
	public void pesquisarFaturamentoGrupoRotaAlternativa() {
		mockFaturamentoGrupo(new FaturamentoGrupo(302), null);

		FaturamentoGrupo grupo = imovelBO.pesquisarFaturamentoGrupo(1);

		assertEquals(302, grupo.getId().intValue());
	}

	@Test
	public void verificarAreaConstruidaNula() {
		mockAreaConstruida(new AreaConstruidaTO(null, 15));

		BigDecimal areaConstruida = imovelBO.verificarAreaConstruida(1);

		assertEquals(BigDecimal.valueOf(15), areaConstruida);
	}

	@Test
	public void verificarAreaConstruida() {
		mockAreaConstruida(new AreaConstruidaTO(BigDecimal.valueOf(123.60), 15));

		BigDecimal areaConstruida = imovelBO.verificarAreaConstruida(1);

		assertEquals(BigDecimal.ONE, areaConstruida);
	}

	@Test
	public void verificarAreaConstruidaSemFaixa() {
		mockAreaConstruida(new AreaConstruidaTO(null, null));

		BigDecimal areaConstruida = imovelBO.verificarAreaConstruida(1);

		assertEquals(BigDecimal.ONE, areaConstruida);
	}

	private void mockFaturamentoGrupo(FaturamentoGrupo grupoRotaAlternativa, FaturamentoGrupo grupo) {
		when(imovelRepositorioMock.pesquisarFaturamentoGrupoRotaAlternativa(1)).thenReturn(grupoRotaAlternativa);
		when(imovelRepositorioMock.pesquisarFaturamentoGrupo(1)).thenReturn(grupo);
	}

	private void mockAreaConstruida(AreaConstruidaTO to) {
		when(imovelRepositorioMock.dadosAreaConstruida(1)).thenReturn(to);
	}
}
