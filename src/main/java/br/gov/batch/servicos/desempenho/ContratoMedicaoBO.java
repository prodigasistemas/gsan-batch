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
import br.gov.batch.servicos.micromedicao.MedicaoHistoricoBO;
import br.gov.batch.util.Util;
import br.gov.model.cadastro.Imovel;
import br.gov.model.desempenho.ContratoMedicao;
import br.gov.model.micromedicao.ConsumoHistorico;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.servicos.desempenho.ContratoMedicaoRepositorio;
import br.gov.servicos.faturamento.ConsumoTarifaVigenciaRepositorio;

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
	private MedicaoHistoricoBO medicaoHistoricoBO;
	
	public BigDecimal calcularValorRepasse() {
		//TODO aplicar o coeficiente de acordo com a ligação
		return null;
	}
	
	public BigDecimal calcularValorDiferencaAgua(Imovel imovel, Integer referencia) {
		ContratoMedicao contratoMedicao = contratoMedicaoRepositorio.buscarContratoAtivoPorImovel(imovel.getId());
		Integer referenciaMesZero = getReferenciaMesZero(contratoMedicao);
		
		ConsumoHistorico consumoHistoricoMesZero = consumoHistoricoBO.getConsumoHistoricoPorReferencia(imovel, referenciaMesZero);
		ConsumoHistorico consumoHistoricoAtual = consumoHistoricoBO.getConsumoHistoricoPorReferencia(imovel, referencia);
		
		MedicaoHistorico medicaoHistorico = medicaoHistoricoBO.getMedicaoHistorico(imovel.getId(), referencia);
		
		BigDecimal valorConsumoMesZero = calcularValorConsumo(consumoHistoricoMesZero, medicaoHistorico);
		BigDecimal valorConsumoMesAtual = calcularValorConsumo(consumoHistoricoAtual, medicaoHistorico);
		
		return valorConsumoMesAtual.subtract(valorConsumoMesZero);
	}
	
	public Integer calcularDiferencaConsumoAgua(Imovel imovel, Integer referencia) {
		ContratoMedicao contratoMedicao = contratoMedicaoRepositorio.buscarContratoAtivoPorImovel(imovel.getId());
		Integer consumoMesZero = consumoHistoricoBO.getConsumoMes(imovel, getReferenciaMesZero(contratoMedicao), LigacaoTipo.AGUA);
		Integer consumoReferencia = consumoHistoricoBO.getConsumoMes(imovel, referencia, LigacaoTipo.AGUA);
		
		return consumoReferencia - consumoMesZero;
	}

	public BigDecimal calcularValorConsumo(ConsumoHistorico consumoHistorico, MedicaoHistorico medicaoHistorico) {
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

	public List<Imovel> getAbrangencia(Integer id) {
		// TODO falta implementar ou repassar o metodo direto para o repositorio
		return null;
	}
}
