package br.gov.batch.gerardadosleitura;

import javax.batch.api.chunk.ItemProcessor;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.servicos.faturamento.FaturamentoImovelBO;
import br.gov.batch.servicos.faturamento.to.FaturamentoImovelTO;
import br.gov.batch.util.BatchUtil;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.Rota;
import br.gov.model.util.FormatoData;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.faturamento.FaturamentoAtividadeCronRotaRepositorio;
import static br.gov.model.util.Utilitarios.converterStringParaData;

@Named
public class ProcessarImovelRota implements ItemProcessor {
    @EJB
    private ImovelRepositorio repositorio;

    @EJB
    private FaturamentoImovelBO faturamentoImovelBO;

    @EJB
    private FaturamentoAtividadeCronRotaRepositorio faturamentoAtividadeCronRotaRepositorio;

    @Inject
    private BatchUtil util;

    public ProcessarImovelRota() {
    }

    public Imovel processItem(Object param) throws Exception {
        Imovel imovel = (Imovel) param;

        Integer idRota             = Integer.valueOf(util.parametroDoJob("idRota"));
        Integer idGrupoFaturamento = Integer.valueOf(util.parametroDoJob("idGrupoFaturamento"));
        Integer anoMesFaturamento  = Integer.valueOf(util.parametroDoJob("anoMesFaturamento"));
        String vencimento          = util.parametroDoJob("vencimentoContas");
        
        if (!repositorio.existeContaImovel(imovel.getId(), anoMesFaturamento)) {
            FaturamentoGrupo faturamentoGrupo = new FaturamentoGrupo(idGrupoFaturamento);
            faturamentoGrupo.setAnoMesReferencia(anoMesFaturamento);

            Rota rota = new Rota(idRota);
            rota.setFaturamentoGrupo(faturamentoGrupo);

            if (!repositorio.existeContaImovel(imovel.getId(), anoMesFaturamento)) {
                FaturamentoImovelTO to = new FaturamentoImovelTO();
                to.setRota(rota);
                to.setFaturamentoGrupo(faturamentoGrupo);
                to.setAnoMesFaturamento(anoMesFaturamento);
                to.setDataVencimentoConta(converterStringParaData(vencimento, FormatoData.DIA_MES_ANO));
                to.setIdImovel(imovel.getId());
                
                faturamentoImovelBO.preDeterminarFaturamentoImovel(to);
            }
        }
        
        return imovel;
    }
}