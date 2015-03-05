package br.gov.batch.servicos.micromedicao;

import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.cadastro.ImovelSubcategoriaBO;
import br.gov.batch.servicos.faturamento.AguaEsgotoBO;
import br.gov.batch.servicos.faturamento.to.VolumeMedioAguaEsgotoTO;
import br.gov.model.cadastro.Cliente;
import br.gov.model.cadastro.ClienteRelacaoTipo;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.Hidrometro;
import br.gov.model.micromedicao.HidrometroInstalacaoHistorico;
import br.gov.model.micromedicao.LeituraTipo;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.model.micromedicao.MedicaoTipo;
import br.gov.model.micromedicao.MovimentoRoteiroEmpresa;
import br.gov.model.micromedicao.Rota;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ClienteEnderecoRepositorio;
import br.gov.servicos.cadastro.ClienteRepositorio;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.micromedicao.ConsumoHistoricoRepositorio;
import br.gov.servicos.micromedicao.HidrometroInstalacaoHistoricoRepositorio;
import br.gov.servicos.micromedicao.MovimentoRoteiroEmpresaRepositorio;
import br.gov.servicos.micromedicao.to.FaixaLeituraTO;

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

	@EJB
	private MedicaoHistoricoBO medicaoHistoricoBO;

	@EJB
	private FaixaLeituraBO faixaLeituraBO;

	@EJB
	private AguaEsgotoBO aguaEsgotoBO;

	@EJB
	private ImovelSubcategoriaBO imovelSubcategoriaBO;

	public List<MovimentoRoteiroEmpresa> gerarMovimento(List<Imovel> imoveis, Rota rota) {
		repositorio.deletarPorRota(rota);

		List<Imovel> imoveisParaProcessamento = verificarImoveisProcessados(imoveis, rota.getFaturamentoGrupo());

		return criarMovimentoParaImpressaoSimultanea(imoveisParaProcessamento, rota);
	}

	public void criarMovimentoParaLeitura(Imovel imovel, Integer anoMesCorrente, Integer tipoLeitura) {
		Cliente usuario = imovel.getCliente(ClienteRelacaoTipo.USUARIO);
		Collection<ICategoria> categoria = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasPorImovel(imovel.getId());
		MedicaoHistorico medicao = medicaoHistoricoBO.getMedicaoHistorico(imovel.getId(), Utilitarios.reduzirMeses(anoMesCorrente, 1));

		VolumeMedioAguaEsgotoTO volumeMedioAguaEsgotoTO = aguaEsgotoBO.obterVolumeMedioAguaEsgoto(imovel.getId(), anoMesCorrente,
				MedicaoTipo.LIGACAO_AGUA.getId());
		Hidrometro hidrometro = hidrometroInstalacaoRepositorio.dadosHidrometroInstaladoAgua(imovel.getId());
		FaixaLeituraTO faixaLeitura = faixaLeituraBO.obterDadosFaixaLeitura(imovel, hidrometro, volumeMedioAguaEsgotoTO.getConsumoMedio(), medicao);

		MovimentoRoteiroEmpresa movimento = buildMovimento(imovel);

		movimento.setLeituraTipo(tipoLeitura);
		movimento.setAnoMesMovimento(anoMesCorrente);
		movimento.setDescricaoAbreviadaCategoriaImovel(categoria.iterator().next().getCategoriaDescricaoAbreviada());
		if (medicao != null){
			movimento.setNumeroLeituraAnterior(medicao.getLeituraAtualFaturamento());
			movimento.setCodigoAnormalidadeAnterior(medicao.getLeituraAnormalidadeInformada() != null ? medicao.getLeituraAnormalidadeInformada().getId() : null);
		}
		movimento.setNumeroFaixaLeituraEsperadaInicial(faixaLeitura.getFaixaSuperior());
		movimento.setNumeroFaixaLeituraEsperadaFinal(faixaLeitura.getFaixaInferior());
		movimento.setNumeroConsumoMedio(null);
		movimento.setAnoMesMovimento(anoMesCorrente);
		movimento.setNomeCliente(usuario != null ? usuario.getNome() : null);
		
		repositorio.salvar(movimento);
	}

	private MovimentoRoteiroEmpresa buildMovimento(Imovel imovel) {
		MovimentoRoteiroEmpresa movimento = new MovimentoRoteiroEmpresa();

		movimento.setNumeroMoradores(imovel.getNumeroMorador() != null ? imovel.getNumeroMorador().intValue() : null);
		movimento.setNumeroQuadra(imovel.getQuadra().getNumeroQuadra());
		movimento.setFaturamentoGrupo(imovel.getQuadra().getRota().getFaturamentoGrupo());
		movimento.setCodigoQuadraFace(imovel.getQuadraFace().getNumeroQuadraFace());
		movimento.setEmpresa(imovel.getQuadra().getRota().getEmpresa());
		movimento.setImovel(imovel);
		movimento.setImovelPerfil(imovel.getImovelPerfil());
		movimento.setLocalidade(imovel.getLocalidade());
		movimento.setNomeLocalidade(imovel.getLocalidade().getDescricao());
		movimento.setGerenciaRegional(imovel.getLocalidade().getGerenciaRegional());

		movimento.setLoteImovel(completaComZerosEsquerda(4, imovel.getLote()));
		movimento.setSubloteImovel(completaComZerosEsquerda(3, imovel.getSubLote()));

		movimento.setImovelPerfil(imovel.getImovelPerfil());

		movimento.setLigacaoAguaSituacao(imovel.getLigacaoAguaSituacao());
		movimento.setLigacaoEsgotoSituacao(imovel.getLigacaoEsgotoSituacao());

		movimento.setComplementoEndereco(imovel.getComplementoEndereco());
		movimento.setLogradouro(imovel.getLogradouroCep().getLogradouro());
		movimento.setNomeBairro(imovel.getLogradouroBairro().getBairro().getNome());

		movimento.setUltimaAlteracao(new Date());

		movimento.setCodigoQuadraFace(imovel.getQuadraFace() != null ? imovel.getQuadraFace().getNumeroQuadraFace() : null);

		if (imovel.existeHidrometroAgua()) {
			HidrometroInstalacaoHistorico instalacao = imovel.getLigacaoAgua().getHidrometroInstalacoesHistorico().iterator().next();
			movimento.setMedicaoTipo(instalacao.getMedicaoTipo());
			movimento.setNumeroHidrometro(instalacao.getHidrometro().getNumero());
		} else if (imovel.existeHidrometroPoco()) {
			movimento.setMedicaoTipo(imovel.getHidrometroInstalacaoHistorico().getMedicaoTipo());
			movimento.setNumeroHidrometro(imovel.getHidrometroInstalacaoHistorico().getHidrometro().getNumero());
		}

		if (imovel.pertenceARotaAlternativa()) {
			movimento.setCodigoSetorComercial(imovel.getRotaAlternativa().getSetorComercial().getCodigo());
			movimento.setRota(imovel.getRotaAlternativa());
		} else {
			movimento.setCodigoSetorComercial(imovel.getSetorComercial().getCodigo());
			movimento.setRota(imovel.getQuadra().getRota());
		}

		return movimento;
	}

	public List<MovimentoRoteiroEmpresa> criarMovimentoParaImpressaoSimultanea(List<Imovel> imoveis, Rota rota) {

		List<MovimentoRoteiroEmpresa> movimentos = new ArrayList<MovimentoRoteiroEmpresa>();

		for (Imovel imovel : imoveis) {
			MovimentoRoteiroEmpresa movimento = buildMovimento(imovel);

			movimento.setAnoMesMovimento(rota.getFaturamentoGrupo().getAnoMesReferencia());
			movimento.setFaturamentoGrupo(rota.getFaturamentoGrupo());
			movimento.setRota(rota);
			movimento.setEmpresa(rota.getEmpresa());
			movimento.setCodigoSetorComercial(rota.getSetorComercial().getCodigo());
			movimento.setLeituraTipo(LeituraTipo.LEITURA_E_ENTRADA_SIMULTANEA.getId());

			repositorio.salvar(movimento);

			movimentos.add(movimento);
		}

		return movimentos;
	}

	private List<Imovel> verificarImoveisProcessados(List<Imovel> imoveis, FaturamentoGrupo faturamentoGrupo) {
		List<Imovel> imoveisOutroGrupo = repositorio.pesquisarImoveisGeradosParaOutroGrupo(imoveis, faturamentoGrupo);

		if (!imoveisOutroGrupo.isEmpty()) {
			imoveis.removeAll(imoveisOutroGrupo);
		}

		return imoveis;
	}
}
