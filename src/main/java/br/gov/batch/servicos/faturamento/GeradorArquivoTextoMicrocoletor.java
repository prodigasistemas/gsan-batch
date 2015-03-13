package br.gov.batch.servicos.faturamento;

import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.completaTexto;
import static br.gov.model.util.Utilitarios.extrairAno;
import static br.gov.model.util.Utilitarios.extrairMes;
import static br.gov.model.util.Utilitarios.formatarAnoMesParaMesAno;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.model.micromedicao.MedicaoTipo;
import br.gov.model.micromedicao.MovimentoRoteiroEmpresa;
import br.gov.model.micromedicao.Rota;
import br.gov.persistence.util.IOUtil;
import br.gov.servicos.faturamento.FaturamentoAtividadeCronogramaRepositorio;
import br.gov.servicos.micromedicao.MovimentoRoteiroEmpresaRepositorio;
import br.gov.servicos.micromedicao.RotaRepositorio;

@Stateless
public class GeradorArquivoTextoMicrocoletor {

	@EJB
	private MovimentoRoteiroEmpresaRepositorio movimentoRepositorio;

	@EJB
	private FaturamentoAtividadeCronogramaRepositorio faturamentoAtividadeCronogramaRepositorio;

	@EJB
	private RotaRepositorio rotaRepositorio;

	public GeradorArquivoTextoMicrocoletor() {
		super();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void gerar(Integer idRota) {
		Rota rota = rotaRepositorio.obterPorID(idRota);
		int referencia = rota.getFaturamentoGrupo().getAnoMesReferencia();

		StringBuilder texto = new StringBuilder();

		List<MovimentoRoteiroEmpresa> movimentos = movimentoRepositorio.pesquisarMovimentoParaLeitura(idRota, referencia, 0, 0);

		for (MovimentoRoteiroEmpresa movimento : movimentos) {
		    texto.append(adicionarLinha(movimento));
        }

		faturamentoAtividadeCronogramaRepositorio.atualizarFaturamentoAtividadeCronograma(rota.getFaturamentoGrupo().getId(), referencia);

		if (!movimentos.isEmpty()){
			criarArquivo(referencia, rota, texto);
		}
	}

	private String adicionarLinha(MovimentoRoteiroEmpresa movimento) {
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
		linha.append(completaTexto(15, recuperarMedicaoTipo(movimento.getMedicaoTipo())));
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
		linha.append(completaComZerosEsquerda(5, movimento.calcularConsumoMinimo()));
		linha.append(completaComZerosEsquerda(5, movimento.calcularConsumoMaximo()));
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
		linha.append(completaComZerosEsquerda(5, 0));
		linha.append(completaComZerosEsquerda(10, movimento.getLocalidade().getId()));
		linha.append(completaTexto(4, ""));
		linha.append(completaComZerosEsquerda(3, movimento.getCodigoQuadraFace()));

		return linha.toString();
	}

	private String recuperarMedicaoTipo(Integer medicao) {
		if (medicao != null) {
			if (medicao.intValue() == MedicaoTipo.LIGACAO_AGUA.getId()) {
				return "SO AGUA";
			} else {
				return "SO ESGOTO";
			}
		} else {
			return "";
		}
	}

	private void criarArquivo(Integer referencia, Rota rota, StringBuilder texto) {
		String ano = extrairAno(referencia).toString().substring(2, 4);
		String mes = completaComZerosEsquerda(2, extrairMes(referencia));
		String grupo = completaComZerosEsquerda(3, rota.getFaturamentoGrupo().getId());
		String local = completaComZerosEsquerda(3, rota.getSetorComercial().getLocalidade().getId());
		String setor = completaComZerosEsquerda(3, rota.getSetorComercial().getId());
		String cdRota = completaComZerosEsquerda(3, rota.getCodigo());

		String nomeArquivo = "cons" + ano + mes + "." + grupo + "." + local + "." + setor + "." + cdRota + ".txt";
		IOUtil.criarArquivoTexto(nomeArquivo, "/tmp/", texto.toString());
	}
}
