package br.gov.batch.servicos.faturamento;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.model.cadastro.Cliente;
import br.gov.model.cadastro.ClienteRelacaoTipo;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.Quadra;
import br.gov.model.cadastro.SetorComercial;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.Conta;
import br.gov.servicos.cadastro.ClienteRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.to.GerarContaTO;

public class ContaBOTest {
	
	@InjectMocks
	private ContaBO contaBO;
	
	@Mock
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorio;
	
	@Mock
	private ClienteRepositorio clienteRepositorio;

	Imovel imovel = new Imovel();
	
	SistemaParametros sistemaParametros;
	
	@Before
	public void init(){
		contaBO = new ContaBO();
		
		sistemaParametros = new SistemaParametros();
		sistemaParametros.setNumeroMesesValidadeConta((short) 5);
		
		contaBO.setSistemaParametros(sistemaParametros);
		
		MockitoAnnotations.initMocks(this);
	}
	
	private void preparaTeste(){
		SetorComercial setor = new SetorComercial();
		setor.setId(1);
		setor.setCodigo(8);
		
		Quadra quadra = new Quadra();
		quadra.setId(1);
		quadra.setNumeroQuadra(200);
		
		imovel.setId(1);
		imovel.setSetorComercial(setor);
		imovel.setQuadra(quadra);
		
		when(medicaoHistoricoRepositorio.buscarPorLigacaoAgua(1, 201405)).thenReturn(null);
		when(clienteRepositorio.buscarClientePorImovel(1, ClienteRelacaoTipo.RESPONSAVEL)).thenReturn(new Cliente());
	}
	
	@Test
	public void criarContaComValoresZerados() throws Exception{
		preparaTeste();
		
		Conta conta = contaBO.buildConta(toContaZerados());
		Assert.assertEquals(201405, conta.getReferencia().intValue());
		Assert.assertEquals(201405, conta.getReferenciaContabil().intValue());
		Assert.assertEquals(0, conta.getValorAgua().intValue());
		Assert.assertEquals(0, conta.getValorEsgoto().intValue());
		Assert.assertEquals(0, conta.getValorCreditos().intValue());
		Assert.assertEquals(0, conta.getValorDebitos().intValue());
		Assert.assertEquals(0, conta.getValorImposto().intValue());
		Assert.assertEquals(0, conta.getPercentualEsgoto().intValue());
		Assert.assertEquals(0, conta.getPercentualColeta().intValue());
	}
	
	@Test
	public void criarContaComValoresPreenchidos() throws Exception{
		preparaTeste();
		
		Conta conta = contaBO.buildConta(toContaPreenchidos());
		
		Assert.assertEquals(201405, conta.getReferencia().intValue());
		Assert.assertEquals(201405, conta.getReferenciaContabil().intValue());
		Assert.assertEquals(0, conta.getValorAgua().intValue());
		Assert.assertEquals(0, conta.getValorEsgoto().intValue());
		Assert.assertEquals(2.45, conta.getValorCreditos().doubleValue(), 0);
		Assert.assertEquals(5.45, conta.getValorDebitos().doubleValue(), 0);
		Assert.assertEquals(0.78, conta.getValorImposto().doubleValue(), 0);
		Assert.assertEquals(5.60, conta.getPercentualEsgoto().doubleValue(), 0);
		Assert.assertEquals(56, conta.getPercentualColeta().intValue());		
	}
	
	private GerarContaTO toContaZerados(){
		GerarContaTO to = new GerarContaTO();
		to.setImovel(imovel);
		to.setAnoMesFaturamento(201405);
		to.setValorTotalDebitos(BigDecimal.ZERO);
		to.setValorTotalImposto(BigDecimal.ZERO);
		to.setValorTotalCreditos(BigDecimal.ZERO);
		to.setPercentualColeta((short) 0);
		to.setPercentualEsgoto(BigDecimal.ZERO);
		to.setDataVencimentoRota(new Date());
		
		return to;
	}
	
	private GerarContaTO toContaPreenchidos(){
		GerarContaTO to = new GerarContaTO();
		to.setImovel(imovel);
		to.setAnoMesFaturamento(201405);
		to.setValorTotalDebitos(new BigDecimal("5.45"));
		to.setValorTotalImposto(new BigDecimal("0.78"));
		to.setValorTotalCreditos(new BigDecimal("2.45"));
		to.setPercentualColeta((short)56);
		to.setPercentualEsgoto(new BigDecimal("5.60"));
		to.setDataVencimentoRota(new Date());

		return to;
	}
}