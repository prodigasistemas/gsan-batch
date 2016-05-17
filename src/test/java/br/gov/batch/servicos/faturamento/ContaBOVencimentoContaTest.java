package br.gov.batch.servicos.faturamento;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;

import br.gov.model.Status;
import br.gov.model.cadastro.Cliente;
import br.gov.model.cadastro.ClienteRelacaoTipo;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.ImovelContaEnvio;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.servicos.cadastro.ClienteRepositorio;

public class ContaBOVencimentoContaTest {
	
	@InjectMocks
	private ContaBO contaBO;
	
	@Mock
	private ClienteRepositorio clienteRepositorioMock;
	
	@Mock
	private SistemaParametros sistemaParametrosMock;
	
	private String textoDia10MesSeguinte;
	
	private Date dia15ProximoMes;
	
	private Date diaRotaMarco;
	
	private Date diaRotaAbril;
	
	private DateFormat format = new SimpleDateFormat("dd-MM-yyyy");
	
	private Imovel imovel;
	
	@Before
	public void setUp(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, 15);
		dia15ProximoMes = cal.getTime();
		textoDia10MesSeguinte = format.format(cal.getTime());
		
		imovel = new Imovel();
		imovel.setId(1);
		
		cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.MONTH, Calendar.MARCH);
		cal.set(Calendar.DAY_OF_MONTH, 02);
		diaRotaMarco = cal.getTime();
		
		cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.MONTH, Calendar.APRIL);
		cal.set(Calendar.DAY_OF_MONTH, 01);
		diaRotaAbril= cal.getTime();
		
		contaBO = new ContaBO();
		
		MockitoAnnotations.initMocks(this);
	}
	
	private OngoingStubbing<Cliente> mountClienteRepositorioMock() {
		return when(clienteRepositorioMock.buscarClientePorImovel(1, ClienteRelacaoTipo.RESPONSAVEL));
	}
	
	private OngoingStubbing<Short> mountSistemaParametrosRepositorioMock() {
		return when(sistemaParametrosMock.getNumeroMinimoDiasEmissaoVencimento());
	}
	
	private OngoingStubbing<Short> mountSistemaParametrosRepositorioDiasCorreiosMock() {
		return when(sistemaParametrosMock.getNumeroDiasAdicionaisCorreios());
	}
	
	@Test
	public void semVencimentoAlternativo(){
		mountClienteRepositorioMock().thenReturn(null);
		mountSistemaParametrosRepositorioMock().thenReturn((short) 10);
		
		assertEquals(textoDia10MesSeguinte, format.format(contaBO.determinarVencimentoConta(imovel, dia15ProximoMes)));
	}
	
	@Test
	public void vencimentoAlternativoImovelAntesVencerRota(){
		imovel.setDiaVencimento((short) 5);
		mountClienteRepositorioMock().thenReturn(null);
		mountSistemaParametrosRepositorioMock().thenReturn((short) 3);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dia15ProximoMes);
		
		assertEquals(format.format(cal.getTime()), format.format(contaBO.determinarVencimentoConta(imovel, dia15ProximoMes)));
	}
	
	@Test
	public void vencimentoAlternativoImovelAposVencerRota(){
		imovel.setDiaVencimento((short) 25);
		mountClienteRepositorioMock().thenReturn(null);
		mountSistemaParametrosRepositorioMock().thenReturn((short) 10);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dia15ProximoMes);
		cal.set(Calendar.DAY_OF_MONTH, 25);
		
		assertEquals(format.format(cal.getTime()), format.format(contaBO.determinarVencimentoConta(imovel, dia15ProximoMes)));
	}

	@Test
	public void vencimentoAlternativoImovelComDiasParaEmissaoSuperiorAoVencimento(){
		imovel.setDiaVencimento((short) 5);
		mountClienteRepositorioMock().thenReturn(null);
		mountSistemaParametrosRepositorioMock().thenReturn((short) 60);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dia15ProximoMes);
		cal.set(Calendar.DAY_OF_MONTH, 5);
		cal.add(Calendar.MONTH, 1);
		
		assertEquals(format.format(cal.getTime()), format.format(contaBO.determinarVencimentoConta(imovel, dia15ProximoMes)));
	}
	
	@Test
	public void vencimentoAlternativcoImovelComExtratoFaturamentoVencimentoMesSeguinte(){
		imovel.setDiaVencimento((short) 5);
		imovel.setIndicadorEmissaoExtratoFaturamento((short) 2);
		imovel.setIndicadorVencimentoMesSeguinte((short) 1);
		mountClienteRepositorioMock().thenReturn(null);
		mountSistemaParametrosRepositorioMock().thenReturn((short) 10);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dia15ProximoMes);
		cal.set(Calendar.DAY_OF_MONTH, 5);
		cal.add(Calendar.MONTH, 1);
		
		assertEquals(format.format(cal.getTime()), format.format(contaBO.determinarVencimentoConta(imovel, dia15ProximoMes)));
	}
		
	@Test
	public void vencimentoAlternativoCliente(){
		Cliente cliente = new Cliente();
		cliente.setDiaVencimento((short) 5);
		mountClienteRepositorioMock().thenReturn(cliente);
		mountSistemaParametrosRepositorioMock().thenReturn((short) 3);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dia15ProximoMes);
		
		assertEquals(format.format(cal.getTime()), format.format(contaBO.determinarVencimentoConta(imovel, dia15ProximoMes)));
	}
	
	@Test
	public void vencimentoAlternativoClienteMesSeguinte(){
		Cliente cliente = new Cliente();
		cliente.setDiaVencimento((short) 5);
		cliente.setIndicadorVencimentoMesSeguinte((short) 1);
		mountClienteRepositorioMock().thenReturn(cliente);
		mountSistemaParametrosRepositorioMock().thenReturn((short) 10);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dia15ProximoMes);
		cal.set(Calendar.DAY_OF_MONTH, 5);
		cal.add(Calendar.MONTH, 1);
		
		assertEquals(format.format(cal.getTime()), format.format(contaBO.determinarVencimentoConta(imovel, dia15ProximoMes)));
	}
	
	@Test
	public void vencimentoAlternativoImovelPorEmissaoExtratoFaturamento(){
		Cliente cliente = new Cliente();
		imovel.setIndicadorEmissaoExtratoFaturamento((short) 1);
		mountClienteRepositorioMock().thenReturn(cliente);
		mountSistemaParametrosRepositorioMock().thenReturn((short) 10);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dia15ProximoMes);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		assertEquals(format.format(cal.getTime()), format.format(contaBO.determinarVencimentoConta(imovel, dia15ProximoMes)));
	}
	
	@Test
	public void semVencimentoAlternativoDebitoEmContaEEnvioPelosCorreios(){
		Cliente cliente = new Cliente();
		imovel.setImovelContaEnvio(ImovelContaEnvio.ENVIAR_CLIENTE_RESPONSAVEL);
		imovel.setIndicadorDebitoConta(Status.INATIVO.getId());
		
		mountClienteRepositorioMock().thenReturn(cliente);
		mountSistemaParametrosRepositorioMock().thenReturn((short) 10);
		mountSistemaParametrosRepositorioDiasCorreiosMock().thenReturn((short) 10);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dia15ProximoMes);
		cal.add(Calendar.DAY_OF_MONTH, 10);
		
		assertEquals(format.format(cal.getTime()), format.format(contaBO.determinarVencimentoConta(imovel, dia15ProximoMes)));
	}
	
	@Test
	public void vencimentoFimMarco() {
		Cliente cliente = new Cliente();
		imovel.setIndicadorEmissaoExtratoFaturamento((short)1);
		
		mountClienteRepositorioMock().thenReturn(cliente);
		mountSistemaParametrosRepositorioMock().thenReturn((short) 10);
		mountSistemaParametrosRepositorioDiasCorreiosMock().thenReturn((short) 10);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(diaRotaMarco);
		cal.set(Calendar.MONTH, Calendar.MARCH);
		cal.set(Calendar.DAY_OF_MONTH, 31); 
		
		assertEquals(format.format(cal.getTime()), format.format(contaBO.determinarVencimentoConta(imovel, diaRotaMarco)));
		
	}
	
	@Test
	public void vencimentoInicioAbril() {
		Cliente cliente = new Cliente();
		imovel.setIndicadorEmissaoExtratoFaturamento((short)2);
		
		mountClienteRepositorioMock().thenReturn(cliente);
		mountSistemaParametrosRepositorioMock().thenReturn((short) 10);
		mountSistemaParametrosRepositorioDiasCorreiosMock().thenReturn((short) 10);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(diaRotaAbril);
		cal.set(Calendar.MONTH, Calendar.APRIL);
		cal.set(Calendar.DAY_OF_MONTH, 01); 
		
		assertEquals(format.format(cal.getTime()), format.format(contaBO.determinarVencimentoConta(imovel, diaRotaAbril)));
		
	}
	
}
