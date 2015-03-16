package br.gov.batch.servicos.micromedicao;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

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

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<HidrometroMedicaoHistoricoTO> obterDadosTiposMedicao(Integer idImovel, Integer anoMesReferencia) {

		List<HidrometroMedicaoHistoricoTO> listaHidrometroMedicaoHistorico = new ArrayList<>();

		HidrometroTO hidrometroTO = hidrometroInstalacaoHistoricoRepositorio.dadosInstalacaoHidrometroAgua(idImovel);
		
		if (hidrometroTO != null){
		    listaHidrometroMedicaoHistorico.add(criaMedicaoHistorico(idImovel, anoMesReferencia, hidrometroTO));
		}

		hidrometroTO = hidrometroInstalacaoHistoricoRepositorio.dadosInstalacaoHidrometroPoco(idImovel);
		if (hidrometroTO != null){
		    listaHidrometroMedicaoHistorico.add(criaMedicaoHistorico(idImovel, anoMesReferencia, hidrometroTO));
		}
		
		return listaHidrometroMedicaoHistorico;
	}

    private HidrometroMedicaoHistoricoTO criaMedicaoHistorico(Integer idImovel, Integer anoMesReferencia, HidrometroTO hidrometroTO) {
        HidrometroMedicaoHistoricoTO hidrometroMedicaoHistorico = new HidrometroMedicaoHistoricoTO(hidrometroTO);
        hidrometroMedicaoHistorico.setMedicaoHistorico(this.getMedicaoHistorico(idImovel, anoMesReferencia));
        return hidrometroMedicaoHistorico;
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
