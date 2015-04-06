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

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.micromedicao.ConsumoAnormalidade;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo03Test {
	
	@TestSubject
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
		expect(consumoHistoricoRepositorioMock.buscarUltimos6ConsumosAguaImovel(to.getIdImovel())).andReturn(consumosHistoricos);
		replay(consumoHistoricoRepositorioMock);
		
		expect(medicaoHistoricoRepositorioMock.buscarLeituraAnormalidadeFaturamento(consumoHistorico)).andReturn(1);
		replay(medicaoHistoricoRepositorioMock);
	}
}
