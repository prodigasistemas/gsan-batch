package br.gov.batch.servicos.cadastro;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.cadastro.to.AreaConstruidaTO;

@RunWith(EasyMockRunner.class)
public class ImovelBOTest {

	@TestSubject
	private ImovelBO imovelBO;

	@Mock
	private ImovelRepositorio imovelRepositorioMock;

	@Before
	public void setup() {
		imovelBO = new ImovelBO();
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
		expect(imovelRepositorioMock.pesquisarFaturamentoGrupoRotaAlternativa(1)).andReturn(grupoRotaAlternativa);
		expect(imovelRepositorioMock.pesquisarFaturamentoGrupo(1)).andReturn(grupo);
		replay(imovelRepositorioMock);
	}

	private void mockAreaConstruida(AreaConstruidaTO to) {
		expect(imovelRepositorioMock.dadosAreaConstruida(1)).andReturn(to);
		replay(imovelRepositorioMock);
	}
}
