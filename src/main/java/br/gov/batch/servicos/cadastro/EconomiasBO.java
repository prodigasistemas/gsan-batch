package br.gov.batch.servicos.cadastro;

import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.ICategoria;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;

@Stateless
public class EconomiasBO {
    
    @EJB
    private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio; 
    
    public Integer quantidadeEconomiasVirtuais(Integer idImovel){
        
        Integer qtdEconomias = 0;
        
        Collection<ICategoria> colecaoSubcategoria = imovelSubcategoriaRepositorio.buscarSubcategoria(idImovel);
        
        for (ICategoria iCategoria : colecaoSubcategoria) {
            if (iCategoria.getCategoria().getFatorEconomias() != null){
                qtdEconomias += iCategoria.getCategoria().getFatorEconomias(); 
            }
            else{
                qtdEconomias += iCategoria.getQuantidadeEconomias();
            }
            
        }
        return qtdEconomias;
    }
    

}
