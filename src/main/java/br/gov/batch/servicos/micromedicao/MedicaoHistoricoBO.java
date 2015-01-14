package br.gov.batch.servicos.micromedicao;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.micromedicao.HidrometroInstalacaoHistoricoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.to.HidrometroMedicaoHistoricoTO;
import br.gov.servicos.to.HidrometroTO;

@Stateless
public class MedicaoHistoricoBO {
	
	@EJB
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorio;
	
	@EJB
	private HidrometroInstalacaoHistoricoRepositorio hidrometroInstalacaoHistoricoRepositorio;

	public List<HidrometroMedicaoHistoricoTO> obterDadosTiposMedicao(Integer idImovel, Integer anoMesReferencia) {
		
		List<HidrometroMedicaoHistoricoTO> listaHidrometroMedicaoHistorico = new ArrayList<>();
		
		List<HidrometroTO> dadosHidrometroInstalacao = hidrometroInstalacaoHistoricoRepositorio.dadosInstalacaoHidrometro(idImovel);
		
		for (HidrometroTO hidrometroTO : dadosHidrometroInstalacao) {
			HidrometroMedicaoHistoricoTO hidrometroMedicaoHistorico = new HidrometroMedicaoHistoricoTO(hidrometroTO);
			
			MedicaoHistorico medicaoHistorico = getMedicaoHistorico(idImovel, anoMesReferencia);
			
			hidrometroMedicaoHistorico.setNumeroLeituraInstalacao(medicaoHistorico.getLeituraAtualFaturamento());
			hidrometroMedicaoHistorico.setLeituraAtualFaturamento(medicaoHistorico.getLeituraAtualFaturamento());
			hidrometroMedicaoHistorico.setDataLeituraAtualFaturamento(medicaoHistorico.getDataLeituraAtualFaturamento());
			hidrometroMedicaoHistorico.setLeituraSituacaoAtual(medicaoHistorico.getLeituraSituacaoAtual());
			hidrometroMedicaoHistorico.setConsumoMedioHidrometro(medicaoHistorico.getConsumoMedioHidrometro());
			hidrometroMedicaoHistorico.setDataLeituraAtualInformada(medicaoHistorico.getDataLeituraAtualInformada());
			hidrometroMedicaoHistorico.setLeituraAtualInformada(medicaoHistorico.getLeituraAtualInformada());
			
			listaHidrometroMedicaoHistorico.add(hidrometroMedicaoHistorico);
		}
		
		return listaHidrometroMedicaoHistorico;
	}

	public MedicaoHistorico getMedicaoHistorico(Integer idImovel, Integer anoMesReferencia) {
		MedicaoHistorico medicaoHistorico = medicaoHistoricoRepositorio.buscarPorLigacaoAguaOuPoco(idImovel, anoMesReferencia);
		if (medicaoHistorico == null) {
			Integer anoMesReferenciaAnterior = Utilitarios.reduzirMeses(anoMesReferencia, 1);
			medicaoHistorico = medicaoHistoricoRepositorio.buscarPorLigacaoAguaOuPoco(idImovel, anoMesReferenciaAnterior);
		}
		return medicaoHistorico;
	}
}
