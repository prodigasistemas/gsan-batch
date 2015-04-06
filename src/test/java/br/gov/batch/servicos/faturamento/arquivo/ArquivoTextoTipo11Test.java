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

	private int TAMANHO_LINHA = 1;

	@Mock
	public FaturamentoAtividadeCronogramaBO faturamentoAtividadeCronogramaBO;

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

		SistemaParametros sistemaParametros = new SistemaParametros();
		sistemaParametros.setCodigoEmpresaFebraban(Short.valueOf("9999"));
		sistemaParametros.setAnoMesArrecadacao(201501);
		sistemaParametros.setNumero0800Empresa("080084178111");
		sistemaParametros.setCnpjEmpresa("603603603603");
		sistemaParametros.setInscricaoEstadual("3231230532312305");
		sistemaParametros.setValorMinimoEmissaoConta(BigDecimal.valueOf(1));
		sistemaParametros.setPercentualToleranciaRateio(BigDecimal.valueOf(1));
		sistemaParametros.setDecrementoMaximoConsumoRateio(1);
		sistemaParametros.setIncrementoMaximoConsumoRateio(1);
		sistemaParametros.setIndicadorTarifaCategoria(Short.valueOf("1"));
		sistemaParametros.setVersaoCelular("CEL NOKIA");
		sistemaParametros.setIndicadorBloqueioContaMobile(1);
		sistemaParametros.setNumeroModuloDigitoVerificador(Short.valueOf("1"));
		sistemaParametros.setNumeroDiasBloqueioCelular(3);

		arquivo.setSistemaParametros(sistemaParametros);
	}

	@Test
	public void testeComRotaValida() {
		carregarMocks();

		String linha = arquivo.build(to);
		assertTrue(linha.length() >= TAMANHO_LINHA);
	}

	public void carregarMocks() {
		expect(faturamentoAtividadeCronogramaBO.obterDiferencaDiasCronogramas(to.getRota(), FaturamentoAtividade.EFETUAR_LEITURA)).andReturn(Long.valueOf(31)).times(2);
		replay(faturamentoAtividadeCronogramaBO);
	}
}
