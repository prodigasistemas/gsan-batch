package br.gov.batch.servicos.arrecadacao;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.arrecadacao.DebitoAutomatico;
import br.gov.model.arrecadacao.DebitoAutomaticoMovimento;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.servicos.arrecadacao.DebitoAutomaticoMovimentoRepositorio;
import br.gov.servicos.arrecadacao.DebitoAutomaticoRepositorio;

@Stateless
public class DebitoAutomaticoMovimentoBO {
	
	@EJB
	private DebitoAutomaticoMovimentoRepositorio debitoAutomaticoMovimentoRepositorio;
	
	@EJB
	private DebitoAutomaticoRepositorio debitoAutomaticoRepositorio;

	public void gerarMovimentoDebitoAutomatico(Imovel imovel, Conta conta, FaturamentoGrupo faturamentoGrupo) {
		DebitoAutomatico debitoAutomatico = debitoAutomaticoRepositorio.obterDebitoAutomaticoPorImovel(imovel.getId());
		
		if (debitoAutomatico != null){
			DebitoAutomaticoMovimento debitoAutomaticoMovimento = buildMovimentoDebitoAutomatico(conta, debitoAutomatico, faturamentoGrupo);
			
			debitoAutomaticoMovimentoRepositorio.inserir(debitoAutomaticoMovimento);
		}
	}
	
	public DebitoAutomaticoMovimento buildMovimentoDebitoAutomatico(Conta conta, DebitoAutomatico debitoAutomatico, FaturamentoGrupo faturamentoGrupo){
	    return new DebitoAutomaticoMovimento(). new Builder()
	            .debitoAutomatico(debitoAutomatico)
	            .contaGeral(conta.getContaGeral())
	            .faturamentoGrupo(faturamentoGrupo)
	            .dataVencimento(conta.getDataVencimentoConta())
	            .atualizaProcessamento()
	            .atualizaUltimaAlteracao()
	            .build();
	}
}
