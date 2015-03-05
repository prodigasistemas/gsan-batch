package br.gov.batch.servicos.micromedicao;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.batch.servicos.micromedicao.to.DadosLeituraTO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.micromedicao.Rota;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.micromedicao.MovimentoRoteiroEmpresaRepositorio;
import br.gov.servicos.micromedicao.RotaRepositorio;

@Stateless
public class DadosLeituraBO {
	
	@EJB
	private MovimentoRoteiroEmpresaRepositorio movimentoRoteiroEmpresaRepositorio;
	
	@EJB
	private RotaRepositorio rotaRepositorio;
	
	@EJB
	private ImovelRepositorio imovelRepositorio;
	
	@EJB
	private MovimentoRoteiroEmpresaBO movimentoRoteiroEmpresaBO;
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void gerarDadosParaLeitura(DadosLeituraTO to) {
		Rota rota = rotaRepositorio.obterPorID(to.getIdRota());
		
		movimentoRoteiroEmpresaRepositorio.deletarPorReferenciaERota(to.getAnoMesFaturamento(), rota);
		
		if (!movimentoRoteiroEmpresaRepositorio.existeMovimentoParaGrupoDiferenteDoImovel(to.getIdImovel(), to.getIdGrupo(), to.getAnoMesFaturamento())) {
			Imovel imovel = imovelRepositorio.obterPorID(to.getIdImovel());
			movimentoRoteiroEmpresaBO.criarMovimentoParaLeitura(imovel, to.getAnoMesFaturamento(), rota.getLeituraTipo());
		}
	}
}
