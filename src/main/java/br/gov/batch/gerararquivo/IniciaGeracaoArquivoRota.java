package br.gov.batch.gerararquivo;

import java.util.Properties;

import javax.batch.api.chunk.ItemProcessor;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
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
public class IniciaGeracaoArquivoRota implements ItemProcessor {

	@EJB
	private BatchLogger logger;

	@EJB
	private RotaRepositorio rotaRepositorio;

	@Inject
	private BatchUtil util;

	@Inject
	private ControleProcessoGeracaoArquivo controle;

	@Inject
	private ProcessoRepositorio processoRepositorio;

	public IniciaGeracaoArquivoRota() {
	}

	public Object processItem(Object param) throws Exception {

		ProcessoIniciado processo = processoRepositorio.buscarProcessoIniciadoPorId(Integer.valueOf(util.parametroDoBatch("idProcessoIniciado")));

		if (!processo.emProcessamento()) {
			logger.info(util.parametroDoBatch("idProcessoIniciado"), String.format("Geracao de arquivo CANCELADA para a rota %s.", param));
		} else {
			String idRota = String.valueOf(param);
			Rota rota = rotaRepositorio.obterPorID(Integer.valueOf(idRota));

			Properties processoParametros = new Properties();
			processoParametros.put("idRota", idRota);
			processoParametros.put("anoMesFaturamento", util.parametroDoBatch("anoMesFaturamento"));
			processoParametros.put("idProcessoIniciado", util.parametroDoBatch("idProcessoIniciado"));
			processoParametros.put("idGrupoFaturamento", util.parametroDoBatch("idGrupoFaturamento"));

			JobOperator jo = BatchRuntime.getJobOperator();

			Long executionRota = null;
			if (rota.getLeituraTipo().intValue() == LeituraTipo.LEITURA_E_ENTRADA_SIMULTANEA.getId()) {
				executionRota = jo.start("job_gerar_arquivo", processoParametros);
			} else if (rota.getLeituraTipo().intValue() == LeituraTipo.MICROCOLETOR.getId()) {
				executionRota = jo.start("job_gerar_arquivo_microcoletor", processoParametros);
			}

			logger.logBackgroud(String.format("[executionId: %s] - Rota [%s] marcada para geracao de arquivos. ", executionRota, param));

			controle.iniciaGeracaoArquivoRota();
		}

		return param;
	}
}