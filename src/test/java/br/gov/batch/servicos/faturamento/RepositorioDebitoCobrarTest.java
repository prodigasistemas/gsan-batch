package br.gov.batch.servicos.faturamento;

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.test.ShrinkWrapBuilder;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.DebitoCobrar;
import br.gov.servicos.faturamento.DebitoCobrarRepositorio;


@RunWith(Arquillian.class)
public class RepositorioDebitoCobrarTest {
		
	@Deployment
    public static Archive<?> createDeployment() {
		return ShrinkWrapBuilder.createDeployment();
    }
	
	@Inject
	DebitoCobrarRepositorio repositorio;
	
	@Test
	@UsingDataSet({"cadastros.yml","debitosCobrar.yml"})
	@Transactional(TransactionMode.ROLLBACK)
	public void buscarImovelPorId2() throws Exception {
		Imovel imovel = new Imovel();
		imovel.setId(1L);

		Collection<DebitoCobrar> debitos = repositorio.debitosCobrarPorImovelComPendenciaESemRevisao(imovel);
		
		assertTrue(debitos.size() == 1);
	}
}
