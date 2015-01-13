package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.ImovelContaEnvio;
import br.gov.model.faturamento.FaturamentoParametro.NOME_PARAMETRO_FATURAMENTO;
import br.gov.servicos.faturamento.FaturamentoParametroRepositorio;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo01Test {
    
    @TestSubject
    private ArquivoTextoTipo01 arquivoTextoTipo01;
    
    @Mock
    private FaturamentoParametroRepositorio repositorioParametros;
    
    @Before
    public void init(){
        arquivoTextoTipo01 = new ArquivoTextoTipo01();
    }
    
    @Test
    public void emitirContaFebrabanCosanpa(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("false");
        replay(repositorioParametros);
        
        
        Imovel imovel = new Imovel();
        
        assertFalse(arquivoTextoTipo01.naoEmitirConta(imovel.getImovelContaEnvio()));
    }
    
    @Test
    public void emitirContaJuazeiro(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("true");
        replay(repositorioParametros);
        
        
        Imovel imovel = new Imovel();
        imovel.setImovelContaEnvio(ImovelContaEnvio.ENVIAR_IMOVEL.getId());
        
        assertFalse(arquivoTextoTipo01.naoEmitirConta(imovel.getImovelContaEnvio()));
    }
    
    @Test
    public void naoEmitirContaFebrabanCosanpaClienteReponsavelGrupo(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("false");
        replay(repositorioParametros);
        
        Imovel imovel = new Imovel();
        imovel.setImovelContaEnvio(ImovelContaEnvio.ENVIAR_CLIENTE_RESPONSAVEL_FINAL_GRUPO.getId());
        
        assertTrue(arquivoTextoTipo01.naoEmitirConta(imovel.getImovelContaEnvio()));
    }
    
    @Test
    public void emitirConta(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("true");
        replay(repositorioParametros);        
        
        Imovel imovel = new Imovel();
        
        assertFalse(arquivoTextoTipo01.naoEmitirConta(imovel.getImovelContaEnvio()));
    }
    
    @Test
    public void naoEmitirContaBraille(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("true");
        replay(repositorioParametros);
        
        Imovel imovel = new Imovel();
        imovel.setImovelContaEnvio(ImovelContaEnvio.ENVIAR_CONTA_BRAILLE.getId());
        
        assertTrue(arquivoTextoTipo01.naoEmitirConta(imovel.getImovelContaEnvio()));
    }
    
    
    
    @Test
    public void naoEmitirContaFebrabanCosanpa(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("false");
        replay(repositorioParametros);
        
        Imovel imovel = new Imovel();
        
        assertTrue(arquivoTextoTipo01.emitirConta(imovel.getImovelContaEnvio()));
    }
    
    @Test
    public void naoEmitirContaJuazeiro(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("true");
        replay(repositorioParametros);
        
        Imovel imovel = new Imovel();
        imovel.setImovelContaEnvio(ImovelContaEnvio.ENVIAR_IMOVEL.getId());
        
        assertTrue(arquivoTextoTipo01.emitirConta(imovel.getImovelContaEnvio()));
    }
    
    @Test
    public void emitirContaFebrabanCosanpaClienteReponsavelGrupo(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("false");
        replay(repositorioParametros);

        Imovel imovel = new Imovel();
        imovel.setImovelContaEnvio(ImovelContaEnvio.ENVIAR_CLIENTE_RESPONSAVEL_FINAL_GRUPO.getId());
        
        assertFalse(arquivoTextoTipo01.emitirConta(imovel.getImovelContaEnvio()));
    }
    
    @Test
    public void naoEmitirConta(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("true");
        replay(repositorioParametros);
        
        Imovel imovel = new Imovel();
        
        assertTrue(arquivoTextoTipo01.emitirConta(imovel.getImovelContaEnvio()));
    }
    
    @Test
    public void emitirContaBraille(){
        expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("true");
        replay(repositorioParametros);
        
        Imovel imovel = new Imovel();
        imovel.setImovelContaEnvio(ImovelContaEnvio.ENVIAR_CONTA_BRAILLE.getId());
        
        assertFalse(arquivoTextoTipo01.emitirConta(imovel.getImovelContaEnvio()));
    }    
}
