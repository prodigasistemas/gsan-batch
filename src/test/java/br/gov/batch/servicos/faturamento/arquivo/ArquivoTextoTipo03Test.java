package br.gov.batch.servicos.faturamento.arquivo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.micromedicao.ConsumoAnormalidade;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;

public class ArquivoTextoTipo03Test {
	
	@InjectMocks
	private ArquivoTextoTipo03 arquivo;
	
	private int TAMANHO_LINHA = 29;
	
	@Mock 
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorioMock;
	
	@Mock 
	private ConsumoHistoricoRepositorio consumoHistoricoRepositorioMock;

	private ArquivoTextoTO to;
	private Collection<ConsumoHistorico> consumosHistoricos;
	private ConsumoHistorico consumoHistorico;
	
	@Before
	public void setup() {
		to = new ArquivoTextoTO();
		to.setImovel(new Imovel(1));
		to.setIdImovel(1);
		arquivo = new ArquivoTextoTipo03();
		arquivo.setArquivoTextoTO(to);
		
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
		
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void buildArquivoTextoTipo03() {
		carregarMocks();
		assertNotNull(arquivo.build(to));
	}
	
	@Test
	public void buildArquivoTextoTipo03TamanhoLinha() {
		carregarMocks();
		String linha = arquivo.build(to);
		assertTrue(linha.length() >= TAMANHO_LINHA);
	}
	
	private void carregarMocks() {
		when(consumoHistoricoRepositorioMock.buscarUltimos6ConsumosAguaImovel(to.getIdImovel())).thenReturn(consumosHistoricos);
		
		when(medicaoHistoricoRepositorioMock.buscarLeituraAnormalidadeFaturamento(consumoHistorico)).thenReturn(1);
	}
}
