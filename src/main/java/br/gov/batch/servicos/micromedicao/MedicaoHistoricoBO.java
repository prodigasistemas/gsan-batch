package br.gov.batch.servicos.micromedicao;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.logging.Logger;

import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.micromedicao.HidrometroInstalacaoHistoricoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.to.HidrometroMedicaoHistoricoTO;
import br.gov.servicos.to.HidrometroTO;

@Stateless
public class MedicaoHistoricoBO {
    private static Logger logger = Logger.getLogger(MedicaoHistoricoBO.class);

	@EJB
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorio;

	@EJB
	private HidrometroInstalacaoHistoricoRepositorio hidrometroInstalacaoHistoricoRepositorio;

	//TODO: Pode adicionar mais de um hidrometro do mesmo tipo de ligacao
	//TODO: Refactoring
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<HidrometroMedicaoHistoricoTO> obterDadosTiposMedicao(Integer idImovel, Integer anoMesReferencia) {

		List<HidrometroMedicaoHistoricoTO> listaHidrometroMedicaoHistorico = new ArrayList<>();

		logger.info("LINHA 08 - ANTES  obterDadosTiposMedicao: dadosInstalacaoHidrometro");

		HidrometroTO hidrometroTO = hidrometroInstalacaoHistoricoRepositorio.dadosInstalacaoHidrometroAgua(idImovel);
		
		if (hidrometroTO != null){
		    HidrometroMedicaoHistoricoTO hidrometroMedicaoHistorico = new HidrometroMedicaoHistoricoTO(hidrometroTO);
		    hidrometroMedicaoHistorico.setMedicaoHistorico(this.getMedicaoHistorico(idImovel, anoMesReferencia));
		    listaHidrometroMedicaoHistorico.add(hidrometroMedicaoHistorico);
		}

		hidrometroTO = hidrometroInstalacaoHistoricoRepositorio.dadosInstalacaoHidrometroPoco(idImovel);
		if (hidrometroTO != null){
		    HidrometroMedicaoHistoricoTO hidrometroMedicaoHistorico = new HidrometroMedicaoHistoricoTO(hidrometroTO);
		    hidrometroMedicaoHistorico.setMedicaoHistorico(this.getMedicaoHistorico(idImovel, anoMesReferencia));
		    listaHidrometroMedicaoHistorico.add(hidrometroMedicaoHistorico);
		}
		
		logger.info("LINHA 08 - DEPOIS obterDadosTiposMedicao: dadosInstalacaoHidrometro");

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
