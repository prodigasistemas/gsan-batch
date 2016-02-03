package br.gov.batch.servicos.micromedicao;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.Status;
import br.gov.model.atendimentopublico.LigacaoAgua;
import br.gov.model.atendimentopublico.LigacaoAguaSituacao;
import br.gov.model.atendimentopublico.LigacaoEsgotoSituacao;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.Quadra;
import br.gov.model.faturamento.FaturamentoSituacaoTipo;
import br.gov.model.micromedicao.HidrometroInstalacaoHistorico;
import br.gov.model.micromedicao.Rota;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.micromedicao.RotaRepositorio;

@RunWith(EasyMockRunner.class)
public class RotaBOTest {

	@TestSubject
	private RotaBO rotaBO;

	@Mock
	private RotaRepositorio rotaRepositorioMock;

	@Mock
	private ImovelRepositorio imovelRepositorioMock;

	private LigacaoAguaSituacao ligacaoAguaSituacaoAtivo;
	private LigacaoEsgotoSituacao ligacaoEsgotoSituacaoAtivo;
	private LigacaoAgua ligacaoAguaComInstalacao;
	private LigacaoAgua ligacaoAguaSemInstalacao;
	private List<Imovel> imoveis;
	private Rota rota1;
	private int totalImoveis;
	
	private FaturamentoSituacaoTipo faturamentoSituacaoTipo;

	@Before
	public void setup() {
		rotaBO = new RotaBO();

		ligacaoAguaSituacaoAtivo = new LigacaoAguaSituacao();
		ligacaoAguaSituacaoAtivo.setSituacaoFaturamento(Short.valueOf("1"));

		ligacaoEsgotoSituacaoAtivo = new LigacaoEsgotoSituacao();
		ligacaoEsgotoSituacaoAtivo.setSituacaoFaturamento(Short.valueOf("1"));

		ligacaoAguaComInstalacao = new LigacaoAgua();
		Set<HidrometroInstalacaoHistorico> hidrometroInstalacoesHistorico = new HashSet<HidrometroInstalacaoHistorico>();
		hidrometroInstalacoesHistorico.add(new HidrometroInstalacaoHistorico());
		ligacaoAguaComInstalacao.setHidrometroInstalacoesHistorico(hidrometroInstalacoesHistorico);
		
		ligacaoAguaSemInstalacao = new LigacaoAgua();
		
		faturamentoSituacaoTipo = new FaturamentoSituacaoTipo();
		faturamentoSituacaoTipo.setId(1);

		imoveis = new ArrayList<Imovel>();

		Imovel imovel = new Imovel(1);
		imovel.setLigacaoAguaSituacao(ligacaoAguaSituacaoAtivo);
		imovel.setLigacaoAgua(ligacaoAguaComInstalacao);
		imoveis.add(imovel);
		
		imovel = new Imovel(2);
		imovel.setLigacaoEsgotoSituacao(ligacaoEsgotoSituacaoAtivo);
		imovel.setHidrometroInstalacaoHistorico(new HidrometroInstalacaoHistorico());
		imoveis.add(imovel);

		imovel = new Imovel(3);
		imovel.setLigacaoAguaSituacao(new LigacaoAguaSituacao(LigacaoAguaSituacao.POTENCIAL));
		imoveis.add(imovel);

		imovel = new Imovel(4);
		imovel.setLigacaoAguaSituacao(new LigacaoAguaSituacao(LigacaoAguaSituacao.SUPRIMIDO));
		Rota rota = new Rota();
		rota.setIndicadorFiscalizarSuprimido(Status.ATIVO.getId());
		Quadra quadra = new Quadra();
		quadra.setRota(rota);
		imovel.setQuadra(quadra);
		imoveis.add(imovel);
		
		imovel = new Imovel(5);
		imovel.setLigacaoAguaSituacao(new LigacaoAguaSituacao(LigacaoAguaSituacao.SUPRIMIDO));
		rota = new Rota();
		rota.setIndicadorFiscalizarSuprimido(Status.INATIVO.getId());
		quadra = new Quadra();
		quadra.setRota(rota);
		imovel.setQuadra(quadra);
		imoveis.add(imovel);

		imovel = new Imovel(6);
		imovel.setLigacaoAguaSituacao(new LigacaoAguaSituacao(LigacaoAguaSituacao.CORTADO));
		rota = new Rota();
		rota.setIndicadorFiscalizarCortado(Status.ATIVO.getId());
		quadra = new Quadra();
		quadra.setRota(rota);
		imovel.setQuadra(quadra);
		imoveis.add(imovel);
		
		imovel = new Imovel(7);
		imovel.setLigacaoAguaSituacao(new LigacaoAguaSituacao(LigacaoAguaSituacao.CORTADO));
		rota = new Rota();
		rota.setIndicadorFiscalizarCortado(Status.INATIVO.getId());
		quadra = new Quadra();
		quadra.setRota(rota);
		imovel.setQuadra(quadra);
		imoveis.add(imovel);
		
		imovel = new Imovel(8);
		imovel.setLigacaoAguaSituacao(ligacaoAguaSituacaoAtivo);
		imovel.setLigacaoAgua(ligacaoAguaComInstalacao);
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
		imoveis.add(imovel);
		
		imovel = new Imovel(9);
		imovel.setLigacaoAguaSituacao(ligacaoAguaSituacaoAtivo);
		imovel.setLigacaoAgua(ligacaoAguaSemInstalacao);
		imovel.setHidrometroInstalacaoHistorico(new HidrometroInstalacaoHistorico());
		imovel.setFaturamentoSituacaoTipo(faturamentoSituacaoTipo);
		imoveis.add(imovel);
		
		totalImoveis = imoveis.size();
				
		rota1 = new Rota(1);
	}

