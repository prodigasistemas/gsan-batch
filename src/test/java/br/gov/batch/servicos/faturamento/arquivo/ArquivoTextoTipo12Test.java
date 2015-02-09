package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.Categoria;
import br.gov.model.cadastro.ImovelPerfil;
import br.gov.model.micromedicao.ConsumoAnormalidade;
import br.gov.model.micromedicao.ConsumoAnormalidadeAcao;
import br.gov.model.micromedicao.LeituraAnormalidadeConsumo;
import br.gov.servicos.micromedicao.ConsumoAnormalidadeAcaoRepositorio;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo12Test {

	@TestSubject
	private ArquivoTextoTipo12 arquivo;
	
	private int TAMANHO_LINHA = 386;
	
	@Mock
	private ConsumoAnormalidadeAcaoRepositorio consumoAnormalidadeAcaoRepositorioMock;
	
	@Before
	public void setup() {
		arquivo = new ArquivoTextoTipo12();
	}
	
	@Test
	public void buildArquivoTextoTipo12() {
		carregarMock();
		
		String linha = arquivo.build(new ArquivoTextoTO());
		
		assertNotNull(linha);
		assertEquals(getLinhaValida(), linha);
	}
	
	@Test
	public void buildArquivoTextoTipo10TamanhoLinha() {
		carregarMock();
		
		String linha = arquivo.build(new ArquivoTextoTO());
		
		String[] linhas = linha.split(System.getProperty("line.separator"));
		
		for (int i = 0; i < linhas.length; i++) {
			assertEquals(TAMANHO_LINHA, linhas[i].length());
		}
	}
	
	private void carregarMock() {
		expect(consumoAnormalidadeAcaoRepositorioMock.consumoAnormalidadeAcaoAtivo()).andReturn(getAcoes());
		replay(consumoAnormalidadeAcaoRepositorioMock);
	}

	private List<ConsumoAnormalidadeAcao> getAcoes() {
		List<ConsumoAnormalidadeAcao> lista = new ArrayList<ConsumoAnormalidadeAcao>();
		
		ConsumoAnormalidadeAcao acao = new ConsumoAnormalidadeAcao(new Integer("1"),
				new ConsumoAnormalidade(new Integer("1")),
				new Categoria(new Integer("1")),
				new ImovelPerfil(new Integer("1")),
				new BigDecimal("1.00"),
				new BigDecimal("1.50"),
				new BigDecimal("2.00"),
				new LeituraAnormalidadeConsumo(1),
				new LeituraAnormalidadeConsumo(2),
				new LeituraAnormalidadeConsumo(3),
				"MSG 1",
				"MSG 2",
				"MSG 3",
				new Short("1"));
		
		lista.add(acao);
		
		acao = new ConsumoAnormalidadeAcao(new Integer("2"),
				new ConsumoAnormalidade(new Integer("2")),
				new Categoria(new Integer("2")),
				new ImovelPerfil(new Integer("2")),
				new BigDecimal("3.00"),
				new BigDecimal("3.50"),
				new BigDecimal("4.00"),
				new LeituraAnormalidadeConsumo(4),
				new LeituraAnormalidadeConsumo(5),
				new LeituraAnormalidadeConsumo(6),
				"MSG 4",
				"MSG 5",
				"MSG 6",
				new Short("1"));
		
		lista.add(acao);
		
		return lista;
	}
	
	private String getLinhaValida() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("120101010102031.001.502.00                                                                                                                   MSG 1                                                                                                                   MSG 2                                                                                                                   MSG 3");
		builder.append(System.getProperty("line.separator"));
		builder.append("120202020405063.003.504.00                                                                                                                   MSG 4                                                                                                                   MSG 5                                                                                                                   MSG 6");
		
		return builder.toString();
	}
}
