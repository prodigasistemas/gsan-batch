package br.gov.batch.gerardadosleitura;

import java.util.List;

import javax.batch.api.chunk.ItemProcessor;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import br.gov.model.Status;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.FaturamentoSituacaoHistorico;
import br.gov.model.faturamento.FaturamentoSituacaoTipo;
import br.gov.servicos.faturamento.FaturamentoSituacaoEJB;
import br.gov.util.BatchUtil;

@Named
public class GerarDadosLeitura implements ItemProcessor {
	private static Logger logger = Logger.getLogger(GerarDadosLeitura.class);
	
	@EJB
	private FaturamentoSituacaoEJB faturamentoSituacaoEJB;
	
    @Inject
    private BatchUtil util;
    
    private Integer anoMesFaturamento = 0;
	
	public GerarDadosLeitura() {
	}

    public Imovel processItem(Object param) {
    	Imovel imovel = (Imovel) param;
    	
    	boolean temHidrometro = temHidrometro(imovel);
    	
    	boolean situacaoFaturamento = situacaoFaturamento(imovel); 

    	boolean condicaoEspecialFaturamento = codicaoEspecialFaturamento(imovel); 
    	
    	logger.info("Imovel: " + imovel.getId() + " temHidrometro: " + temHidrometro + " situacao: " + situacaoFaturamento);
    	
    	
        return imovel;
    }

	private boolean codicaoEspecialFaturamento(Imovel imovel) {
		boolean faturar = impedidoPeloTipoDoFaturamento(imovel.getFaturamentoSituacaoTipo());
		
    	List<FaturamentoSituacaoHistorico> list =  faturamentoSituacaoEJB.situacoesEspeciaisFaturamentoVigentes(imovel.getId());
    	
    	anoMesFaturamento = Integer.valueOf(util.parametroDoBatch("anoMesFaturamento"));
    	
    	for (FaturamentoSituacaoHistorico faturamentoSituacaoHistorico : list) {
			logger.info(faturamentoSituacaoHistorico);
		}
		
		return false;
	}

	private boolean impedidoPeloTipoDoFaturamento(FaturamentoSituacaoTipo tipo) {
		return tipo != null && tipo.getParalisacaoFaturamento() == Status.ATIVO && tipo.getValidoAgua() == Status.ATIVO;
	}

	protected boolean temHidrometro(Imovel imovel) {
		return imovel.getLigacaoAgua() != null 
				&& imovel.getLigacaoAgua().getHidrometroInstalacaoHistorico() != null; 
	}
	
	private boolean situacaoFaturamento(Imovel imovel) {
		return imovel.getLigacaoAguaSituacao().getSituacaoFaturamento() == Status.ATIVO ||
			imovel.getLigacaoEsgotoSituacao().getSituacaoFaturamento() == Status.ATIVO;
	}	
}