package br.gov.batch.servicos.faturamento.arquivo;

import static br.gov.model.util.Utilitarios.completaComEspacosADireita;
import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.completaTexto;
import static br.gov.model.util.Utilitarios.formataData;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.logging.Logger;

import br.gov.batch.servicos.faturamento.AguaEsgotoBO;
import br.gov.batch.servicos.faturamento.GeradorArquivoTextoFaturamento;
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
//	    logger.info("INICIO " + to.getIdImovel() + " - obter por id");
	    
		Imovel imovel = imovelRepositorio.obterPorID(to.getIdImovel());
//		logger.info("FIM " + to.getIdImovel() + " - obter por id");
		Integer anoMesReferencia = to.getAnoMesReferencia();
		
		if (imovel.isFixo()){
		    return builder.toString();
		}
		//TODO: Imovel 2731843 nao possui hidrometro de poco, como é registrado o historico de consumo para poço
		
		
//		logger.info("INICIO " + to.getIdImovel() + " - buscarPorLigacaoAguaOuPoco");
        MedicaoHistorico medicaoHistorico = medicaoHistoricoRepositorio.buscarPorLigacaoAguaOuPoco(imovel.getId(), anoMesReferencia);
//        logger.info("FIM " + to.getIdImovel() + " - buscarPorLigacaoAguaOuPoco");
        
        if (medicaoHistorico == null){
            return builder.toString();
        }

//		logger.info("INICIO " + to.getIdImovel() + " - obterDadosTiposMedicao");
		List<HidrometroMedicaoHistoricoTO> listaHidrometroMedicaoHistorico = medicaoHistoricoBO.obterDadosTiposMedicao(imovel.getId(), anoMesReferencia);
