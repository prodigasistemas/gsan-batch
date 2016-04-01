package br.gov.batch.servicos.faturamento;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.Cliente;
import br.gov.model.faturamento.ImpostoTipo;
import br.gov.model.faturamento.ImpostoTipoAliquota;
import br.gov.servicos.cadastro.ClienteRepositorio;
import br.gov.servicos.faturamento.ImpostoTipoAliquotaRepositorio;
import br.gov.servicos.faturamento.ImpostoTipoRepositorio;
import br.gov.servicos.to.ImpostoDeduzidoTO;
import br.gov.servicos.to.ImpostosDeduzidosContaTO;

@Stateless
public class ImpostosContaBO {

	@EJB
	private ClienteRepositorio clienteRepositorio;

	@EJB
	private ImpostoTipoRepositorio impostoTipoRepositorio;

	@EJB
	private ImpostoTipoAliquotaRepositorio impostoTipoAliquotaRepositorio;

	private BigDecimal valorImpostoDeduzido = null;
	private BigDecimal valorImpostoDeduzidoFinal = null;
	private BigDecimal percetagemTotalAliquota = null;
	private BigDecimal valorImpostoDeduzidoTotal = null;
	private BigDecimal baseCalculo = null;
	
	private ImpostoDeduzidoTO impostoDeduzidoTO = null;

	public ImpostosDeduzidosContaTO gerarImpostosDeduzidosConta(Integer idImovel, Integer anoMesReferencia, BigDecimal valorDebito, BigDecimal valorCredito) {
		ImpostosDeduzidosContaTO retorno = new ImpostosDeduzidosContaTO();

		Cliente clienteFederal = clienteRepositorio.buscarClienteFederalResponsavelPorImovel(idImovel);

		if (clienteFederal == null) {
			retorno.setListaImpostosDeduzidos(null);
			retorno.setValorTotalImposto(new BigDecimal("0.00"));
			retorno.setValorBaseCalculo(new BigDecimal("0.00"));

			return retorno;
		}

		inicializaValoresCalculo();

		baseCalculo = new BigDecimal("0.00");

		Iterator<ImpostoTipo> iteratorImpostoTipo = impostoTipoRepositorio.buscarImpostoTipoAtivos().iterator();

		Collection<ImpostoDeduzidoTO> colecaoHelper = new ArrayList<ImpostoDeduzidoTO>();
		ImpostoTipoAliquota impostoTipoAliquota = null;
		ImpostoTipo impostoTipo = null;

		while (iteratorImpostoTipo.hasNext()) {
			impostoTipo = (ImpostoTipo) iteratorImpostoTipo.next();

			impostoTipoAliquota = impostoTipoAliquotaRepositorio.buscarAliquotaImposto(impostoTipo.getId(), anoMesReferencia);

			percetagemTotalAliquota = percetagemTotalAliquota.add(impostoTipoAliquota.getPercentualAliquota());

			impostoDeduzidoTO = new ImpostoDeduzidoTO();

			if (iteratorImpostoTipo.hasNext()) {
				valorImpostoDeduzido = calculaValorImpostoDeduzido(impostoTipoAliquota, impostoTipoAliquota.getPercentualAliquota());

				atualizaImpostoDeduzidoTO(impostoTipoAliquota);

				valorImpostoDeduzidoFinal = valorImpostoDeduzidoFinal.add(valorImpostoDeduzido);
			} else {
				valorImpostoDeduzidoTotal = calculaValorImpostoDeduzido(impostoTipoAliquota, percetagemTotalAliquota);

				valorImpostoDeduzido = valorImpostoDeduzidoTotal.subtract(valorImpostoDeduzidoFinal);

				valorImpostoDeduzido = valorImpostoDeduzido.setScale(2, BigDecimal.ROUND_DOWN);

				atualizaImpostoDeduzidoTO(impostoTipoAliquota);

				valorImpostoDeduzidoFinal = valorImpostoDeduzidoTotal;
			}

			colecaoHelper.add(impostoDeduzidoTO);
		}

		retorno.setListaImpostosDeduzidos(colecaoHelper);

		valorImpostoDeduzidoFinal = valorImpostoDeduzidoFinal.setScale(2, BigDecimal.ROUND_DOWN);

		retorno.setValorTotalImposto(valorImpostoDeduzidoFinal);
		retorno.setValorBaseCalculo(baseCalculo);

		return retorno;
	}

	private void atualizaImpostoDeduzidoTO(ImpostoTipoAliquota impostoTipoAliquota) {
		impostoDeduzidoTO.setIdImpostoTipo(impostoTipoAliquota.getImpostoTipo().getId());

		impostoDeduzidoTO.setValor(valorImpostoDeduzido);

		impostoDeduzidoTO.setPercentualAliquota(impostoTipoAliquota.getPercentualAliquota());
	}

	private BigDecimal calculaValorImpostoDeduzido(ImpostoTipoAliquota impostoTipoAliquota, BigDecimal percetagemAliquota) {
		BigDecimal percetagem = dividiArredondando(percetagemAliquota, new BigDecimal("100.00"));
		BigDecimal valor = baseCalculo.multiply(percetagem);
		
		return valor.setScale(2, BigDecimal.ROUND_HALF_DOWN);
	}

	private void inicializaValoresCalculo() {
		valorImpostoDeduzido = new BigDecimal("0.00");
		valorImpostoDeduzidoFinal = new BigDecimal("0.00");
		percetagemTotalAliquota = new BigDecimal("0.00");
		valorImpostoDeduzidoTotal = new BigDecimal("0.00");
	}

	private static BigDecimal dividiArredondando(BigDecimal dividendo, BigDecimal divisor) {
		BigDecimal resultado = new BigDecimal("0.00");

		if (dividendo != null) {
			resultado = dividendo.divide(divisor, 7, BigDecimal.ROUND_HALF_UP);
		}

		return resultado;
	}
}