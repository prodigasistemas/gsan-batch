package br.gov.batch.servicos.batch;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.to.ControleExecucaoTO;
import br.gov.model.batch.ControleProcessoAtividade;
import br.gov.model.batch.ProcessoAtividade;
import br.gov.servicos.batch.ControleProcessoAtividadeRepositorio;

@RunWith(EasyMockRunner.class)
public class ControleExecucaoBOTest {
	
	@TestSubject
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
		expect(repositorioMock.obterPorID(1)).andReturn(controleProcessoAtividadeMock);
		replay(repositorioMock);
	}
	
	private void mockControleProcessoAtividade(){
		expect(controleProcessoAtividadeMock.getId()).andReturn(1);
		expect(controleProcessoAtividadeMock.getTotalItens()).andReturn(10);
		expect(controleProcessoAtividadeMock.getAtividade()).andReturn(processoAtividadeMock).times(2);
		replay(controleProcessoAtividadeMock);
	}
	
	private void mockProcessoAtividade(){
		expect(processoAtividadeMock.getLimiteExecucao()).andReturn(Short.valueOf("6"));
		expect(processoAtividadeMock.getDescricao()).andReturn("Alguma atividade");
		replay(processoAtividadeMock);
	}
}
