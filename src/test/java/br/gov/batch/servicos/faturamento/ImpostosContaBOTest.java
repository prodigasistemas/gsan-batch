package br.gov.batch.servicos.faturamento;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.model.cadastro.Cliente;
import br.gov.model.faturamento.ImpostoTipo;
import br.gov.model.faturamento.ImpostoTipoAliquota;
import br.gov.servicos.cadastro.ClienteRepositorio;
import br.gov.servicos.faturamento.ImpostoTipoAliquotaRepositorio;
import br.gov.servicos.faturamento.ImpostoTipoRepositorio;
import br.gov.servicos.to.ImpostosDeduzidosContaTO;

public class ImpostosContaBOTest {

	@InjectMocks
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
		
		MockitoAnnotations.initMocks(this);
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
		when(clienteRepositorioMock.buscarClienteFederalResponsavelPorImovel(1)).thenReturn(null);
	}
	
	private void mockClienteFederal() {
		when(clienteRepositorioMock.buscarClienteFederalResponsavelPorImovel(1)).thenReturn(new Cliente());
	}
	
	private void mockImpostoTipo() {
		when(impostoTipoRepositorioMock.buscarImpostoTipoAtivos()).thenReturn(getImpostosTipo());
	}
	
	private void mockImpostoTipoAliquota() {
		when(impostoTipoAliquotaRepositorioMock.buscarAliquotaImposto(1, 201601)).thenReturn(getImpostosTipoAliquota());
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
