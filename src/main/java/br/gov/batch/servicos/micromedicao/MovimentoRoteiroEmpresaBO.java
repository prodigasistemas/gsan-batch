package br.gov.batch.servicos.micromedicao;

import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.LeituraTipo;
import br.gov.model.micromedicao.MovimentoRoteiroEmpresa;
import br.gov.model.micromedicao.Rota;
import br.gov.servicos.cadastro.ClienteEnderecoRepositorio;
import br.gov.servicos.cadastro.ClienteRepositorio;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;
import br.gov.servicos.micromedicao.HidrometroInstalacaoHistoricoRepositorio;
import br.gov.servicos.micromedicao.MovimentoRoteiroEmpresaRepositorio;

@Stateless
public class MovimentoRoteiroEmpresaBO {

	@EJB
	private MovimentoRoteiroEmpresaRepositorio repositorio;
	
	@EJB
	private ImovelRepositorio imovelRepositorio;
	
	@EJB
	private ClienteRepositorio clienteRepositorio;
	
	@EJB
	private ClienteEnderecoRepositorio clienteEnderecoRepositorio;
	
	@EJB
	private HidrometroInstalacaoHistoricoRepositorio hidrometroInstalacaoRepositorio;
	
	@EJB
	private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;
	
	@EJB
	private ConsumoHistoricoRepositorio consumoHistoricoRepositorio;
	
	
	public List<MovimentoRoteiroEmpresa> gerarMovimentoRoteiroEmpresa(List<Imovel> imoveis, Rota rota) {
		repositorio.deletarPorRota(rota);
		
		List<Imovel> imoveisParaProcessamento = verificarImoveisProcessados(imoveis, rota.getFaturamentoGrupo());
		
		List<MovimentoRoteiroEmpresa> movimentos = criarMovimentoRoteiroEmpresa(imoveisParaProcessamento, rota);
		
		return movimentos;
	}
	
	public void criarMovimentoRoteiroEmpresa(List<Imovel> imoveis, Integer anoMesCorrente, LeituraTipo tipoLeitura) {
	    for (Imovel imovel : imoveis) {
	        MovimentoRoteiroEmpresa movimento = new MovimentoRoteiroEmpresa();
	        
	        movimento.setLeituraTipo(tipoLeitura.getId());
	        movimento.setAnoMesMovimento(anoMesCorrente);
	        movimento.setImovel(imovel);
            movimento.setImovelPerfil(imovel.getImovelPerfil());
            
            if (imovel.getQuadraFace() != null) {
                movimento.setCodigoQuadraFace(imovel.getQuadraFace().getNumeroQuadraFace());
            }

            movimento.setLocalidade(imovel.getLocalidade());
            movimento.setNomeLocalidade(imovel.getLocalidade().getDescricao());
            movimento.setLoteImovel(completaComZerosEsquerda(4, imovel.getLote()));
            movimento.setSubloteImovel(completaComZerosEsquerda(3, imovel.getSubLote()));
            movimento.setImovelPerfil(imovel.getImovelPerfil());
            
            
            if(imovel.existeHidrometroAgua()){
                movimento.setMedicaoTipo(imovel.getLigacaoAgua().getHidrometroInstalacoesHistorico().iterator().next().getMedicaoTipo());
            }else if (imovel.existeHidrometroPoco()){
                movimento.setMedicaoTipo(imovel.getHidrometroInstalacaoHistorico().getMedicaoTipo());
            }
            
            if (imovel.pertenceARotaAlternativa()) {
                movimento.setCodigoSetorComercial(imovel.getRotaAlternativa().getSetorComercial().getCodigo());
            } else {
                movimento.setCodigoSetorComercial(imovel.getSetorComercial().getCodigo());
            }
            
//            movimento.setRota(rota);
	        
        }
	}
	
    public List<MovimentoRoteiroEmpresa> criarMovimentoRoteiroEmpresa(List<Imovel> imoveis, Rota rota) {

        List<MovimentoRoteiroEmpresa> movimentos = new ArrayList<MovimentoRoteiroEmpresa>();
        
        for (Imovel imovel : imoveis) {
            MovimentoRoteiroEmpresa movimento = new MovimentoRoteiroEmpresa();
            
            movimento.setAnoMesMovimento(rota.getFaturamentoGrupo().getAnoMesReferencia());
            movimento.setImovel(imovel);
            movimento.setFaturamentoGrupo(rota.getFaturamentoGrupo());
            movimento.setLocalidade(imovel.getLocalidade());
            movimento.setGerenciaRegional(imovel.getLocalidade().getGerenciaRegional());
            movimento.setLigacaoAguaSituacao(imovel.getLigacaoAguaSituacao());
            movimento.setLigacaoEsgotoSituacao(imovel.getLigacaoEsgotoSituacao());
            movimento.setRota(rota);
            movimento.setEmpresa(rota.getEmpresa());
            movimento.setCodigoSetorComercial(rota.getSetorComercial().getCodigo());
            movimento.setNumeroQuadra(imovel.getQuadra().getNumeroQuadra());
            movimento.setLoteImovel(imovel.getLote() != null ? imovel.getLote().toString() : "");
            movimento.setSubloteImovel(imovel.getSubLote() != null ? imovel.getSubLote().toString() : "");
            movimento.setImovelPerfil(imovel.getImovelPerfil());
            movimento.setUltimaAlteracao(new Date());
            movimento.setLeituraTipo(LeituraTipo.LEITURA_E_ENTRADA_SIMULTANEA.getId());
            
            repositorio.salvar(movimento);
            
            movimentos.add(movimento);
        }
        
        return movimentos;
    }

	private List<Imovel> verificarImoveisProcessados(List<Imovel> imoveis, FaturamentoGrupo faturamentoGrupo) {
		List<Imovel> imoveisOutroGrupo = repositorio.pesquisarImoveisGeradosParaOutroGrupo(imoveis, faturamentoGrupo);
		
		if(!imoveisOutroGrupo.isEmpty()) {
			imoveis.removeAll(imoveisOutroGrupo);
		}
		
		return imoveis;
	}
	
}
