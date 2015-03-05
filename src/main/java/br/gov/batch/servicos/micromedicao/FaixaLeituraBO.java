package br.gov.batch.servicos.micromedicao;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.micromedicao.FaixaLeituraEsperadaParametros;
import br.gov.model.micromedicao.Hidrometro;
import br.gov.model.micromedicao.LeituraSituacao;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.model.micromedicao.Rota;
import br.gov.model.micromedicao.StatusFaixaFalsa;
import br.gov.model.micromedicao.StatusUsoFaixaFalsa;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaCategoriaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaVigenciaRepositorio;
import br.gov.servicos.micromedicao.FaixaLeituraRepositorio;
import br.gov.servicos.micromedicao.to.FaixaLeituraTO;
import br.gov.servicos.to.ConsumoTarifaCategoriaTO;
import br.gov.servicos.to.ConsumoTarifaVigenciaTO;

@Stateless
public class FaixaLeituraBO {

	private SistemaParametros sistemaParametro;
		
	 @EJB
	private SistemaParametrosRepositorio sistemaParametrosRepositorio;
	 
	 @EJB
	 private ConsumoTarifaRepositorio consumoTarifaRepositorio;
	
	 @EJB
	private ConsumoTarifaVigenciaRepositorio consumoTarifaVigenciaRepositorio;
	
	 @EJB
	private ConsumoTarifaCategoriaRepositorio consumoTarifaCategoriaRepositorio;

	 @EJB
	private FaixaLeituraRepositorio faixaLeituraRepositorio;
	 
	 @PostConstruct
	 public void init(){
	     sistemaParametro = sistemaParametrosRepositorio.getSistemaParametros();
	 }
	
	 @TransactionAttribute(TransactionAttributeType.REQUIRED)
	 public FaixaLeituraTO obterDadosFaixaLeitura(Imovel imovel, Hidrometro hidrometro, Integer consumoMedioHidrometro, MedicaoHistorico medicaoHistorico) {
		
		if (hidrometro == null) {
			return new FaixaLeituraTO(0, 0);
		} else {
			FaixaLeituraTO faixaLeituraEsperada = this.calcularFaixaLeituraEsperada(consumoMedioHidrometro, medicaoHistorico, hidrometro, medicaoHistorico.getLeituraAtualFaturamento());

			if (isGerarFaixaNormal(imovel)) {
				return faixaLeituraEsperada;
			} else {
				FaixaLeituraTO faixaLeituraFalsa = this.calcularFaixaLeituraFalsa(imovel, consumoMedioHidrometro.intValue(), 
						medicaoHistorico.getLeituraAtualFaturamento(),medicaoHistorico, true, hidrometro);
				if (faixaLeituraFalsa.isHidrometroSelecionado()) {
					return faixaLeituraFalsa;
				} else {
					return faixaLeituraEsperada;
				}
			}
		}
	}
	
	public FaixaLeituraTO calcularFaixaLeituraEsperada(int media,MedicaoHistorico medicaoHistorico, Hidrometro hidrometro, Integer leituraAnteriorPesquisada) {

		BigDecimal faixaInicial = null;
		BigDecimal faixaFinal = null;
		BigDecimal leituraAnterior = null;

		leituraAnterior = obterLeituraAnteriorParaCalculoFaixa(medicaoHistorico, leituraAnteriorPesquisada);

		BigDecimal mediaConsumo = new BigDecimal(media);
		
		List<FaixaLeituraEsperadaParametros> faixaLeituraParametros = faixaLeituraRepositorio.obterFaixasLeitura();

		for (FaixaLeituraEsperadaParametros faixaLeitura : faixaLeituraParametros) {
			
			if (faixaLeitura.getMediaInicial() == 0) {
				faixaInicial = leituraAnterior;
				faixaFinal = leituraAnterior.add(mediaConsumo).add(new BigDecimal(faixaLeitura.getMediaFinal()));
			} else{
				if (media >= faixaLeitura.getMediaInicial() && media <= faixaLeitura.getMediaFinal() ) {
					faixaInicial = leituraAnterior.add((faixaLeitura.getFatorFaixaInicial()).multiply(mediaConsumo));
					faixaFinal = leituraAnterior.add((faixaLeitura.getFatorFaixaFinal()).multiply(mediaConsumo));
				
				}
			}
		}
		
		FaixaLeituraTO faixaLeitura =  new FaixaLeituraTO(Utilitarios.arredondarParaCima(faixaInicial), Utilitarios.arredondarParaCima(faixaFinal));

		return verificarViradaHidrometroFaixaEsperada(hidrometro,faixaLeitura);
	}

