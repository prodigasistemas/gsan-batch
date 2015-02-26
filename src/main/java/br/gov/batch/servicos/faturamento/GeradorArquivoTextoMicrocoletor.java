package br.gov.batch.servicos.faturamento;

import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.completaTexto;
import static br.gov.model.util.Utilitarios.formatarAnoMesParaMesAno;

import java.util.Calendar;
import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.model.cadastro.Categoria;
import br.gov.model.cadastro.Logradouro;
import br.gov.model.micromedicao.ArquivoTextoRoteiroEmpresa;
import br.gov.model.micromedicao.MedicaoTipo;
import br.gov.model.micromedicao.MovimentoRoteiroEmpresa;
import br.gov.model.micromedicao.ServicoTipoCelular;
import br.gov.model.micromedicao.SituacaoTransmissaoLeitura;
import br.gov.model.util.IOUtil;
import br.gov.servicos.cadastro.QuadraRepositorio;
import br.gov.servicos.micromedicao.ArquivoTextoRoteiroEmpresaRepositorio;
import br.gov.servicos.micromedicao.MovimentoRoteiroEmpresaRepositorio;

@Stateless
public class GeradorArquivoTextoMicrocoletor {

	@EJB
	private MovimentoRoteiroEmpresaRepositorio movimentoRepositorio;

	@EJB
	private ArquivoTextoRoteiroEmpresaRepositorio roteiroRepositorio;

	@EJB
	private QuadraRepositorio quadraRepositorio;

