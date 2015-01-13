package br.gov.batch.servicos.faturamento.arquivo;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.FaturamentoSituacaoHistorico;
import br.gov.model.micromedicao.Hidrometro;
import br.gov.model.micromedicao.LeituraSituacao;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.model.micromedicao.MedicaoTipo;
import br.gov.model.util.Utilitarios;

public class ArquivoTextoTipo08 {

	public String build(Imovel imovel, Integer referencia) {

		StringBuilder arquivoTextoRegistroTipo08 = new StringBuilder();
		int quantidadeLinhas = 0;

		Object[] retorno = new Object[2];

		Date dataLigacao = null;

		Date dataInstalacaoHidrometro = null;

		Integer anoMesReferenciaAnterior = Utilitarios.reduzirMeses(referencia, 1);

		Collection colecaoDadosMedicaoHistorico = null; // this.getControladorMicromedicao().obterDadosTiposMedicao(imovel, anoMesReferenciaAnterior);

		if (colecaoDadosMedicaoHistorico != null && !colecaoDadosMedicaoHistorico.isEmpty()) {

			Iterator iterator = colecaoDadosMedicaoHistorico.iterator();

			while (iterator.hasNext()) {

				Object[] arrayMedicaoHistorico = (Object[]) iterator.next();
				MedicaoHistorico medicaoHistorico = new MedicaoHistorico();
				Hidrometro hidrometro = null;

				quantidadeLinhas = quantidadeLinhas + 1;

				arquivoTextoRegistroTipo08.append("08");
				arquivoTextoRegistroTipo08.append(Utilitarios.completaComZerosEsquerda(9, imovel.getId().toString()));

				MedicaoTipo medicaoTipo;

				// Leitura Inicial do Hidrometro
				Integer leituraInicialHidrometro = 0;
				if (!arrayMedicaoHistorico[3].equals(null)) {
					leituraInicialHidrometro = (Integer) arrayMedicaoHistorico[3];
				}

				if (arrayMedicaoHistorico[7] != null && !((Integer) arrayMedicaoHistorico[7]).equals(0)) {
					medicaoTipo.LIGACAO_AGUA;
					arquivoTextoRegistroTipo08.append(MedicaoTipo.LIGACAO_AGUA.toString());
				} else {
					medicaoTipoMedicaoTipo.POCO;
					arquivoTextoRegistroTipo08.append(MedicaoTipo.POCO.toString());
				}

				// NÚMERO DO HIDRÔMETRO
				if (arrayMedicaoHistorico[0] != null) {

					hidrometro = new Hidrometro();
					hidrometro.setNumero(String.valueOf(arrayMedicaoHistorico[0]));

					arquivoTextoRegistroTipo08.append(Utilitarios.completaString(hidrometro.getNumero(), 11));
				} else {
					arquivoTextoRegistroTipo08.append(Utilitarios.completaString("",11));
				}

				// DATA INSTALAÇÃO HIDRÔMETRO
				if (arrayMedicaoHistorico[2] != null) {
					dataInstalacaoHidrometro = (Date) arrayMedicaoHistorico[2];
					arquivoTextoRegistroTipo08.append(Utilitarios.formatarDataAAAAMMDD(dataInstalacaoHidrometro));
				} else {
					arquivoTextoRegistroTipo08.append(Utilitarios.completaString("", 8));
				}

				// NÚMERO DE DÍGITOS DE LEITURA
				if (arrayMedicaoHistorico[1] != null) {

					hidrometro.setNumeroDigitosLeitura((Short) arrayMedicaoHistorico[1]);
					arquivoTextoRegistroTipo08.append(hidrometro.getNumeroDigitosLeitura().toString());
				} else {
					arquivoTextoRegistroTipo08.append(Utilitarios.completaString("", 1));
				}

				// LEITURA ANTERIOR FATURADA
				Integer leituraAnteriorFaturada = (Integer) arrayMedicaoHistorico[3];
				String filtroPoTipoMedicao = "";

				// LEITURA ANTERIOR INFORMADA
				Integer leituraAnteriorInformada = null;
				if (arrayMedicaoHistorico[12] != null) {
					leituraAnteriorInformada = (Integer) arrayMedicaoHistorico[12];

				}

				if (medicaoTipo.getId().intValue() == MedicaoTipo.LIGACAO_AGUA.intValue()) {
					dataLigacao = imovel.getLigacaoAgua().getDataLigacao();
					filtroPoTipoMedicao = FiltroMedicaoHistorico.LIGACAO_AGUA_ID;
				} else {
					dataLigacao = imovel.getLigacaoEsgoto().getDataLigacao();
					filtroPoTipoMedicao = FiltroMedicaoHistorico.IMOVEL_ID;
				}

				Date dataLeituraAnteriorFaturamento = null;
				Date dataLeituraAnteriorInformada = null;

				Collection<MedicaoHistorico> colMedicaoHistoricoMenos1Mes = null; //getControladorUtil().pesquisar(filtroMedicaoHistorico, MedicaoHistorico.class.getName());
				MedicaoHistorico medicaoHistoricoAtual = (MedicaoHistorico) Utilitarios.retonarObjetoDeColecao(colMedicaoHistoricoMenos1Mes);

				if (medicaoHistoricoAtual != null && !medicaoHistoricoAtual.equals("")) {
					leituraAnteriorFaturada = medicaoHistoricoAtual.getLeituraAnteriorFaturamento();
					leituraAnteriorInformada = medicaoHistoricoAtual.getLeituraAnteriorInformada();
					dataLeituraAnteriorFaturamento = medicaoHistoricoAtual.getDataLeituraAnteriorFaturamento();
					dataLeituraAnteriorInformada = medicaoHistoricoAtual.getDataLeituraAtualInformada();
				}

				medicaoHistorico.setLeituraAnteriorFaturamento(leituraAnteriorFaturada);

				if (leituraAnteriorInformada != null) {
					medicaoHistorico.setLeituraAnteriorInformada(leituraAnteriorInformada);
				}

				arquivoTextoRegistroTipo08.append(Utilitarios.adicionarZerosEsquedaNumero(7, leituraAnteriorFaturada.toString()));

				// DATA DA LEITURA ANTERIOR FATURADA
				if (dataLeituraAnteriorFaturamento != null && !dataLeituraAnteriorFaturamento.equals("")) {
					arquivoTextoRegistroTipo08.append(Utilitarios.formatarDataAAAAMMDD(dataLeituraAnteriorFaturamento));
				} else {
					if (arrayMedicaoHistorico[4] != null) {
						arquivoTextoRegistroTipo08.append(Utilitarios.formatarDataAAAAMMDD((Date) arrayMedicaoHistorico[4]));
					} else {
						arquivoTextoRegistroTipo08.append(Utilitarios.completaString("", 8));
					}
				}

				// LEITURA SITUACAO ANTERIOR
				if (arrayMedicaoHistorico[5] != null) {

					LeituraSituacao leituraSituacao = new LeituraSituacao();
					leituraSituacao.setId((Integer) arrayMedicaoHistorico[5]);

					arquivoTextoRegistroTipo08.append(leituraSituacao.getId().toString());
					medicaoHistorico.setLeituraSituacaoAtual(leituraSituacao);
				} else {
					arquivoTextoRegistroTipo08.append(Utilitarios.completaString("", 1));
				}

				sistemaParametro.setAnoMesFaturamento(anoMesReferencia);

				boolean houveIntslacaoHidrometro = this.getControladorMicromedicao().verificarInstalacaoSubstituicaoHidrometro(imovel.getId(), medicaoTipo);
				int[] consumoMedioHidrometro = this.getControladorMicromedicao().obterVolumeMedioAguaEsgoto(imovel.getId(),anoMesReferencia, medicaoTipo.getId(), houveIntslacaoHidrometro);

				Integer[] faixaLeitura = this.obterDadosLimiteLeituraEsperada(imovel, hidrometro, consumoMedioHidrometro[0], medicaoHistorico, sistemaParametro);

				arquivoTextoRegistroTipo08.append(Utilitarios.adicionarZerosEsquedaNumero(7, faixaLeitura[0].toString()));
				arquivoTextoRegistroTipo08.append(Utilitarios.adicionarZerosEsquedaNumero(7, faixaLeitura[1].toString()));

				// CONSUMO MÉDIO HIDRÔMETRO
				arquivoTextoRegistroTipo08.append(Utilitarios.adicionarZerosEsquedaNumero(6,String.valueOf(consumoMedioHidrometro[0])));

				// DESCRIÇÂO LOCAL INSTALAÇÂO DO HIDROMETRO
				if (arrayMedicaoHistorico[9] != null) {
					arquivoTextoRegistroTipo08.append(Utilitarios.completaString("" + arrayMedicaoHistorico[9], 20));
				} else {
					arquivoTextoRegistroTipo08.append(Utilitarios.completaString("", 20));
				}

				// LEITURA ANTERIOR INFORMADA
				if (leituraAnteriorInformada != null) {
					arquivoTextoRegistroTipo08.append(Utilitarios.adicionarZerosEsquedaNumero(7,leituraAnteriorInformada.toString()));
				} else {
					arquivoTextoRegistroTipo08.append(Utilitarios.completaString("", 7));
				}

				// DATA DA LEITURA ANTERIOR INFORMADA
				if (dataLeituraAnteriorInformada != null && !dataLeituraAnteriorInformada.equals("")) {
					arquivoTextoRegistroTipo08.append(Utilitarios.formatarDataAAAAMMDD(dataLeituraAnteriorInformada));
				} else {
					if (arrayMedicaoHistorico[10] != null) {
						arquivoTextoRegistroTipo08.append(Utilitarios.formatarDataAAAAMMDD((Date) arrayMedicaoHistorico[10]));
					} else {
						arquivoTextoRegistroTipo08.append(Utilitarios.completaString("", 8));
					}
				}

				arquivoTextoRegistroTipo08.append(Utilitarios.formatarDataAAAAMMDD(dataLigacao));

				// TIPO DE RATEIO
				if (arrayMedicaoHistorico[11] != null) {
					arquivoTextoRegistroTipo08.append(((Integer) arrayMedicaoHistorico[11]).toString());
				} else {
					arquivoTextoRegistroTipo08.append(" ");
				}

				// LEITURA INSTALAÇÃO HIDROMETRO
				if (arrayMedicaoHistorico[3] != null) {
					arquivoTextoRegistroTipo08.append(Utilitarios.adicionarZerosEsquedaNumero(7,arrayMedicaoHistorico[3].toString()));
				} else {
					arquivoTextoRegistroTipo08.append(Utilitarios.completaString("", 7));
				}

				// INDICADOR PARALISAR LEITURA
				String indicadorParalisacaoLeituraHidrometroAgua = "2";
				if (imovel.getFaturamentoSituacaoTipo() != null) {

					Collection<FaturamentoSituacaoHistorico> colFiltroFaturamentoSituacaoHistorico = null; 

					FaturamentoSituacaoHistorico faturamentoSituacaoHistorico = (FaturamentoSituacaoHistorico) Utilitarios.retonarObjetoDeColecao(colFiltroFaturamentoSituacaoHistorico);

					if ((faturamentoSituacaoHistorico != null 
							&& anoMesReferencia >= faturamentoSituacaoHistorico.getAnoMesFaturamentoSituacaoInicio() 
							&& anoMesReferencia <= faturamentoSituacaoHistorico.getAnoMesFaturamentoSituacaoFim())) {

						if (imovel.getFaturamentoSituacaoTipo().getIndicadorParalisacaoLeitura().equals(new Short("1"))) {

							if ((medicaoTipo.getId().equals(MedicaoTipo.LIGACAO_AGUA) 
									&& imovel.getFaturamentoSituacaoTipo().getIndicadorValidoAgua().equals(ConstantesSistema.INDICADOR_USO_ATIVO))
									|| (medicaoTipo.getId().equals(MedicaoTipo.POCO) 
											&& imovel.getFaturamentoSituacaoTipo().getIndicadorValidoEsgoto().equals(ConstantesSistema.INDICADOR_USO_ATIVO))) {
								indicadorParalisacaoLeituraHidrometroAgua = "1";
							}
						}
					}
				}
				arquivoTextoRegistroTipo08.append(System.getProperty("line.separator"));
			}
		}

		return arquivoTextoRegistroTipo08.toString();
	}
}