	private BigDecimal obterLeituraAnteriorParaCalculoFaixa(MedicaoHistorico medicaoHistorico, Integer leituraAnteriorPesquisada) {
		BigDecimal leituraAnterior;
		if (leituraAnteriorPesquisada == null) {
			leituraAnterior = new BigDecimal(obterLeituraAnterior(medicaoHistorico));
		} else {
			leituraAnterior = new BigDecimal(leituraAnteriorPesquisada);
		}
		return leituraAnterior;
	}
	
	private int obterLeituraAnterior(MedicaoHistorico medicaoHistorico) {

		if (medicaoHistorico.possuiLeituraInformada()) {
			if (medicaoHistorico.getLeituraAnteriorInformada().intValue() == medicaoHistorico.getLeituraAtualInformada().intValue()) {
				return medicaoHistorico.getLeituraAnteriorInformada();
			} else {
				return medicaoHistorico.getLeituraAnteriorFaturamento();
			}
		} else {
			return medicaoHistorico.getLeituraAnteriorFaturamento();
		}
	}
	
	private FaixaLeituraTO verificarViradaHidrometroFaixaEsperada(Hidrometro hidrometro, FaixaLeituraTO faixaLeitura) {

		if (hidrometro != null && hidrometro.possuiNumeroDigitosLeitura()) {

			Double leituraMaximaHidrometro = hidrometro.getLeituraMaxima();
			int valorDigitos = leituraMaximaHidrometro.intValue() - 1;

			if (faixaLeitura.getFaixaInferior() > valorDigitos) {
				faixaLeitura.setFaixaInferior(faixaLeitura.getFaixaInferior() - leituraMaximaHidrometro.intValue());
			}

			if (faixaLeitura.getFaixaSuperior() > valorDigitos) {
				faixaLeitura.setFaixaSuperior(faixaLeitura.getFaixaSuperior() - leituraMaximaHidrometro.intValue());
			}
		}
		return faixaLeitura;
	}
	
	public FaixaLeituraTO calcularFaixaLeituraFalsa(Imovel imovel, int media, Integer leituraAnterior, MedicaoHistorico medicaoHistorico,
			boolean hidrometroSelecionado, Hidrometro hidrometro) {

		FaixaLeituraTO faixaLeitura = new FaixaLeituraTO();

		BigDecimal multiplicaFaxaFalsa = obterFatorMultiplicacaoFaixaFalsa(imovel);
		Integer leituraAnteriorFalsa = null;

		if (multiplicaFaxaFalsa != null) {

			hidrometroSelecionado = verificarLeituraAnteriorMedia(media, medicaoHistorico);

			if ((multiplicaFaxaFalsa.doubleValue() % 100 == 0) || (hidrometroSelecionado)) {

				Integer numeroMeses = new Integer(sistemaParametro.getMesesMediaConsumo());

				Integer consumoMinimo = pesquisarConsumoMinimoTarifa(imovel.getId());

				if (leituraAnterior != null && numeroMeses != null && media != 0) {

					leituraAnteriorFalsa = leituraAnterior - (numeroMeses * media) - (consumoMinimo + 1);

					if (leituraAnteriorFalsa.intValue() < 0) {
						leituraAnteriorFalsa = verificarLeituraAnteriorFalsaNegativa(leituraAnteriorFalsa, hidrometro);
					}
					faixaLeitura = calcularFaixaLeituraEsperada(media, null, hidrometro, leituraAnteriorFalsa);
				}
			}
		}
		faixaLeitura.setHidrometroSelecionado(hidrometroSelecionado);

		return faixaLeitura;
	}

