package br.gov.batch.gerardadosleitura;

import java.util.Properties;

import javax.batch.api.chunk.ItemProcessor;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.BatchLogger;
import br.gov.batch.ControleExecucaoAtividade;
import br.gov.batch.util.BatchUtil;
import br.gov.model.batch.ProcessoIniciado;
import br.gov.model.micromedicao.LeituraTipo;
import br.gov.model.micromedicao.Rota;
import br.gov.servicos.batch.ProcessoIniciadoRepositorio;
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
	private ProcessoIniciadoRepositorio repositorio;
	
    @Inject
    private ControleExecucaoAtividade controle;

	public IniciaProcessamentoRota() {
	}

	public Object processItem(Object param) throws Exception {
		ProcessoIniciado processo = repositorio.obterPorID(Integer.valueOf(util.parametroDoJob("idProcessoIniciado")));

		if (!processo.emProcessamento()) {
			logger.info(util.parametroDoJob("idProcessoIniciado"), String.format("Leitura CANCELADA da rota %s.", param));
		} else {
			String idRota = String.valueOf(param);
			Rota rota = rotaRepositorio.obterPorID(Integer.valueOf(idRota));

            Properties processoParametros = new Properties();
            processoParametros.put("idProcessoIniciado" , util.parametroDoJob("idProcessoIniciado"));
            processoParametros.put("anoMesFaturamento"  , util.parametroDoJob("anoMesFaturamento"));
            processoParametros.put("idGrupoFaturamento" , util.parametroDoJob("idGrupoFaturamento"));
            processoParametros.put("vencimentoContas"   , util.parametroDoJob("vencimentoContas"));
            processoParametros.put("idControleAtividade", util.parametroDoJob("idControleAtividade"));
            processoParametros.put("idRota"             , idRota);

			JobOperator jo = BatchRuntime.getJobOperator();

			Long executionRota = null;
			
			if (rota.getLeituraTipo().intValue() == LeituraTipo.LEITURA_E_ENTRADA_SIMULTANEA.getId()) {
				executionRota = jo.start("job_processar_rota_leitura_entrada_simultanea", processoParametros);
			} else {
				executionRota = jo.start("job_processar_rota_convencional", processoParametros);
			}

			controle.iniciaProcessamentoItem(Integer.valueOf(util.parametroDoJob("idControleAtividade")));
			
			logger.logBackgroud(String.format("[executionId: %s] - Rota [%s] marcada para processamento. Tipo de leitura: " + LeituraTipo.findById(rota.getLeituraTipo()), executionRota, param));
		}

		return param;
	}
}