package br.gov.batch.servicos.desempenho;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.joda.time.DateTime;
import org.joda.time.Days;

import br.gov.batch.servicos.faturamento.tarifa.ConsumoImovelCategoriaBO;
import br.gov.batch.servicos.micromedicao.ConsumoBO;
import br.gov.batch.servicos.micromedicao.ConsumoHistoricoBO;
import br.gov.batch.servicos.micromedicao.MedicaoHistoricoBO;
import br.gov.batch.util.Util;
import br.gov.model.atendimentopublico.LigacaoAguaSituacao;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.desempenho.ContratoMedicao;
import br.gov.model.desempenho.ContratoMedicaoCoeficiente;
import br.gov.model.faturamento.DebitoCreditoSituacaoRepositorio;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.servicos.atendimentopublico.LigacaoAguaSituacaoRepositorio;
import br.gov.servicos.desempenho.ContratoMedicaoCoeficienteRepositorio;
import br.gov.servicos.desempenho.ContratoMedicaoRepositorio;
import br.gov.servicos.to.MedicaoPerformanceTO;

@Stateless
public class ContratoMedicaoBO {
	
	@EJB
	private ConsumoImovelCategoriaBO consumoImovelBO;
	
	@EJB
	private ConsumoBO consumoBO;
	
	@EJB
	private ConsumoHistoricoBO consumoHistoricoBO;
	
	@EJB
	private ContratoMedicaoRepositorio contratoMedicaoRepositorio;

	@EJB
	private MedicaoHistoricoBO medicaoHistoricoBO;
	
	@EJB
	private LigacaoAguaSituacaoRepositorio ligacaoAguaSituacaoRepositorio;
	
	@EJB
	private ContratoMedicaoCoeficienteRepositorio contratoMedicaoCoeficienteRepositorio;
	
	@EJB
	private DebitoCreditoSituacaoRepositorio debitoCreditoSituacaoRepositorio;
	
	public MedicaoPerformanceTO getMedicaoPerformanceTO(ContratoMedicao contratoMedicao, Imovel imovel, int referencia) {
		MedicaoPerformanceTO medicaoPerformanceTO = new MedicaoPerformanceTO();
		medicaoPerformanceTO.setImovel(imovel);
		medicaoPerformanceTO.setReferencia(referencia);
		medicaoPerformanceTO.setContratoMedicao(contratoMedicao);
		
		Integer referenciaMesZero = getReferenciaMesZero(contratoMedicao);
		medicaoPerformanceTO.setValorAguaFaturadoMesZero(calcularValorConsumoMesZero(contratoMedicao, imovel, referenciaMesZero, referencia));
		medicaoPerformanceTO.setValorAguaFaturado(calcularValorConsumo(imovel, referencia, referencia));
		medicaoPerformanceTO.setValorDiferencaAgua(calcularValorDiferencaAgua(medicaoPerformanceTO.getValorAguaFaturadoMesZero(),
																			  medicaoPerformanceTO.getValorAguaFaturado()));
		
		medicaoPerformanceTO.setConsumoReferencia(getConsumoMes(imovel, referencia));
		medicaoPerformanceTO.setConsumoMesZero(getConsumoMes(imovel, referenciaMesZero));
		medicaoPerformanceTO.setDiferencaConsumoAgua(calcularDiferencaConsumoAgua(medicaoPerformanceTO.getConsumoMesZero(),
																				  medicaoPerformanceTO.getConsumoReferencia()));
		
		Integer idDebitoCreditoSituacao = debitoCreditoSituacaoRepositorio.buscarDebitoCreditoSituacaoPorIdEAnoMesReferencia(imovel.getId(), referencia);
		medicaoPerformanceTO.setDebitoCreditoSituacao(idDebitoCreditoSituacao);
		medicaoPerformanceTO.setValorMedicao(calcularValorMedicao(medicaoPerformanceTO, referencia));
		
		return medicaoPerformanceTO;
	}
	
	public BigDecimal calcularValorMedicao(MedicaoPerformanceTO medicaoPerformanceTO, int referencia) {
		Imovel imovel = medicaoPerformanceTO.getImovel();
		ContratoMedicao contratoMedicao = medicaoPerformanceTO.getContratoMedicao();
		
		LigacaoAguaSituacao ligacaoAguaSituacao = ligacaoAguaSituacaoRepositorio.buscarLigacaoAguaSituacao(contratoMedicao.getId(), imovel.getId());
		if(ligacaoAguaSituacao == null) {
			ligacaoAguaSituacao = ligacaoAguaSituacaoRepositorio.obterPorID(LigacaoAguaSituacao.FACTIVEL);
		}
		
		ContratoMedicaoCoeficiente contratoMedicaoCoeficiente = contratoMedicaoCoeficienteRepositorio.buscarPorContratoELigacaoAguaSituacao(
																															 contratoMedicao.getId(), 
																															 ligacaoAguaSituacao.getId());
		BigDecimal coeficiente = contratoMedicaoCoeficiente.getCoeficiente();
		coeficiente = coeficiente.divide(new BigDecimal(100));
		
		return medicaoPerformanceTO.getValorDiferencaAgua().multiply(coeficiente);
	}

	public BigDecimal calcularValorDiferencaAgua(Imovel imovel, Integer referencia) {
		ContratoMedicao contratoMedicao = getContratoMedicao(imovel.getId());
		Integer referenciaMesZero = getReferenciaMesZero(contratoMedicao);
		
		return calcularValorDiferencaAgua(imovel, referenciaMesZero, referencia);
	}

