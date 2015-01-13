package br.gov.batch.servicos.faturamento;

import br.gov.model.util.Utilitarios;

public class ExtratoQuitacaoBO {
	public String obterMsgQuitacaoDebitos(Integer idImovel, Integer anoMesReferencia) {
		String mensagem = "";
		
		Integer anoMesAnterior = Utilitarios.reduzirMeses(anoMesReferencia, 1);
		
		Integer anoAnterior = Utilitarios.extrairAno(anoMesAnterior);
		
//		ExtratoQuitacao extratoQuitacao = this.obterExtratoQuitacaoImovel(imovel.getId(), anoAnterior);
//		
//		if (extratoQuitacao != null && extratoQuitacao.getIndicadorImpressaoNaConta().equals(new Integer(ConstantesSistema.NAO))) {
//			mensagem = "Em cumprimento a lei 12.007/2009, declaramos quitados os dÈbitos de consumo de ·gua e/ou esgoto do ano de " + anoAnterior +  ".";
//		} 
		return mensagem;
	}
}
