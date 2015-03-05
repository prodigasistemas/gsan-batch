package br.gov.batch.gerardadosleitura;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.AbstractItemReader;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import br.gov.batch.servicos.micromedicao.RotaBO;
import br.gov.batch.util.BatchUtil;
import br.gov.model.cadastro.Imovel;

@Named
public class CarregarImoveisRotaLeitura extends AbstractItemReader {
	@EJB
	private RotaBO rotaBO;

	@Inject
	private BatchUtil util;

	@Inject
	@BatchProperty(name = "primeiroItem")
	private String primeiroItem;

	@Inject
	@BatchProperty(name = "numItens")
	private String numItens;

	private Queue<Imovel> imoveis = new ArrayDeque<Imovel>();

	public void open(Serializable ckpt) throws Exception {
		int firstItem0 = Integer.valueOf(primeiroItem);
		int numItems0 = Integer.valueOf(numItens);

		int firstItem = firstItem0;
		int numItems = numItems0 - (firstItem - firstItem0);

		Integer idRota = Integer.valueOf(util.parametroDoBatch("idRota"));

		List<Imovel> lista = rotaBO.imoveisParaLeitura(idRota, firstItem, numItems);

		imoveis = new ArrayDeque<Imovel>(lista);
	}

	//TODO: Testar apenas com imoveis.poll()
	public Imovel readItem() throws Exception {
		if (!imoveis.isEmpty()) {
			return imoveis.poll();
		}
		return null;
	}
}