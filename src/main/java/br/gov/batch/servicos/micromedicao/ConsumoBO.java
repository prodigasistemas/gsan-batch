package br.gov.batch.servicos.micromedicao;

import java.math.BigDecimal;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.cadastro.EconomiasBO;
import br.gov.batch.servicos.cadastro.ImovelBO;
import br.gov.model.Status;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaCategoriaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaVigenciaRepositorio;
import br.gov.servicos.micromedicao.ConsumoMinimoAreaRepositorio;
import br.gov.servicos.to.ConsumoTarifaVigenciaTO;

@Stateless
public class ConsumoBO {
    
    @EJB
    private SistemaParametrosRepositorio sistemaParametrosRepositorio;

    @EJB
    private ConsumoTarifaRepositorio consumoTarifaRepositorio;
    
    @EJB
    private ConsumoTarifaVigenciaRepositorio consumoTarifaVigenciaRepositorio;
    
    @EJB
    private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;
    
    @EJB
    private ConsumoTarifaCategoriaRepositorio consumoTarifaCategoriaRepositorio;
    
    @EJB
    private EconomiasBO economiasBO;

    @EJB
    private ImovelBO imovelBO;
    
    @EJB
    private ConsumoMinimoAreaRepositorio consumoMinimoAreaRepositorio;
    
    private SistemaParametros sistemaParametros;

    @PostConstruct
    public void init(){
        sistemaParametros = sistemaParametrosRepositorio.getSistemaParametros();
    }
    
    
    public int consumoNaoMedido(Integer idImovel, Integer anoMesReferencia) {
        if (sistemaParametros.getIndicadorNaoMedidoTarifa() == Status.ATIVO.getId()){
            return this.consumoMinimoLigacao(idImovel);
        } else{
            return this.obterConsumoNaoMedidoSemTarifa(idImovel, anoMesReferencia);
        }
        
    }
    
    public int obterConsumoNaoMedidoSemTarifa(Integer idImovel, Integer anoMesReferencia) {
        Integer qtdEconomiasVirtuais = economiasBO.quantidadeEconomiasVirtuais(idImovel);

        BigDecimal areaConstruida = imovelBO.verificarAreaConstruida(idImovel);

        BigDecimal areaConstruidaVirtual = areaConstruida.divide(new BigDecimal(qtdEconomiasVirtuais), 2, BigDecimal.ROUND_HALF_UP);
        
        Collection<ICategoria> subcategoria = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasSubcategoria(idImovel);
        
        Integer consumoNaoMedido = 0;
        
        for (ICategoria sub : subcategoria) {
            Integer consumoMinimo = consumoMinimoAreaRepositorio.pesquisarConsumoMinimoArea(areaConstruidaVirtual, anoMesReferencia, null, sub.getId());

            if (sub.getCategoria().getFatorEconomias() != null) {
                consumoNaoMedido += consumoMinimo * sub.getCategoria().getFatorEconomias();
            } else {
                consumoNaoMedido += consumoMinimo * sub.getQuantidadeEconomias();
            }
        }
        
        return consumoNaoMedido;
    }

    public Integer consumoMinimoLigacao(Integer idImovel) {
        
        Integer idTarifa = consumoTarifaRepositorio.consumoTarifaDoImovel(idImovel);
        
        Collection<ICategoria> economias = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasPorImovel(idImovel);
        
        Integer consumoMinimoLigacao = this.obterConsumoMinimoLigacaoPorCategoria(idImovel, idTarifa, economias);

        return consumoMinimoLigacao;
    }

    public int obterConsumoMinimoLigacaoPorCategoria(Integer idImovel, Integer idTarifa, Collection<ICategoria> categorias){
        
        int consumoMinimoLigacao = 0;
        
        ConsumoTarifaVigenciaTO consumoTarifaVigencia = consumoTarifaVigenciaRepositorio.maiorDataVigenciaConsumoTarifa(idTarifa);
        
        for (ICategoria economia : categorias) {
            Integer consumoMinimoTarifa = consumoTarifaCategoriaRepositorio.consumoMinimoTarifa(economia, consumoTarifaVigencia.getIdVigencia());
            
            if (economia.getFatorEconomias() != null) {
                consumoMinimoLigacao += consumoMinimoTarifa * economia.getFatorEconomias().intValue();
            } else {
                consumoMinimoLigacao += consumoMinimoTarifa * economia.getQuantidadeEconomias();
            }

        }
        
        return consumoMinimoLigacao;
    }
    
}
