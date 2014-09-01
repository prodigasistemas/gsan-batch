package br.gov.batch.servicos.micromedicao;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.cadastro.ImovelBO;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.micromedicao.HidrometroInstalacaoHistoricoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.to.HidrometroTO;

@Stateless
public class HidrometroBO {
	
	@EJB
	private ImovelBO imovelBO;

	@EJB
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorio;
	
	@EJB
	private HidrometroInstalacaoHistoricoRepositorio hidrometroInstalacaoHistoricoRepositorio;
	
	public boolean houveSubstituicao(Integer idImovel){
		return false;
	}

	public boolean verificarInstalacaoSubstituicaoHidrometro(){ 
		
//		Date dataLeituraAnteriorFaturada = this.obterDataLeituraAnterior(idImovel, medicaoTipo);
//		Date dataInstalacaoHidrometro = this.obterDataInstalacaoHidrometro(idImovel);
//		
//		if ( (dataLeituraAnteriorFaturada != null && dataInstalacaoHidrometro != null)
//				&& (
//					((Util.compararData(dataInstalacaoHidrometro, new Date()) < 0) && (Util.compararData(dataInstalacaoHidrometro, dataLeituraAnteriorFaturada) > 0))
//					|| (Util.compararData(dataInstalacaoHidrometro, dataLeituraAnteriorFaturada) == 0)
//				    )) {
//			
//			return true;
//		} 
		
		return false;
	}

	private Date obterDataLeituraAnterior(Integer idImovel) {
	
	Date dataLeituraFaturada = null;
	
	FaturamentoGrupo faturamentoGrupo = imovelBO.pesquisarFaturamentoGrupo(idImovel);
	
	Integer anoMesReferencia = faturamentoGrupo.getAnoMesReferencia();
	
	Integer anoMesReferenciaAnterior = Utilitarios.reduzirMeses(anoMesReferencia, 1);
	
	MedicaoHistorico medicaoHistoricoAtual =  medicaoHistoricoRepositorio.buscarPorImovelEReferencia(idImovel, anoMesReferencia);

	if (medicaoHistoricoAtual != null) {
		dataLeituraFaturada = medicaoHistoricoAtual.getDataLeituraAnteriorFaturamento();
	}else{
		dataLeituraFaturada = this.obterDataMedicao(idImovel, anoMesReferenciaAnterior);
	}

	return dataLeituraFaturada;

}
	
	public Date obterDataMedicao(Integer idImovel, Integer anoMesReferencia){
		List<HidrometroTO> dadosHidrometro = hidrometroInstalacaoHistoricoRepositorio.dadosInstalacaoHidrometro(idImovel);
		
		Date dataMedicao = null;
		
		if (dadosHidrometro.size() > 0){
			dataMedicao = dadosHidrometro.get(0).getDataInstalacao();
		}
		
		MedicaoHistorico medicaoHistoricoTO = medicaoHistoricoRepositorio.buscarPorLigacaoAguaOuPoco(idImovel, anoMesReferencia);
		
		if (medicaoHistoricoTO == null){
			Integer mesAnterior = Utilitarios.reduzirMeses(anoMesReferencia, 1);
			medicaoHistoricoTO = medicaoHistoricoRepositorio.buscarPorLigacaoAguaOuPoco(idImovel, mesAnterior);
		}
		
		if (medicaoHistoricoTO != null){
			dataMedicao = medicaoHistoricoTO.getDataLeituraAtualFaturamento();
		}
		
		return dataMedicao;
	}
}
