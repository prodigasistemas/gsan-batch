package br.gov.batch.servicos.desempenho;

import java.math.BigDecimal;
import java.util.Date;

import br.gov.batch.servicos.micromedicao.ConsumoHistoricoBO;
import br.gov.batch.util.Util;
import br.gov.model.cadastro.Imovel;
import br.gov.model.desempenho.ContratoMedicao;
import br.gov.model.micromedicao.LigacaoTipo;

public class CalculadoraDesempenho {

	private Imovel imovel;
	private ContratoMedicao contratoMedicao;
	private ConsumoHistoricoBO consumoHistoricoBO;
	
	public CalculadoraDesempenho(Imovel imovel, ContratoMedicao contratoMedicao, ConsumoHistoricoBO consumoHistoricoBO) {
		this.imovel = imovel;
		this.contratoMedicao = contratoMedicao;
		this.consumoHistoricoBO = consumoHistoricoBO;
	}
	
	public BigDecimal calcularValorDiferencaAgua(Integer referencia) {
		return null;
	}
	
	public Integer calcularDiferencaConsumoAgua(Integer referencia) {
		
		Integer consumoMesZero = consumoHistoricoBO.getConsumoMes(imovel, getReferenciaMesZero(), LigacaoTipo.AGUA);
		
		Integer consumoReferencia = consumoHistoricoBO.getConsumoMes(imovel, referencia, LigacaoTipo.AGUA);
		
		return consumoReferencia - consumoMesZero;
	}
	
	private Integer getReferenciaMesZero() {
		Date referenciaAssinatura = contratoMedicao.getDataAssinatura();
		
		return Util.getAnoMesComoInteger(referenciaAssinatura);
	}

}
