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
	
	public void salvar(UnidadeMedida obj) {
		entity.persist(obj);
	}
	
	public List<UnidadeMedida> list(){
		return entity.createQuery("from UnidadeMedida", UnidadeMedida.class).getResultList();
	}

	public void atualizar(UnidadeMedida obj) throws Exception {
		entity.merge(obj);
	}
	
	public void excluir(UnidadeMedida obj) {
		UnidadeMedida objRemover = entity.find(UnidadeMedida.class, obj.getCodigo());
		entity.remove(objRemover);		
	}	
}
