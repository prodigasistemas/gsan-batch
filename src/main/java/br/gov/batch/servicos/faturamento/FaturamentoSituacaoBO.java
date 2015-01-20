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
    		List<FaturamentoSituacaoHistorico> faturamentoSituacoes = faturamentoSituacaoRepositorio.faturamentosHistoricoVigentesPorImovel(imovel.getId());
    		
    		FaturamentoSituacaoHistorico faturamentoSituacao = faturamentoSituacoes.get(0);

			if (isSituacaoEmVigencia(anoMesReferencia, faturamentoSituacao) && statusParalisacaoFaturamento) {
				paralisar = Status.ATIVO;
			} 		
		}
    	
    	return paralisar;
    }
	
	private boolean isSituacaoEmVigencia(Integer referencia, FaturamentoSituacaoHistorico situacao) {
		if (referencia >= situacao.getAnoMesFaturamentoSituacaoInicio() && referencia <= situacao.getAnoMesFaturamentoSituacaoFim()) {
			return true;
		} else {
			return false;
		}
	}
}
