package br.gov.ejb;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.gov.model.UnidadeMedida;
import br.gov.model.cadastro.Imovel;

@Stateless
public class ImovelEJB{

	@PersistenceContext
	private EntityManager entity;
	
	public void salvar(UnidadeMedida obj) {
		entity.persist(obj);
	}
	
	public List<Imovel> list(){
		return entity.createQuery("from Imovel", Imovel.class).getResultList();
	}
}
