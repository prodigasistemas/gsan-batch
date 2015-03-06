package br.gov.batch.gerararquivomicrocoletor;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.Particao;
import br.gov.batch.util.BatchUtil;
import br.gov.servicos.micromedicao.MovimentoRoteiroEmpresaRepositorio;

@Named
public class ParticionadorMicrocoletor extends Particao {

	@EJB
	protected MovimentoRoteiroEmpresaRepositorio repositorio;

	@Inject
	private BatchUtil util;

	public int totalItens() {
		int idRota = Integer.valueOf(util.parametroDoBatch("idRota"));
		int referencia = Integer.valueOf(util.parametroDoBatch("anoMesFaturamento"));
		return (int) repositorio.totalMovimentosParaLeitura(idRota, referencia);
	}
}
