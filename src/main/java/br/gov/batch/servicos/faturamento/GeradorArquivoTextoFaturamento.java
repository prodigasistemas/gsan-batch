package br.gov.batch.servicos.faturamento;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.faturamento.arquivo.ArquivoTexto;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo01;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo02;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo03;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo04;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo05;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo06;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo07;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo08;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo09;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo10;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo11;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo12;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo13;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo14;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cobranca.CobrancaDocumento;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.ArquivoTextoRoteiroEmpresa;
import br.gov.model.micromedicao.Rota;
import br.gov.model.micromedicao.SituacaoTransmissaoLeitura;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.cobranca.CobrancaDocumentoRepositorio;
import br.gov.servicos.faturamento.ContaRepositorio;
import br.gov.servicos.micromedicao.ArquivoTextoRoteiroEmpresaDivisaoRepositorio;
import br.gov.servicos.micromedicao.ArquivoTextoRoteiroEmpresaRepositorio;

@Stateless
public class GeradorArquivoTextoFaturamento {

	@EJB
	private ArquivoTextoRoteiroEmpresaDivisaoRepositorio arquivoDivisaoRepositorio;

	@EJB
	private ArquivoTextoRoteiroEmpresaRepositorio arquivoRepositorio;

	@EJB
	private ImovelRepositorio imovelRepositorio;

	@EJB
	private ContaRepositorio contaRepositorio;

	@EJB
	private CobrancaDocumentoRepositorio cobrancaDocumentoRepositorio;

	private ArquivoTextoTO to;
	
	private StringBuilder arquivoTexto;
	
	private StringBuilder arquivoTextoDivisao;

	public GeradorArquivoTextoFaturamento() {
		super();

		to = new ArquivoTextoTO();
		arquivoTexto = new StringBuilder();
		arquivoTextoDivisao = new StringBuilder();
	}

	public void gerar(Rota rota, Integer anoMesFaturamento) {
		final int quantidadeRegistros = 3000;
		int primeiroRegistro = 0;

		List<Imovel> imoveis = imoveisParaGerarArquivoTextoFaturamento(rota, primeiroRegistro, quantidadeRegistros);

		List<Imovel> imoveisArquivo = new ArrayList<Imovel>();

		for (Imovel imovel : imoveis) {
			if (imovel.isCondominio()) {
				if (imovel.existeHidrometro()) {
					List<Imovel> imoveisCondominio = imoveisCondominioParaGerarArquivoTextoFaturamento(rota, imovel.getId());

					boolean imovelMicroComConta = false;

					for (Imovel imovelCondominio : imoveisCondominio) {
						if (contaRepositorio.existeContaPreFaturada(imovelCondominio.getId(), anoMesFaturamento)) {
							imovelMicroComConta = true;
							break;
						}
					}

					if (imovelMicroComConta) {
						imoveisArquivo.add(imovel);
					}
				}
			} else {
//				carregarArquivo(imovel, anoMesReferencia, rota, faturamentoGrupo, dataComando);
//
//				int tamanhoArquivoRetorno = 0;
//
//				if (arquivoTexto.length() != 0) {
//					arquivoTexto.append(System.getProperty("line.separator"));
//				}
//				tamanhoArquivoRetorno = getQuantidadeLinhas();
//				tamanhoArquivo += tamanhoArquivoRetorno;
//
//				if (rota.getNumeroLimiteImoveis() != null && !rota.getNumeroLimiteImoveis().equals("")) {
//
//					if (arquivoTextoDivisao.length() != 0) {
//						arquivoTextoDivisao.append(System.getProperty("line.separator"));
//					}
//
//					if (arquivoTextoDivisao != null) {
//						arquivoTextoDivisao.append(arquivoTexto);
//						imoveisDivididos.add(imovel.getId());
//						tamanhoArquivoDiv  += tamanhoArquivoRetorno;
//					}
//				}
//
//				if (tamanhoArquivoRetorno != 0) {
//					imoveisArquivo.add(imovel);
//				}
			}
		}
	}

	public int getQuantidadeLinhas() {
		String[] linhas = arquivoTexto.toString().split(System.getProperty("line.separator"));
		return linhas.length;
	}
	
	public boolean existeArquivoTextoRota(Integer idRota, Integer anoMesReferencia) {
		boolean retorno = true;

		ArquivoTextoRoteiroEmpresa arquivo = arquivoRepositorio.pesquisarPorRotaEReferencia(idRota, anoMesReferencia);

		if (arquivo != null) {
			if (arquivo.getSituacaoTransmissaoLeitura() == SituacaoTransmissaoLeitura.DISPONIVEL.getId()) {
				arquivoDivisaoRepositorio.deletarPorArquivoTextoRoteiroEmpresa(arquivo.getId());
				arquivoRepositorio.deletarPorId(arquivo.getId());
			} else {
				retorno = false;
			}
		}

		return retorno;
	}

