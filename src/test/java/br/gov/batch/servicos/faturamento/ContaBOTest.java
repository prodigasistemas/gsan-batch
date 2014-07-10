package br.gov.batch.servicos.faturamento;

import java.math.BigDecimal;
import java.util.Date;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.Cleanup;
import org.jboss.arquillian.persistence.CleanupStrategy;
import org.jboss.arquillian.persistence.ShouldMatchDataSet;
import org.jboss.arquillian.persistence.TestExecutionPhase;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.test.ShrinkWrapBuilder;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.Quadra;
import br.gov.model.cadastro.SetorComercial;
import br.gov.servicos.to.GerarContaTO;

@RunWith(Arquillian.class)
public class ContaBOTest {
	
	@Deployment
    public static Archive<?> createDeployment() {
		return ShrinkWrapBuilder.createDeployment();
    }
	
	@Inject
	private ContaBO contaBO;
	
	Imovel imovel = new Imovel();
	
	private void gerarContaSimples(){
		SetorComercial setor = new SetorComercial();
		setor.setId(1);
		setor.setCodigo(8);
		
		Quadra quadra = new Quadra();
		quadra.setId(1);
		quadra.setNumeroQuadra(200);
		
		imovel.setId(1L);
		imovel.setSetorComercial(setor);
		imovel.setQuadra(quadra);
	}
	
	@Test
	@UsingDataSet("criarContaInput.yml")
	@ShouldMatchDataSet("criarContaOutput01.yml")
	@Cleanup(phase = TestExecutionPhase.AFTER, strategy = CleanupStrategy.USED_ROWS_ONLY)
	public void criarContaComValoresZerados(){
		gerarContaSimples();
		
		contaBO.gerarConta(toContaZerados());
	}
	
	@Test
	@UsingDataSet("criarContaInput.yml")
	@ShouldMatchDataSet("criarContaOutput02.yml")
	@Cleanup(phase = TestExecutionPhase.AFTER, strategy = CleanupStrategy.USED_ROWS_ONLY)
	public void criarContaComValoresPreenchidos(){
		gerarContaSimples();
		
		contaBO.gerarConta(toContaPreenchidos());
	}
	
	private GerarContaTO toContaZerados(){
		GerarContaTO to = new GerarContaTO();
		to.setImovel(imovel);
		to.setAnoMesFaturamento(201405);
		to.setValorTotalDebitos(BigDecimal.ZERO);
		to.setValorTotalImposto(BigDecimal.ZERO);
		to.setValorTotalCreditos(BigDecimal.ZERO);
		to.setPercentualColeta(BigDecimal.ZERO);
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
		to.setPercentualColeta(new BigDecimal("5.60"));
		to.setPercentualEsgoto(new BigDecimal("5.60"));
		to.setDataVencimentoRota(new Date());
		
		return to;
	}
}