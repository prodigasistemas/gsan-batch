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
	private List<Imovel> imoveis;

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
		imovel.setLigacaoAguaSituacao(new LigacaoAguaSituacao(LigacaoAguaSituacao.CORTADO));
		rota = new Rota();
		rota.setIndicadorFiscalizarCortado(Status.ATIVO.getId());
		quadra = new Quadra();
		quadra.setRota(rota);
		imovel.setQuadra(quadra);
		imoveis.add(imovel);
	}

	@Test
	public void imoveisParaArquivoConvencionalComRotaAlternativa() {
		carregarRotaAlternativaMock(true);
		carregarImoveisComRotaAlternativaMock();

		List<Imovel> imoveis = rotaBO.imoveisParaArquivoConvencional(1);

		assertEquals(4, imoveis.size());
	}

	@Test
	public void imoveisParaArquivoConvencionalSemRotaAlternativa() {
		carregarRotaAlternativaMock(false);
		carregarImoveisSemRotaAlternativaMock();

		List<Imovel> imoveis = rotaBO.imoveisParaArquivoConvencional(1);

		assertEquals(4, imoveis.size());
	}

	private void carregarRotaAlternativaMock(boolean rotaAlternativa) {
		expect(rotaRepositorioMock.isRotaAlternativa(1)).andReturn(rotaAlternativa);
		replay(rotaRepositorioMock);
	}

	private void carregarImoveisComRotaAlternativaMock() {
		expect(imovelRepositorioMock.imoveisParaArquivoConvencionalComRotaAlternativa(1)).andReturn(imoveis);
		replay(imovelRepositorioMock);
	}

	private void carregarImoveisSemRotaAlternativaMock() {
		expect(imovelRepositorioMock.imoveisParaArquivoConvencionalSemRotaAlternativa(1)).andReturn(imoveis);
		replay(imovelRepositorioMock);
	}
}
