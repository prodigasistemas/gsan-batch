package br.gov.ejb;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.gov.model.cadastro.Imovel;
import br.gov.model.operacao.UnidadeMedida;

@Stateless
public class ImovelEJB{

	@PersistenceContext
	private EntityManager entity;
	
	public List<Imovel> listar(long firstItem, long numItems){
		return entity.createQuery("from Imovel where id < 13000", Imovel.class)
				.setFirstResult((int) firstItem).setMaxResults((int) numItems)
				.getResultList();
	}
	
	public long quantidadeImoveis(){
		return entity.createQuery("select count (i) from Imovel i where i.id < 13000", Long.class).getSingleResult();
	}
}
