package br.gov.batch.servicos.faturamento.arquivo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.faturamento.ConsumoTarifaFaixaRepositorio;
import br.gov.servicos.to.ConsumoTarifaFaixaTO;

public class ArquivoTextoTipo10Test {

	@InjectMocks
	private ArquivoTextoTipo10 arquivo;
	
	private int TAMANHO_LINHA = 42;
	
	@Mock
	private SistemaParametros sistemaParametrosMock;
	
	@Mock
	private ConsumoTarifaFaixaRepositorio consumoTarifaFaixaRepositorioMock;
	
	private ArquivoTextoTO to;
	
	@Before
	public void setup() {
		to = new ArquivoTextoTO();
		to.addIdsConsumoTarifaCategoria(1);
		to.addIdsConsumoTarifaCategoria(3);
		
		arquivo = new ArquivoTextoTipo10();
		
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void buildArquivoTextoTipo10() {
		carregarMock();
		
		String linha = arquivo.build(to);
		
		assertNotNull(linha);
		assertEquals(getLinhaValida(), linha);
	}
	
	@Test
	public void buildArquivoTextoTipo10TamanhoLinha() {
		carregarMock();
		
		String linha = arquivo.build(to);
		String[] linhas = linha.split(System.getProperty("line.separator"));
		
		for (int i = 0; i < linhas.length; i++) {
			assertEquals(TAMANHO_LINHA, linhas[i].length());
		}
	}
	
	private void carregarMock() {
		when(sistemaParametrosMock.indicadorTarifaCategoria()).thenReturn(false);
		
		when(consumoTarifaFaixaRepositorioMock.dadosConsumoTarifaFaixa(to.getIdsConsumoTarifaCategoria())).thenReturn(getFaixas());
	}

	private List<ConsumoTarifaFaixaTO> getFaixas() {
		List<ConsumoTarifaFaixaTO> lista = new ArrayList<ConsumoTarifaFaixaTO>();
		
		ConsumoTarifaFaixaTO to = new ConsumoTarifaFaixaTO();
		to.setIdConsumoTarifaFaixa(1);
		to.setDataVigencia(Utilitarios.converterStringParaData("2014-09-01", FormatoData.ANO_MES_DIA_SEPARADO));
		to.setNumeroConsumoFaixaInicio(0);
		to.setNumeroConsumoFaixaFim(10);
		to.setValorTarifa(new BigDecimal("14.00"));
		to.setIdCategoria(1);
		to.setIdSubcategoria(1);
		lista.add(to);
		
		to = new ConsumoTarifaFaixaTO();
		to.setIdConsumoTarifaFaixa(3);
		to.setDataVigencia(Utilitarios.converterStringParaData("2014-02-20", FormatoData.ANO_MES_DIA_SEPARADO));
		to.setNumeroConsumoFaixaInicio(21);
		to.setNumeroConsumoFaixaFim(30);
		to.setValorTarifa(new BigDecimal("100.00"));
		to.setIdCategoria(3);
		to.setIdSubcategoria(3);
		lista.add(to);
		
		return lista;
	}
	
	private String getLinhaValida() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("1001201409011  100000000001000000000014.00");
		builder.append(System.getProperty("line.separator"));
		builder.append("1003201402203  300002100003000000000100.00");
		
		return builder.toString();
	}
}
