package br.gov.batch.servicos.micromedicao;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.servicos.micromedicao.HidrometroInstalacaoHistoricoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.to.HidrometroMedicaoHistoricoTO;
import br.gov.servicos.to.HidrometroTO;

public class MedicaoHistoricoBOTest {
	
	@InjectMocks
	private MedicaoHistoricoBO medicaoHistoricoBO;
	
	@Mock
	private HidrometroInstalacaoHistoricoRepositorio hidrometroInstalacaoHistoricoRepositorioMock;
	
	@Mock
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorioMock;
	
	private Date dataLeituraAtualFaturamento;
	
	private Date dataLeituraAtualInformada;

	@Before
	public void setup(){
		medicaoHistoricoBO = new MedicaoHistoricoBO();
		dataLeituraAtualFaturamento = new Date();
		dataLeituraAtualInformada = new Date();
		
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void medicaoHistoricoMesSemMedicao() {
		mockHidrometroInstalacaoHistoricoRepositorio();
		mockMedicaoHistoricoNull();
		
		List<HidrometroMedicaoHistoricoTO> listaHidrometro = medicaoHistoricoBO.obterDadosTiposMedicao(1, 201501);
		HidrometroMedicaoHistoricoTO to = listaHidrometro.get(0);
		
		assertEquals(8, to.getLeituraAtualFaturamento().intValue());
		assertEquals(dataLeituraAtualFaturamento, to.getDataLeituraAtualFaturamento());
		assertEquals(2, to.getLeituraSituacaoAtual().intValue());
		assertEquals(5, to.getConsumoMedioHidrometro().intValue());
		assertEquals(dataLeituraAtualInformada, to.getDataLeituraAtualInformada());
		assertEquals(6, to.getLeituraAtualInformada().intValue());
	}
	
	@Test
	public void medicaoHistoricoMesComMedicao() {
		mockHidrometroInstalacaoHistoricoRepositorio();
		mockMedicaoHistorico();
		
		List<HidrometroMedicaoHistoricoTO> listaHidrometro = medicaoHistoricoBO.obterDadosTiposMedicao(1, 201501);
		HidrometroMedicaoHistoricoTO to = listaHidrometro.get(0);
		
		assertEquals(10, to.getLeituraAtualFaturamento().intValue());
		assertEquals(dataLeituraAtualFaturamento, to.getDataLeituraAtualFaturamento());
		assertEquals(2, to.getLeituraSituacaoAtual().intValue());
		assertEquals(5, to.getConsumoMedioHidrometro().intValue());
		assertEquals(dataLeituraAtualInformada, to.getDataLeituraAtualInformada());
		assertEquals(7, to.getLeituraAtualInformada().intValue());
	}
	
	private void mockHidrometroInstalacaoHistoricoRepositorio() {
		when(hidrometroInstalacaoHistoricoRepositorioMock.dadosInstalacaoHidrometroAgua(1)).thenReturn(getHidrometro());
		when(hidrometroInstalacaoHistoricoRepositorioMock.dadosInstalacaoHidrometroPoco(1)).thenReturn(null);
	}
	
	private void mockMedicaoHistoricoNull() {
		when(medicaoHistoricoRepositorioMock.buscarPorLigacaoAguaOuPoco(1, 201501)).thenReturn(null);
		when(medicaoHistoricoRepositorioMock.buscarPorLigacaoAguaOuPoco(1, 201412)).thenReturn(getMedicaoHistoricoMesAnterior());
	}
	
	private void mockMedicaoHistorico() {
		when(medicaoHistoricoRepositorioMock.buscarPorLigacaoAguaOuPoco(1, 201501)).thenReturn(getMedicaoHistorico());
	}
	
	private HidrometroTO getHidrometro() {
		HidrometroTO to = new HidrometroTO();
		to.setNumero("123456");
		to.setNumeroDigitosLeitura(Short.valueOf("1"));
		to.setDataInstalacao(new Date());
		to.setNumeroLeituraInstalacao(123);
		to.setIdImovel(1);
		to.setDescricaoLocalInstalacao("teste");
		to.setRateioTipo(0);
		to.setMedicaoTipo(1);
		
		return to;
	}
	
	private MedicaoHistorico getMedicaoHistorico() {
		MedicaoHistorico medicaoHistorico = new MedicaoHistorico();
		medicaoHistorico.setLeituraAtualFaturamento(10);
		medicaoHistorico.setDataLeituraAtualFaturamento(dataLeituraAtualFaturamento);
		medicaoHistorico.setLeituraSituacaoAtual(2);
		medicaoHistorico.setConsumoMedioHidrometro(5);
		medicaoHistorico.setDataLeituraAtualInformada(dataLeituraAtualInformada);
		medicaoHistorico.setLeituraAtualInformada(7);
		return medicaoHistorico;
	}
	
	private MedicaoHistorico getMedicaoHistoricoMesAnterior() {
		MedicaoHistorico medicaoHistorico = new MedicaoHistorico();
		medicaoHistorico.setLeituraAtualFaturamento(8);
		medicaoHistorico.setDataLeituraAtualFaturamento(dataLeituraAtualFaturamento);
		medicaoHistorico.setLeituraSituacaoAtual(2);
		medicaoHistorico.setConsumoMedioHidrometro(5);
		medicaoHistorico.setDataLeituraAtualInformada(dataLeituraAtualInformada);
		medicaoHistorico.setLeituraAtualInformada(6);
		return medicaoHistorico;
	}
}
