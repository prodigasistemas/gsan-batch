package br.gov.batch.servicos.faturamento;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.batch.servicos.cadastro.ImovelSubcategoriaBO;
import br.gov.batch.servicos.micromedicao.ConsumoAnormalidadeAcaoBO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.ImovelPerfil;
import br.gov.model.micromedicao.ConsumoAnormalidade;
import br.gov.model.micromedicao.ConsumoAnormalidadeAcao;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;
import br.gov.servicos.to.AnormalidadeHistoricoConsumoTO;

public class MensagemAnormalidadeTest {

    @InjectMocks
    private MensagemAnormalidadeContaBO bo;
    
    @Mock
    private ConsumoHistoricoRepositorio consumoHistoricoRepositorio;
    
    @Mock
    private ImovelSubcategoriaBO imovelSubcategoriaBO;
    
    @Mock
    private ConsumoAnormalidadeAcaoBO consumoAnormalidadeAcaoBO;
    
    private AnormalidadeHistoricoConsumoTO anormalidadeHistoricoConsumo;
    
    private Imovel imovel;
    
    Integer anoMesReferencia = 201501;
    
    ConsumoAnormalidadeAcao acao;
    
    @Before
    public void init(){
        anormalidadeHistoricoConsumo = new AnormalidadeHistoricoConsumoTO(1, ConsumoAnormalidade.BAIXO_CONSUMO, LigacaoTipo.AGUA.getId(), anoMesReferencia);
        
        imovel = new Imovel(1);
        imovel.setImovelPerfil(new ImovelPerfil(1));
        
        bo = new MensagemAnormalidadeContaBO();
        acao = new ConsumoAnormalidadeAcao();
        acao.setDescricaoContaMensagemMes1("ACAO 01");
        acao.setDescricaoContaMensagemMes2("ACAO 02");
        acao.setDescricaoContaMensagemMes3("ACAO 03");
        
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void semMensagemAnormalidade(){
        mockSemAnormalidadeConsumo();
        String[] retorno = bo.obterMensagemAnormalidadeConsumo(imovel, anoMesReferencia);
        assertNull(retorno);
    }
    
    @Test
    public void comAnormalidadeAguaMasSemAcao(){
        mockComAnormalidadeAgua();
        mockBuscaCategoria();
        mockAcaoASerTomada(null);
        String[] retorno = bo.obterMensagemAnormalidadeConsumo(imovel, anoMesReferencia);
        assertNull(retorno);
    }
    
    @Test
    public void comAnormalidadeAguaComAcaoMasSemAnormalidadeEsgoto(){
        mockComAnormalidadeAguaESemEsgotoReferenciaAnterior();
        mockBuscaCategoria();
        mockAcaoASerTomada(acao);
        String[] retorno = bo.obterMensagemAnormalidadeConsumo(imovel, anoMesReferencia);
        assertEquals(acao.getDescricaoContaMensagemMes1(), retorno[0]);
    }
    
    @Test
    public void comAnormalidadeAguaComAcaoComAnormalidadeEsgoto(){
        mockComAnormalidadeAguaEComEsgotoReferenciaAnterior();
        mockBuscaCategoria();
        mockAcaoASerTomada(acao);
        String[] retorno = bo.obterMensagemAnormalidadeConsumo(imovel, anoMesReferencia);
        assertEquals(acao.getDescricaoContaMensagemMes3(), retorno[0]);
    }
    
    public void mockSemAnormalidadeConsumo(){
        when(consumoHistoricoRepositorio.anormalidadeHistoricoConsumo(1, LigacaoTipo.AGUA, anoMesReferencia)).thenReturn(null);
        when(consumoHistoricoRepositorio.anormalidadeHistoricoConsumo(1, LigacaoTipo.ESGOTO, anoMesReferencia)).thenReturn(null);
    }
        
    public void mockComAnormalidadeAguaESemEsgotoReferenciaAnterior(){
        when(consumoHistoricoRepositorio.anormalidadeHistoricoConsumo(1, LigacaoTipo.AGUA, anoMesReferencia)).thenReturn(anormalidadeHistoricoConsumo);
        when(consumoHistoricoRepositorio.anormalidadeHistoricoConsumo(1, LigacaoTipo.ESGOTO, Utilitarios.reduzirMeses(anoMesReferencia, 1))).thenReturn(null);
    }
    
    public void mockComAnormalidadeAguaEComEsgotoReferenciaAnterior(){
        when(consumoHistoricoRepositorio.anormalidadeHistoricoConsumo(1, LigacaoTipo.AGUA, anoMesReferencia)).thenReturn(anormalidadeHistoricoConsumo);
        when(consumoHistoricoRepositorio.anormalidadeHistoricoConsumo(1, LigacaoTipo.ESGOTO, Utilitarios.reduzirMeses(anoMesReferencia, 1))).thenReturn(anormalidadeHistoricoConsumo);
        when(consumoHistoricoRepositorio.anormalidadeHistoricoConsumo(1, LigacaoTipo.ESGOTO, Utilitarios.reduzirMeses(anoMesReferencia, 1), anormalidadeHistoricoConsumo.getIdAnormalidade())).thenReturn(anormalidadeHistoricoConsumo);
    }
    
    public void mockComAnormalidadeAgua(){
        when(consumoHistoricoRepositorio.anormalidadeHistoricoConsumo(1, LigacaoTipo.AGUA, anoMesReferencia)).thenReturn(anormalidadeHistoricoConsumo);
    }
    
    public void mockBuscaCategoria(){
        when(imovelSubcategoriaBO.buscaIdCategoriaComMaisEconomias(1)).thenReturn(1);
    }
    
    public void mockAcaoASerTomada(ConsumoAnormalidadeAcao acao){
        when(consumoAnormalidadeAcaoBO.acaoASerTomada(ConsumoAnormalidade.BAIXO_CONSUMO, 1, 1)).thenReturn(acao);
    }
}