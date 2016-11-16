package br.gov.batch.servicos.micromedicao;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.ContaRepositorio;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;

@Stateless
public class ConsumoHistoricoBO {

	@EJB
	private SistemaParametrosRepositorio sistemaParametrosRepositorio;

	@EJB
	private ConsumoHistoricoRepositorio consumoHistoricoRepositorio;

	@EJB
	private ConsumoBO consumoBO;
	
	@EJB
	private ContaRepositorio contaRepositorio;

	SistemaParametros sistemaParametro;

	@PostConstruct
	public void init() {
		sistemaParametro = sistemaParametrosRepositorio.getSistemaParametros();
	}

	// FIXME: Método não está sendo utilizado
	public Integer obterVolumeMedioAguaEsgoto(Integer idImovel, Integer anoMesReferencia, LigacaoTipo ligacaoTipo) {

		Integer referenciaFinal = Utilitarios.reduzirMeses(anoMesReferencia, 1);
		Integer referenciaInicial = Utilitarios.reduzirMeses(referenciaFinal, sistemaParametro.getMesesMediaConsumo());

		int numeroMesesMaximoCalculoMedia = sistemaParametro.getNumeroMesesMaximoCalculoMedia();
		int referenciaInicialMaximo = Utilitarios.reduzirMeses(referenciaInicial, numeroMesesMaximoCalculoMedia);

		List<ConsumoHistorico> medias = consumoHistoricoRepositorio.obterConsumoMedio(idImovel, referenciaInicialMaximo, referenciaFinal, ligacaoTipo.getId());
		Integer mediaConsumo = 0;
		if (medias.isEmpty()) {
			mediaConsumo = consumoBO.consumoMinimoLigacao(idImovel);
		} else {
			mediaConsumo = calcularMediaConsumo(medias, anoMesReferencia);
		}

		return mediaConsumo;
	}

	public Integer calcularMediaConsumo(List<ConsumoHistorico> consumos, Integer anoMesReferencia) {
		Collections.sort(consumos);

		int mesesParaMedia = sistemaParametro.getMesesMediaConsumo();
		int maximoMesesCalculoMedia = sistemaParametro.getNumeroMesesMaximoCalculoMedia();

		int mesesCortados = 0;
		int somaConsumo = 0;
		int mesesConsumo = 0;
		
		for (int item = 0; item < consumos.size();) {
			ConsumoHistorico consumo = consumos.get(item);

			if (mesesConsumo == mesesParaMedia || mesesCortados == maximoMesesCalculoMedia) {
				break;
			}

			if (consumo.getReferenciaFaturamento().intValue() == anoMesReferencia.intValue()) {
				somaConsumo += consumo.getNumeroConsumoCalculoMedia();
				mesesConsumo++;
				item++;
			} else {
				mesesCortados++;
			}
			anoMesReferencia = Utilitarios.reduzirMeses(anoMesReferencia, 1);
		}

		return mesesConsumo > 0 ? somaConsumo / mesesConsumo : 0;
	}

	public ConsumoHistorico getConsumoHistoricoPorReferencia(Imovel imovel, Integer referencia) {
		return consumoHistoricoRepositorio.buscarConsumoHistoricoPeloImoveEReferencia(imovel.getId(), referencia);
	}
}
