package br.gov.batch.servicos.faturamento;

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.faturamento.FaturamentoAtividadeCronograma;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.Rota;
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
	
	public long obterDiferencaDiasCronogramas(Rota rota, Integer idFaturamentoAtividade) {

		Date dataCronogramaMesAtual = repositorio.pesquisarFaturamentoAtividadeCronogramaDataPrevista(
				rota.getFaturamentoGrupo().getId(),idFaturamentoAtividade, rota.getFaturamentoGrupo().getAnoMesReferencia());
		
		Date dataCronogramaMesAnterior = repositorio.pesquisarFaturamentoAtividadeCronogramaDataPrevista(
				rota.getFaturamentoGrupo().getId(),idFaturamentoAtividade, 
				Utilitarios.reduzirMeses(rota.getFaturamentoGrupo().getAnoMesReferencia(), 1));
		
		if (dataCronogramaMesAnterior != null) {
			return Utilitarios.obterDiferencaEntreDatas(dataCronogramaMesAnterior, dataCronogramaMesAtual);
		} else {
			return rota.getNumeroDiasConsumoAjuste();
		}
	}
}
