package br.gov.batch.servicos.micromedicao;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.cadastro.ImovelBO;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.micromedicao.HidrometroInstalacaoHistoricoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.to.HidrometroTO;

@Stateless
public class HidrometroBO {

	@EJB
	private ImovelBO imovelBO;
	
	@EJB
	private MedicaoHistoricoBO medicaoHistoricoBO;

	@EJB
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorio;

	@EJB
	private HidrometroInstalacaoHistoricoRepositorio hidrometroInstalacaoHistoricoRepositorio;

	@EJB
	private SistemaParametrosRepositorio sistemaParametrosRepositorio;

	public boolean houveSubstituicao(Integer idImovel) {
		Date dataLeituraAnteriorFaturada = this.obterDataLeituraAnterior(idImovel);
		Date dataInstalacaoHidrometro = this.obterDataInstalacaoHidrometro(idImovel);

		if (dataLeituraAnteriorFaturada != null && dataInstalacaoHidrometro != null) {
			if (dataInstalacaoHidrometro.before(new Date()) && !dataLeituraAnteriorFaturada.after(dataInstalacaoHidrometro)) {
				return true;
			}
		}

		return false;
	}

	private Date obterDataLeituraAnterior(Integer idImovel) {
		Date dataLeituraFaturada = null;

		FaturamentoGrupo faturamentoGrupo = imovelBO.pesquisarFaturamentoGrupo(idImovel);

		Integer anoMesReferencia = faturamentoGrupo.getAnoMesReferencia();

		Integer anoMesReferenciaAnterior = Utilitarios.reduzirMeses(anoMesReferencia, 1);

		MedicaoHistorico medicaoHistoricoAtual = medicaoHistoricoRepositorio.buscarPorImovelEReferencia(idImovel, anoMesReferencia);

		if (medicaoHistoricoAtual != null) {
			dataLeituraFaturada = medicaoHistoricoAtual.getDataLeituraAnteriorFaturamento();
		} else {
			dataLeituraFaturada = this.obterDataMedicao(idImovel, anoMesReferenciaAnterior);
		}

		return dataLeituraFaturada;
	}

	private Date obterDataInstalacaoHidrometro(Integer idImovel) {
		List<HidrometroTO> dadosHidrometro = hidrometroInstalacaoHistoricoRepositorio.dadosInstalacaoHidrometro(idImovel);

		Date dataMedicao = null;

		if (dadosHidrometro.size() > 0) {
			dataMedicao = dadosHidrometro.get(0).getDataInstalacao();
		}

		return dataMedicao;
	}

	public Date obterDataMedicao(Integer idImovel, Integer anoMesReferencia) {
		Date dataMedicao = this.obterDataInstalacaoHidrometro(idImovel);

		MedicaoHistorico medicaoHistoricoTO = medicaoHistoricoBO.getMedicaoHistorico(idImovel, anoMesReferencia);

		if (medicaoHistoricoTO != null) {
			dataMedicao = medicaoHistoricoTO.getDataLeituraAtualFaturamento();
		}

		return dataMedicao;
	}
}