//		logger.info("FIM " + to.getIdImovel() + " - obterDadosTiposMedicao"); 
		
		for (HidrometroMedicaoHistoricoTO hidrometroMedicaoHistorico : listaHidrometroMedicaoHistorico) {
		
		    
//		    logger.info("INICIO " + to.getIdImovel() + " - getConsumoMedioHidrometro"); 
			consumoMedio = getConsumoMedioHidrometro(imovel.getId(), hidrometroMedicaoHistorico.getMedicaoTipo(), anoMesReferencia);
//			logger.info("FIM " + to.getIdImovel() + " - getConsumoMedioHidrometro");
			
			hidrometro   = getNumeroHidrometro(hidrometroMedicaoHistorico.getNumero());

//			logger.info("INICIO " + to.getIdImovel() + " - buildLinha08");
			builder.append(TIPO_REGISTRO_08_MEDICAO);
			builder.append(completaComZerosEsquerda(9, imovel.getId()));
			builder.append(hidrometroMedicaoHistorico.getMedicaoTipo());
			builder.append(completaComEspacosADireita(11, hidrometro.getNumero()));

			builder.append(getDataInstalacaoHidrometro(hidrometroMedicaoHistorico.getDataInstalacao()));
			builder.append(completaTexto(1, hidrometroMedicaoHistorico.getNumeroDigitosLeitura()));
			builder.append(completaComZerosEsquerda(7, medicaoHistorico.getLeituraAnteriorFaturamento()));
			builder.append(getDataLeituraAnteriorFaturada(medicaoHistorico, hidrometroMedicaoHistorico.getDataInstalacao()));
			builder.append(getSituacaoLeituraAtual(hidrometroMedicaoHistorico, medicaoHistorico));
			
			//logger.info("INICIO " + to.getIdImovel() + " - buildFaixaLeitura");
			buildFaixaLeitura(imovel, medicaoHistorico);
			//logger.info("FIM " + to.getIdImovel() + " - buildFaixaLeitura"); 
			
			builder.append(completaComZerosEsquerda(6, consumoMedio));
			builder.append(completaTexto(20, hidrometroMedicaoHistorico.getDescricaoLocalInstalacao()));
			builder.append(getLeituraAnteriorInformada(medicaoHistorico));
			builder.append(getDataLeituraAnteiorInformada(medicaoHistorico.getDataLeituraAtualInformada(), hidrometroMedicaoHistorico.getDataLeituraAtualInformada()));
			builder.append(formataData(getDataLigacao(imovel, hidrometroMedicaoHistorico.getMedicaoTipo()), FormatoData.ANO_MES_DIA));
			builder.append(completaTexto(1, hidrometroMedicaoHistorico.getRateioTipo()));
			builder.append(getLeituraInstalacaoHidrometro(hidrometroMedicaoHistorico.getNumeroLeituraInstalacao()));
			builder.append(System.getProperty("line.separator"));
//			logger.info("FIM " + to.getIdImovel() + " - buildLinha08");
		}

		return builder.toString();
	}

	private String getSituacaoLeituraAtual(HidrometroMedicaoHistoricoTO to, MedicaoHistorico medicaoHistorico) {
		if (to.getLeituraSituacaoAtual() != null) {
			medicaoHistorico.setLeituraSituacaoAtual(to.getLeituraSituacaoAtual());
			return to.getLeituraSituacaoAtual().toString();
		} else {
			return " ";
		}
	}

	private String getDataLeituraAnteiorInformada(Date dataLeituraAnteriorInformada, Date dataLeituraAnteriorInformadaHidrometro) {
		if (possuiDataLeituraAnteriorInformada(dataLeituraAnteriorInformada)) {
			return Utilitarios.formataData(dataLeituraAnteriorInformada, FormatoData.ANO_MES_DIA);
		} else {
			return dataLeituraAnteriorInformadaHidrometro != null ? Utilitarios.formataData(dataLeituraAnteriorInformadaHidrometro, FormatoData.ANO_MES_DIA) : Utilitarios.completaTexto(8, " ");
		}
	}

	private boolean possuiDataLeituraAnteriorInformada(Date dataLeituraAnteriorInformada) {
		return dataLeituraAnteriorInformada != null && !dataLeituraAnteriorInformada.equals("");
	}

	private int getConsumoMedioHidrometro(Integer idImovel, Integer medicaoTipo, Integer anoMesReferencia) {
		VolumeMedioAguaEsgotoTO volumeMedioAguaEsgotoTO = aguaEsgotoBO.obterVolumeMedioAguaEsgoto(idImovel, anoMesReferencia, medicaoTipo);
		return volumeMedioAguaEsgotoTO.getConsumoMedio();
	}

	private void buildFaixaLeitura(Imovel imovel, MedicaoHistorico medicaoHistorico ) {
		FaixaLeituraTO faixaLeitura = faixaLeituraBO.obterDadosFaixaLeitura(imovel, hidrometro, consumoMedio, medicaoHistorico);

		builder.append(Utilitarios.completaComZerosEsquerda(7, faixaLeitura.getFaixaInferior().toString()));
		builder.append(Utilitarios.completaComZerosEsquerda(7, faixaLeitura.getFaixaSuperior().toString()));
	}

	private String getLeituraAnteriorInformada(MedicaoHistorico medicaoHistoricoAtual) {
		return medicaoHistoricoAtual.getLeituraAnteriorInformada() != null ? Utilitarios.completaComZerosEsquerda(7, medicaoHistoricoAtual.getLeituraAnteriorInformada().toString()) : Utilitarios.completaTexto(7, " ");
	}

	private String getDataLeituraAnteriorFaturada(MedicaoHistorico medicaoHistorico, Date dataInstalacaoHidrometro) {
		if (possuiLeituraAnteriorFaturada(medicaoHistorico)) {
			return Utilitarios.formataData(medicaoHistorico.getDataLeituraAnteriorFaturamento(), FormatoData.ANO_MES_DIA);
		} else {
			return dataInstalacaoHidrometro != null ? Utilitarios.formataData(dataInstalacaoHidrometro, FormatoData.ANO_MES_DIA) : Utilitarios.completaTexto(8, " ");
		}
	}

	private boolean possuiLeituraAnteriorFaturada(MedicaoHistorico medicaoHistorico ) {
		return medicaoHistorico.getDataLeituraAnteriorFaturamento() != null && !medicaoHistorico.getDataLeituraAnteriorFaturamento().equals("");
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
