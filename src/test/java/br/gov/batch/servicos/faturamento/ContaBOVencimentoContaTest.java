package br.gov.batch.servicos.faturamento;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.easymock.EasyMockRunner;
import org.easymock.IExpectationSetters;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.Status;
import br.gov.model.cadastro.Cliente;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.ImovelContaEnvio;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.servicos.cadastro.ClienteRepositorio;

@RunWith(EasyMockRunner.class)
public class ContaBOVencimentoContaTest {
	
	@TestSubject
	private ContaBO contaBO;
	
	@Mock
	private ClienteRepositorio clienteRepositorioMock;
	
	@Mock
	private SistemaParametros sistemaParametrosMock;
	
	private String textoDia10MesSeguinte;
	
	private Date dia15ProximoMes;
	
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
		
		
		contaBO = new ContaBO();
	}
	
	private void replayAll() {
		replay(clienteRepositorioMock);
		replay(sistemaParametrosMock);
	}
	
	private IExpectationSetters<Cliente> mountClienteRepositorioMock() {
		return expect(clienteRepositorioMock.buscarClienteResponsavelPorImovel(1));
	}
	
	private IExpectationSetters<Short> mountSistemaParametrosRepositorioMock() {
		return expect(sistemaParametrosMock.getNumeroMinimoDiasEmissaoVencimento());
	}
	
	private IExpectationSetters<Short> mountSistemaParametrosRepositorioDiasCorreiosMock() {
		return expect(sistemaParametrosMock.getNumeroDiasAdicionaisCorreios());
	}
	
	@Test
	public void semVencimentoAlternativo(){
		mountClienteRepositorioMock().andReturn(null);
		mountSistemaParametrosRepositorioMock().andReturn((short) 10);
		replayAll();
		
		assertEquals(textoDia10MesSeguinte, format.format(contaBO.determinarVencimentoConta(imovel, dia15ProximoMes)));
	}
	
	@Test
	public void vencimentoAlternativoImovelAntesVencerRota(){
		imovel.setDiaVencimento((short) 5);
		mountClienteRepositorioMock().andReturn(null);
		mountSistemaParametrosRepositorioMock().andReturn((short) 3);
		replayAll();
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dia15ProximoMes);
		
		assertEquals(format.format(cal.getTime()), format.format(contaBO.determinarVencimentoConta(imovel, dia15ProximoMes)));
	}
	
	@Test
	public void vencimentoAlternativoImovelAposVencerRota(){
		imovel.setDiaVencimento((short) 25);
		mountClienteRepositorioMock().andReturn(null);
		mountSistemaParametrosRepositorioMock().andReturn((short) 10);
		replayAll();
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dia15ProximoMes);
		cal.set(Calendar.DAY_OF_MONTH, 25);
		
		assertEquals(format.format(cal.getTime()), format.format(contaBO.determinarVencimentoConta(imovel, dia15ProximoMes)));
	}

	@Test
	public void vencimentoAlternativoImovelComDiasParaEmissaoSuperiorAoVencimento(){
		imovel.setDiaVencimento((short) 5);
		mountClienteRepositorioMock().andReturn(null);
		mountSistemaParametrosRepositorioMock().andReturn((short) 60);
		replayAll();
		
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
		mountClienteRepositorioMock().andReturn(null);
		mountSistemaParametrosRepositorioMock().andReturn((short) 10);
		replayAll();
		
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
		mountClienteRepositorioMock().andReturn(cliente);
		mountSistemaParametrosRepositorioMock().andReturn((short) 3);
		replayAll();
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dia15ProximoMes);
		
		assertEquals(format.format(cal.getTime()), format.format(contaBO.determinarVencimentoConta(imovel, dia15ProximoMes)));
	}
	
	@Test
	public void vencimentoAlternativoClienteMesSeguinte(){
		Cliente cliente = new Cliente();
		cliente.setDiaVencimento((short) 5);
		cliente.setIndicadorVencimentoMesSeguinte((short) 1);
		mountClienteRepositorioMock().andReturn(cliente);
		mountSistemaParametrosRepositorioMock().andReturn((short) 10);
		replayAll();
		
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
		mountClienteRepositorioMock().andReturn(cliente);
		mountSistemaParametrosRepositorioMock().andReturn((short) 10);
		replayAll();
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dia15ProximoMes);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		assertEquals(format.format(cal.getTime()), format.format(contaBO.determinarVencimentoConta(imovel, dia15ProximoMes)));
	}
	
	@Test
	public void semVencimentoAlternativoDebitoEmContaEEnvioPelosCorreios(){
		Cliente cliente = new Cliente();
		imovel.setImovelContaEnvio(ImovelContaEnvio.ENVIAR_CLIENTE_RESPONSAVEL.getId());
		imovel.setIndicadorDebitoConta(Status.INATIVO.getId());
		
		mountClienteRepositorioMock().andReturn(cliente);
		mountSistemaParametrosRepositorioMock().andReturn((short) 10);
		mountSistemaParametrosRepositorioDiasCorreiosMock().andReturn((short) 10);
		replayAll();
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dia15ProximoMes);
		cal.add(Calendar.DAY_OF_MONTH, 10);
		
		assertEquals(format.format(cal.getTime()), format.format(contaBO.determinarVencimentoConta(imovel, dia15ProximoMes)));
	}
	
}
