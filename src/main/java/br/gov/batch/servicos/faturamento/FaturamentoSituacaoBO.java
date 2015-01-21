package br.gov.batch.servicos.faturamento;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.Status;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.FaturamentoSituacaoHistorico;
import br.gov.servicos.faturamento.FaturamentoSituacaoRepositorio;

@Stateless
public class FaturamentoSituacaoBO {

	@EJB
	private FaturamentoSituacaoRepositorio faturamentoSituacaoRepositorio;

	
	public Status verificarParalisacaoFaturamentoAgua(Imovel imovel, Integer anoMesReferencia) {
		return verificarParalisacaoFaturamento(imovel, anoMesReferencia, imovel.getFaturamentoSituacaoTipo().paralisacaoFaturamentoAgua());
    }
	
	public Status verificarParalisacaoFaturamentoEsgoto(Imovel imovel, Integer anoMesReferencia) {
		return verificarParalisacaoFaturamento(imovel, anoMesReferencia, imovel.getFaturamentoSituacaoTipo().paralisacaoFaturamentoEsgoto());
    }

	public Status verificarParalisacaoFaturamento(Imovel imovel, Integer anoMesReferencia, boolean statusParalisacaoFaturamento) {
    	Status paralisar = Status.INATIVO;
    	
    	if (imovel.getFaturamentoSituacaoTipo() != null) {
    		if (obterFaturamentoSituacaoVigente(imovel, anoMesReferencia) != null && statusParalisacaoFaturamento) {
    			paralisar = Status.ATIVO;
    		}
		}
    	
    	return paralisar;
    }
	
	public boolean isSituacaoEmVigencia(Integer referencia, FaturamentoSituacaoHistorico situacao) {
		if (referencia >= situacao.getAnoMesFaturamentoSituacaoInicio() && referencia <= situacao.getAnoMesFaturamentoSituacaoFim()) {
			return true;
		} else {
			return false;
		}
	}
	
	public FaturamentoSituacaoHistorico obterFaturamentoSituacaoVigente(Imovel imovel, Integer anoMesReferencia) {
		List<FaturamentoSituacaoHistorico> situacoes = faturamentoSituacaoRepositorio.faturamentosHistoricoVigentesPorImovel(imovel.getId());
		
		for (FaturamentoSituacaoHistorico situacao : situacoes) {
			if (isSituacaoEmVigencia(anoMesReferencia, situacao))
				return situacao;
		}
		return null;
	}
}
