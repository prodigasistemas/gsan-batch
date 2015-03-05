package br.gov.batch.servicos.faturamento.arquivo;

import static br.gov.model.util.Utilitarios.completaComEspacosADireita;
import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.completaTexto;
import static br.gov.model.util.Utilitarios.formataData;
import static br.gov.model.util.Utilitarios.reduzirMeses;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.logging.Logger;

import br.gov.batch.servicos.faturamento.AguaEsgotoBO;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.batch.servicos.faturamento.to.VolumeMedioAguaEsgotoTO;
import br.gov.batch.servicos.micromedicao.FaixaLeituraBO;
import br.gov.batch.servicos.micromedicao.HidrometroBO;
import br.gov.batch.servicos.micromedicao.MedicaoHistoricoBO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.micromedicao.Hidrometro;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.model.micromedicao.MedicaoTipo;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.micromedicao.to.FaixaLeituraTO;
import br.gov.servicos.to.HidrometroMedicaoHistoricoTO;

@Stateless
public class ArquivoTextoTipo08 extends ArquivoTexto {
    private static Logger logger = Logger.getLogger(ArquivoTextoTipo08.class);
	@EJB
	private FaixaLeituraBO faixaLeituraBO;

	@EJB
	private HidrometroBO hidrometroBO;

	@EJB
	private AguaEsgotoBO aguaEsgotoBO;

	@EJB
	private MedicaoHistoricoBO medicaoHistoricoBO;

	@EJB
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorio;
	
	@EJB
	private ImovelRepositorio imovelRepositorio;

	private Hidrometro hidrometro;
	private Integer consumoMedio;

