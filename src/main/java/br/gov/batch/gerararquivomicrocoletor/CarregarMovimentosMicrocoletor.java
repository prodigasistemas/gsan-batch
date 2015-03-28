package br.gov.batch.gerararquivomicrocoletor;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.AbstractItemReader;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.util.BatchUtil;
import br.gov.model.micromedicao.MovimentoRoteiroEmpresa;
import br.gov.servicos.micromedicao.MovimentoRoteiroEmpresaRepositorio;

// FIXME: Apagar classe (apagar código comentado no job_gerar_arquivo_microcoletor)
@Named
public class CarregarMovimentosMicrocoletor extends AbstractItemReader {

	@EJB
	private MovimentoRoteiroEmpresaRepositorio movimentoRoteiroEmpresaRepositorio;

	@Inject
	private BatchUtil util;

	@Inject
	@BatchProperty(name = "primeiroItem")
	private String primeiroItem;

	@Inject
	@BatchProperty(name = "numItens")
	private String numItens;

	private Queue<MovimentoRoteiroEmpresa> movimentos = new ArrayDeque<MovimentoRoteiroEmpresa>();

	public void open(Serializable ckpt) throws Exception {
		int firstItem0 = Integer.valueOf(primeiroItem);
		int numItems0 = Integer.valueOf(numItens);

		int firstItem = firstItem0;
		int numItems = numItems0 - (firstItem - firstItem0);

		Integer idRota = Integer.valueOf(util.parametroDoJob("idRota"));
		Integer referencia = Integer.valueOf(util.parametroDoJob("anoMesFaturamento"));

		List<MovimentoRoteiroEmpresa> lista = movimentoRoteiroEmpresaRepositorio.pesquisarMovimentoParaLeitura(idRota, referencia, firstItem, numItems);

		movimentos = new ArrayDeque<MovimentoRoteiroEmpresa>(lista);
	}

	public MovimentoRoteiroEmpresa readItem() throws Exception {
		if (!movimentos.isEmpty()) {
			return movimentos.poll();
		}
		return null;
	}
}