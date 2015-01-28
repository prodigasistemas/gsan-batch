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
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.Quadra;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.FaturamentoAtividade;
import br.gov.model.micromedicao.Rota;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo11Test {
	
	@TestSubject
	public ArquivoTextoTipo11 arquivoTextoTipo11;
	
	@Mock
	public FaturamentoAtividadeCronogramaBO BO;
	
	private Rota rota;
	
	private Imovel imovel;
	
	private SistemaParametros sp;
	
	@Before
	public void init(){
		arquivoTextoTipo11 = new ArquivoTextoTipo11();
		
		rota = new Rota();
		rota.setId(1);
		rota.setIndicadorSequencialLeitura(8);
		rota.setDataAjusteLeitura(new Date());
		rota.setIndicadorAjusteConsumo(Short.valueOf("7"));
		rota.setIndicadorTransmissaoOffline(Short.valueOf("6"));
		
		imovel = new Imovel();
		Quadra quadra = new Quadra();
		quadra.setRota(rota);
		imovel.setQuadra(quadra);
		
		sp = new SistemaParametros();
		sp.setCodigoEmpresaFebraban(Short.valueOf("9999"));
		sp.setAnoMesArrecadacao(201501);
		sp.setNumero0800Empresa("080084178111");
		sp.setCnpjEmpresa("603603603603");
		sp.setInscricaoEstadual("3231230532312305");
		sp.setValorMinimoEmissaoConta(BigDecimal.valueOf(1));
		sp.setPercentualToleranciaRateio(BigDecimal.valueOf(1));
		sp.setDecrementoMaximoConsumoRateio(1);
		sp.setIncrementoMaximoConsumoRateio(1);
		sp.setIndicadorTarifaCategoria(Short.valueOf("1"));
		sp.setVersaoCelular("CEL NOKIA");
		sp.setIndicadorBloqueioContaMobile(1);
		sp.setNumeroModuloDigitoVerificador(Short.valueOf("1"));
	}

	@Test
	public void testeComRotaValida(){
		carregarMocks();
		
		String linha = arquivoTextoTipo11.build(sp, imovel, 1,201501);
		int tamanhoLinha = linha.length();
		
		System.out.println(linha);
		System.out.println(tamanhoLinha);
		
		assertTrue(tamanhoLinha >= 1);
	}
	
	public void carregarMocks() {
		expect(BO.obterDiferencaDiasCronogramas(rota, FaturamentoAtividade.EFETUAR_LEITURA)).andReturn(Long.valueOf(31));
		replay(BO);
	}
}