	public List<Imovel> imoveisParaGerarArquivoTextoFaturamento(Rota rota, int primeiroRegistro, int quantidadeRegistros) {
		List<Imovel> imoveisConsulta = null;

		if (rota.alternativa()) {
			imoveisConsulta = imovelRepositorio.imoveisParaGerarArquivoTextoFaturamentoPorRotaAlternativa(rota.getId(), primeiroRegistro, quantidadeRegistros);
		} else {
			imoveisConsulta = imovelRepositorio.imoveisParaGerarArquivoTextoFaturamento(rota.getId(), primeiroRegistro, quantidadeRegistros);
		}

		List<Imovel> imoveis = new ArrayList<Imovel>();

		for (Imovel imovel : imoveisConsulta) {
			if (imovel.pertenceACondominio() || imovel.isCondominio() || imovel.existeHidrometroAgua() || imovel.existeHidrometroPoco()) {
				imoveis.add(imovel);
			}
		}

		return imoveis;
	}

	public List<Imovel> imoveisCondominioParaGerarArquivoTextoFaturamento(Rota rota, Integer idCondominio) {
		List<Imovel> imoveisConsulta = null;

		if (rota.alternativa()) {
			imoveisConsulta = imovelRepositorio.imoveisCondominioParaGerarArquivoTextoFaturamentoPorRotaAlternativa(idCondominio);
		} else {
			imoveisConsulta = imovelRepositorio.imoveisCondominioParaGerarArquivoTextoFaturamento(idCondominio);
		}

		List<Imovel> imoveis = new ArrayList<Imovel>();

		for (Imovel imovel : imoveisConsulta) {
			if (imovel.pertenceACondominio() || imovel.isCondominio() || imovel.existeHidrometroAgua() || imovel.existeHidrometroPoco()) {
				imoveis.add(imovel);
			}
		}

		return imoveis;
	}

	private void carregarArquivo(Imovel imovel, Integer anoMesReferencia, Rota rota, FaturamentoGrupo faturamentoGrupo, Date dataComando) {

		Conta conta = contaRepositorio.pesquisarContaArquivoTextoFaturamento(imovel.getId(), anoMesReferencia, faturamentoGrupo.getId());

		gerarArquivoTexto(imovel, conta, anoMesReferencia, rota, faturamentoGrupo, dataComando);
	}

	private void gerarArquivoTexto(Imovel imovel, Conta conta, Integer anoMesReferencia, Rota rota, FaturamentoGrupo faturamentoGrupo, Date dataComando) {

		CobrancaDocumento cobrancaDocumento = cobrancaDocumentoRepositorio.cobrancaDocumentoImpressaoSimultanea(
				Utilitarios.reduzirDias(dataComando, 10), imovel.getId());

		to = new ArquivoTextoTO(imovel, conta, anoMesReferencia, faturamentoGrupo, rota, cobrancaDocumento);

		ArquivoTexto arquivo = new ArquivoTextoTipo01();
		arquivoTexto.append(arquivo.build(to));

		arquivo = new ArquivoTextoTipo02();
		arquivoTexto.append(arquivo.build(to));

		arquivo = new ArquivoTextoTipo03();
		arquivoTexto.append(arquivo.build(to));

		arquivo = new ArquivoTextoTipo04();
		arquivoTexto.append(arquivo.build(to));

		arquivo = new ArquivoTextoTipo05();
		arquivoTexto.append(arquivo.build(to));

		arquivo = new ArquivoTextoTipo06();
		arquivoTexto.append(arquivo.build(to));

		arquivo = new ArquivoTextoTipo07();
		arquivoTexto.append(arquivo.build(to));

		arquivo = new ArquivoTextoTipo08();
		arquivoTexto.append(arquivo.build(to));

		arquivo = new ArquivoTextoTipo09();
		arquivoTexto.append(arquivo.build(to));

		arquivo = new ArquivoTextoTipo10();
		arquivoTexto.append(arquivo.build(to));
	}

	private void gerarPassosFinais() {
		arquivoTexto.append(System.getProperty("line.separator"));
		
		ArquivoTexto arquivo = new ArquivoTextoTipo11();
		arquivoTexto.append(arquivo.build(to));
		
		arquivo = new ArquivoTextoTipo12();
		arquivoTexto.append(arquivo.build(to));

		arquivo = new ArquivoTextoTipo13();
		arquivoTexto.append(arquivo.build(to));

		arquivo = new ArquivoTextoTipo14();
		arquivoTexto.append(arquivo.build(to));
	}
}
