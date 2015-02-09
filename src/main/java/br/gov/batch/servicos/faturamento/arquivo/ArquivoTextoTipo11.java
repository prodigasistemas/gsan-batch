package br.gov.batch.servicos.faturamento.arquivo;

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.faturamento.FaturamentoAtividadeCronogramaBO;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.Status;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.FaturamentoAtividade;
import br.gov.model.micromedicao.Rota;
import br.gov.model.util.ConstantesSistema;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;

@Stateless
public class ArquivoTextoTipo11 extends ArquivoTexto {

	@EJB
	private FaturamentoAtividadeCronogramaBO faturamentoAtividadeCronogramaBO;
	
	public ArquivoTextoTipo11() {
		super();
	}

	public String build(ArquivoTextoTO to) {
		Rota rota = verificarRota(to.getImovel());

		builder.append(TIPO_REGISTRO_11);
		builder.append(Utilitarios.completaComZerosEsquerda(4, sistemaParametros.getCodigoEmpresaFebraban()));
		builder.append(Utilitarios.formataData(Utilitarios.converteParaDataComUltimoDiaMes(sistemaParametros.getAnoMesArrecadacao()), FormatoData.ANO_MES_DIA));
		builder.append(to.getAnoMesReferencia());
		builder.append(Utilitarios.completaComEspacosADireita(12, sistemaParametros.getNumero0800Empresa()));
		builder.append(Utilitarios.completaComZerosEsquerda(14, sistemaParametros.getCnpjEmpresa()));
		builder.append(Utilitarios.completaComZerosEsquerda(20, sistemaParametros.getInscricaoEstadual()));
		builder.append(Utilitarios.completaComZerosEsquerda(14, Utilitarios.formatarBigDecimalComPonto(sistemaParametros.getValorMinimoEmissaoConta())));
		builder.append(Utilitarios.completaComZerosEsquerda(4, Utilitarios.formatarBigDecimalComPonto(sistemaParametros.getPercentualToleranciaRateio())));
		builder.append(Utilitarios.completaComZerosEsquerda(6, sistemaParametros.getDecrementoMaximoConsumoRateio()));
		builder.append(Utilitarios.completaComZerosEsquerda(6, sistemaParametros.getIncrementoMaximoConsumoRateio()));
		builder.append(Utilitarios.completaComZerosEsquerda(1, sistemaParametros.getIndicadorTarifaCategoria()));
		buildDadosDaRota(rota);
		builder.append(Utilitarios.completaComEspacosADireita(10, sistemaParametros.getVersaoCelular()));
		builder.append(Utilitarios.completaComZerosEsquerda(1, sistemaParametros.getIndicadorBloqueioContaMobile()));
		builder.append((rota != null && rota.getIndicadorSequencialLeitura() != null) ? Utilitarios.completaComZerosEsquerda(1, rota.getIndicadorSequencialLeitura()) : 
				Utilitarios.completaComZerosEsquerda(1, Status.INATIVO.getId()));
		builder.append(Utilitarios.completaComZerosEsquerda(2, faturamentoAtividadeCronogramaBO.obterDiferencaDiasCronogramas(rota, FaturamentoAtividade.EFETUAR_LEITURA)));
		builder.append(Utilitarios.completaComEspacosADireita(2, (sistemaParametros.getNumeroModuloDigitoVerificador() != null 
				&& sistemaParametros.getNumeroModuloDigitoVerificador().compareTo(ConstantesSistema.MODULO_VERIFICADOR_11) == 0) ? 
						ConstantesSistema.MODULO_VERIFICADOR_11 : ConstantesSistema.MODULO_VERIFICADOR_10));
		builder.append(Utilitarios.formataData(Utilitarios.reduzirDias(new Date(), getDiasMobileBloqueio()), FormatoData.ANO_MES_DIA));
		builder.append(Utilitarios.formataData(Utilitarios.adicionarDias(new Date(), getDiasMobileBloqueio()), FormatoData.ANO_MES_DIA));
		builder.append(Utilitarios.completaComZerosEsquerda(4, (rota != null) ? rota.getId() : null));
		builder.append(getIndicadorSequencialLeitura(rota));
		builder.append(getDiferencaDiasCronogramas(rota));
		builder.append(getModuloDigitoVerificador(sistemaParametros.getNumeroModuloDigitoVerificador()));
		builder.append(Utilitarios.formataData(Utilitarios.reduzirDias(new Date(), getDiasMobileBloqueio()), FormatoData.ANO_MES_DIA));
		builder.append(Utilitarios.formataData(Utilitarios.adicionarDias(new Date(), getDiasMobileBloqueio()), FormatoData.ANO_MES_DIA));
		builder.append(Utilitarios.completaComZerosEsquerda(4, (rota != null) ? rota.getId() : null));
		builder.append(Utilitarios.completaComZerosEsquerda(2, to.getSequenciaRota()));

		return builder.toString();
	}

	private String getModuloDigitoVerificador(Short numeroModuloDigitoVerificador) {
		if (numeroModuloDigitoVerificador != null && numeroModuloDigitoVerificador.intValue() == ConstantesSistema.MODULO_VERIFICADOR_11.intValue()) {
			return Utilitarios.completaComEspacosADireita(2, ConstantesSistema.MODULO_VERIFICADOR_11);
		} else {
			return Utilitarios.completaComEspacosADireita(2, ConstantesSistema.MODULO_VERIFICADOR_10);
		}
	}

	private String getDiferencaDiasCronogramas(Rota rota) {
		return Utilitarios.completaComZerosEsquerda(2, faturamentoAtividadeCronogramaBO.obterDiferencaDiasCronogramas(rota, FaturamentoAtividade.EFETUAR_LEITURA));
	}

	private String getIndicadorSequencialLeitura(Rota rota) {
		return (rota != null && rota.getIndicadorSequencialLeitura() != null) ? Utilitarios.completaComZerosEsquerda(1, rota.getIndicadorSequencialLeitura()) :
				Utilitarios.completaComZerosEsquerda(1, Status.INATIVO.getId());
	}

	private Rota verificarRota(Imovel imovel) {
		return (imovel != null && imovel.getRotaAlternativa() != null) ? imovel.getRotaAlternativa() : (imovel != null && imovel.getQuadra() != null) ? imovel.getQuadra().getRota() : null;
	}

	private void buildDadosDaRota(Rota rota) {
		if (rota != null) {
			try {
				builder.append(Utilitarios.completaComEspacosADireita(11, rota.getLeiturista().getUsuario().getLogin()));
				builder.append(Utilitarios.completaComEspacosADireita(40, rota.getLeiturista().getUsuario().getSenha()));
			} catch (Exception e) {
				builder.append(Utilitarios.completaComEspacosADireita(11, "gcom"));
				builder.append(Utilitarios.completaComEspacosADireita(40, Utilitarios.encriptarSenha("senha")));
			}
			builder.append(Utilitarios.completaComEspacosADireita(8, Utilitarios.formataData(rota.getDataAjusteLeitura(), FormatoData.ANO_MES_DIA)));
			builder.append(Utilitarios.completaComEspacosADireita(1, rota.getIndicadorAjusteConsumo()));
			builder.append(Utilitarios.completaComEspacosADireita(1, rota.getIndicadorTransmissaoOffline()));

		} else {
			builder.append(Utilitarios.completaComEspacosADireita(10, ""));
		}
	}

	private int getDiasMobileBloqueio() {
		return (sistemaParametros.getNumeroDiasBloqueioCelular() != null) ? sistemaParametros.getNumeroDiasBloqueioCelular() : 30;
	}
}