	private BigDecimal obterFatorMultiplicacaoFaixaFalsa(Imovel imovel) {
		BigDecimal percentualFaixaFalsaRota = imovel.getQuadra().getRota().getPercentualGeracaoFaixaFalsa();
		BigDecimal percentualFaixaFalsaSistemaParametro = sistemaParametro.getPercentualFaixaFalsa();

		Calendar dataCalendar = new GregorianCalendar();
		int segundos = dataCalendar.get(Calendar.SECOND);

		Integer somaImovelSegundo = imovel.getId() + segundos;

		if (sistemaParametro.getIndicadorUsoFaixaFalsa().equals(StatusUsoFaixaFalsa.ROTA.getId())) {
			if (imovel.getQuadra().getRota().possuiPercentualFaixaFalsa()) {
				return percentualFaixaFalsaRota.multiply(new BigDecimal(somaImovelSegundo));
			}
		} else {
			if (sistemaParametro.getIndicadorUsoFaixaFalsa().equals(StatusUsoFaixaFalsa.SISTEMA_PARAMETRO.getId())) {
				if (!percentualFaixaFalsaSistemaParametro.equals(new BigDecimal(0.0))) {
					return percentualFaixaFalsaSistemaParametro.multiply(new BigDecimal(somaImovelSegundo));
				}
			}
		}
		return null;
	}
	
	public Integer pesquisarConsumoMinimoTarifa(Integer idImovel) {
		ConsumoTarifaCategoriaTO consumoTarifaCategoria =  consumoTarifaRepositorio.consumoTarifaCategoriaDoImovel(idImovel);
		ConsumoTarifaVigenciaTO consumoTarifaVigencia = consumoTarifaVigenciaRepositorio.maiorDataVigenciaConsumoTarifa(consumoTarifaCategoria.getConsumoTarifa().getId());

		return consumoTarifaCategoriaRepositorio.consumoMinimoTarifaCategoria(consumoTarifaCategoria.getCategoria().getId(), consumoTarifaVigencia.getIdVigencia());
	}
	
	private boolean isGerarFaixaNormal(Imovel imovel) {
		return isIndicadorFaixaFalsaInativo() || (isIndicadorFaixaFalsaRota() && isIndicadorFaixaFalsaRotaInativo(imovel.getQuadra().getRota()));
	}
	
	private boolean isIndicadorFaixaFalsaInativo() {
		return sistemaParametro.getIndicadorFaixaFalsa() != null && sistemaParametro.getIndicadorFaixaFalsa().equals(StatusFaixaFalsa.GERAR_FAIXA_FALSA_DESATIVO.getId());
	}
	
	private boolean isIndicadorFaixaFalsaRota() {
		return sistemaParametro.getIndicadorFaixaFalsa() != null && sistemaParametro.getIndicadorFaixaFalsa().equals(StatusFaixaFalsa.GERAR_FAIXA_FALSA_ROTA.getId());
	}
	
	private boolean isIndicadorFaixaFalsaRotaInativo(Rota rota) {
		return rota.getIndicadorGerarFalsaFaixa() != null && rota.getIndicadorGerarFalsaFaixa().equals(StatusFaixaFalsa.GERAR_FAIXA_FALSA_DESATIVO.getId());
	}
	
	public boolean verificarLeituraAnteriorMedia(int media, MedicaoHistorico medicaoHistorico) {
		if (medicaoHistorico.getLeituraSituacaoAtual() == LeituraSituacao.NAO_REALIZADA.getId()
				|| medicaoHistorico.getLeituraAnteriorFaturamento() == 0
				|| media == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	public Integer verificarLeituraAnteriorFalsaNegativa(Integer leituraAnteriorFalsa, Hidrometro hidrometro) {
		
		double leituraAnteriorFalsaNegativa = leituraAnteriorFalsa;
		double numeroDigitosLeitura = hidrometro.getNumeroDigitosLeitura();
		double numeroDigitosLeituraElevadoDez = (int) Math.pow(10,numeroDigitosLeitura);

		leituraAnteriorFalsa = new Double(leituraAnteriorFalsaNegativa + numeroDigitosLeituraElevadoDez).intValue();

		return leituraAnteriorFalsa;
	}
}
