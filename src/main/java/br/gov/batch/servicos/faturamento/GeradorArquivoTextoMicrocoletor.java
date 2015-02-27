package br.gov.batch.servicos.faturamento;

import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.completaTexto;
import static br.gov.model.util.Utilitarios.extrairAno;
import static br.gov.model.util.Utilitarios.extrairMes;
import static br.gov.model.util.Utilitarios.formatarAnoMesParaMesAno;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.model.cadastro.Localidade;
import br.gov.model.micromedicao.ArquivoTextoRoteiroEmpresa;
import br.gov.model.micromedicao.MedicaoTipo;
import br.gov.model.micromedicao.MovimentoRoteiroEmpresa;
import br.gov.model.micromedicao.Rota;
import br.gov.model.micromedicao.ServicoTipoCelular;
import br.gov.model.micromedicao.SituacaoTransmissaoLeitura;
import br.gov.model.util.IOUtil;
import br.gov.servicos.cadastro.QuadraRepositorio;
import br.gov.servicos.faturamento.FaturamentoAtividadeCronogramaRepositorio;
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

	@EJB
	private FaturamentoAtividadeCronogramaRepositorio faturamentoAtividadeCronogramaRepositorio;

	public GeradorArquivoTextoMicrocoletor() {
		super();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void gerar(List<Rota> rotas, int referencia, int idGrupo) {
		StringBuilder arquivoTexto = new StringBuilder();

		MovimentoRoteiroEmpresa movimento = null;

		int quantidadeImoveis = 0;
		int pagina = 1;
		int quantidadeImoveisLocalidadeSetorRota = 0;

		for (Rota rota : rotas) {
			int indice = 0;
			int quantidadeRegistros = 1000;

			boolean flagTerminou = false;
			while (!flagTerminou) {
				List<MovimentoRoteiroEmpresa> movimentos = movimentoRepositorio.pesquisarMovimentoParaLeitura(rota.getId(), referencia, indice);

				if (movimentos != null && !movimentos.isEmpty()) {

					for (int i = 0; i < movimentos.size(); i++) {
						movimento = movimentos.get(i);

						quantidadeImoveis += movimentos.size();

						if (movimentos.size() < quantidadeRegistros) {
							flagTerminou = true;
						} else {
							indice += quantidadeRegistros;
						}

						quantidadeImoveisLocalidadeSetorRota++;
						Localidade localidadeAnterior = null;
						Integer codigoSetorComercialAnterior = null;
						Rota rotaAnterior = null;

						if (localidadeAnterior == null) {
							localidadeAnterior = movimento.getLocalidade();
							codigoSetorComercialAnterior = movimento.getCodigoSetorComercial();
							rotaAnterior = rota;
						} else {
							if (!localidadeAnterior.getId().equals(movimento.getLocalidade().getId())) {
								pagina = 1;
								quantidadeImoveisLocalidadeSetorRota = 1;

							} else if (!codigoSetorComercialAnterior.equals(movimento.getCodigoSetorComercial())) {
								pagina = 1;
								quantidadeImoveisLocalidadeSetorRota = 1;

							} else if (!rotaAnterior.getId().equals(rota.getId()) || quantidadeImoveisLocalidadeSetorRota > 12) {
								pagina++;
								quantidadeImoveisLocalidadeSetorRota = 1;
							}
						}

						localidadeAnterior = movimento.getLocalidade();
						codigoSetorComercialAnterior = movimento.getCodigoSetorComercial();
						rotaAnterior = rota;

						arquivoTexto.append(adicionarLinha(movimento, pagina));
					}

				} else {
					flagTerminou = true;
				}
			}
		}

		faturamentoAtividadeCronogramaRepositorio.atualizarFaturamentoAtividadeCronograma(idGrupo, referencia);

		if (arquivoTexto != null && arquivoTexto.length() > 0) {
			if (liberarGeracaoArquivo(movimento, referencia))
				inserirRoteiro(movimento, referencia, quantidadeImoveis, arquivoTexto);
		}
	}

	private String getNomeArquivo(MovimentoRoteiroEmpresa movimento) {
		String ano = extrairAno(movimento.getAnoMesMovimento()).toString().substring(2, 4);
		String mes = completaComZerosEsquerda(2, extrairMes(movimento.getAnoMesMovimento()));
		String grupo = completaComZerosEsquerda(3, movimento.getRota().getFaturamentoGrupo().getId());

		return "cons" + ano + mes + "." + grupo;
	}

	public void inserirRoteiro(MovimentoRoteiroEmpresa movimento, int referencia, int quantidadeImoveis, StringBuilder texto) {
		ArquivoTextoRoteiroEmpresa roteiro = new ArquivoTextoRoteiroEmpresa();

		roteiro.setAnoMesReferencia(referencia);
		roteiro.setFaturamentoGrupo(movimento.getFaturamentoGrupo());
		roteiro.setEmpresa(movimento.getEmpresa());
		roteiro.setLocalidade(movimento.getLocalidade());
		roteiro.setCodigoSetorComercial1(movimento.getCodigoSetorComercial());

		int[] intervaloQuadras = quadraRepositorio.obterIntervaloQuadrasPorRota(movimento.getRota().getId());
		roteiro.setNumeroQuadraInicial1(intervaloQuadras[0]);
		roteiro.setNumeroQuadraFinal1(intervaloQuadras[1]);

		roteiro.setQuantidadeImovel(quantidadeImoveis);
		roteiro.setSituacaoTransmissaoLeitura(SituacaoTransmissaoLeitura.LIBERADO.getId());
		roteiro.setServicoTipoCelular(ServicoTipoCelular.LEITURA.getId());
		roteiro.setUltimaAlteracao(new Date());

		String nomeArquivo = getNomeArquivo(movimento);
		roteiro.setNomeArquivo(nomeArquivo);

		// TODO: Recuperar caminho por parametros
		IOUtil.criarArquivo(nomeArquivo, "/temp/", texto.toString());

		roteiroRepositorio.salvar(roteiro);
	}

	private boolean liberarGeracaoArquivo(MovimentoRoteiroEmpresa movimento, int referencia) {
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

	private String adicionarLinha(MovimentoRoteiroEmpresa movimento, int pagina) {
		StringBuilder linha = new StringBuilder();

		linha.append(completaTexto(15, movimento.getImovel().getId()));
		linha.append(completaComZerosEsquerda(3, movimento.getCodigoSetorComercial()));
		linha.append(completaComZerosEsquerda(4, movimento.getRota().getCodigo()));
		linha.append(completaTexto(3, ""));
		linha.append(completaTexto(3, ""));
		linha.append(completaTexto(2, ""));
		linha.append(completaTexto(3, ""));
		linha.append(completaTexto(2, ""));
		linha.append(completaComZerosEsquerda(6, movimento.getLoteImovel()));
		linha.append(completaTexto(15, movimento.getImovel().getId()));
		linha.append(completaComZerosEsquerda(1, ""));
		linha.append(completaTexto(12, movimento.getNumeroHidrometro()));
		linha.append(completaTexto(15, movimento.getLigacaoAguaSituacao().getDescricao()));
		linha.append(completaTexto(15, getMedicaoTipo(movimento.getMedicaoTipo())));
		linha.append(completaTexto(40, movimento.getNomeCliente()));
		linha.append(completaTexto(40, movimento.getLogradouro() != null ? movimento.getLogradouro().getDescricaoFormatada().toString() : ""));
		linha.append(completaTexto(6, movimento.getImovel().getNumeroImovel()));
		linha.append(completaTexto(15, movimento.getComplementoEndereco()));
		linha.append(completaTexto(20, movimento.getNomeBairro()));
		linha.append(movimento.isResidencial() ? completaComZerosEsquerda(4, movimento.getQuantidadeEconomias()) : completaComZerosEsquerda(4, ""));
		linha.append(movimento.isComercial() ? completaComZerosEsquerda(4, movimento.getQuantidadeEconomias()) : completaComZerosEsquerda(4, ""));
		linha.append(movimento.isIndustrial() ? completaComZerosEsquerda(4, movimento.getQuantidadeEconomias()) : completaComZerosEsquerda(4, ""));
		linha.append(movimento.isPublico() ? completaComZerosEsquerda(4, movimento.getQuantidadeEconomias()) : completaComZerosEsquerda(4, ""));
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
		linha.append(completaComZerosEsquerda(4, movimento.getLoteImovel()));
		linha.append(completaComZerosEsquerda(3, movimento.getSubloteImovel()));
		linha.append(completaTexto(19, ""));
		linha.append(completaTexto(212, ""));
		linha.append("A01");
		linha.append(formatarAnoMesParaMesAno(movimento.getAnoMesMovimento()));
		linha.append(completaComZerosEsquerda(4, movimento.getFaturamentoGrupo().getId()));
		linha.append(completaComZerosEsquerda(5, pagina));
		linha.append(completaComZerosEsquerda(10, movimento.getLocalidade().getId()));
		linha.append(completaTexto(4, ""));
		linha.append(completaComZerosEsquerda(3, movimento.getCodigoQuadraFace()));

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
