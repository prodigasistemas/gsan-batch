package br.gov.batch.servicos.micromedicao;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.Imovel;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.micromedicao.RotaRepositorio;

@Stateless
public class RotaBO {

	@EJB
	private RotaRepositorio rotaRepositorio;

	@EJB
	private ImovelRepositorio imovelRepositorio;

	public long totalImoveisParaPreFaturamento(int idRota) {
		if (rotaRepositorio.isRotaAlternativa(idRota)) {
			return imovelRepositorio.totalImoveisParaPreFaturamentoComRotaAlternativa(idRota);
		} else {
			return imovelRepositorio.totalImoveisParaPreFaturamentoSemRotaAlternativa(idRota);
		}
	}

	public long totalImoveisParaLeitura(int idRota) {
		if (rotaRepositorio.isRotaAlternativa(idRota)) {
			return imovelRepositorio.totalImoveisParaLeituraComRotaAlternativa(idRota);
		} else {
			return imovelRepositorio.totalImoveisParaLeituraSemRotaAlternativa(idRota);
		}
	}
	
	public List<Imovel> imoveisParaPreFaturamento(Integer idRota, int firstItem, int numItems) {
		if (rotaRepositorio.isRotaAlternativa(idRota)) {
			return imovelRepositorio.imoveisParaPreFaturamentoComRotaAlternativa(idRota, firstItem, numItems);
		} else {
			return imovelRepositorio.imoveisParaPreFaturamentoSemRotaAlternativa(idRota, firstItem, numItems);
		}
	}

	public List<Imovel> imoveisParaLeitura(int idRota, int firstItem, int numItems) {
		List<Imovel> imoveisConsulta = null;

		if (rotaRepositorio.isRotaAlternativa(idRota)) {
			imoveisConsulta = imovelRepositorio.imoveisParaLeituraComRotaAlternativa(idRota, firstItem, numItems);
		} else {
			imoveisConsulta = imovelRepositorio.imoveisParaLeituraSemRotaAlternativa(idRota, firstItem, numItems);
		}

		List<Imovel> imoveis = new ArrayList<Imovel>();

		for (Imovel imovel : imoveisConsulta) {
			if (imovel.getId().equals(new Integer("879100"))) {
				System.out.println("Achei o imóvel faltando...");
			}

			if (!imovel.paralisarEmissaoContas()) {
				if ((imovel.faturamentoAguaAtivo() && imovel.existeHidrometroAgua())
						|| (imovel.faturamentoEsgotoAtivo() && imovel.existeHidrometroPoco())
						|| (imovel.aguaSuprimida() && imovel.fiscalizarSuprimido())
						|| (imovel.aguaCortada() && !imovel.existeHidrometroAgua() && imovel.fiscalizarCortado())) {

					imoveis.add(imovel);
				}
			}
		}

		return imoveis;
	}
}
