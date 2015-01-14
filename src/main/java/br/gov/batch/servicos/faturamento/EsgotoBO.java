package br.gov.batch.servicos.faturamento;

import java.math.BigDecimal;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.atendimentopublico.LigacaoEsgoto;
import br.gov.model.cadastro.Imovel;
import br.gov.servicos.atendimentopublico.LigacaoEsgotoRepositorio;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;

@Stateless
public class EsgotoBO {
    
    @EJB
    private LigacaoEsgotoRepositorio repositorio;
    
    @EJB
    private ImovelSubcategoriaRepositorio imovelSubCategoriaRepositorio;
    
    public BigDecimal percentualEsgotoAlternativo(Imovel imovel){
        BigDecimal percentualEsgoto = BigDecimal.ZERO;
        
        if (imovel.faturamentoEsgotoAtivo()) {
            LigacaoEsgoto ligacaoEsgoto = repositorio.buscarLigacaoEsgotoPorIdImovel(imovel.getId());
            
            percentualEsgoto = ligacaoEsgoto != null ? ligacaoEsgoto.getPercentual() : BigDecimal.ZERO;
        }

        return percentualEsgoto;        
    }
}
