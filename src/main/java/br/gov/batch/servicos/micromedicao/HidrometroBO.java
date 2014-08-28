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
import br.gov.servicos.to.MedicaoHistoricoTO;

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
	
	String filtroPoTipoMedicao = "";
	
	MedicaoHistorico medicaoHistoricoAtual =  medicaoHistoricoRepositorio.obterPorImovelEReferencia(idImovel, anoMesReferencia);

	if (medicaoHistoricoAtual != null) {
		dataLeituraFaturada = medicaoHistoricoAtual.getDataLeituraAnteriorFaturamento();
	} 
//	else {
//		Imovel imovel = new Imovel();
//		imovel.setId(idImovel);
//		
//		Collection colecaoDadosMedicaoHistorico = this.obterDadosTiposMedicao(imovel, anoMesReferenciaAnterior);
//		
//		if (colecaoDadosMedicaoHistorico != null && !colecaoDadosMedicaoHistorico.isEmpty()){
//			
//			Iterator iterator = colecaoDadosMedicaoHistorico.iterator();
//				
//			while(iterator.hasNext()){
//					
//				Object[] arrayMedicaoHistorico = (Object[]) iterator.next();
//				
//				if (arrayMedicaoHistorico[4] != null) {
//					dataLeituraFaturada = (Date) arrayMedicaoHistorico[4];
//				}
//					
//			}
//		}
//	}

	return dataLeituraFaturada;

}
	
	public Date obterDataMedicao(Integer idImovel, Integer anoMesReferencia){
		List<HidrometroTO> dadosHidrometro = hidrometroInstalacaoHistoricoRepositorio.dadosInstalacaoHidrometro(idImovel);
		
		Date dataMedicao = null;
		
		if (dadosHidrometro.size() > 0){
			dataMedicao = dadosHidrometro.get(0).getDataInstalacao();
		}
		
		MedicaoHistoricoTO medicaoHistoricoTO = medicaoHistoricoRepositorio.obterDadosMedicao(idImovel, anoMesReferencia);
		
		if (medicaoHistoricoTO == null){
			Integer mesAnterior = Utilitarios.reduzirMeses(anoMesReferencia, 1);
			medicaoHistoricoTO = medicaoHistoricoRepositorio.obterDadosMedicao(idImovel, mesAnterior);
		}
		
		if (medicaoHistoricoTO != null){
			dataMedicao = medicaoHistoricoTO.getDataLeituraAtualFaturamento();
		}
		
		return dataMedicao;
	}
}
