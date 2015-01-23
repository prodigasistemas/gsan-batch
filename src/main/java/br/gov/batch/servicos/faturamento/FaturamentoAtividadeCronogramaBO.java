package br.gov.batch.servicos.faturamento;

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.faturamento.FaturamentoAtividadeCronograma;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.faturamento.FaturamentoAtividadeCronogramaRepositorio;

@Stateless
public class FaturamentoAtividadeCronogramaBO {

	@EJB
	private FaturamentoAtividadeCronogramaRepositorio repositorio;
	
	public Date obterDataPrevistaDoCronogramaAnterior(FaturamentoGrupo faturamentoGrupo, Integer idAtividade) {
		Integer referenciaAnterior = Utilitarios.reduzirMeses(faturamentoGrupo.getAnoMesReferencia(), 1);

		FaturamentoAtividadeCronograma cronograma = repositorio.buscarPorGrupoEAtividadeEReferencia(faturamentoGrupo.getId(), idAtividade, referenciaAnterior);
		Date dataLeitura = cronograma.getDataPrevista();

		if (dataLeitura == null) {
			dataLeitura = Utilitarios.reduzirDias(new Date(), 30);
		}
		
		return dataLeitura;
	}
}
