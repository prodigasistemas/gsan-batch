package br.gov.batch.servicos.faturamento;

import java.util.LinkedList;
import java.util.List;

import javax.ejb.Stateless;

import br.gov.model.faturamento.DebitoCobradoCategoria;
import br.gov.model.faturamento.DebitoCobradoCategoriaPK;
import br.gov.model.faturamento.DebitoCobrarCategoria;

@Stateless
public class DebitoCobradoCategoriaBO {
	
	public List<DebitoCobradoCategoria> listaDebitoCobradoCategoriaPeloCobrar(List<DebitoCobrarCategoria> debitos){
		List<DebitoCobradoCategoria> cobrados = new LinkedList<DebitoCobradoCategoria>();
		
		for (DebitoCobrarCategoria cobrar : debitos) {
			DebitoCobradoCategoria cobrado = new DebitoCobradoCategoria();
			
			DebitoCobradoCategoriaPK pk = new DebitoCobradoCategoriaPK();
			pk.setCategoriaId(cobrar.getId().getCategoriaId());
			cobrado.setId(pk);
			
			cobrado.setQuantidadeEconomia(cobrar.getQuantidadeEconomia());
			cobrado.setValorCategoria(cobrar.getValorPrestacaoEconomia());
			
			cobrados.add(cobrado);
		}
		
		return cobrados;
	}	
}