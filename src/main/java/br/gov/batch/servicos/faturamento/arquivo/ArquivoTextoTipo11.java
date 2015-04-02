package br.gov.batch.servicos.faturamento.arquivo;

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.batch.servicos.faturamento.FaturamentoAtividadeCronogramaBO;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.Status;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.FaturamentoAtividade;
import br.gov.model.micromedicao.Rota;
import br.gov.model.util.ConstantesSistema;
import br.gov.model.util.FormatoData;

import static br.gov.model.util.Utilitarios.quebraLinha;
import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.completaComEspacosADireita;
import static br.gov.model.util.Utilitarios.converteParaDataComUltimoDiaMes;
import static br.gov.model.util.Utilitarios.formatarBigDecimalComPonto;
import static br.gov.model.util.Utilitarios.formataData;
import static br.gov.model.util.Utilitarios.reduzirDias;
import static br.gov.model.util.Utilitarios.adicionarDias;
import static br.gov.model.util.Utilitarios.encriptarSenha;

@Stateless
public class ArquivoTextoTipo11 extends ArquivoTexto {

	@EJB
	private FaturamentoAtividadeCronogramaBO faturamentoAtividadeCronogramaBO;

	public ArquivoTextoTipo11() {
		super();
	}

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String build(ArquivoTextoTO to) {
		builder.append(quebraLinha);

		Rota rota = verificarRota(to.getImovel());

		builder.append(TIPO_REGISTRO_11_CODIGO_BARRAS);
		builder.append(completaComZerosEsquerda(4, sistemaParametros.getCodigoEmpresaFebraban()));
		builder.append(formataData(converteParaDataComUltimoDiaMes(sistemaParametros.getAnoMesArrecadacao()), FormatoData.ANO_MES_DIA));
		builder.append(to.getAnoMesReferencia());
		builder.append(completaComEspacosADireita(12, sistemaParametros.getNumero0800Empresa()));
		builder.append(completaComZerosEsquerda(14, sistemaParametros.getCnpjEmpresa()));
		builder.append(completaComZerosEsquerda(20, sistemaParametros.getInscricaoEstadual()));
		builder.append(completaComZerosEsquerda(14, formatarBigDecimalComPonto(sistemaParametros.getValorMinimoEmissaoConta())));
		builder.append(completaComZerosEsquerda(4, formatarBigDecimalComPonto(sistemaParametros.getPercentualToleranciaRateio())));
		builder.append(completaComZerosEsquerda(6, sistemaParametros.getDecrementoMaximoConsumoRateio()));
		builder.append(completaComZerosEsquerda(6, sistemaParametros.getIncrementoMaximoConsumoRateio()));
		builder.append(completaComZerosEsquerda(1, sistemaParametros.getIndicadorTarifaCategoria()));
		buildDadosDaRota(rota);
		builder.append(completaComEspacosADireita(10, sistemaParametros.getVersaoCelular()));
		builder.append(completaComZerosEsquerda(1, sistemaParametros.getIndicadorBloqueioContaMobile()));
		builder.append(getIndicadorSequencialLeitura(rota));
		builder.append(getDiferencaDiasCronogramas(rota));
		builder.append(getModuloDigitoVerificador(sistemaParametros.getNumeroModuloDigitoVerificador()));
		builder.append(formataData(reduzirDias(new Date(), getDiasMobileBloqueio()), FormatoData.ANO_MES_DIA));
		builder.append(formataData(adicionarDias(new Date(), getDiasMobileBloqueio()), FormatoData.ANO_MES_DIA));
		builder.append(completaComZerosEsquerda(4, (rota != null) ? rota.getId() : null));
		builder.append(completaComZerosEsquerda(2,to.getSequenciaRota()));

		return builder.toString();
	}

	private String getModuloDigitoVerificador(Short numeroModuloDigitoVerificador) {
		if (numeroModuloDigitoVerificador != null && numeroModuloDigitoVerificador.intValue() == ConstantesSistema.MODULO_VERIFICADOR_11.intValue()) {
			return completaComEspacosADireita(2, ConstantesSistema.MODULO_VERIFICADOR_11);
		} else {
			return completaComEspacosADireita(2, ConstantesSistema.MODULO_VERIFICADOR_10);
		}
	}

	private String getDiferencaDiasCronogramas(Rota rota) {
		return completaComZerosEsquerda(2,
				faturamentoAtividadeCronogramaBO.obterDiferencaDiasCronogramas(rota, FaturamentoAtividade.EFETUAR_LEITURA));
	}

	private String getIndicadorSequencialLeitura(Rota rota) {
		return (rota != null && rota.getIndicadorSequencialLeitura() != null) ? completaComZerosEsquerda(1, rota.getIndicadorSequencialLeitura())
				: completaComZerosEsquerda(1, Status.INATIVO.getId());
	}

	private Rota verificarRota(Imovel imovel) {
		return (imovel != null && imovel.getRotaAlternativa() != null) ? imovel.getRotaAlternativa() : (imovel != null && imovel.getQuadra() != null) ? imovel.getQuadra().getRota() : null;
	}

	private void buildDadosDaRota(Rota rota) {
		if (rota != null) {
			try {
				builder.append(completaComEspacosADireita(11, rota.getLeiturista().getUsuario().getLogin()));
				builder.append(completaComEspacosADireita(40, rota.getLeiturista().getUsuario().getSenha()));
			} catch (Exception e) {
				builder.append(completaComEspacosADireita(11, "gcom"));
				builder.append(completaComEspacosADireita(40, encriptarSenha("senha")));
			}
			builder.append(completaComEspacosADireita(8, formataData(rota.getDataAjusteLeitura(), FormatoData.ANO_MES_DIA)));
			builder.append(completaComEspacosADireita(1, rota.getIndicadorAjusteConsumo()));
			builder.append(completaComEspacosADireita(1, rota.getIndicadorTransmissaoOffline()));

		} else {
			builder.append(completaComEspacosADireita(10, ""));
		}
	}

	private int getDiasMobileBloqueio() {
		return (sistemaParametros.getNumeroDiasBloqueioCelular() != null) ? sistemaParametros.getNumeroDiasBloqueioCelular() : 30;
	}
}
