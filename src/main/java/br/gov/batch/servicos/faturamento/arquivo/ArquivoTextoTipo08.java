package br.gov.batch.servicos.faturamento.arquivo;

import java.util.Date;
import java.util.List;

import br.gov.batch.servicos.micromedicao.FaixaLeituraBO;
import br.gov.batch.servicos.micromedicao.MedicaoHistoricoBO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.micromedicao.Hidrometro;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.model.micromedicao.MedicaoTipo;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.micromedicao.to.FaixaLeituraTO;
import br.gov.servicos.to.HidrometroMedicaoHistoricoTO;

public class ArquivoTextoTipo08 {

	// @Inject
	private SistemaParametros sistemaParametro;

	
	// @EJB
	private FaixaLeituraBO faixaLeituraBO;
	
	// @EJB
	private MedicaoHistoricoBO medicaoHistoricoBO; 

	// @EJB
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorio;
	
	private StringBuilder builder;
	private final String TIPO_REGISTRO = "08";
	
	public String build(Imovel imovel, Integer referencia) {

		builder = new StringBuilder();

		int quantidadeLinhas = 0;

		List<HidrometroMedicaoHistoricoTO> listaHidrometroMedicaoHistorico = medicaoHistoricoBO.obterDadosTiposMedicao(imovel.getId(), referencia);

		for (HidrometroMedicaoHistoricoTO hidrometroMedicaoHistorico : listaHidrometroMedicaoHistorico) {

			MedicaoTipo medicaoTipo = getMedicaoTipo(hidrometroMedicaoHistorico.getMedicaoTipo()); // GET MEDICAO TIPO
			Integer consumoMedio = getConsumoMedioHidrometro(imovel, medicaoTipo, referencia);
			Hidrometro hidrometro = getNumeroHidrometro(hidrometroMedicaoHistorico.getNumero());

			sistemaParametro.setAnoMesFaturamento(referencia);

			quantidadeLinhas = quantidadeLinhas + 1;

			MedicaoHistorico medicaoHistoricoAtual = medicaoHistoricoRepositorio.buscarPorLigacaoAguaOuPoco(imovel.getId(), referencia);
			MedicaoHistorico medicaoHistorico = obterMedicaoHistorico(medicaoHistoricoAtual);

			builder.append(TIPO_REGISTRO);
			builder.append(Utilitarios.completaComZerosEsquerda(9, imovel.getId().toString()));
			builder.append(medicaoTipo.toString());
			builder.append(hidrometro.getNumero());
			builder.append(getDataInstalacaoHidrometro(hidrometroMedicaoHistorico.getDataInstalacao()));
			builder.append(getNumeroDigitosLeitura(hidrometroMedicaoHistorico.getNumeroDigitosLeitura()));
			builder.append(Utilitarios.completaComZerosEsquerda(7, medicaoHistoricoAtual.getLeituraAnteriorFaturamento().toString()));
			builder.append(getDataLeituraAnteriorFaturada(medicaoHistoricoAtual, hidrometroMedicaoHistorico.getDataInstalacao()));
			builder.append(getSituacaoLeituraAtual(hidrometroMedicaoHistorico, medicaoHistorico));
			buildFaixaLeitura(imovel, hidrometro, consumoMedio, medicaoHistorico);
			builder.append(Utilitarios.completaComZerosEsquerda(6, consumoMedio));
			builder.append(getLocalInstalacaoHidrometro(hidrometroMedicaoHistorico.getDescricaoLocalInstalacao()));
			builder.append(getLeituraAnteriorInformada(medicaoHistoricoAtual));
			builder.append(getDataLeituraAnteiorInformada(medicaoHistoricoAtual.getDataLeituraAtualInformada(), hidrometroMedicaoHistorico.getDataLeituraAtualInformada()));
			builder.append(Utilitarios.formataData(getDataLigacao(imovel, medicaoTipo), FormatoData.ANO_MES_DIA));
			builder.append(getTipoRateio(hidrometroMedicaoHistorico.getRateioTipo()));
			builder.append(getLeituraInstalacaoHidrometro(hidrometroMedicaoHistorico.getNumeroLeituraInstalacao()));
			builder.append(System.getProperty("line.separator"));
		}

		return builder.toString();
	}

	private MedicaoHistorico obterMedicaoHistorico(MedicaoHistorico medicaoHistoricoAtual) {
		MedicaoHistorico medicaoHistorico = new MedicaoHistorico();
		medicaoHistorico.setLeituraAnteriorFaturamento(medicaoHistoricoAtual.getLeituraAnteriorFaturamento());

		if (medicaoHistoricoAtual.getLeituraAnteriorInformada() != null) {
			medicaoHistorico.setLeituraAnteriorInformada(medicaoHistoricoAtual.getLeituraAnteriorInformada());
		}
		
		return medicaoHistorico;
	}

	private String getSituacaoLeituraAtual(HidrometroMedicaoHistoricoTO hidrometroMedicaoHistorico, MedicaoHistorico medicaoHistorico) {
		if (hidrometroMedicaoHistorico.getLeituraSituacaoAtual() != null) {
			medicaoHistorico.setLeituraSituacaoAtual(hidrometroMedicaoHistorico.getLeituraSituacaoAtual());
			return hidrometroMedicaoHistorico.getLeituraSituacaoAtual().toString();
		} else {
			return Utilitarios.completaTexto(1, "");
		}
	}

