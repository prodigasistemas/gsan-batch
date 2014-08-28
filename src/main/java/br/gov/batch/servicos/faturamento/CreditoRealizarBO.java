package br.gov.batch.servicos.faturamento;

import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.faturamento.CreditoRealizar;
import br.gov.servicos.faturamento.CreditoRealizarRepositorio;

@Stateless
public class CreditoRealizarBO {

	@EJB
	private CreditoRealizarRepositorio creditoRealizarRepositorio;
	
	public void atualizarCreditoRealizar(Collection<CreditoRealizar> creditosRealizar) {
		creditoRealizarRepositorio.atualizarCreditoRealizar(creditosRealizar);
	}
}
