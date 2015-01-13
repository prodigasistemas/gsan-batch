package br.gov.batch.servicos.micromedicao;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.micromedicao.MedicaoHistorico;
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
		
		List<HidrometroTO> dadosHidrometroInstalacao = hidrometroInstalacaoHistoricoRepositorio.dadosInstalacaoHidrometro(idImovel);
		
		for (HidrometroTO hidrometroTO : dadosHidrometroInstalacao) {
			HidrometroMedicaoHistoricoTO to = new HidrometroMedicaoHistoricoTO(hidrometroTO);
			
			MedicaoHistorico medicaoHistorico = medicaoHistoricoRepositorio.buscarPorLigacaoAguaOuPoco(idImovel, anoMesReferencia);
			if (medicaoHistorico == null) {
//				Integer anoMesReferenciaMenos2 = Utilitarios.subtrairMesDoAnoMes(anoMesReferencia, 1);
//				medicaoHistorico = medicaoHistoricoRepositorio.buscarPorLigacaoAguaOuPoco(idImovel, anoMesReferenciaMenos2);
			}
		}
		
		return null;
	}
}
