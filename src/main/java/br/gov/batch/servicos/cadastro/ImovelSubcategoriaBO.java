package br.gov.batch.servicos.cadastro;

import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.ICategoria;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;

@Stateless
public class ImovelSubcategoriaBO {

    @EJB
    private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio; 

    public Integer buscaIdCategoriaComMaisEconomias(Integer idImovel){
        Collection<ICategoria> categorias = imovelSubcategoriaRepositorio.buscarCategoria(idImovel);
        
        int idCategoria = 0;
        
        int qtdEconomias = 0;
        
        for (ICategoria c : categorias) {
            if (qtdEconomias < c.getQuantidadeEconomias()){
                qtdEconomias =  c.getQuantidadeEconomias();
                idCategoria = c.getId();
            }
        }
        
        return idCategoria;
    }
}
