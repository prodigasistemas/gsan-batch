package br.gov.batch.gerardadosleitura;

import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Named;

import br.gov.model.cadastro.Imovel;

@Named
public class GerarDadosLeitura implements ItemProcessor {
	
	public GerarDadosLeitura() {
	}

    public Imovel processItem(Object param) {
    	ImovelPreFaturamento i = (ImovelPreFaturamento) param;
    	
    	Imovel imovel = new Imovel();
    	
    	boolean temHidrometro = temHidrometro(imovel);
    	
        return imovel;
    }

	protected boolean temHidrometro(Imovel imovel) {
		return imovel.getLigacaoAgua() != null 
				&& imovel.getLigacaoAgua().getHidrometroInstalacaoHistorico() != null 
				&& imovel.getLigacaoAgua().getHidrometroInstalacaoHistorico().getId() != null;
	}
}