	private Integer obterLeituraAnteriorInformada(HidrometroMedicaoHistoricoTO hidrometroMedicaoHistorico) {
		Integer leituraAnteriorInformada = null;
		if (hidrometroMedicaoHistorico.getLeituraAtualInformada() != null) {
			leituraAnteriorInformada = hidrometroMedicaoHistorico.getLeituraAtualInformada();
		}
		return leituraAnteriorInformada;
	}

	private String getDataLeituraAnteiorInformada(Date dataLeituraAnteriorInformada, Date dataLeituraAnteriorInformadaHidrometro) {
		if (possuiDataLeituraAnteriorInformada(dataLeituraAnteriorInformada)) {
			return Utilitarios.formataData(dataLeituraAnteriorInformada, FormatoData.ANO_MES_DIA);
		} else {
			return dataLeituraAnteriorInformadaHidrometro != null ? 
					Utilitarios.formataData(dataLeituraAnteriorInformadaHidrometro, FormatoData.ANO_MES_DIA) : Utilitarios.completaTexto(8, " ");
		}
	}

	private boolean possuiDataLeituraAnteriorInformada(Date dataLeituraAnteriorInformada) {
		return dataLeituraAnteriorInformada != null && !dataLeituraAnteriorInformada.equals("");
	}

	private String getLocalInstalacaoHidrometro(String local) {
		return local != null ? Utilitarios.completaTexto(20, "" + local) : Utilitarios.completaTexto(20, " ");
	}

	private int getConsumoMedioHidrometro(Imovel imovel, MedicaoTipo medicaoTipo, Integer referencia) {
		boolean houveIntslacaoHidrometro = false; //this.getControladorMicromedicao().verificarInstalacaoSubstituicaoHidrometro(imovel.getId(), medicaoTipo);
		int[] consumoMedioHidrometro = null; //this.getControladorMicromedicao().obterVolumeMedioAguaEsgoto(imovel.getId(),referencia, medicaoTipo.getId(), houveIntslacaoHidrometro);
		return consumoMedioHidrometro[0];
	}

	private void buildFaixaLeitura(Imovel imovel, Hidrometro hidrometro, Integer consumoMedio, MedicaoHistorico medicaoHistorico) {
		FaixaLeituraTO faixaLeitura = faixaLeituraBO.obterDadosFaixaLeitura(imovel, hidrometro, consumoMedio, medicaoHistorico);

		builder.append(Utilitarios.completaComZerosEsquerda(7, faixaLeitura.getFaixaInferior().toString()));
		builder.append(Utilitarios.completaComZerosEsquerda(7, faixaLeitura.getFaixaSuperior().toString()));
	}

	private String getLeituraAnteriorInformada(MedicaoHistorico medicaoHistorico) {
		return medicaoHistorico.getLeituraAnteriorInformada() != null ? 
				Utilitarios.completaComZerosEsquerda(7,medicaoHistorico.getLeituraAnteriorInformada().toString()) : Utilitarios.completaTexto(7, " ");
	}

	private String  getDataLeituraAnteriorFaturada(MedicaoHistorico medicaoHistorico, Date dataInstalacaoHidrometro) {
		if (possuiLeituraAnteriorFaturada(medicaoHistorico)) {
			return Utilitarios.formataData(medicaoHistorico.getDataLeituraAnteriorFaturamento(), FormatoData.ANO_MES_DIA);
		} else {
			return dataInstalacaoHidrometro != null ? Utilitarios.formataData(dataInstalacaoHidrometro, FormatoData.ANO_MES_DIA) : Utilitarios.completaTexto(8, " ");
		}
	}

	private boolean possuiLeituraAnteriorFaturada(MedicaoHistorico medicaoHistorico) {
		return medicaoHistorico.getDataLeituraAnteriorFaturamento() != null && !medicaoHistorico.getDataLeituraAnteriorFaturamento().equals("");
	}

	private String getLeituraInstalacaoHidrometro(Integer leituraInstalacao) {
		return leituraInstalacao != null ? Utilitarios.completaComZerosEsquerda(7,leituraInstalacao) : Utilitarios.completaTexto(7, " ");
	}
	
	private Date getDataLigacao(Imovel imovel, MedicaoTipo medicaoTipo) {
		return medicaoTipo  == MedicaoTipo.LIGACAO_AGUA ? imovel.getLigacaoAgua().getDataLigacao() : imovel.getLigacaoEsgoto().getDataLigacao();
	}

	private String getNumeroDigitosLeitura(Short numeroDigitosLeitura) {
		return numeroDigitosLeitura != null ? numeroDigitosLeitura.toString() : Utilitarios.completaTexto(1, " ");
	}

	private String getDataInstalacaoHidrometro(Date dataInstalacao) {
		return dataInstalacao != null ? Utilitarios.formataData(dataInstalacao, FormatoData.ANO_MES_DIA) : Utilitarios.completaTexto(8, " ");
	}

	private Hidrometro getNumeroHidrometro(String numeroHidrometro) {
		Hidrometro hidrometro = null;
		if (numeroHidrometro != null) {
			hidrometro = new Hidrometro();
			hidrometro.setNumero(numeroHidrometro);

			builder.append(Utilitarios.completaTexto(11, hidrometro.getNumero()));
		} else {
			builder.append(Utilitarios.completaTexto(11, " "));
		}
		
		return hidrometro;
	}

	private MedicaoTipo getMedicaoTipo(Integer medicaoTipo) {
		return (medicaoTipo != null && !((Integer) medicaoTipo).equals(0)) ? MedicaoTipo.LIGACAO_AGUA : MedicaoTipo.POCO;
	}

	private String getTipoRateio(Integer tipo) {
		return tipo != null ? (tipo).toString() : " ";
	}
}
