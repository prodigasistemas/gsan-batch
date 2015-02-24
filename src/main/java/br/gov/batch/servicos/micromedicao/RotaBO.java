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

	public List<Imovel> imoveisParaPreFaturamento(Integer idRota, int firstItem, int numItems) {
		if (rotaRepositorio.isRotaAlternativa(idRota)) {
			return imovelRepositorio.imoveisParaPreFaturamentoComRotaAlternativa(idRota, firstItem, numItems);
		} else {
			return imovelRepositorio.imoveisParaPreFaturamentoSemRotaAlternativa(idRota, firstItem, numItems);
		}
	}

	public List<Imovel> imoveisParaArquivoConvencional(int idRota) {
		List<Imovel> imoveisConsulta = null;

		if (rotaRepositorio.isRotaAlternativa(idRota)) {
			imoveisConsulta = imovelRepositorio.imoveisParaArquivoConvencionalComRotaAlternativa(idRota);
		} else {
			imoveisConsulta = imovelRepositorio.imoveisParaArquivoConvencionalSemRotaAlternativa(idRota);
		}

		List<Imovel> imoveis = new ArrayList<Imovel>();

		for (Imovel imovel : imoveisConsulta) {

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
