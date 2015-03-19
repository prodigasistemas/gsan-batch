package br.gov.batch.gerararquivomicrocoletor;

import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.extrairAno;
import static br.gov.model.util.Utilitarios.extrairMes;
import static br.gov.model.util.Utilitarios.obterQuantidadeLinhasTexto;
import static br.gov.model.util.Utilitarios.quebraLinha;
import static br.gov.persistence.util.IOUtil.arquivosFiltrados;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.batch.api.chunk.ItemProcessor;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import br.gov.batch.util.BatchUtil;
import br.gov.model.cadastro.Empresa;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.ArquivoTextoRoteiroEmpresa;
import br.gov.model.micromedicao.LeituraTipo;
import br.gov.model.micromedicao.Rota;
import br.gov.model.micromedicao.ServicoTipoCelular;
import br.gov.model.micromedicao.SituacaoTransmissaoLeitura;
import br.gov.model.seguranca.SegurancaParametro.NOME_PARAMETRO_SEGURANCA;
import br.gov.persistence.util.IOUtil;
import br.gov.servicos.cadastro.QuadraRepositorio;
import br.gov.servicos.micromedicao.ArquivoTextoRoteiroEmpresaRepositorio;
import br.gov.servicos.micromedicao.RotaRepositorio;
import br.gov.servicos.seguranca.SegurancaParametroRepositorio;

@Named
public class AgruparArquivosMicrocoletor implements ItemProcessor {
	private static Logger logger = Logger.getLogger(AgruparArquivosMicrocoletor.class);

	@EJB
	private ArquivoTextoRoteiroEmpresaRepositorio roteiroRepositorio;

	@EJB
	private QuadraRepositorio quadraRepositorio;

	@Inject
	private BatchUtil util;

	@EJB
	private RotaRepositorio rotaRepositorio;

	@EJB
	private SegurancaParametroRepositorio segurancaParametroRepositorio;

	private Integer anoMesReferencia;
	private Integer idGrupo;
	private String nomeArquivo;
	private Integer idEmpresa;

	private Empresa empresa;

	public AgruparArquivosMicrocoletor() {}

	// TODO: Refactoring
	public Object processItem(Object param) throws Exception {
		anoMesReferencia = Integer.valueOf(util.parametroDoBatch("anoMesFaturamento"));
		idGrupo = Integer.valueOf(util.parametroDoBatch("idGrupoFaturamento"));
		nomeArquivo = montarNomeArquivo();

		// TODO: Grupo microcoletor possui rotas atendidas por empresas diferentes
		List<Rota> rotas = rotaRepositorio.obterPeloGrupoETipoLeitura(idGrupo, LeituraTipo.MICROCOLETOR);

		if (!rotas.isEmpty()) {
			empresa = rotas.get(0).getEmpresa();
		}

		logger.info("Agrupando os arquivos de microcoletor para o grupo");

		String[] wildcards = new String[] { nomeArquivo + "*.txt" };

		List<File> arquivos = Arrays.asList(arquivosFiltrados(recuperarCaminhoArquivos(), wildcards));

		arquivos.sort(Comparator.naturalOrder());

		StringBuilder texto = new StringBuilder();

		int qtdLinhas = 1;
		int pagina = 1;

		int localidade = -1;
		int setor = -1;
		int rota = -1;

		int localidadeAnterior = -1;
		int setorAnterior = -1;
		int rotaAnterior = -1;

		for (File file : arquivos) {
			FileReader reader = new FileReader(file);
			BufferedReader b = new BufferedReader(reader);
			String linha = null;
			localidade = Integer.valueOf(file.getName().substring(13, 16));
			setor = Integer.valueOf(file.getName().substring(17, 20));
			rota = Integer.valueOf(file.getName().substring(21, 24));

			if (localidadeAnterior != localidade) {
				pagina = 1;
				qtdLinhas = 1;

			} else if (setorAnterior != setor) {
				pagina = 1;
				qtdLinhas = 1;

			} else if (rotaAnterior != rota) {
				pagina++;
				qtdLinhas = 1;
			}

			while ((linha = b.readLine()) != null) {
				linha = linha.substring(0, 643) + completaComZerosEsquerda(5, pagina) + linha.substring(648);
				texto.append(linha).append(quebraLinha);
				if (qtdLinhas > 12) {
					pagina++;
					qtdLinhas = 1;
				}
			}

			localidadeAnterior = localidade;
			setorAnterior = setor;
			rotaAnterior = rota;
			b.close();
		}

		arquivos.forEach(e -> e.delete());

		if (texto != null && texto.length() > 0) {
			if (liberarGeracaoArquivo(idEmpresa)) {
				inserirRoteiro(texto);
			}
		}

		return param;
	}

	public void inserirRoteiro(StringBuilder texto) {
		ArquivoTextoRoteiroEmpresa roteiro = new ArquivoTextoRoteiroEmpresa();
		roteiro.setAnoMesReferencia(anoMesReferencia);
		roteiro.setFaturamentoGrupo(new FaturamentoGrupo(idGrupo));
		roteiro.setEmpresa(empresa);
		roteiro.setQuantidadeImovel(obterQuantidadeLinhasTexto(texto));
		roteiro.setSituacaoTransmissaoLeitura(SituacaoTransmissaoLeitura.LIBERADO.getId());
		roteiro.setServicoTipoCelular(ServicoTipoCelular.LEITURA.getId());
		roteiro.setUltimaAlteracao(new Date());
		roteiro.setNomeArquivo(nomeArquivo);

		IOUtil.criarArquivoTextoCompactado(nomeArquivo, recuperarCaminhoArquivos(), texto.toString());

		roteiroRepositorio.salvar(roteiro);
	}

	private String recuperarCaminhoArquivos() {
		String caminhoArquivos = segurancaParametroRepositorio.recuperaPeloNome(NOME_PARAMETRO_SEGURANCA.CAMINHO_ARQUIVOS);

		return caminhoArquivos + idGrupo + "/" + anoMesReferencia;
	}

	private boolean liberarGeracaoArquivo(Integer idEmpresa) {
		boolean gerar = true;

		ArquivoTextoRoteiroEmpresa arquivo = roteiroRepositorio.pesquisarPorGrupoEReferencia(anoMesReferencia, idGrupo);
		if (arquivo != null && arquivo.getEmpresa().getId().intValue() == idEmpresa) {
			if (arquivo.getSituacaoTransmissaoLeitura().intValue() == SituacaoTransmissaoLeitura.LIBERADO.getId()) {
				roteiroRepositorio.excluir(arquivo.getId());
			} else {
				gerar = false;
			}
		}

		return gerar;
	}

	private String montarNomeArquivo() {
		String ano = extrairAno(anoMesReferencia).toString().substring(2, 4);
		String mes = completaComZerosEsquerda(2, extrairMes(anoMesReferencia));

		return "cons" + ano + mes + "." + idGrupo;
	}
}