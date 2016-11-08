package br.gov.batch.servicos.faturamento.arquivo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.batch.servicos.faturamento.AguaEsgotoBO;
import br.gov.batch.servicos.faturamento.EsgotoBO;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.batch.servicos.faturamento.to.VolumeMedioAguaEsgotoTO;
import br.gov.batch.servicos.micromedicao.ConsumoBO;
import br.gov.batch.servicos.micromedicao.HidrometroBO;
import br.gov.model.Status;
import br.gov.model.cadastro.Categoria;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.Subcategoria;
import br.gov.model.faturamento.ConsumoTarifa;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.to.DadosBancariosTO;

public class ArquivoTextoTipo01DadosConsumoTest {

	@InjectMocks
	private ArquivoTextoTipo01DadosConsumo arquivo;
	
	@Mock
    private HidrometroBO hidrometroBOMock;

    @Mock
    private AguaEsgotoBO aguaEsgotoBOMock;
    
    @Mock
    private EsgotoBO esgotoBOMock;
    
    @Mock
    private ConsumoBO consumoBOMock;
    
    @Mock
    private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorioMock;
    
    @Mock
    private ImovelRepositorio repositorioImovel;
    
	private Imovel imovel;
	
	private FaturamentoGrupo faturamentoGrupo;
	
	private ArquivoTextoTO arquivoTextoTO;

	@Before
    public void init(){

    	imovel = new Imovel(1234567);
    	imovel.setConsumoTarifa(new ConsumoTarifa(1));
    	
    	faturamentoGrupo = new FaturamentoGrupo(1);
        faturamentoGrupo.setAnoMesReferencia(201501);
        
        arquivo = new ArquivoTextoTipo01DadosConsumo();
        
        arquivoTextoTO = new ArquivoTextoTO();
        arquivoTextoTO.setImovel(imovel);
        arquivoTextoTO.setFaturamentoGrupo(faturamentoGrupo);
        
        MockitoAnnotations.initMocks(this);
	}
	
	private List<ICategoria> categoriasSetUp() {
    	Categoria categoria = new Categoria();
    	
    	categoria.setConsumoAlto(50);
    	categoria.setConsumoEstouro(50);
    	categoria.setNumeroConsumoMaximoEc(500);
    	categoria.setMediaBaixoConsumo(30);
    	categoria.setQuantidadeEconomias(1);
    	categoria.setVezesMediaAltoConsumo(new BigDecimal("2.0"));
    	categoria.setVezesMediaEstouro(new BigDecimal("3.0"));
    	categoria.setPorcentagemMediaBaixoConsumo(new BigDecimal("50.0"));
    	
    	List<ICategoria> categorias = new ArrayList<ICategoria>();
    	categorias.add(categoria);
    	
    	return categorias;
    }
    
    private List<ICategoria> subcategoriasSetUp() {
    	Subcategoria subcategoria = new Subcategoria();
    	
    	subcategoria.setIndicadorSazonalidade(Status.INATIVO.getId());

    	List<ICategoria> subcategorias = new ArrayList<ICategoria>();
    	subcategorias.add(subcategoria);
    	
    	return subcategorias;
    }
    
	@Test
    public void buildArquivoDadosConsumo() {
    	carregarMocks();
    	
    	Map<Integer, StringBuilder> mapDados = arquivo.build(arquivoTextoTO);
    	
    	assertNotNull(mapDados);
    	assertEquals(12, mapDados.keySet().size());
    	assertEquals("000020", mapDados.get(12).toString());
    	assertEquals("      ", mapDados.get(15).toString());
    	assertEquals("      ", mapDados.get(16).toString());
    	assertEquals("000000", mapDados.get(17).toString());
    	assertEquals("030.00", mapDados.get(18).toString());
    	assertEquals("01", mapDados.get(20).toString());
    	assertEquals("0000500000500000303.0 2.0 050.00000500", mapDados.get(21).toString());
    	assertEquals("00", mapDados.get(25).toString());
    	assertEquals("000010", mapDados.get(32).toString());
    	assertEquals("000010", mapDados.get(33).toString());
    	assertEquals("      ", mapDados.get(43).toString());
    	assertEquals("      ", mapDados.get(44).toString());
    }
	
	public void carregarMocks() {
        when(repositorioImovel.obterPorID(imovel.getId())).thenReturn(imovel);
	    
    	DadosBancariosTO to = new DadosBancariosTO();
    	to.setCodigoAgencia("00000");
    	to.setDescricaoBanco("BANCO DO BRASIL");

    	List<ICategoria> categorias = categoriasSetUp(); 
    	List<ICategoria> subcategorias = subcategoriasSetUp();
    	
    	VolumeMedioAguaEsgotoTO volumeMedioTO = new VolumeMedioAguaEsgotoTO(20, 6);
    	
    	boolean instalacaoOuSubstituicaoHidrometro = false;
    	BigDecimal percentualEsgotoAlternativo = new BigDecimal("30");
    	
    	when(hidrometroBOMock.houveInstalacaoOuSubstituicao(imovel.getId())).thenReturn(instalacaoOuSubstituicaoHidrometro);
    	
    	when(aguaEsgotoBOMock.obterVolumeMedioAguaEsgoto(imovel.getId(),faturamentoGrupo.getAnoMesReferencia(), LigacaoTipo.AGUA.getId()))
    		.thenReturn(volumeMedioTO);
    	
    	when(esgotoBOMock.percentualEsgotoAlternativo(imovel)).thenReturn(percentualEsgotoAlternativo);
    	
    	when(consumoBOMock.consumoMinimoLigacao(imovel.getId())).thenReturn(10);
    	when(consumoBOMock.consumoNaoMedido(imovel.getId(), faturamentoGrupo.getAnoMesReferencia())).thenReturn(10);
    	
    	when(imovelSubcategoriaRepositorioMock.buscarCategoria(imovel.getId())).thenReturn(categorias);
    	when(imovelSubcategoriaRepositorioMock.buscarSubcategoria(imovel.getId())).thenReturn(subcategorias);
    }
}
