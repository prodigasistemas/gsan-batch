package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.anyObject;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.servicos.faturamento.FaturamentoAtividadeCronogramaBO;
import br.gov.model.Status;
import br.gov.model.cadastro.Categoria;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.cadastro.Subcategoria;
import br.gov.model.faturamento.ConsumoTarifa;
import br.gov.model.faturamento.ConsumoTarifaCategoria;
import br.gov.model.faturamento.ConsumoTarifaVigencia;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.faturamento.TarifaTipoCalculo;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaCategoriaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaVigenciaRepositorio;
import br.gov.servicos.faturamento.FaturamentoAtividadeCronogramaRepositorio;
import br.gov.servicos.faturamento.TarifaTipoCalculoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.to.ConsumoTarifaVigenciaTO;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo09Test {

	@TestSubject
	private ArquivoTextoTipo09 arquivo;
	
	@Mock
	private TarifaTipoCalculoRepositorio tarifaTipoCalculoRepositorioMock;

	@Mock
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorioMock;
	
	@Mock
	private ConsumoTarifaVigenciaRepositorio consumoTarifaVigenciaRepositorioMock;
	
	@Mock
	private ConsumoTarifaCategoriaRepositorio consumoTarifaCategoriaRepositorioMock;
	
	@Mock
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorioMock;
	
	@Mock
	private FaturamentoAtividadeCronogramaRepositorio faturamentoAtividadeCronogramaRepositorioMock;
	
	@Mock
	private FaturamentoAtividadeCronogramaBO faturamentoAtividadeCronogramaBOMock;
	
	
	private Imovel imovel;
	private Integer anoMesReferencia;
	private FaturamentoGrupo grupo;
	private SistemaParametros sistemaParametros;
	
	private TarifaTipoCalculo tipoCalculoTarifa;
	private Collection<ICategoria> dadosSubcategoria;
	private ConsumoTarifaVigenciaTO consumoTarifaVigenteTO;
	private Date dataVigencia;
	private Date dataFaturamento;
	private Subcategoria subcategoria;
	private ConsumoTarifaCategoria consumoTarifaCategoria;
	
	@Before
	public void setUp() {
		
		imovel = new Imovel(1);
		imovel.setConsumoTarifa(new ConsumoTarifa(1));
		
		tipoCalculoTarifa = new TarifaTipoCalculo();
		tipoCalculoTarifa.setId(TarifaTipoCalculo.CALCULO_POR_REFERENCIA);
		
		Integer referenciaVigencia = 201512;
		anoMesReferencia = 201501;

		dataVigencia = Utilitarios.criarData(1, Utilitarios.extrairMes(referenciaVigencia), Utilitarios.extrairAno(referenciaVigencia));
		dataFaturamento = Utilitarios.criarData(1, Utilitarios.extrairMes(anoMesReferencia), Utilitarios.extrairAno(anoMesReferencia));
		
		consumoTarifaVigenteTO = new ConsumoTarifaVigenciaTO(1, dataFaturamento);
		
		ConsumoTarifa consumoTarifa = new ConsumoTarifa();
		consumoTarifa.setId(1);
		consumoTarifa.setTarifaTipoCalculo(TarifaTipoCalculo.CALCULO_POR_REFERENCIA);
		
		ConsumoTarifaVigencia consumoTarifaVigencia = new ConsumoTarifaVigencia();
		consumoTarifaVigencia.setDataVigencia(dataVigencia);
		consumoTarifaVigencia.setConsumoTarifa(consumoTarifa);
		consumoTarifaVigencia.setId(1);
		
		Categoria categoria = new Categoria();
		categoria.setId(1);

		subcategoria = new Subcategoria(1);
		subcategoria.setCategoria(categoria);
		
		dadosSubcategoria =  new ArrayList<ICategoria>();
		dadosSubcategoria.add(subcategoria);
		
		consumoTarifaCategoria = new ConsumoTarifaCategoria();
		consumoTarifaCategoria.setConsumoTarifaVigencia(consumoTarifaVigencia);
		consumoTarifaCategoria.setCategoria(categoria);
		consumoTarifaCategoria.setSubCategoria(subcategoria);
		consumoTarifaCategoria.setNumeroConsumoMinimo(10);
		consumoTarifaCategoria.setValorTarifaMinima(new BigDecimal(14.00));
		
		sistemaParametros = new SistemaParametros();
		sistemaParametros.setIndicadorTarifaCategoria(Status.ATIVO.getId());
		
		grupo = new FaturamentoGrupo();
		grupo.setAnoMesReferencia(anoMesReferencia);
		arquivo = new ArquivoTextoTipo09(imovel, grupo, sistemaParametros);
	}
	
	@Test
	public void buildArquivoTextoTipo08() {
		carregarMocks();
		assertNotNull(arquivo.build().toString());
	}
	
	@Test
	public void buildArquivoTextoTipo08TamanhoLinha() {
		carregarMocks();
		assertTrue(arquivo.build().toString().length() == 37);
	}
	
	@Test
	public void buildArquivoTextoTipo08Layout() {
		carregarMocks();
		
		StringBuilder linha = new StringBuilder("0901201512011   00001000000000014.00");
		linha.append(System.getProperty("line.separator"));
		
		assertEquals(linha.toString(), arquivo.build().toString());
	}
	
	private void carregarMocks() {

		
		expect(tarifaTipoCalculoRepositorioMock.tarifaTipoCalculoAtiva()).andReturn(tipoCalculoTarifa).times(3);
		replay(tarifaTipoCalculoRepositorioMock);
		
		expect(imovelSubcategoriaRepositorioMock.buscarSubcategoria(imovel.getId())).andReturn(dadosSubcategoria).times(3);
		replay(imovelSubcategoriaRepositorioMock);
		
		expect(consumoTarifaVigenciaRepositorioMock.maiorDataVigenciaConsumoTarifaPorData(anyObject(), anyObject())).andReturn(consumoTarifaVigenteTO).times(3);
		replay(consumoTarifaVigenciaRepositorioMock);
		
		expect(consumoTarifaCategoriaRepositorioMock.buscarConsumoTarifaCategoriaVigente(
				consumoTarifaVigenteTO.getDataVigencia(),
				consumoTarifaVigenteTO.getIdVigencia(),
				subcategoria.getCategoria().getId(),
				subcategoria.getSubcategoria().getId())).andReturn(consumoTarifaCategoria).times(3);
		replay(consumoTarifaCategoriaRepositorioMock);
	}

}
