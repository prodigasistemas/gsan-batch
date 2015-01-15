package br.gov.batch.servicos.faturamento;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.faturamento.ExtratoQuitacao;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.faturamento.ExtratoQuitacaoRepositorio;

@Stateless
public class ExtratoQuitacaoBO {
    @EJB
    private ExtratoQuitacaoRepositorio repositorio;
    
	public String obterMsgQuitacaoDebitos(Integer idImovel, Integer anoMesReferencia) {
		String mensagem = "";
		
		Integer anoMesAnterior = Utilitarios.reduzirMeses(anoMesReferencia, 1);
		
		Integer anoAnterior = Utilitarios.extrairAno(anoMesAnterior);
		
		ExtratoQuitacao extratoQuitacao = repositorio.buscarPorImovelEAno(idImovel, anoAnterior);
		
		if (extratoQuitacao != null && !extratoQuitacao.imprimirNaConta()) {
			mensagem = "Em cumprimento a lei 12.007/2009, declaramos quitados os debitos de consumo de agua e/ou esgoto do ano de " + anoAnterior +  ".";
		} 
		return mensagem;
	}
}
