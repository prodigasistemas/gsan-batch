package br.gov.batch.servicos.faturamento;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.model.atendimentopublico.LigacaoEsgoto;
import br.gov.model.cadastro.Imovel;
import br.gov.servicos.atendimentopublico.LigacaoEsgotoRepositorio;

public class EsgotoBOTest {
	
	@InjectMocks
	private EsgotoBO esgotoBO;
	
	@Mock
	private Imovel imovel;
	
	@Mock
	private LigacaoEsgotoRepositorio repositorio;
	
	@Before
	public void setup(){
		esgotoBO = new EsgotoBO();
		
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void percentualEsgotoAlternativo(){
		mockImovel();
		mockRepositorio();
		
		assertEquals(new BigDecimal(10), esgotoBO.percentualEsgotoAlternativo(imovel));
	}

	private void mockImovel() {
		when(imovel.getId()).thenReturn(1);
		when(imovel.faturamentoEsgotoAtivo()).thenReturn(true);
	}
	
	private void mockRepositorio(){
		when(repositorio.buscarLigacaoEsgotoPorIdImovel(1)).thenReturn(getLigacaoEsgoto());
	}
	
	private LigacaoEsgoto getLigacaoEsgoto(){
		LigacaoEsgoto ligacaoEsgoto = new LigacaoEsgoto();
		ligacaoEsgoto.setPercentual(new BigDecimal(10));
		
		return ligacaoEsgoto;
	}
}
