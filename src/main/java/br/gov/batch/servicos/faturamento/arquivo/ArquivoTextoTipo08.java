package br.gov.batch.servicos.faturamento.arquivo;

import java.util.ArrayList;
import java.util.Date;

import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.micromedicao.Hidrometro;
import br.gov.model.micromedicao.HidrometroInstalacaoHistorico;
import br.gov.model.micromedicao.LeituraSituacao;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.model.micromedicao.MedicaoTipo;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;

public class ArquivoTextoTipo08 {

//	@Inject
	private SistemaParametros sistemaParametro;

	private StringBuilder builder;
	private final String TIPO_REGISTRO = "08";
	
	public String build(Imovel imovel, Integer referencia) {

		builder = new StringBuilder();

		int quantidadeLinhas = 0;

		ArrayList<HidrometroInstalacaoHistorico> colecaoDadosMedicaoHistorico = null; // this.getControladorMicromedicao().obterDadosTiposMedicao(imovel, anoMesReferenciaAnterior);

		if (colecaoDadosMedicaoHistorico != null && !colecaoDadosMedicaoHistorico.isEmpty()) {

			for (HidrometroInstalacaoHistorico hidrometroInstalacao : colecaoDadosMedicaoHistorico ) {

				Object[] arrayMedicaoHistorico = null; 

				MedicaoHistorico medicaoHistorico = new MedicaoHistorico();
				MedicaoTipo medicaoTipo = getMedicaoTipo((Integer)arrayMedicaoHistorico[7]);
				Integer leituraInicialHidrometro = getLeituraInicialHidrometro((Integer)arrayMedicaoHistorico[3]);
				Integer leituraAnteriorFaturada = (Integer) arrayMedicaoHistorico[3];
				Date  dataLigacao = getDataLigacao(imovel, medicaoTipo);

				Hidrometro hidrometro = getNumeroHidrometro((Integer)arrayMedicaoHistorico[1]);

				sistemaParametro.setAnoMesFaturamento(referencia);
				
				quantidadeLinhas = quantidadeLinhas + 1;

				builder.append(TIPO_REGISTRO);
				builder.append(Utilitarios.completaComZerosEsquerda(9, imovel.getId().toString()));
				builder.append(medicaoTipo.toString());
				builder.append(hidrometro.getNumero());
				builder.append(getDataInstalacaoHidrometro((Date) arrayMedicaoHistorico[2]));
				builder.append(getNumeroDigitosLeitura((Short) arrayMedicaoHistorico[1]));

				// LEITURA ANTERIOR INFORMADA
				Integer leituraAnteriorInformada = null;
				if (arrayMedicaoHistorico[12] != null) {
					leituraAnteriorInformada = (Integer) arrayMedicaoHistorico[12];
				}
				
				Date dataLeituraAnteriorFaturamento = null;
				Date dataLeituraAnteriorInformada = null;

				ArrayList<MedicaoHistorico> colMedicaoHistoricoMenos1Mes = null; //getControladorUtil().pesquisar(filtroMedicaoHistorico, MedicaoHistorico.class.getName());
				MedicaoHistorico medicaoHistoricoAtual = (MedicaoHistorico) colMedicaoHistoricoMenos1Mes.get(0);

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

				builder.append(Utilitarios.completaComZerosEsquerda(7,leituraAnteriorFaturada.toString()));
				builder.append(getDataLeituraAnteriorFaturada(medicaoHistoricoAtual, (Date)arrayMedicaoHistorico[4]));
				
				// LEITURA SITUACAO ANTERIOR
				if (arrayMedicaoHistorico[5] != null) {
					LeituraSituacao leituraSituacao = new LeituraSituacao();
					leituraSituacao.setId((Integer) arrayMedicaoHistorico[5]);
					medicaoHistorico.setLeituraSituacaoAtual(leituraSituacao);

					builder.append(leituraSituacao.getId().toString());
				} else {
					builder.append(Utilitarios.completaTexto(1, ""));
				}

				buildFaixaLeitura();
				builder.append(getConsumoMedioHidrometro(imovel, medicaoTipo, referencia));
				builder.append(getLocalInstalacaoHidrometro((String)arrayMedicaoHistorico[9]));
				builder.append(getLeituraAnteriorInformada(medicaoHistoricoAtual));
				builder.append(getDataLeituraAnteiorInformada(medicaoHistoricoAtual.getDataLeituraAtualInformada(), (Date)arrayMedicaoHistorico[10]));
				builder.append(Utilitarios.formataData(dataLigacao, FormatoData.ANO_MES_DIA));
				builder.append(getTipoRateio((Integer)arrayMedicaoHistorico[11]));
				builder.append(leituraInicialHidrometro);
				builder.append(System.getProperty("line.separator"));
			}
		}

		return builder.toString();
	}

	private String getDataLeituraAnteiorInformada(Date dataLeituraAnteriorInformada, Date data2) {
		if (dataLeituraAnteriorInformada != null && !dataLeituraAnteriorInformada.equals("")) {
			return Utilitarios.formataData(dataLeituraAnteriorInformada, FormatoData.ANO_MES_DIA);
		} else {
			if (data2 != null) {
				return Utilitarios.formataData(data2, FormatoData.ANO_MES_DIA);
			} else {
				return Utilitarios.completaTexto(8, " "); 
			}
		}
	}

