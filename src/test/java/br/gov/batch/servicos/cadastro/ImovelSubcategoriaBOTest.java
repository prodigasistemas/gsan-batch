package br.gov.batch.servicos.cadastro;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;
import java.util.Collection;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.cadastro.ICategoria;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.to.ImovelSubcategoriaTO;
import static org.junit.Assert.assertEquals;

@RunWith(EasyMockRunner.class)
public class ImovelSubcategoriaBOTest {
    
    @TestSubject
    private ImovelSubcategoriaBO imovelSubcategoriaBO;
    
    @Mock
    private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;
    
    Collection<ICategoria> categorias = null;
    
    @Before
    public void init(){
        imovelSubcategoriaBO = new ImovelSubcategoriaBO();
        
        categorias = new ArrayList<ICategoria>();
        
        ImovelSubcategoriaTO to = new ImovelSubcategoriaTO();
        to.setId(1);
        to.setSubcategoriaQuantidadeEconomias(2L);
        categorias.add(to);
        
        to = new ImovelSubcategoriaTO();
        to.setId(2);
        to.setSubcategoriaQuantidadeEconomias(4L);
        categorias.add(to);
    }
    
    @Test
    public void testBuscaIdCategoriaComMaisEconomias(){
        expect(imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasCategoria(1)).andReturn(categorias);
        replay(imovelSubcategoriaRepositorio);
        
        Integer id = imovelSubcategoriaBO.buscaIdCategoriaComMaisEconomias(1);
        
        assertEquals(2, id.intValue());
    }
    
    @Test
    public void testBuscaIdCategoriaSemEconomias(){
        expect(imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasCategoria(1)).andReturn(new ArrayList<ICategoria>());
        replay(imovelSubcategoriaRepositorio);
        
        Integer id = imovelSubcategoriaBO.buscaIdCategoriaComMaisEconomias(1);
        
        assertEquals(0, id.intValue());
    }    
}
