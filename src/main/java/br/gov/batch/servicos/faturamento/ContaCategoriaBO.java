package br.gov.batch.servicos.faturamento;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.ContaCategoria;
import br.gov.model.faturamento.ContaCategoriaPK;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.faturamento.ContaCategoriaRepositorio;
import br.gov.servicos.to.ImovelSubcategoriaTO;

@Stateless
public class ContaCategoriaBO {
	
	@EJB
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;
	
	@EJB
	private ContaCategoriaRepositorio contaCategoriaRepositorio;

	public void inserirContasCategoriaValoresZerados(Integer idImovel, Conta conta) throws Exception {
		Collection<ImovelSubcategoriaTO> colecaoCategoriaOUSubcategoria = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasPorImovel(idImovel);
		Collection<ContaCategoria> contasCategoria = this.gerarContaCategoriaValoresZerados(conta, colecaoCategoriaOUSubcategoria);
		contaCategoriaRepositorio.inserir(contasCategoria);
	}
	
	private Collection<ContaCategoria> gerarContaCategoriaValoresZerados(Conta conta, Collection<ImovelSubcategoriaTO> colecaoCategorias) throws Exception {
		Collection<ContaCategoria> helper = new ArrayList<ContaCategoria>();
		
		ContaCategoria contaCategoria = null;
		ContaCategoriaPK contaCategoriaPK = null;

		for (ImovelSubcategoriaTO categoria : colecaoCategorias) {
			
			contaCategoria = new ContaCategoria();
			contaCategoriaPK = new ContaCategoriaPK();
			contaCategoriaPK.setContaId(conta.getId());
			contaCategoriaPK.setCategoriaId(categoria.getCategoria().getId());
			contaCategoriaPK.setSubcategoriaId(categoria.getSubcategoria().getId());
			contaCategoria.setPk(contaCategoriaPK);
			contaCategoria.setQuantidadeEconomia(categoria.getQuantidadeEconomias().shortValue());
			contaCategoria.setValorAgua(new BigDecimal("0.00"));
			contaCategoria.setConsumoAgua(0);
			contaCategoria.setValorEsgoto(new BigDecimal("0.00"));
			contaCategoria.setConsumoEsgoto(0);
			contaCategoria.setValorTarifaMinimaAgua(new BigDecimal("0.00"));
			contaCategoria.setConsumoMinimoAgua(0);
			contaCategoria.setValorTarifaMinimaEsgoto(new BigDecimal("0.00"));
			contaCategoria.setConsumoMinimoEsgoto(0);
			contaCategoria.setUltimaAlteracao(new java.sql.Date(System.currentTimeMillis()));
			
			helper.add(contaCategoria);
		}

		return helper;
	}
}
