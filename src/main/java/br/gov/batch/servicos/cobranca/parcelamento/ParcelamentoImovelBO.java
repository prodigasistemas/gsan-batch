package br.gov.batch.servicos.cobranca.parcelamento;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.cobranca.Parcelamento;
import br.gov.model.cobranca.parcelamento.ParcelamentoSituacao;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.GuiaPagamento;
import br.gov.servicos.arrecadacao.pagamento.GuiaPagamentoRepositorio;
import br.gov.servicos.arrecadacao.pagamento.PagamentoRepositorio;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.cobranca.parcelamento.ParcelamentoRepositorio;
import br.gov.servicos.faturamento.ContaRepositorio;

@Stateless
public class ParcelamentoImovelBO {
    
    @EJB
    private SistemaParametrosRepositorio sistemaParametrosRepositorio;

    @EJB
    private ContaRepositorio contaRepositorio;
    
    @EJB
    private ParcelamentoRepositorio parcelamentoRepositorio;
    
    @EJB
    private GuiaPagamentoRepositorio guiaPagamentoRepositorio;
    
    @EJB
    private PagamentoRepositorio pagamentoRepositorio;
    
    public boolean imovelSemParcelamento(Integer idImovel) {
        SistemaParametros sistemaParametros = sistemaParametrosRepositorio.getSistemaParametros();
        
        boolean semParcelamento = false;

        Parcelamento parcelamento = parcelamentoRepositorio.pesquisaParcelamento(idImovel, sistemaParametros.getAnoMesArrecadacao(), ParcelamentoSituacao.NORMAL);
        
        if (parcelamento == null){
            semParcelamento = true;
        } else {
            if (parcelamento.semEntrada() || parcelamento.confirmado()) {
                semParcelamento = true;
            } else {
                GuiaPagamento guia = guiaPagamentoRepositorio.guiaDoParcelamento(parcelamento.getId());
                
                if (guia != null) {
                    semParcelamento = pagamentoRepositorio.guiaPaga(guia.getId());
                } else {
                    List<Conta> contasDoParcelamento = contaRepositorio.recuperarPeloParcelamento(parcelamento.getId());
                    
                    if (contasDoParcelamento.size() > 0){
                        int quantidadeContasComPagamento = 0;
                        
                        for (Conta conta : contasDoParcelamento) {
                            if (pagamentoRepositorio.contaPaga(conta.getId())){
                                quantidadeContasComPagamento++;
                            }
                        }
                        
                        semParcelamento = quantidadeContasComPagamento == contasDoParcelamento.size();
                    } else{
                        semParcelamento = true;
                    }
                }
            }
        }
        
        return semParcelamento;
    }
}
