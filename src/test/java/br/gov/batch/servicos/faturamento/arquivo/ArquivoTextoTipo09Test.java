package br.gov.batch.servicos.faturamento.arquivo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.batch.servicos.faturamento.tarifa.ConsumoTarifaBO;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
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
import br.gov.servicos.faturamento.ConsumoTarifaFaixaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaVigenciaRepositorio;
import br.gov.servicos.faturamento.FaturamentoAtividadeCronogramaRepositorio;
import br.gov.servicos.faturamento.TarifaTipoCalculoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.to.ConsumoTarifaVigenciaTO;

public class ArquivoTextoTipo09Test {

	@InjectMocks
	private ArquivoTextoTipo09 arquivo;
	
	private int TAMANHO_LINHA = 37;
	
	@Mock private TarifaTipoCalculoRepositorio tarifaTipoCalculoRepositorioMock;

	@Mock private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorioMock;
	
	@Mock private ConsumoTarifaVigenciaRepositorio consumoTarifaVigenciaRepositorioMock;
	
	@Mock private ConsumoTarifaCategoriaRepositorio consumoTarifaCategoriaRepositorioMock;
	
	@Mock private MedicaoHistoricoRepositorio medicaoHistoricoRepositorioMock;
	
	@Mock private FaturamentoAtividadeCronogramaRepositorio faturamentoAtividadeCronogramaRepositorioMock;
	
	@Mock private ConsumoTarifaFaixaRepositorio consumoTarifaFaixaRepositorioMock;
	
	@Mock private SistemaParametros sistemaParametrosMock;
	
	@Mock private ConsumoTarifaBO consumotarifaBOMock;
	
	private ArquivoTextoTO to;
	
	private Imovel imovel;
	private Integer anoMesReferencia;
	private FaturamentoGrupo faturamentoGrupo;
	
	private TarifaTipoCalculo tipoCalculoTarifa;
	private Collection<ICategoria> dadosSubcategoria;
	private ConsumoTarifaVigenciaTO consumoTarifaVigenteTO;
	private Date dataVigencia;
	private Date dataFaturamento;
	private Subcategoria subcategoria;
	private ConsumoTarifaCategoria consumoTarifaCategoria;
	private List<ConsumoTarifaCategoria> consumoTarifasCategoria;
	
	@Before
	public void setup() {
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
		consumoTarifaCategoria.setSubcategoria(subcategoria);
		consumoTarifaCategoria.setNumeroConsumoMinimo(10);
		consumoTarifaCategoria.setValorTarifaMinima(new BigDecimal(14.00));
		
		faturamentoGrupo = new FaturamentoGrupo();
		faturamentoGrupo.setAnoMesReferencia(anoMesReferencia);
		
		to = new ArquivoTextoTO();
		to.setImovel(imovel);
		to.setFaturamentoGrupo(faturamentoGrupo);
		to.addIdsConsumoTarifaCategoria(1);
		arquivo = new ArquivoTextoTipo09();
		
		consumoTarifasCategoria = new ArrayList<ConsumoTarifaCategoria>();
		consumoTarifasCategoria.add(consumoTarifaCategoria);
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void buildArquivoTextoTipo09() {
		carregarMocks();
		assertNotNull(arquivo.build(to).toString());
	}
	
	@Test
	public void buildArquivoTextoTipo09TamanhoLinha() {
		carregarMocks();
		assertTrue(arquivo.build(to).toString().length() == TAMANHO_LINHA);
	}
	
	@Test
	public void buildArquivoTextoTipo09Layout() {
		carregarMocks();
		
		StringBuilder linha = new StringBuilder("0901201512011   00001000000000014.00");
		linha.append(System.getProperty("line.separator"));
		
		assertEquals(linha.toString(), arquivo.build(to).toString());
	}
	
	private void carregarMocks() {
		when(sistemaParametrosMock.indicadorTarifaCategoria()).thenReturn(true);
		
		when(tarifaTipoCalculoRepositorioMock.tarifaTipoCalculoAtiva()).thenReturn(tipoCalculoTarifa);
		
		when(imovelSubcategoriaRepositorioMock.buscarSubcategoria(imovel.getId())).thenReturn(dadosSubcategoria);
		
		when(consumoTarifaVigenciaRepositorioMock.maiorDataVigenciaConsumoTarifaPorData(any(), any())).thenReturn(consumoTarifaVigenteTO);
		
		when(consumoTarifaCategoriaRepositorioMock.buscarConsumoTarifaCategoriaVigente(
				consumoTarifaVigenteTO.getDataVigencia(),
				consumoTarifaVigenteTO.getIdVigencia(),
				subcategoria.getCategoria().getId(),
				subcategoria.getSubcategoria().getId())).thenReturn(consumoTarifaCategoria);
		
		when(consumotarifaBOMock.obterConsumoTarifasCategoria(imovel, sistemaParametrosMock)).thenReturn(consumoTarifasCategoria);
	}
}
