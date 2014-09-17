package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.cadastro.Imovel;
import br.gov.model.micromedicao.ConsumoAnormalidade;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo03Test {
	
	@TestSubject
	private ArquivoTextoTipo03 arquivoTextoTipo03;
	
	@Mock 
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorioMock;
	
	@Mock 
	private ConsumoHistoricoRepositorio consumoHistoricoRepositorioMock;
	
	private Imovel imovel;
	private Collection<ConsumoHistorico> consumosHistoricos;
	private ConsumoHistorico consumoHistorico;
	
	@Before
	public void setup() {
		imovel = new Imovel(1);
		
		consumoHistorico = new ConsumoHistorico();
		consumoHistorico.setId(1);
		consumoHistorico.setLigacaoTipo(LigacaoTipo.AGUA.getId());
		consumoHistorico.setReferenciaFaturamento(201408);
		consumoHistorico.setNumeroConsumoFaturadoMes(100);
		ConsumoAnormalidade consumoAnormalidade = new ConsumoAnormalidade();
		consumoAnormalidade.setId(1);
		consumoHistorico.setConsumoAnormalidade(consumoAnormalidade);
		
		consumosHistoricos = new ArrayList<ConsumoHistorico>();
		consumosHistoricos.add(consumoHistorico);
		
		arquivoTextoTipo03 = new ArquivoTextoTipo03();
	}
	
	@Test
	public void buildArquivoTextoTipo03() {
		carregarMocks();
		
		assertNotNull(arquivoTextoTipo03.build(imovel));
	}
	
	@Test
	public void buildArquivoTextoTipo03TamanhoLinha() {
		carregarMocks();
		
		String linha = arquivoTextoTipo03.build(imovel);
		int tamanhoLinha = linha.length();
		
		System.out.println(linha);
		System.out.println(tamanhoLinha);
		
		assertTrue(tamanhoLinha >= 29);
	}
	
	private void carregarMocks() {
		expect(consumoHistoricoRepositorioMock.buscarUltimos6ConsumosAguaImovel(imovel)).andReturn(consumosHistoricos);
		replay(consumoHistoricoRepositorioMock);
		
		expect(medicaoHistoricoRepositorioMock.buscarLeituraAnormalidadeFaturamento(consumoHistorico)).andReturn(1L);
		replay(medicaoHistoricoRepositorioMock);
	}
}
