package br.gov.batch.gerararquivo;

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
public class IniciaGeracaoArquivoRota implements ItemProcessor {

	@EJB
	private BatchLogger logger;

	@EJB
	private RotaRepositorio rotaRepositorio;

	@Inject
	private BatchUtil util;

    @Inject
    private ControleExecucaoAtividade controle;


	@Inject
	private ProcessoIniciadoRepositorio processoRepositorio;
	
	public IniciaGeracaoArquivoRota() {
	}

	public Object processItem(Object param) throws Exception {

		ProcessoIniciado processo = processoRepositorio.obterPorID(Integer.valueOf(util.parametroDoJob("idProcessoIniciado")));

		if (!processo.emProcessamento()) {
			logger.info(util.parametroDoJob("idProcessoIniciado"), String.format("Geracao de arquivo CANCELADA para a rota %s.", param));
		} else {
			String idRota = String.valueOf(param);
			Rota rota = rotaRepositorio.obterPorID(Integer.valueOf(idRota));

			Properties processoParametros = new Properties();
			processoParametros.put("idProcessoIniciado" , util.parametroDoJob("idProcessoIniciado"));
			processoParametros.put("anoMesFaturamento"  , util.parametroDoJob("anoMesFaturamento"));
			processoParametros.put("idGrupoFaturamento" , util.parametroDoJob("idGrupoFaturamento"));
			processoParametros.put("idControleAtividade", util.parametroDoJob("idControleAtividade"));			
			processoParametros.put("vencimentoContas"   , util.parametroDoJob("vencimentoContas"));			
			processoParametros.put("idRota"             , idRota);

			JobOperator jo = BatchRuntime.getJobOperator();
			
			Long executionRota = null;
			if (rota.getLeituraTipo().intValue() == LeituraTipo.LEITURA_E_ENTRADA_SIMULTANEA.getId()) {
				executionRota = jo.start("job_gerar_arquivo_leitura_entrada_simultanea", processoParametros);
			} else if (rota.getLeituraTipo().intValue() == LeituraTipo.MICROCOLETOR.getId()) {
				executionRota = jo.start("job_gerar_arquivo_microcoletor", processoParametros);
			}

			logger.logBackgroud(String.format("[executionId: %s] - Rota [%s] marcada para geracao de arquivos. ", executionRota, param));

			controle.iniciaProcessamentoItem(Integer.valueOf(util.parametroDoJob("idControleAtividade")));
		}

		return param;
	}
}