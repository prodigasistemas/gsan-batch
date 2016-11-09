package br.gov.batch.servicos.cadastro;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.model.cadastro.ICategoria;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.to.ImovelSubcategoriaTO;

public class ImovelSubcategoriaBOTest {
    
    @InjectMocks
    private ImovelSubcategoriaBO imovelSubcategoriaBO;
    
    @Mock
    private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;
    
    Collection<ICategoria> categorias = null;
    
    @Before
    public void init(){
        imovelSubcategoriaBO = new ImovelSubcategoriaBO();
        
        categorias = new ArrayList<ICategoria>();
        
        ImovelSubcategoriaTO to = new ImovelSubcategoriaTO(1);
        to.setSubcategoriaQuantidadeEconomias(2L);
        categorias.add(to);
        
        to = new ImovelSubcategoriaTO(1, 2);
        to.setSubcategoriaQuantidadeEconomias(4L);
        categorias.add(to);
        
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testBuscaIdCategoriaComMaisEconomias(){
        when(imovelSubcategoriaRepositorio.buscarCategoria(1)).thenReturn(categorias);
        
        Integer id = imovelSubcategoriaBO.buscaIdCategoriaComMaisEconomias(1);
        
        assertEquals(2, id.intValue());
    }
    
    @Test
    public void testBuscaIdCategoriaSemEconomias(){
        when(imovelSubcategoriaRepositorio.buscarCategoria(1)).thenReturn(new ArrayList<ICategoria>());
        
        Integer id = imovelSubcategoriaBO.buscaIdCategoriaComMaisEconomias(1);
        
        assertEquals(0, id.intValue());
    }    
}