	public ArquivoTextoTipo08() {
		super();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String build(ArquivoTextoTO to) {
	    
		Imovel imovel = imovelRepositorio.obterPorID(to.getIdImovel());
		Integer anoMesReferencia = to.getAnoMesReferencia();
		
		if (imovel.isFixo()){
		    return builder.toString();
		}
		//TODO: Imovel 2731843 nao possui hidrometro de poco, como é registrado o historico de consumo para poço
        MedicaoHistorico medicaoHistorico = medicaoHistoricoRepositorio.buscarPorLigacaoAguaOuPoco(imovel.getId(), reduzirMeses(anoMesReferencia, 1));
		List<HidrometroMedicaoHistoricoTO> listaHidrometroMedicaoHistorico = medicaoHistoricoBO.obterDadosTiposMedicao(imovel.getId(), anoMesReferencia);
		
		for (HidrometroMedicaoHistoricoTO hidrometroMedicaoHistorico : listaHidrometroMedicaoHistorico) {
		    
			consumoMedio = getConsumoMedioHidrometro(imovel.getId(), hidrometroMedicaoHistorico.getMedicaoTipo(), anoMesReferencia);
			hidrometro   = getNumeroHidrometro(hidrometroMedicaoHistorico.getNumero());

			builder.append(TIPO_REGISTRO_08_MEDICAO);
			builder.append(completaComZerosEsquerda(9, imovel.getId()));
			builder.append(hidrometroMedicaoHistorico.getMedicaoTipo());
			builder.append(completaComEspacosADireita(11, hidrometro.getNumero()));

			builder.append(getDataInstalacaoHidrometro(hidrometroMedicaoHistorico.getDataInstalacao()));
			builder.append(completaTexto(1, hidrometroMedicaoHistorico.getNumeroDigitosLeitura()));
			//builder.append(completaComZerosEsquerda(7, medicaoHistorico.getLeituraAtualFaturamento()));
			builder.append(completaComZerosEsquerda(7, getLeituraFaturada(medicaoHistorico, hidrometroMedicaoHistorico)));
			builder.append(getDataLeituraFaturada(medicaoHistorico, hidrometroMedicaoHistorico.getDataInstalacao()));
			builder.append(getSituacaoLeituraAtual(hidrometroMedicaoHistorico, medicaoHistorico));
			
			buildFaixaLeitura(imovel, medicaoHistorico, hidrometroMedicaoHistorico);
			
			builder.append(completaComZerosEsquerda(6, consumoMedio));
			builder.append(completaComEspacosADireita(20, hidrometroMedicaoHistorico.getDescricaoLocalInstalacao()));
			builder.append(getLeituraInformada(medicaoHistorico));
			builder.append(getDataLeituraInformada(medicaoHistorico, hidrometroMedicaoHistorico.getDataLeituraAtualInformada()));
			builder.append(formataData(getDataLigacao(imovel, hidrometroMedicaoHistorico.getMedicaoTipo()), FormatoData.ANO_MES_DIA));
			builder.append(completaTexto(1, hidrometroMedicaoHistorico.getRateioTipo()));
			builder.append(getLeituraInstalacaoHidrometro(hidrometroMedicaoHistorico.getNumeroLeituraInstalacao()));
			builder.append(System.getProperty("line.separator"));
		}

		return builder.toString();
	}

	private String getSituacaoLeituraAtual(HidrometroMedicaoHistoricoTO to, MedicaoHistorico medicaoHistorico) {
		if (to.getLeituraSituacaoAtual() != null) {
			return to.getLeituraSituacaoAtual().toString();
		} else {
			return " ";
		}
	}

	private String getDataLeituraInformada(MedicaoHistorico medicaoHistorico, Date dataLeituraAnteriorInformadaHidrometro) {
		if (possuiDataLeituraAnteriorInformada(medicaoHistorico)) {
			return Utilitarios.formataData(medicaoHistorico.getDataLeituraAtualInformada(), FormatoData.ANO_MES_DIA);
		} else {
			return dataLeituraAnteriorInformadaHidrometro != null ? Utilitarios.formataData(dataLeituraAnteriorInformadaHidrometro, FormatoData.ANO_MES_DIA) : Utilitarios.completaTexto(8, " ");
		}
	}

	private boolean possuiDataLeituraAnteriorInformada(MedicaoHistorico medicaoHistorico) {
		return medicaoHistorico != null && medicaoHistorico.getDataLeituraAtualInformada() != null && !medicaoHistorico.getDataLeituraAtualInformada().equals("");
	}

	private int getConsumoMedioHidrometro(Integer idImovel, Integer medicaoTipo, Integer anoMesReferencia) {
		VolumeMedioAguaEsgotoTO volumeMedioAguaEsgotoTO = aguaEsgotoBO.obterVolumeMedioAguaEsgoto(idImovel, anoMesReferencia, medicaoTipo);
		return volumeMedioAguaEsgotoTO.getConsumoMedio();
	}

	private void buildFaixaLeitura(Imovel imovel, MedicaoHistorico medicaoHistorico, HidrometroMedicaoHistoricoTO hidrometroTO) {
		
		if (medicaoHistorico == null) {
			medicaoHistorico = buildMedicaoHistoricoParaFaixaLeitura(hidrometroTO);
		}
		FaixaLeituraTO faixaLeitura = faixaLeituraBO.obterDadosFaixaLeitura(imovel, hidrometro, consumoMedio, medicaoHistorico);

		builder.append(Utilitarios.completaComZerosEsquerda(7, faixaLeitura.getFaixaInferior().toString()));
		builder.append(Utilitarios.completaComZerosEsquerda(7, faixaLeitura.getFaixaSuperior().toString()));
	}
	
	private MedicaoHistorico buildMedicaoHistoricoParaFaixaLeitura(HidrometroMedicaoHistoricoTO hidrometroTO) {
		MedicaoHistorico medicao = new MedicaoHistorico();
		
		medicao.setLeituraAtualFaturamento(getLeituraFaturada(null, hidrometroTO));
		medicao.setDataLeituraAtualFaturamento(hidrometroTO.getDataInstalacao());
		
		return medicao;
	}

	private String getLeituraInformada(MedicaoHistorico medicaoHistorico) {
		return (medicaoHistorico != null && medicaoHistorico.getLeituraAtualInformada() != null) ? Utilitarios.completaComZerosEsquerda(7, medicaoHistorico.getLeituraAtualInformada().toString()) : Utilitarios.completaTexto(7, " ");
	}

	private Integer getLeituraFaturada(MedicaoHistorico medicaoHistorico, HidrometroMedicaoHistoricoTO hidrometroTO) {
		if (possuiLeituraFaturada(medicaoHistorico)) {
			return medicaoHistorico.getLeituraAtualFaturamento();
		} else {
			return hidrometroTO.getNumeroLeituraInstalacao();
		}
	}
	
	private boolean possuiLeituraFaturada(MedicaoHistorico medicaoHistorico ) {
		return medicaoHistorico != null && medicaoHistorico.getLeituraAtualFaturamento() != null && !medicaoHistorico.getLeituraAtualFaturamento().equals("");
	}
	
	private String getDataLeituraFaturada(MedicaoHistorico medicaoHistorico, Date dataInstalacaoHidrometro) {
		if (possuiDataLeituraFaturada(medicaoHistorico)) {
			return Utilitarios.formataData(medicaoHistorico.getDataLeituraAtualFaturamento(), FormatoData.ANO_MES_DIA);
		} else {
			return dataInstalacaoHidrometro != null ? Utilitarios.formataData(dataInstalacaoHidrometro, FormatoData.ANO_MES_DIA) : Utilitarios.completaTexto(8, " ");
		}
	}

	private boolean possuiDataLeituraFaturada(MedicaoHistorico medicaoHistorico ) {
		return medicaoHistorico != null && medicaoHistorico.getDataLeituraAtualFaturamento() != null && !medicaoHistorico.getDataLeituraAtualFaturamento().equals("");
	}

	private String getLeituraInstalacaoHidrometro(Integer leituraInstalacao) {
		return leituraInstalacao != null ? Utilitarios.completaComZerosEsquerda(7, leituraInstalacao) : Utilitarios.completaTexto(7, " ");
	}

	private Date getDataLigacao(Imovel imovel, Integer medicaoTipo) {
		return medicaoTipo.intValue() == MedicaoTipo.LIGACAO_AGUA.getId() ? imovel.getLigacaoAgua().getDataLigacao() : imovel.getLigacaoEsgoto().getDataLigacao();
	}

	private String getDataInstalacaoHidrometro(Date dataInstalacao) {
		return dataInstalacao != null ? Utilitarios.formataData(dataInstalacao, FormatoData.ANO_MES_DIA) : Utilitarios.completaTexto(8, " ");
	}

	private Hidrometro getNumeroHidrometro(String numeroHidrometro) {
		Hidrometro hidrometro = null;
		if (numeroHidrometro != null) {
			hidrometro = new Hidrometro();
			hidrometro.setNumero(numeroHidrometro);
		}

		return hidrometro;
	}
}
