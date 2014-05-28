package br.gov.batch.gerardadosleitura;

import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Named;

import org.jboss.logging.Logger;

import br.gov.model.Status;
import br.gov.model.cadastro.Imovel;

@Named
public class GerarDadosLeitura implements ItemProcessor {
	private static Logger logger = Logger.getLogger(GerarDadosLeitura.class);
	
	public GerarDadosLeitura() {
	}

    public Imovel processItem(Object param) {
    	Imovel imovel = (Imovel) param;
    	
    	boolean temHidrometro = temHidrometro(imovel);
    	
    	boolean situacaoFaturamento = situacaoFaturamento(imovel); 
    	
    	logger.info("Imovel: " + imovel.getId() + " temHidrometro: " + temHidrometro + " situacao: " + situacaoFaturamento);
    	
        return imovel;
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