	@Test
	public void imoveisParaLeituraComRotaAlternativa() {
		carregarRotaAlternativaMock(true);
		carregarImoveisParaLeituraComRotaAlternativaMock();

		List<Imovel> imoveis = rotaBO.imoveisParaLeitura(1, 1, 10);

		assertEquals(4, imoveis.size());
	}

	@Test
	public void imoveisParaLeituraSemRotaAlternativa() {
		carregarRotaAlternativaMock(false);
		carregarImoveisParaLeituraSemRotaAlternativaMock();

		List<Imovel> imoveis = rotaBO.imoveisParaLeitura(1, 1, 10);

		assertEquals(4, imoveis.size());
	}
	
	@Test
	public void imoveisParaPreFaturamentoComRotaAlternativa() {
		carregarRotaAlternativaMock(true);
		carregarImoveisParaPreFaturamentoComRotaAlternativaMock();
		
		List<Imovel> imoveis = rotaBO.imoveisParaPreFaturamento(1, 1, 10);
		
		assertEquals(totalImoveis, imoveis.size());
	}
	
	@Test
	public void imoveisParaPreFaturamentoSemRotaAlternativa() {
		carregarRotaAlternativaMock(false);
		carregarImoveisParaPreFaturamentoSemRotaAlternativaMock();
		
		List<Imovel> imoveis = rotaBO.imoveisParaPreFaturamento(1, 1, 10);
		
		assertEquals(totalImoveis, imoveis.size());
	}
	
	@Test
	public void totalImoveisParaPreFaturamentoComRotaAlternativa() {
		carregarRotaAlternativaMock(true);
		carregarTotalImoveisParaPreFaturamentoComRotaAlternativaMock(rota1.getId());
		
		long total = rotaBO.totalImoveisParaPreFaturamento(rota1.getId());
		
		assertEquals((long) totalImoveis, total);
	}
	
	@Test
	public void totalImoveisParaPreFaturamentoSemRotaAlternativa() {
		carregarRotaAlternativaMock(false);
		carregarTotalImoveisParaPreFaturamentoSemRotaAlternativaMock(rota1.getId());
		
		long total = rotaBO.totalImoveisParaPreFaturamento(rota1.getId());
		
		assertEquals((long) totalImoveis, total);
	}
	
	@Test
	public void totalImoveisParaLeituraComRotaAlternativa() {
		carregarRotaAlternativaMock(true);
		carregarTotalImoveisParaLeituraComRotaAlternativaMock(rota1.getId());
		
		long total = rotaBO.totalImoveisParaLeitura(rota1.getId());
		
		assertEquals((long) totalImoveis, total);
	}
	
	@Test
	public void totalImoveisParaLeituraSemRotaAlternativa() {
		carregarRotaAlternativaMock(false);
		carregarTotalImoveisParaLeituraSemRotaAlternativaMock(rota1.getId());
		
		long total = rotaBO.totalImoveisParaLeitura(rota1.getId());
		
		assertEquals((long) totalImoveis, total);
	}

	private void carregarRotaAlternativaMock(boolean rotaAlternativa) {
		expect(rotaRepositorioMock.isRotaAlternativa(1)).andReturn(rotaAlternativa);
		replay(rotaRepositorioMock);
	}

	private void carregarImoveisParaLeituraComRotaAlternativaMock() {
		expect(imovelRepositorioMock.imoveisParaLeituraComRotaAlternativa(1, 1, 10)).andReturn(imoveis);
		replay(imovelRepositorioMock);
	}

	private void carregarImoveisParaLeituraSemRotaAlternativaMock() {
		expect(imovelRepositorioMock.imoveisParaLeituraSemRotaAlternativa(1, 1, 10)).andReturn(imoveis);
		replay(imovelRepositorioMock);
	}
	
	private void carregarImoveisParaPreFaturamentoComRotaAlternativaMock() {
		expect(imovelRepositorioMock.imoveisParaPreFaturamentoComRotaAlternativa(1, 1, 10)).andReturn(imoveis);
		replay(imovelRepositorioMock);
	}
	
	private void carregarImoveisParaPreFaturamentoSemRotaAlternativaMock() {
		expect(imovelRepositorioMock.imoveisParaPreFaturamentoSemRotaAlternativa(1, 1, 10)).andReturn(imoveis);
		replay(imovelRepositorioMock);
	}
	
	private void carregarTotalImoveisParaPreFaturamentoComRotaAlternativaMock(int idRota) {
		expect(imovelRepositorioMock.totalImoveisParaPreFaturamentoComRotaAlternativa(idRota)).andReturn((long) imoveis.size());
		replay(imovelRepositorioMock);
	}
	
	private void carregarTotalImoveisParaPreFaturamentoSemRotaAlternativaMock(int idRota) {
		expect(imovelRepositorioMock.totalImoveisParaPreFaturamentoSemRotaAlternativa(idRota)).andReturn((long) imoveis.size());
		replay(imovelRepositorioMock);
	}
	
	private void carregarTotalImoveisParaLeituraComRotaAlternativaMock(int idRota) {
		expect(imovelRepositorioMock.totalImoveisParaLeituraComRotaAlternativa(idRota)).andReturn((long) imoveis.size());
		replay(imovelRepositorioMock);
	}
	
	private void carregarTotalImoveisParaLeituraSemRotaAlternativaMock(int idRota) {
		expect(imovelRepositorioMock.totalImoveisParaLeituraSemRotaAlternativa(idRota)).andReturn((long) imoveis.size());
		replay(imovelRepositorioMock);
	}
}
