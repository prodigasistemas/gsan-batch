package br.gov.batch.servicos.desempenho;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.joda.time.DateTime;
import org.joda.time.Days;

import br.gov.batch.servicos.faturamento.tarifa.ConsumoImovelCategoriaBO;
import br.gov.batch.servicos.micromedicao.ConsumoHistoricoBO;
import br.gov.batch.util.Util;
import br.gov.model.cadastro.Imovel;
import br.gov.model.desempenho.ContratoMedicao;
import br.gov.model.faturamento.ConsumoImovelCategoriaTO;
import br.gov.model.faturamento.ConsumoTarifaVigencia;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.servicos.desempenho.ContratoMedicaoRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaVigenciaRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;

@Stateless
public class ContratoMedicaoBO {
	
	@EJB
	private ConsumoImovelCategoriaBO consumoImovelBO;
	
	@EJB
	private ConsumoHistoricoBO consumoHistoricoBO;
	
	@EJB
	private ConsumoTarifaVigenciaRepositorio consumoTarifaVigenciaRepositorio;

	@EJB
	private ContratoMedicaoRepositorio contratoMedicaoRepositorio;

	@EJB
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorio;
	
	public BigDecimal calcularValorDiferencaAgua(Imovel imovel, Integer referencia) {
		ContratoMedicao contratoMedicao = contratoMedicaoRepositorio.buscarContratoAtivoPorImovel(imovel.getId());
		Integer referenciaMesZero = getReferenciaMesZero(contratoMedicao);
		
		BigDecimal valorConsumoMesZero = calcularValorConsumo(imovel, referenciaMesZero);
		BigDecimal valorConsumoMesAtual = calcularValorConsumo(imovel, referencia);
		
		return valorConsumoMesAtual.subtract(valorConsumoMesZero);
	}
	
	public Integer calcularDiferencaConsumoAgua(Imovel imovel, Integer referencia) {
		ContratoMedicao contratoMedicao = contratoMedicaoRepositorio.buscarContratoAtivoPorImovel(imovel.getId());
		Integer consumoMesZero = consumoHistoricoBO.getConsumoMes(imovel, getReferenciaMesZero(contratoMedicao), LigacaoTipo.AGUA);
		Integer consumoReferencia = consumoHistoricoBO.getConsumoMes(imovel, referencia, LigacaoTipo.AGUA);
		
		return consumoReferencia - consumoMesZero;
	}

	public BigDecimal calcularValorConsumo(Imovel imovel, int referencia) {
		MedicaoHistorico medicaoHistorico = medicaoHistoricoRepositorio.buscarPorLigacaoAgua(imovel.getId(), referencia);
		ConsumoHistorico consumoHistorico = consumoHistoricoBO.getConsumoHistoricoPorReferencia(imovel, referencia);
		
		List<ConsumoTarifaVigencia> tarifasVigentes = consumoTarifaVigenciaRepositorio.buscarTarifasPorPeriodo(medicaoHistorico.getDataLeituraAnteriorFaturamento(),
																												medicaoHistorico.getDataLeituraAtualInformada());
		BigDecimal valorTotalConsumo = BigDecimal.ZERO;
		
		for (ConsumoTarifaVigencia consumoTarifaVigencia : tarifasVigentes) {
			BigDecimal valorConsumoVigencia = consumoImovelBO.getValorTotalConsumoImovel(consumoHistorico, medicaoHistorico);
			valorTotalConsumo = valorTotalConsumo.add(valorConsumoVigencia);
		}
		
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
}
