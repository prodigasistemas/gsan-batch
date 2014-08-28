package br.gov.batch.servicos.faturamento;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.CreditoRealizado;
import br.gov.model.faturamento.CreditoRealizadoCategoria;
import br.gov.servicos.faturamento.CreditoRealizadoCategoriaRepositorio;
import br.gov.servicos.faturamento.CreditoRealizadoRepositorio;
import br.gov.servicos.to.CreditosContaTO;

@Stateless
public class CreditoRealizadoBO {

	@EJB
	private CreditoRealizadoRepositorio creditoRealizadoRepositorio;
	
	@EJB
	private CreditoRealizadoCategoriaRepositorio creditoRealizadoCategoriaRepositorio;
	
	public void inserirCreditoRealizado(CreditosContaTO creditosContaTO, Conta conta) {
		
		Set<CreditoRealizado> creditosRealizados = creditosContaTO.getMapCreditoRealizado().keySet();
		
		for (CreditoRealizado creditoRealizado : creditosRealizados) {
			creditoRealizado.setConta(conta);
			creditoRealizado.setUltimaAlteracao(new Date());
			Long idCreditoRealizado = creditoRealizadoRepositorio.inserir(creditoRealizado);
			creditoRealizado.setId(idCreditoRealizado);
			
			Collection<CreditoRealizadoCategoria> creditosRealizadosCategoria = creditosContaTO.getMapCreditoRealizado().get(creditoRealizado);
			creditoRealizadoCategoriaRepositorio.inserir(creditosRealizadosCategoria, creditoRealizado);
		}
	}
}
