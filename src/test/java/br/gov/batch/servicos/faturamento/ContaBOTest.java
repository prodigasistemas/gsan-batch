package br.gov.batch.servicos.faturamento;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Date;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.Cleanup;
import org.jboss.arquillian.persistence.CleanupStrategy;
import org.jboss.arquillian.persistence.TestExecutionPhase;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.test.ShrinkWrapBuilder;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.Quadra;
import br.gov.model.cadastro.SetorComercial;
import br.gov.model.faturamento.Conta;
import br.gov.servicos.to.GerarContaTO;

@RunWith(Arquillian.class)
public class ContaBOTest {
	
	@Deployment
    public static Archive<?> createDeployment() {
		return ShrinkWrapBuilder.createDeployment();
    }
	
	@Inject
	private ContaBO contaBO;
	
	GerarContaTO to = new GerarContaTO();
	
	private void gerarContaSimples(){
		SetorComercial setor = new SetorComercial();
		setor.setId(1);
		setor.setCodigo(8);
		
		Quadra quadra = new Quadra();
		quadra.setId(1);
		quadra.setNumeroQuadra(200);
		
		Imovel imovel = new Imovel();
		imovel.setId(1L);
		imovel.setSetorComercial(setor);
		imovel.setQuadra(quadra);
		
		to.setImovel(imovel);
		to.setAnoMesFaturamento(201405);
		to.setValorTotalDebitos(BigDecimal.ZERO);
		
		to.setDataVencimentoRota(new Date());
		
	}
	
	@Test
	@UsingDataSet("criarContaInput.yml")
	@Cleanup(phase = TestExecutionPhase.AFTER, strategy = CleanupStrategy.USED_ROWS_ONLY)
	public void criarConta(){
		gerarContaSimples();
		
		Conta conta = contaBO.gerarConta(to);
		
		assertEquals(BigDecimal.ZERO, conta.getValorDebitos());
	}
}
