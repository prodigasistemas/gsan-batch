package br.gov.ejb;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.gov.model.operacao.UnidadeMedida;

@Stateless
public class UnidadeMedidaEJB{

	@PersistenceContext
	private EntityManager entity;
	
	public List<UnidadeMedida> listar(){
		return entity.createQuery("from UnidadeMedida", UnidadeMedida.class).getResultList();
	}
}
