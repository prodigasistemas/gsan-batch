package br.gov.batch.gerararquivomicrocoletor;

import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.extrairAno;
import static br.gov.model.util.Utilitarios.extrairMes;
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
import br.gov.model.micromedicao.ArquivoTextoRoteiroEmpresa;
import br.gov.model.micromedicao.MovimentoRoteiroEmpresa;
import br.gov.model.micromedicao.ServicoTipoCelular;
import br.gov.model.micromedicao.SituacaoTransmissaoLeitura;
import br.gov.model.util.Utilitarios;
import br.gov.persistence.util.IOUtil;
import br.gov.servicos.cadastro.QuadraRepositorio;
import br.gov.servicos.micromedicao.ArquivoTextoRoteiroEmpresaRepositorio;

@Named
public class AgruparArquivosMicrocoletor implements ItemProcessor {
	private static Logger logger = Logger.getLogger(AgruparArquivosMicrocoletor.class);

	@EJB
	private ArquivoTextoRoteiroEmpresaRepositorio roteiroRepositorio;

	@EJB
	private QuadraRepositorio quadraRepositorio;

	@Inject
	private BatchUtil util;

	private Integer anoMesReferencia;
	private Integer idGrupo;
	private String nomeArquivo;

	public AgruparArquivosMicrocoletor() {
	}

	// FIXME: Parametrizar diretorio
	public Object processItem(Object param) throws Exception {
		anoMesReferencia = Integer.valueOf(util.parametroDoBatch("anoMesFaturamento"));
		idGrupo = Integer.valueOf(util.parametroDoBatch("idGrupoFaturamento"));
		nomeArquivo = montarNomeArquivo();

		logger.info("Agrupando os arquivos de microcoletor para o grupo");

		String[] wildcards = new String[] { nomeArquivo + "*.txt" };

		List<File> arquivos = Arrays.asList(arquivosFiltrados("/tmp", wildcards));

		arquivos.sort(Comparator.naturalOrder());

		StringBuilder texto = new StringBuilder();

		for (File file : arquivos) {
			FileReader reader = new FileReader(file);
			BufferedReader b = new BufferedReader(reader);
			String linha = null;

			while ((linha = b.readLine()) != null) {
				texto.append(linha).append(quebraLinha);
			}
			b.close();
		}

		// arquivos.forEach(e -> e.delete());

		// TODO: Recuperar dados do movimento
		MovimentoRoteiroEmpresa movimento = null;
		if (texto != null && texto.length() > 0) {
			if (liberarGeracaoArquivo(movimento)) {
				inserirRoteiro(movimento, texto);
			}
		}

		return param;
	}

	public void inserirRoteiro(MovimentoRoteiroEmpresa movimento, StringBuilder texto) {
		ArquivoTextoRoteiroEmpresa roteiro = new ArquivoTextoRoteiroEmpresa();

		roteiro.setAnoMesReferencia(anoMesReferencia);
		roteiro.setFaturamentoGrupo(movimento.getFaturamentoGrupo());
		roteiro.setEmpresa(movimento.getEmpresa());
		roteiro.setLocalidade(movimento.getLocalidade());
		roteiro.setCodigoSetorComercial1(movimento.getCodigoSetorComercial());

		int[] intervaloQuadras = quadraRepositorio.obterIntervaloQuadrasPorRota(movimento.getRota().getId());
		roteiro.setNumeroQuadraInicial1(intervaloQuadras[0]);
		roteiro.setNumeroQuadraFinal1(intervaloQuadras[1]);

		roteiro.setQuantidadeImovel(Utilitarios.obterQuantidadeLinhasTexto(texto));
		roteiro.setSituacaoTransmissaoLeitura(SituacaoTransmissaoLeitura.LIBERADO.getId());
		roteiro.setServicoTipoCelular(ServicoTipoCelular.LEITURA.getId());
		roteiro.setUltimaAlteracao(new Date());

		roteiro.setNomeArquivo(nomeArquivo);

		IOUtil.criarArquivoTextoCompactado(nomeArquivo, "/tmp/", texto.toString());

		roteiroRepositorio.salvar(roteiro);
	}

	private boolean liberarGeracaoArquivo(MovimentoRoteiroEmpresa movimento) {
		boolean gerar = true;

		ArquivoTextoRoteiroEmpresa arquivo = roteiroRepositorio.pesquisarPorGrupoEReferencia(anoMesReferencia, idGrupo);
		if (arquivo != null && arquivo.getEmpresa().getId().intValue() == movimento.getEmpresa().getId().intValue()) {
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