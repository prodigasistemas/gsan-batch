package br.gov.batch.cadastro;

import static junit.framework.Assert.assertEquals;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

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
	@Transactional(TransactionMode.ROLLBACK)
	public void buscarImovelPorId2() throws Exception {
		System.out.println(" ********************************************************************* ");
		
		StringBuilder sql = new StringBuilder();
		sql.append("select lo from Localidade lo ");
		
		Localidade lo = localidadeRepositorio.find(1L);
		
		System.out.println("************ " + lo.getNome());
		
		assertEquals("Belem", lo.getNome());
	}
}
