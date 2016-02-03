package br.gov.batch.servicos.faturamento;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.cadastro.Cliente;
import br.gov.model.faturamento.ImpostoTipo;
import br.gov.model.faturamento.ImpostoTipoAliquota;
import br.gov.servicos.cadastro.ClienteRepositorio;
import br.gov.servicos.faturamento.ImpostoTipoAliquotaRepositorio;
import br.gov.servicos.faturamento.ImpostoTipoRepositorio;
import br.gov.servicos.to.ImpostosDeduzidosContaTO;

@RunWith(EasyMockRunner.class)
public class ImpostosContaBOTest {

	@TestSubject
	private ImpostosContaBO bo;

	@Mock
	private ClienteRepositorio clienteRepositorioMock;
	
	@Mock
	private ImpostoTipoRepositorio impostoTipoRepositorioMock;
	
	@Mock
	private ImpostoTipoAliquotaRepositorio impostoTipoAliquotaRepositorioMock;

	@Before
	public void setup() {
		bo = new ImpostosContaBO();
	}

	@Test
	public void clienteFederalNulo() {
		mockClienteFederalNulo();

		ImpostosDeduzidosContaTO to = bo.gerarImpostosDeduzidosConta(1, 201601, new BigDecimal("10.00"), new BigDecimal("3.00"));

		assertEquals(null, to.getListaImpostosDeduzidos());
		assertEquals(new BigDecimal("0.00"), to.getValorBaseCalculo());
		assertEquals(new BigDecimal("0.00"), to.getValorTotalImposto());
	}

	@Test
	public void calculoImpostosDeduzidos() {
		mockClienteFederal();
		mockImpostoTipo();
		mockImpostoTipoAliquota();
		
		ImpostosDeduzidosContaTO to = bo.gerarImpostosDeduzidosConta(1, 201601, new BigDecimal("10.00"), new BigDecimal("3.00"));

		assertEquals(2, to.getListaImpostosDeduzidos().size());
		assertEquals(new BigDecimal("0.00"), to.getValorBaseCalculo());
		assertEquals(new BigDecimal("0.00"), to.getValorTotalImposto());
	}
	
	private void mockClienteFederalNulo() {
		expect(clienteRepositorioMock.buscarClienteFederalResponsavelPorImovel(1)).andReturn(null);
		replay(clienteRepositorioMock);
	}
	
	private void mockClienteFederal() {
		expect(clienteRepositorioMock.buscarClienteFederalResponsavelPorImovel(1)).andReturn(new Cliente());
		replay(clienteRepositorioMock);
	}
	
	private void mockImpostoTipo() {
		expect(impostoTipoRepositorioMock.buscarImpostoTipoAtivos()).andReturn(getImpostosTipo());
		replay(impostoTipoRepositorioMock);
	}
	
	private void mockImpostoTipoAliquota() {
		expect(impostoTipoAliquotaRepositorioMock.buscarAliquotaImposto(1, 201601)).andReturn(getImpostosTipoAliquota()).times(2);
		replay(impostoTipoAliquotaRepositorioMock);
	}	

	private Collection<ImpostoTipo> getImpostosTipo() {
		Collection<ImpostoTipo> impostosTipo = new ArrayList<ImpostoTipo>();
		ImpostoTipo impostoTipo = new ImpostoTipo();
		impostoTipo.setId(1);
		impostosTipo.add(impostoTipo);
		impostosTipo.add(impostoTipo);
		
		return impostosTipo;
	}
	
	private ImpostoTipoAliquota getImpostosTipoAliquota() {
		ImpostoTipoAliquota aliquota = new ImpostoTipoAliquota();
		ImpostoTipo impostoTipo = new ImpostoTipo();
		impostoTipo.setId(1);
		aliquota.setImpostoTipo(impostoTipo);
		aliquota.setPercentualAliquota(new BigDecimal("2.00"));
		
		return aliquota;
	}
}
