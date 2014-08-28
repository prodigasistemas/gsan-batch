package br.gov.batch.servicos.arrecadacao;

import java.util.Date;

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
			DebitoAutomaticoMovimento debitoAutomaticoMovimento = new DebitoAutomaticoMovimento();
			debitoAutomaticoMovimento.setDebitoAutomatico(debitoAutomatico);
			debitoAutomaticoMovimento.setDataVencimento(conta.getDataVencimentoConta());
			debitoAutomaticoMovimento.setContaGeral(conta.getContaGeral());
			debitoAutomaticoMovimento.setFaturamentoGrupo(faturamentoGrupo);
			debitoAutomaticoMovimento.setProcessamento(new Date());
			debitoAutomaticoMovimento.setEnvioBanco(null);
			debitoAutomaticoMovimento.setRetornoBanco(null);
			debitoAutomaticoMovimento.setUltimaAlteracao(new Date());
			debitoAutomaticoMovimento.setNumeroSequenciaArquivoEnviado(null);
			debitoAutomaticoMovimento.setNumeroSequenciaArquivoRecebido(null);
			
			debitoAutomaticoMovimentoRepositorio.inserir(debitoAutomaticoMovimento);
		}
	}
}
