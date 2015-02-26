package br.gov.batch.servicos.micromedicao;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.FaturamentoGrupo;
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
		List<MovimentoRoteiroEmpresa> movimentos = repositorio.criarMovimentoRoteiroEmpresa(imoveisParaProcessamento, rota);
		
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
