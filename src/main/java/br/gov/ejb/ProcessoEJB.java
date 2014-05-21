package br.gov.ejb;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.gov.model.batch.ProcessoIniciado;
import br.gov.model.batch.ProcessoParametro;
import br.gov.model.batch.ProcessoSituacao;

@Stateless
public class ProcessoEJB {

	@PersistenceContext
	private EntityManager entity;
	
	public Properties buscarParametrosPorProcessoIniciado(ProcessoIniciado processoIniciado){
		List<ProcessoParametro> processoParametros = this.buscarProcessoParametros(processoIniciado);
		
		Properties parametros = new Properties();
		
		for (ProcessoParametro processoParametro : processoParametros) {
			parametros.setProperty(processoParametro.getNomeParametro(), processoParametro.getValor());
		}
		
		return parametros;
	}
	
	public List<ProcessoParametro> buscarProcessoParametros(ProcessoIniciado processoIniciado){
		return entity.createQuery("from ProcessoParametro where processoIniciado.id = :processoIniciadoId", ProcessoParametro.class)
				.setParameter("processoIniciadoId", processoIniciado.getId())
				.getResultList();
	}
	
	public boolean atualizaSituacaoProcesso(Integer idProcessoIniciado, ProcessoSituacao situacao){
		int result = entity.createQuery("update ProcessoIniciado set situacao = :situacao, inicio = :inicio, ultimaAlteracao = :ultimaAlteracao "
										+ "where id = :processoId ")
						.setParameter("situacao", situacao.getId())
						.setParameter("inicio", new Date())
						.setParameter("ultimaAlteracao", new Date())
						.setParameter("processoId", idProcessoIniciado)
						.executeUpdate();
		return result >= 1;
	}
}
