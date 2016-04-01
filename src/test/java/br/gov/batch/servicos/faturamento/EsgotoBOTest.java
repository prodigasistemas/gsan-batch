package br.gov.batch.servicos.faturamento;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.atendimentopublico.LigacaoEsgoto;
import br.gov.model.cadastro.Imovel;
import br.gov.servicos.atendimentopublico.LigacaoEsgotoRepositorio;

@RunWith(EasyMockRunner.class)
public class EsgotoBOTest {
	
	@TestSubject
	private EsgotoBO esgotoBO;
	
	@Mock
	private Imovel imovel;
	
	@Mock
	private LigacaoEsgotoRepositorio repositorio;
	
	@Before
	public void setup(){
		esgotoBO = new EsgotoBO();
	}
	
	@Test
	public void percentualEsgotoAlternativo(){
		mockImovel();
		mockRepositorio();
		
		assertEquals(new BigDecimal(10), esgotoBO.percentualEsgotoAlternativo(imovel));
	}

	private void mockImovel() {
		expect(imovel.getId()).andReturn(1);
		expect(imovel.faturamentoEsgotoAtivo()).andReturn(true);
		replay(imovel);
	}
	
	private void mockRepositorio(){
		expect(repositorio.buscarLigacaoEsgotoPorIdImovel(1)).andReturn(getLigacaoEsgoto());
		replay(repositorio);
	}
	
	private LigacaoEsgoto getLigacaoEsgoto(){
		LigacaoEsgoto ligacaoEsgoto = new LigacaoEsgoto();
		ligacaoEsgoto.setPercentual(new BigDecimal(10));
		
		return ligacaoEsgoto;
	}
}