	private String getLocalInstalacaoHidrometro(String local) {
		if (local != null) {
			return Utilitarios.completaTexto(20, "" + local);
		} else {
			return Utilitarios.completaTexto(20, " ");
		}
	}

	private String  getConsumoMedioHidrometro(Imovel imovel, MedicaoTipo medicaoTipo, Integer referencia) {
		boolean houveIntslacaoHidrometro = false; //this.getControladorMicromedicao().verificarInstalacaoSubstituicaoHidrometro(imovel.getId(), medicaoTipo);
		int[] consumoMedioHidrometro = null; //this.getControladorMicromedicao().obterVolumeMedioAguaEsgoto(imovel.getId(),referencia, medicaoTipo.getId(), houveIntslacaoHidrometro);
		return Utilitarios.completaComZerosEsquerda(6,String.valueOf(consumoMedioHidrometro[0]));
	}

	private void buildFaixaLeitura() {
		Integer[] faixaLeitura = null; //this.obterDadosLimiteLeituraEsperada(imovel, hidrometro, consumoMedioHidrometro[0], medicaoHistorico, sistemaParametro);

		builder.append(Utilitarios.completaComZerosEsquerda(7, faixaLeitura[0].toString()));
		builder.append(Utilitarios.completaComZerosEsquerda(7, faixaLeitura[1].toString()));
	}

	private String getLeituraAnteriorInformada(MedicaoHistorico medicaoHistoricoAtual) {
		if (medicaoHistoricoAtual.getLeituraAnteriorInformada() != null) {
			return Utilitarios.completaComZerosEsquerda(7,medicaoHistoricoAtual.getLeituraAnteriorInformada().toString());
		} else {
			return Utilitarios.completaTexto(7, " ");
		}
	}

	private String  getDataLeituraAnteriorFaturada(MedicaoHistorico medicaoHistorico, Date data2) {
		if (medicaoHistorico.getDataLeituraAnteriorFaturamento() != null && !medicaoHistorico.getDataLeituraAnteriorFaturamento().equals("")) {
			return Utilitarios.formataData(medicaoHistorico.getDataLeituraAnteriorFaturamento(), FormatoData.ANO_MES_DIA);
		} else {
			if (data2 != null) {
				return Utilitarios.formataData(data2, FormatoData.ANO_MES_DIA);
			} else {
				return Utilitarios.completaTexto(8, " ");
			}
		}
	}

	private Integer getLeituraInicialHidrometro(Integer leituraInstalacao) {
		if (!leituraInstalacao.equals(null)) {
			return leituraInstalacao;
		} else {
			return 0;
		}
	}

	private Date getDataLigacao(Imovel imovel, MedicaoTipo medicaoTipo) {
		Date dataLigacao;
		if (medicaoTipo  == MedicaoTipo.LIGACAO_AGUA ) {
			dataLigacao = imovel.getLigacaoAgua().getDataLigacao();
		//	filtroPoTipoMedicao = FiltroMedicaoHistorico.LIGACAO_AGUA_ID;
		} else {
			dataLigacao = imovel.getLigacaoEsgoto().getDataLigacao();
		//	filtroPoTipoMedicao = FiltroMedicaoHistorico.IMOVEL_ID;
		}
		return dataLigacao;
		
	}

	private String getNumeroDigitosLeitura(Short numeroDigitosLeitura) {
		if (numeroDigitosLeitura != null) {
			return numeroDigitosLeitura.toString();
		} else {
			return Utilitarios.completaTexto(1, " ");
		}
	}

	private String getDataInstalacaoHidrometro(Date dataInstalacao) {
		if (dataInstalacao != null) {
			return Utilitarios.formataData(dataInstalacao, FormatoData.ANO_MES_DIA);
		} else {
			return Utilitarios.completaTexto(8, " ");
		}
	}

	private Hidrometro getNumeroHidrometro(Integer numeroHidrometro) {
		Hidrometro hidrometro = null;
		if (numeroHidrometro != null) {
			hidrometro = new Hidrometro();
			hidrometro.setNumero(String.valueOf(numeroHidrometro));

			builder.append(Utilitarios.completaTexto(11, hidrometro.getNumero()));
		} else {
			builder.append(Utilitarios.completaTexto(11, " "));
		}
		
		return hidrometro;
	}

	private MedicaoTipo getMedicaoTipo(Integer medicaoTipo) {
		if (medicaoTipo != null && !((Integer) medicaoTipo).equals(0)) {
			return MedicaoTipo.LIGACAO_AGUA;
		} else {
			return MedicaoTipo.POCO;
		}
	}

	private String getTipoRateio(Integer tipo) {
		if (tipo != null) {
			return (tipo).toString();
		} else {
			return " ";
		}
	}

	private String getLeituraInstalacaoHidrometro(HidrometroInstalacaoHistorico hidrometroInstalacao) {
		if (hidrometroInstalacao.getNumeroLeituraInstalacao() != null) {
			return Utilitarios.completaComZerosEsquerda(7,hidrometroInstalacao.getNumeroLeituraInstalacao());
		} else {
			return Utilitarios.completaTexto(7, " ");
		}
	}
}