	public BigDecimal calcularValorDiferencaAgua(Imovel imovel, Integer referenciaMesZero, Integer referencia) {
		BigDecimal valorConsumoMesZero = calcularValorConsumo(imovel, referenciaMesZero, referencia);
		BigDecimal valorConsumoMesAtual = calcularValorConsumo(imovel, referencia, referencia);
		
		return calcularValorDiferencaAgua(valorConsumoMesZero, valorConsumoMesAtual);
	}
	
	private BigDecimal calcularValorDiferencaAgua(BigDecimal valorConsumoMesZero, BigDecimal valorConsumoReferencia) {
		return valorConsumoReferencia.subtract(valorConsumoMesZero);
	}
	
	public Integer calcularDiferencaConsumoAgua(Imovel imovel, Integer referenciaMesZero, Integer referencia) {
		Integer consumoMesZero = consumoHistoricoBO.getConsumoMes(imovel, referenciaMesZero, LigacaoTipo.AGUA);
		Integer consumoReferencia = consumoHistoricoBO.getConsumoMes(imovel, referencia, LigacaoTipo.AGUA);
		
		return calcularDiferencaConsumoAgua(consumoMesZero, consumoReferencia);
	}
	
	public Integer getConsumoMes(Imovel imovel, Integer referenciaMesZero) {
		return consumoHistoricoBO.getConsumoMes(imovel, referenciaMesZero, LigacaoTipo.AGUA);
	}
	
	private Integer calcularDiferencaConsumoAgua(Integer consumoMesZero, Integer consumoReferencia) {
		Integer diferenca = consumoReferencia - consumoMesZero;
		
		return diferenca < 0 ? 0 : diferenca;
	}
	
	public BigDecimal calcularValorConsumoMesZero(ContratoMedicao contratoMedicao, Imovel imovel, Integer referenciaConsumoHistorico, Integer referencia) {
		MedicaoHistorico medicaoHistorico = medicaoHistoricoBO.getMedicaoHistorico(imovel.getId(), referencia);
		ConsumoHistorico consumoHistorico = consumoHistoricoBO.getConsumoHistoricoPorReferencia(imovel, referenciaConsumoHistorico);
		
		if(medicaoHistorico == null || consumoHistorico == null) {
			return BigDecimal.ZERO;
		}

		Collection<ICategoria> categorias = consumoBO.buscarQuantidadeEconomiasPorImovelAbrangencia(contratoMedicao.getId(), 
																									consumoHistorico.getImovel().getId());
		BigDecimal valorTotalConsumo = consumoImovelBO.getValorTotalConsumoImovel(consumoHistorico, medicaoHistorico, categorias);
		
		return valorTotalConsumo;
	}

	
	public BigDecimal calcularValorConsumo(Imovel imovel, Integer referenciaConsumoHistorico, Integer referencia) {
		MedicaoHistorico medicaoHistorico = medicaoHistoricoBO.getMedicaoHistorico(imovel.getId(), referencia);
		ConsumoHistorico consumoHistorico = consumoHistoricoBO.getConsumoHistoricoPorReferencia(imovel, referenciaConsumoHistorico);
		
		if(medicaoHistorico == null || consumoHistorico == null) {
			return BigDecimal.ZERO;
		}
		
		BigDecimal valorTotalConsumo = consumoImovelBO.getValorTotalConsumoImovel(consumoHistorico, medicaoHistorico);
		
		return valorTotalConsumo;
	}

	public int calcularQuantidadeDiasLeitura(DateTime dataLeituraAnterior, DateTime dataLeituraAtual) {
		int qtdDias = Days.daysBetween(dataLeituraAnterior.toLocalDate(), dataLeituraAtual.toLocalDate()).getDays();
		
		return qtdDias++;
	}
	
	public Integer getReferenciaMesZero(ContratoMedicao contratoMedicao) {
		Date referenciaAssinatura = contratoMedicao.getDataAssinatura();
		
		return Util.getAnoMesComoInteger(referenciaAssinatura);
	}

	public ContratoMedicao getContratoMedicao(Integer imovelId) {
		return contratoMedicaoRepositorio.buscarContratoAtivoPorImovel(imovelId); 
	}

	public List<Imovel> getAbrangencia(int idContrato, int anoMesReferencia) {
		ContratoMedicao contratoMedicao = contratoMedicaoRepositorio.obterPorID(idContrato);
		List<Imovel> imoveis = contratoMedicaoRepositorio.buscarImoveis(idContrato, anoMesReferencia);
		List<Imovel> imoveisFaturadosCancelados = new ArrayList<Imovel>();
		
		for (Imovel imovel : imoveis) {
			if(contratoMedicaoRepositorio.possuiContaFaturadaNormal(imovel.getId(), anoMesReferencia)
				|| contratoMedicaoRepositorio.possuiContaFaturadaIncluida(imovel.getId(), anoMesReferencia)
				|| contratoMedicaoRepositorio.possuiContaFaturadaRetificada(contratoMedicao.getVigenciaInicial(), 
																			contratoMedicao.getVigenciaFinal(), 
																			imovel.getId(), 
																			anoMesReferencia)) {
				imoveisFaturadosCancelados.add(imovel);
			}
			
			if(contratoMedicaoRepositorio.possuiCancelamento(contratoMedicao.getVigenciaInicial(), 
															 contratoMedicao.getVigenciaFinal(),
															 imovel.getId(), 
															 anoMesReferencia)) {
				imoveisFaturadosCancelados.add(imovel);
			}
		}
		
		return imoveisFaturadosCancelados;
	}

	public List<ContratoMedicao> getContratoMedicaoPorReferencia(Integer referencia) {
		return contratoMedicaoRepositorio.buscarContratosMedicaoPorReferencia(referencia);
	}
}
