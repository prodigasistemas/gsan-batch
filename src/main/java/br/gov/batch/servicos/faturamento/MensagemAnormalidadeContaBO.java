package br.gov.batch.servicos.faturamento;

import static br.gov.model.util.Utilitarios.reduzirMeses;

import javax.ejb.EJB;

import br.gov.batch.servicos.cadastro.ImovelSubcategoriaBO;
import br.gov.batch.servicos.micromedicao.ConsumoAnormalidadeAcaoBO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.micromedicao.ConsumoAnormalidadeAcao;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;
import br.gov.servicos.to.AnormalidadeHistoricoConsumoTO;

public class MensagemAnormalidadeContaBO {
    
    @EJB
    private ConsumoHistoricoRepositorio consumoHistoricoRepositorio;
    
    @EJB
    private ImovelSubcategoriaBO imovelSubcategoriaBO;
    
    @EJB
    private ConsumoAnormalidadeAcaoBO consumoAnormalidadeAcaoBO;
    
    public String[] obterMensagemAnormalidadeConsumo(Imovel imovel, Integer anoMesReferencia) {

        String[] mensagemConta = null;
        
        AnormalidadeHistoricoConsumoTO anormalidadeConsumo = 
                consumoHistoricoRepositorio.anormalidadeHistoricoConsumo(imovel.getId(), LigacaoTipo.AGUA, anoMesReferencia);
        
        if (anormalidadeConsumo == null){
            anormalidadeConsumo = consumoHistoricoRepositorio.anormalidadeHistoricoConsumo(imovel.getId(), LigacaoTipo.ESGOTO, anoMesReferencia);
        }
        
        if (anormalidadeConsumo != null && anormalidadeConsumo.anormalidadeporBaixoAltoOuEstouroConsumo()){
            Integer idCategoria = imovelSubcategoriaBO.buscaIdCategoriaComMaisEconomias(imovel.getId());
            
            ConsumoAnormalidadeAcao acao = consumoAnormalidadeAcaoBO.acaoASerTomada(anormalidadeConsumo.getIdAnormalidade(), idCategoria, imovel.getImovelPerfil().getId());
            
            if (acao != null){
                anormalidadeConsumo = consumoHistoricoRepositorio.anormalidadeHistoricoConsumo(imovel.getId(), LigacaoTipo.ESGOTO, reduzirMeses(anoMesReferencia, 1));
                
                String mensagemContaAnormalidade = "";
                
                if (anormalidadeConsumo == null){
                    mensagemContaAnormalidade = acao.getDescricaoContaMensagemMes1();
                }else{
                    anormalidadeConsumo = consumoHistoricoRepositorio
                            .anormalidadeHistoricoConsumo(imovel.getId(), LigacaoTipo.ESGOTO, reduzirMeses(anoMesReferencia, 1), anormalidadeConsumo.getIdAnormalidade());
                    
                    if (anormalidadeConsumo == null) {
                        mensagemContaAnormalidade = acao.getDescricaoContaMensagemMes2();
                    } else {
                        mensagemContaAnormalidade = acao.getDescricaoContaMensagemMes3();
                    }
                }
                
                mensagemConta = quebraMensagemEmDuasPartes(mensagemContaAnormalidade); 
            }
        }
        
        return mensagemConta;
    }
    
    private String[] quebraMensagemEmDuasPartes(String mensagemContaAnormalidade) {
        String[] msg = new String[]{"", ""};
        
        if (mensagemContaAnormalidade.length() < 60) {
            msg[0] = mensagemContaAnormalidade;
        } else {
            msg[0] = mensagemContaAnormalidade.substring(0, 60);
            msg[1] = mensagemContaAnormalidade.substring(60);
        }
        
        return msg;
    }
}
