package br.gov.batch.servicos.faturamento.arquivo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.batch.servicos.faturamento.FaturamentoAtividadeCronogramaBO;
import br.gov.batch.servicos.faturamento.FaturamentoSituacaoBO;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.Status;
import br.gov.model.atendimentopublico.LigacaoAguaSituacao;
import br.gov.model.atendimentopublico.LigacaoEsgotoSituacao;
import br.gov.model.cadastro.Categoria;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.ImovelPerfil;
import br.gov.model.cadastro.Subcategoria;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.FaturamentoAtividade;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;

public class ArquivoTextoTipo01DadosFaturamentoTest {

	@InjectMocks
	private ArquivoTextoTipo01DadosFaturamento arquivo;
	
	@Mock
    private FaturamentoSituacaoBO faturamentoSituacaoBOMock;
	
	@Mock
    private FaturamentoAtividadeCronogramaBO faturamentoAtividadeCronogramaBOMock;
	
	@Mock
    private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorioMock;
	
    @Mock
    private ImovelRepositorio repositorioImovel;
	
	private Imovel imovel;
	private FaturamentoGrupo faturamentoGrupo;
	private Conta conta;
	
	private ArquivoTextoTO arquivoTextoTO;

	@Before
	public void setup() {
		
		imovel = new Imovel(1234567);
    	imovel.setIndicadorImovelCondominio(Status.INATIVO.getId());
    	imovel.setImovelPerfil(new ImovelPerfil(1));
		
		faturamentoGrupo = new FaturamentoGrupo(1);
        faturamentoGrupo.setAnoMesReferencia(201501);
        
        LigacaoAguaSituacao ligacaoAguaSituacao = new LigacaoAguaSituacao(LigacaoAguaSituacao.LIGADO);
    	ligacaoAguaSituacao.setSituacaoFaturamento(Status.ATIVO.getId());
    	ligacaoAguaSituacao.setIndicadorAbastecimento(Status.ATIVO.getId());
    	imovel.setLigacaoAguaSituacao(ligacaoAguaSituacao);
    	
    	LigacaoEsgotoSituacao ligacaoEsgotoSituacao = new LigacaoEsgotoSituacao(LigacaoEsgotoSituacao.POTENCIAL);
    	ligacaoEsgotoSituacao.setSituacaoFaturamento(Status.INATIVO.getId());
    	imovel.setLigacaoEsgotoSituacao(ligacaoEsgotoSituacao);
    	
        conta = new Conta();
        conta.setLigacaoAguaSituacao(ligacaoAguaSituacao);
        conta.setLigacaoEsgotoSituacao(ligacaoEsgotoSituacao);
        conta.setFaturamentoGrupo(faturamentoGrupo);
        
        arquivo = new ArquivoTextoTipo01DadosFaturamento();
        
        arquivoTextoTO = new ArquivoTextoTO();
        arquivoTextoTO.setImovel(imovel);
        arquivoTextoTO.setConta(conta);
        arquivoTextoTO.setFaturamentoGrupo(faturamentoGrupo);
        arquivoTextoTO.setAnoMesReferencia(201501);
        
        MockitoAnnotations.initMocks(this);
	}
	
	private List<ICategoria> categoriasSetup() {
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
    
    private List<ICategoria> subcategoriasSetup() {
    	Subcategoria subcategoria = new Subcategoria();
    	
    	subcategoria.setIndicadorSazonalidade(Status.INATIVO.getId());

    	List<ICategoria> subcategorias = new ArrayList<ICategoria>();
    	subcategorias.add(subcategoria);
    	
    	return subcategorias;
    }
    
    @Test
    public void buildArquivoDadosFaturamento() {
    	carregarMocks();
    	
    	Map<Integer, StringBuilder> mapDados = arquivo.build(arquivoTextoTO);
    	
    	String linha = getLinha(mapDados);
    	
    	assertNotNull(mapDados);
    	assertEquals(11, mapDados.keySet().size());
    	assertEquals(linha, "                                     20150123123112         20112");
    }
    
    private String getLinha(Map<Integer, StringBuilder> mapDados) {
    	StringBuilder builder = new StringBuilder();
    	
    	Collection<StringBuilder> dados = mapDados.values();
    	
    	Iterator<StringBuilder> iterator = dados.iterator();
    	while (iterator.hasNext()) {
    		builder.append(iterator.next());
    	}
    	
    	return builder.toString();
    }
    
	public void carregarMocks() {
    	List<ICategoria> categorias = categoriasSetup(); 
    	List<ICategoria> subcategorias = subcategoriasSetup();
    	
        when(repositorioImovel.obterPorID(imovel.getId())).thenReturn(imovel);
    	
    	when(imovelSubcategoriaRepositorioMock.buscarCategoria(imovel.getId())).thenReturn(categorias);
    	when(imovelSubcategoriaRepositorioMock.buscarSubcategoria(imovel.getId())).thenReturn(subcategorias);
    	
    	when(faturamentoAtividadeCronogramaBOMock.obterDataPrevistaDoCronogramaAnterior(faturamentoGrupo, FaturamentoAtividade.EFETUAR_LEITURA)).thenReturn(Utilitarios.converterStringParaData("2015-01-23", FormatoData.ANO_MES_DIA_SEPARADO));
    	
    	when(faturamentoSituacaoBOMock.verificarParalisacaoFaturamentoAgua(imovel, faturamentoGrupo.getAnoMesReferencia())).thenReturn(Status.ATIVO);
    	when(faturamentoSituacaoBOMock.verificarParalisacaoFaturamentoEsgoto(imovel, faturamentoGrupo.getAnoMesReferencia())).thenReturn(Status.INATIVO);
    }
}
