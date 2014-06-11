package br.gov.batch.servicos.faturamento;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.MergeProperties;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.DebitoCobrado;
import br.gov.model.faturamento.DebitoCobrar;
import br.gov.model.faturamento.DebitoCobrarGeral;
import br.gov.servicos.to.DebitoCobradoTO;

@Stateless
public class DebitoCobradoBO {

	@EJB
	private DebitoCobrarBO debitoCobrarBO;
	
	public DebitoCobradoTO gerarDebitoCobrado(Imovel imovel, int anoMesFaturamento){
		DebitoCobradoTO to = new DebitoCobradoTO();
		
		Collection<DebitoCobrar> colecaoDebitosACobrar = debitoCobrarBO.debitosCobrarVigentes(imovel);
		
		BigDecimal valorPrestacao = null;
		
		BigDecimal valorDebito = new BigDecimal(0.0);
		
		for (DebitoCobrar debitoACobrar : colecaoDebitosACobrar) {
			valorPrestacao = debitoACobrar.getValorPrestacao();
			
			valorPrestacao = valorPrestacao.add(debitoACobrar.getResiduoPrestacao()).setScale(2);
			valorDebito = valorDebito.add(valorPrestacao);
			
			DebitoCobrado debitoCobrado = new DebitoCobrado();
			MergeProperties.mergeProperties(debitoCobrado, debitoACobrar);

			DebitoCobrarGeral debitoACobrarGeral = new DebitoCobrarGeral();
			debitoACobrarGeral.setId(debitoACobrar.getId());
			debitoCobrado.setDebitoCobrarGeral(debitoACobrarGeral);
			debitoCobrado.setUltimaAlteracao(new Date());
			debitoCobrado.setValorPrestacao(valorPrestacao);
			debitoCobrado.setNumeroPrestacao(debitoACobrar.getNumeroPrestacaoDebito());
			debitoCobrado.setNumeroPrestacaoDebito((short) (debitoACobrar.getNumeroPrestacaoCobradas() + 1));
			to.addDebitoCobrado(debitoCobrado);
			to.addValorDebito(valorDebito);
		}
		return to;
	}
}
