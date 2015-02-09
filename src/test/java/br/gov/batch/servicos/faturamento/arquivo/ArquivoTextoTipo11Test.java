package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Date;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.servicos.faturamento.FaturamentoAtividadeCronogramaBO;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.Quadra;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.FaturamentoAtividade;
import br.gov.model.micromedicao.Rota;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo11Test {

	@TestSubject
	public ArquivoTextoTipo11 arquivo;

	@Mock
	public FaturamentoAtividadeCronogramaBO faturamentoAtividadeCronogramaBO;
	
	@Mock SistemaParametros sistemaParametrosMock;

	private ArquivoTextoTO to;

	@Before
	public void setup() {
		arquivo = new ArquivoTextoTipo11();
		
		to = new ArquivoTextoTO();
		to.setSequenciaRota(1);

		Rota rota = new Rota();
		rota.setId(1);
		rota.setIndicadorSequencialLeitura(8);
		rota.setDataAjusteLeitura(new Date());
		rota.setIndicadorAjusteConsumo(Short.valueOf("7"));
		rota.setIndicadorTransmissaoOffline(Short.valueOf("6"));
		to.setRota(rota);
		
		Imovel imovel = new Imovel();
		Quadra quadra = new Quadra();
		quadra.setRota(rota);
		imovel.setQuadra(quadra);
		to.setImovel(imovel);

		to.setAnoMesReferencia(201501);
	}

	@Test
	public void testeComRotaValida() {
		carregarMocks();

		String linha = arquivo.build(to);
		int tamanhoLinha = linha.length();

		assertTrue(tamanhoLinha >= 1);
	}

	public void carregarMocks() {
		expect(faturamentoAtividadeCronogramaBO.obterDiferencaDiasCronogramas(to.getRota(), FaturamentoAtividade.EFETUAR_LEITURA)).andReturn(Long.valueOf(31));
		replay(faturamentoAtividadeCronogramaBO);
		
		mockParametros();
	}
	
	public void mockParametros() {
		expect(sistemaParametrosMock.getCodigoEmpresaFebraban()).andReturn(Short.valueOf("9999"));
		expect(sistemaParametrosMock.getAnoMesArrecadacao()).andReturn(201501);
		expect(sistemaParametrosMock.getNumero0800Empresa()).andReturn("080084178111");
		expect(sistemaParametrosMock.getCnpjEmpresa()).andReturn("603603603603");
		expect(sistemaParametrosMock.getInscricaoEstadual()).andReturn("3231230532312305");
		expect(sistemaParametrosMock.getValorMinimoEmissaoConta()).andReturn(BigDecimal.valueOf(1));
		expect(sistemaParametrosMock.getPercentualToleranciaRateio()).andReturn(BigDecimal.valueOf(1));
		expect(sistemaParametrosMock.getDecrementoMaximoConsumoRateio()).andReturn(1);
		expect(sistemaParametrosMock.getIncrementoMaximoConsumoRateio()).andReturn(1);
		expect(sistemaParametrosMock.getIndicadorTarifaCategoria()).andReturn(Short.valueOf("1"));
		expect(sistemaParametrosMock.getVersaoCelular()).andReturn("CEL NOKIA");
		expect(sistemaParametrosMock.getIndicadorBloqueioContaMobile()).andReturn(1);
		expect(sistemaParametrosMock.getNumeroModuloDigitoVerificador()).andReturn(Short.valueOf("1")).times(2);
		expect(sistemaParametrosMock.getNumeroDiasBloqueioCelular()).andReturn(3).times(4);
		replay(sistemaParametrosMock);
	}
}
