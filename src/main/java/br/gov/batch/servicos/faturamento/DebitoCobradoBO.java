package br.gov.batch.servicos.faturamento;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.DebitoCobrado;
import br.gov.model.faturamento.DebitoCobradoCategoria;
import br.gov.servicos.faturamento.DebitoCobradoCategoriaRepositorio;
import br.gov.servicos.faturamento.DebitoCobradoRepositorio;
import br.gov.servicos.faturamento.DebitoCobrarRepositorio;
import br.gov.servicos.to.DebitosContaTO;

@Stateless
public class DebitoCobradoBO {

	@EJB
	private DebitoCobradoRepositorio debitoCobradoRepositorio;
	
	@EJB
	private DebitoCobradoCategoriaRepositorio debitoCobradoCategoriaRepositorio;
	
	@EJB
	private DebitoCobrarRepositorio debitoCobrarRepositorio;
	
	public void inserirDebitoCobrado(DebitosContaTO debitosContaTO, Conta conta) {
		for (DebitoCobrado debitoCobrado : debitosContaTO.getDebitosCobrados()) {
			debitoCobrado.setConta(conta);
			debitoCobrado.setCobradoEm(new Date());
			debitoCobrado.setUltimaAlteracao(new Date());

			Integer idDebitoCobrado = debitoCobradoRepositorio.inserir(debitoCobrado);
			
			List<DebitoCobradoCategoria> debitoCobradoCategorias = debitosContaTO.getCategorias(debitoCobrado);
			
			Collection<DebitoCobradoCategoria> debitosCobradosCategoria = new ArrayList<DebitoCobradoCategoria>();
			for (DebitoCobradoCategoria debitoCobradoCategoria : debitoCobradoCategorias) {
				debitoCobradoCategoria.getId().setDebitoCobradoId(idDebitoCobrado);
				debitoCobradoCategoria.setUltimaAlteracao(new Date());
				
				debitosCobradosCategoria.add(debitoCobradoCategoria);
			}

			debitoCobradoCategoriaRepositorio.inserir(debitosCobradosCategoria);
		}
	}
}
