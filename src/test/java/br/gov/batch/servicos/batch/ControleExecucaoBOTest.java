package br.gov.batch.servicos.batch;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.batch.to.ControleExecucaoTO;
import br.gov.model.batch.ControleProcessoAtividade;
import br.gov.model.batch.ProcessoAtividade;
import br.gov.servicos.batch.ControleProcessoAtividadeRepositorio;

public class ControleExecucaoBOTest {
	
	@InjectMocks
	private ControleExecucaoBO controleExecucaoBO;
	
	@Mock
	private ControleProcessoAtividadeRepositorio repositorioMock;
	
	@Mock
	private ControleProcessoAtividade controleProcessoAtividadeMock;
	
	@Mock
	private ProcessoAtividade processoAtividadeMock;
	
	@Before
	public void setup() {
		controleExecucaoBO = new ControleExecucaoBO();
		
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void criaExecucaoAtividadeTest(){
		mockRepositorio();
		mockControleProcessoAtividade();
		mockProcessoAtividade();
		
		ControleExecucaoTO controle = controleExecucaoBO.criaExecucaoAtividade(1);
		
		assertEquals(1, controle.getIdControle().intValue());
		assertEquals(10, controle.getTotalItens().intValue());
		assertEquals(6, controle.getMaximoExecucoes().intValue());
		assertEquals("Alguma atividade", controle.getDescAtividade());
	}
	
	private void mockRepositorio(){
		when(repositorioMock.obterPorID(1)).thenReturn(controleProcessoAtividadeMock);
	}
	
	private void mockControleProcessoAtividade(){
		when(controleProcessoAtividadeMock.getId()).thenReturn(1);
		when(controleProcessoAtividadeMock.getTotalItens()).thenReturn(10);
		when(controleProcessoAtividadeMock.getAtividade()).thenReturn(processoAtividadeMock);
	}
	
	private void mockProcessoAtividade(){
		when(processoAtividadeMock.getLimiteExecucao()).thenReturn(Short.valueOf("6"));
		when(processoAtividadeMock.getDescricao()).thenReturn("Alguma atividade");
	}
}