	public GeradorArquivoTextoMicrocoletor() {
		super();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	// public void gerar(List<Rota> rotas, int referencia, int idGrupo) {
	//
	// StringBuilder arquivoTxt = new StringBuilder();
	//
	// MovimentoRoteiroEmpresa movimento = null;
	//
	// int quantidadeImoveis = 0;
	//
	// // PAGINAÇÃO DO ARQUIVO
	// Integer numeroPaginacao = 1;
	// Localidade localidadeAnterior = null;
	// Integer codigoSetorComercialAnterior = null;
	// Rota rotaAnterior = null;
	// int qtdImoveisLocalidadeSetorRota = 0;
	//
	// for (Rota rota : rotas) {
	//
	// // CONTROLE DE PAGINAÇÃO DA PESQUISA
	// int quantidadeRegistrosPesquisa = 1000;
	// boolean flagTerminou = false;
	//
	// while (!flagTerminou) {
	//
	// List<MovimentoRoteiroEmpresa> movimentos =
	// movimentoRepositorio.pesquisarMovimentoParaLeitura(rota.getId().intValue(),
	// referencia);
	//
	// if (movimentos != null && !movimentos.isEmpty()) {
	// for (MovimentoRoteiroEmpresa movimento : movimentos) {
	//
	// // QUANTIDADE TOTAL DE IMÓVEIS
	// quantidadeImoveis += movimentos.size();
	//
	// // CONTROLE DE PAGINAÇÃO DA PESQUISA
	// if (movimentos.size() < quantidadeRegistrosPesquisa) {
	// flagTerminou = true;
	// }
	//
	// boolean ligacaoAgua = false;
	// boolean ligacaoPoco = false;
	//
	// // cria uma string builder para adicionar no arquivo
	// StringBuilder arquivoTxtLinha = new StringBuilder();
	//
	// // QUANTIDADE DE IMOVEIS POR LOCALIDADE, SETOR E ROTA
	// qtdImoveisLocalidadeSetorRota++;
	//
	// // PAGINAÇÃO DO ARQUIVO
	// if (localidadeAnterior == null) {
	//
	// // CARREGANDO PELA PRIMEIRA VEZ AS INFORMAÇÕES DE
	// // LOCALIDADE E SETOR ANTERIORES
	// localidadeAnterior = movimento.getLocalidade();
	// codigoSetorComercialAnterior = movimento.getSetorComercial().getCodigo();
	// rotaAnterior = rota;
	//
	// } else {
	//
	// // QUEBRA DE LOCALIDADE
	// if
	// (!localidadeAnterior.getId().equals(movimento.getLocalidade().getId())) {
	//
	// numeroPaginacao = 1;
	// qtdImoveisLocalidadeSetorRota = 1;
	// }
	// // QUEBRA DE SETOR COMERCIAL
	// else if
	// (!codigoSetorComercialAnterior.equals(movimento.getSetorComercial().getCodigo()))
	// {
	//
	// numeroPaginacao = 1;
	// qtdImoveisLocalidadeSetorRota = 1;
	// }
	// /*
	// * QUEBRA DE ROTA QUEBRA POR QUANTIDADE DE IMÓVEIS
	// * COM MESMA: LOCALIDADE, SETOR E ROTA
	// */
	// else if (!rotaAnterior.getId().equals(rota.getId()) ||
	// qtdImoveisLocalidadeSetorRota > 12) {
	//
	// numeroPaginacao++;
	// qtdImoveisLocalidadeSetorRota = 1;
	// }
	// }
	//
	// // GERANDO O ARQUIVO TXT
	// adicionarLinhaTxt(arquivoTxt, arquivoTxtLinha, null, null, null, null,
	// null, movimento, ligacaoAgua, ligacaoPoco, null, false,
	// rota.getId(), null, referencia, numeroPaginacao);
	//
	// /*
	// * PAGINAÇÃO DO ARQUIVO GUARDANDO AS INFORMAÇÕES DA
	// * LOCALIDADE ANTERIOR E DO SETOR COMERCIAL ANTERIOR
	// */
	// localidadeAnterior = movimento.getLocalidade();
	// codigoSetorComercialAnterior = movimento.getSetorComercial().getCodigo();
	// rotaAnterior = rota;
	// }
	// } else {
	//
	// flagTerminou = true;
	// }
	// }
	// }
	//
	// repositorioMicromedicao.atualizarFaturamentoAtividadeCronograma(idGrupo,
	// referencia);
	//
	// // INSERINDO NA BASE O ARQUIVO_TEXTO_ROTEIRO_EMPRESA
	// if (arquivoTxt != null && arquivoTxt.length() != 0) {
	//
	// String anoCom2Digitos = "" +
	// Util.obterAno(movimento.getAnoMesMovimento());
	// anoCom2Digitos = anoCom2Digitos.substring(2, 4);
	//
	// String nomeArquivo = "cons" + anoCom2Digitos +
	// Util.adicionarZerosEsquedaNumero(2, "" +
	// Util.obterMes(movimento.getAnoMesMovimento())) + "."
	// + Util.adicionarZerosEsquedaNumero(3,
	// movimento.getRota().getFaturamentoGrupo().getId().toString());
	//
	// this.inserirArquivo(referencia, movimento, quantidadeImoveis, arquivoTxt,
	// nomeArquivo);
	// }
	// }
	public void inserirArquivo(int referencia, MovimentoRoteiroEmpresa movimento, int quantidadeImoveis, StringBuilder texto, String nomeArquivo) {
		ArquivoTextoRoteiroEmpresa roteiro = new ArquivoTextoRoteiroEmpresa();

		roteiro.setAnoMesReferencia(referencia);
		roteiro.setFaturamentoGrupo(movimento.getFaturamentoGrupo());
		roteiro.setEmpresa(movimento.getEmpresa());
		roteiro.setLocalidade(movimento.getLocalidade());
		roteiro.setCodigoSetorComercial1(movimento.getSetorComercial().getCodigo());

		int[] intervaloQuadras = quadraRepositorio.obterIntervaloQuadrasPorRota(movimento.getRota().getId());
		roteiro.setNumeroQuadraInicial1(intervaloQuadras[0]);
		roteiro.setNumeroQuadraFinal1(intervaloQuadras[1]);

		roteiro.setQuantidadeImovel(quantidadeImoveis);
		roteiro.setNomeArquivo(nomeArquivo);
		roteiro.setSituacaoTransmissaoLeitura(SituacaoTransmissaoLeitura.LIBERADO.getId());
		roteiro.setServicoTipoCelular(ServicoTipoCelular.LEITURA.getId());
		roteiro.setUltimaAlteracao(new Date());

		// TODO: Recuperar caminho por parametros
		IOUtil.criarArquivo(nomeArquivo, "/temp/", texto.toString());

		roteiroRepositorio.salvar(roteiro);
	}

	private boolean liberarGeracaoArquivo(int referencia, MovimentoRoteiroEmpresa movimento) {
		boolean gerar = true;

		ArquivoTextoRoteiroEmpresa arquivo = roteiroRepositorio.pesquisarPorGrupoEReferencia(referencia, movimento.getFaturamentoGrupo().getId());
		if (arquivo != null && arquivo.getEmpresa().getId().intValue() == movimento.getEmpresa().getId().intValue()) {
			if (arquivo.getSituacaoTransmissaoLeitura().intValue() == SituacaoTransmissaoLeitura.LIBERADO.getId()) {
				roteiroRepositorio.excluir(arquivo.getId());
			} else {
				gerar = false;
			}
		}

		return gerar;
	}

	private String adicionarLinhaTxt(MovimentoRoteiroEmpresa movimento, Integer numeroPagina) {

		StringBuilder linha = new StringBuilder();

		linha.append(completaTexto(15, movimento.getImovel().getId()));
		linha.append(completaComZerosEsquerda(3, movimento.getSetorComercial().getCodigo()));
		linha.append(completaComZerosEsquerda(4, movimento.getRota().getCodigo()));
		linha.append(completaTexto(3, ""));
		linha.append(completaTexto(3, ""));
		linha.append(completaTexto(2, ""));
		linha.append(completaTexto(3, ""));
		linha.append(completaTexto(2, ""));
		linha.append(completaComZerosEsquerda(6, movimento.getNumeroLoteImovel()));
		linha.append(completaTexto(15, movimento.getImovel().getId()));
		linha.append(completaComZerosEsquerda(1, ""));
		linha.append(completaTexto(12, movimento.getNumeroHidrometro()));
		linha.append(completaTexto(15, movimento.getLigacaoAguaSituacao().getDescricao()));
		linha.append(completaTexto(15, getMedicaoTipo(movimento.getMedicaoTipo())));
		linha.append(completaTexto(40, movimento.getNomeCliente()));
		linha.append(completaTexto(40, getLogradouro(movimento.getLogradouro())));
		linha.append(completaTexto(6, movimento.getImovel().getNumeroImovel()));
		linha.append(completaTexto(15, movimento.getComplementoEndereco()));
		linha.append(completaTexto(20, movimento.getNomeBairro()));

		if (movimento.getDescricaoAbreviadaCategoriaImovel() != null) {
			if (movimento.getDescricaoAbreviadaCategoriaImovel().equals(Categoria.RESIDENCIAL_DESCRICAO_ABREVIADA)) {
				linha.append(completaComZerosEsquerda(4, movimento.getQuantidadeEconomias()));
				linha.append(completaComZerosEsquerda(4, ""));
				linha.append(completaComZerosEsquerda(4, ""));
				linha.append(completaComZerosEsquerda(4, ""));
			} else if (movimento.getDescricaoAbreviadaCategoriaImovel().equals(Categoria.COMERCIAL_DESCRICAO_ABREVIADA)) {
				linha.append(completaComZerosEsquerda(4, ""));
				linha.append(completaComZerosEsquerda(4, movimento.getQuantidadeEconomias()));
				linha.append(completaComZerosEsquerda(4, ""));
				linha.append(completaComZerosEsquerda(4, ""));
			} else if (movimento.getDescricaoAbreviadaCategoriaImovel().equals(Categoria.INDUSTRIAL_DESCRICAO_ABREVIADA)) {
				linha.append(completaComZerosEsquerda(4, ""));
				linha.append(completaComZerosEsquerda(4, ""));
				linha.append(completaComZerosEsquerda(4, movimento.getQuantidadeEconomias()));
				linha.append(completaComZerosEsquerda(4, ""));
				linha.append(completaComZerosEsquerda(4, ""));
			} else {
				linha.append(completaComZerosEsquerda(4, ""));
				linha.append(completaComZerosEsquerda(4, ""));
				linha.append(completaComZerosEsquerda(4, ""));
				linha.append(completaComZerosEsquerda(4, movimento.getQuantidadeEconomias()));
			}
		} else {
			linha.append(completaComZerosEsquerda(4, ""));
			linha.append(completaComZerosEsquerda(4, ""));
			linha.append(completaComZerosEsquerda(4, ""));
			linha.append(completaComZerosEsquerda(4, ""));
		}

		linha.append(completaComZerosEsquerda(4, ""));
		linha.append(completaComZerosEsquerda(8, movimento.getNumeroLeituraAnterior()));
		linha.append(completaComZerosEsquerda(4, movimento.getCodigoAnormalidadeAnterior()));
		linha.append(completaComZerosEsquerda(5, getConsumoMinimo(movimento)));
		linha.append(completaComZerosEsquerda(5, getConsumoMaximo(movimento)));
		linha.append(completaComZerosEsquerda(5, movimento.getNumeroConsumoMedio()));
		linha.append(completaTexto(60, ""));
		linha.append(completaComZerosEsquerda(2, movimento.getNumeroMoradores()));
		linha.append(movimento.getAnoMesMovimento());
		linha.append(completaTexto(16, ""));
		linha.append(completaTexto(16, ""));
		linha.append(completaComZerosEsquerda(1, ""));
		linha.append(completaComZerosEsquerda(3, ""));
		linha.append(completaComZerosEsquerda(1, ""));
		linha.append(completaComZerosEsquerda(3, ""));
		linha.append(completaComZerosEsquerda(1, ""));
		linha.append(completaComZerosEsquerda(3, ""));
		linha.append(completaComZerosEsquerda(1, ""));
		linha.append(completaComZerosEsquerda(3, ""));
		linha.append(completaComZerosEsquerda(1, ""));
		linha.append(completaComZerosEsquerda(3, ""));
		linha.append(completaComZerosEsquerda(4, movimento.getNumeroQuadra()));
		linha.append(completaComZerosEsquerda(4, movimento.getNumeroLoteImovel()));
		linha.append(completaComZerosEsquerda(3, movimento.getNumeroSubloteImovel()));
		linha.append(completaTexto(19, ""));
		linha.append(completaTexto(212, ""));
		linha.append("A01");
		linha.append(formatarAnoMesParaMesAno(movimento.getAnoMesMovimento()));
		linha.append(completaComZerosEsquerda(4, movimento.getFaturamentoGrupo().getId().toString()));
		linha.append(completaComZerosEsquerda(5, numeroPagina.toString()));
		linha.append(completaComZerosEsquerda(10, movimento.getLocalidade().getId().toString()));
		linha.append(completaTexto(4, ""));
		linha.append(completaComZerosEsquerda(3, movimento.getCodigoQuadraFace().toString()));

		return linha.toString();
	}

	private Integer getConsumoMaximo(MovimentoRoteiroEmpresa movimento) {
		if (movimento.getNumeroFaixaLeituraEsperadaFinal() != null) {
			return movimento.getNumeroFaixaLeituraEsperadaFinal() - movimento.getNumeroLeituraAnterior();
		} else {
			return 0;
		}
	}

	private Integer getConsumoMinimo(MovimentoRoteiroEmpresa movimento) {
		if (movimento.getNumeroFaixaLeituraEsperadaInicial() != null) {
			return movimento.getNumeroFaixaLeituraEsperadaInicial() - movimento.getNumeroLeituraAnterior();
		} else {
			return 0;
		}
	}

	private String getLogradouro(Logradouro logradouro) {
		if (logradouro != null) {
			return logradouro.getDescricaoFormatada().toString();
		} else {
			return "";
		}
	}

	private String getMedicaoTipo(Integer medicao) {
		if (medicao.intValue() == MedicaoTipo.LIGACAO_AGUA.getId()) {
			return "SO AGUA";
		} else if (medicao.intValue() == MedicaoTipo.POCO.getId()) {
			return "SO ESGOTO";
		} else {
			return "";
		}
	}
}
