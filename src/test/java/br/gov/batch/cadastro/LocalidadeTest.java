package br.gov.batch.cadastro;

import static org.junit.Assert.assertEquals;

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
import br.gov.model.cadastro.Localidade;
import br.gov.servicos.cadastro.LocalidadeRepositorio;


@RunWith(Arquillian.class)
public class LocalidadeTest {
		
	@Deployment
    public static Archive<?> createDeployment() {
		return ShrinkWrapBuilder.createDeployment();
    }
	
	@Inject
	LocalidadeRepositorio localidadeRepositorio;
	
	@Test
	@UsingDataSet("cadastros.yml")
	@Cleanup(phase = TestExecutionPhase.AFTER, strategy = CleanupStrategy.USED_ROWS_ONLY)
	public void buscarLocalidadeExistente() throws Exception {
		Localidade localidade = localidadeRepositorio.find(1L);
		assertEquals("Belem", localidade.getNome());
	}
	
	@Test
	@UsingDataSet("cadastros.yml")
	@Cleanup(phase = TestExecutionPhase.AFTER, strategy = CleanupStrategy.USED_ROWS_ONLY)
	public void buscarLocalidadeInexistente() throws Exception {
		Localidade localidade = localidadeRepositorio.find(10L);
		assertEquals(null, localidade);
	}
}
