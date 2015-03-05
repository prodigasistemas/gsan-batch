package br.gov.batch.gerardadosleitura;

import java.util.Properties;

import javax.batch.api.chunk.ItemProcessor;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.util.BatchUtil;
import br.gov.model.batch.ProcessoIniciado;
import br.gov.model.micromedicao.LeituraTipo;
import br.gov.model.micromedicao.Rota;
import br.gov.servicos.batch.ProcessoRepositorio;
import br.gov.servicos.micromedicao.RotaRepositorio;

@Named
public class IniciaProcessamentoRota implements ItemProcessor {

	@EJB
	private BatchLogger logger;

	@EJB
	private RotaRepositorio rotaRepositorio;

	@Inject
	private BatchUtil util;

	@Inject
	private ControleProcessoRota controle;

	@Inject
	protected JobContext jobContext;

	@Inject
	private ProcessoRepositorio processoRepositorio;

	public IniciaProcessamentoRota() {
	}

	public Object processItem(Object param) throws Exception {
		ProcessoIniciado processo = processoRepositorio.buscarProcessoIniciadoPorId(Integer.valueOf(util.parametroDoBatch("idProcessoIniciado")));

		if (!processo.emProcessamento()) {
			logger.info(util.parametroDoBatch("idProcessoIniciado"), String.format("Leitura CANCELADA da rota %s.", param));
		} else {
			String idRota = String.valueOf(param);
			Rota rota = rotaRepositorio.obterPorID(Integer.valueOf(idRota));

			Properties processoParametros = new Properties();
			processoParametros.put("idProcessoIniciado", util.parametroDoBatch("idProcessoIniciado"));
			processoParametros.put("idRota", idRota);
			processoParametros.put("anoMesFaturamento", util.parametroDoBatch("anoMesFaturamento"));
			processoParametros.put("idGrupoFaturamento", util.parametroDoBatch("idGrupoFaturamento"));
			processoParametros.put("parentExecutionId", String.valueOf(jobContext.getExecutionId()));

			JobOperator jo = BatchRuntime.getJobOperator();

			Long executionRota = null;

			if (rota.getLeituraTipo().intValue() == LeituraTipo.LEITURA_E_ENTRADA_SIMULTANEA.getId()) {
				executionRota = jo.start("job_processar_rota", processoParametros);
			} else {
				executionRota = jo.start("job_processar_rota_leitura", processoParametros);
			}
			
			logger.logBackgroud(String.format("[executionId: %s] - Rota [%s] marcada para processamento. Tipo de leitura: " + LeituraTipo.findById(rota.getLeituraTipo()), executionRota, param));
			controle.iniciaProcessamentoRota();
		}

		return param;
	}
}