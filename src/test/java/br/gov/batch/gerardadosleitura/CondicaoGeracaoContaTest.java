package br.gov.batch.gerardadosleitura;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import br.gov.model.atendimentopublico.LigacaoAguaSituacao;
import br.gov.model.atendimentopublico.LigacaoEsgotoSituacao;
import br.gov.model.cadastro.Imovel;

public class CondicaoGeracaoContaTest {

	private CondicaoGeracaoConta condicaoGeracaoConta;
	private Imovel imovel;
	private LigacaoAguaSituacao ligacaoAguaSituacao;
	private LigacaoEsgotoSituacao ligacaoEsgotoSituacao;
	private boolean aguaEsgotoZerado;
	
	@Before
	public void setup(){
		condicaoGeracaoConta = new CondicaoGeracaoConta();

		imovel = new Imovel();
		ligacaoAguaSituacao = new LigacaoAguaSituacao();
		ligacaoAguaSituacao.setId(LigacaoAguaSituacao.LIGADO);
		imovel.setLigacaoAguaSituacao(ligacaoAguaSituacao);
		
		ligacaoEsgotoSituacao = new LigacaoEsgotoSituacao();
		ligacaoEsgotoSituacao.setId(LigacaoEsgotoSituacao.LIGADO);
		imovel.setLigacaoEsgotoSituacao(ligacaoEsgotoSituacao);
	}
	

	@Test
	public void primeiraCondicaoNaoGeraContaComAguaEsgotoZerados() throws Exception {
		aguaEsgotoZerado = true;
		boolean naoGeraConta = condicaoGeracaoConta.primeiraCondicaoNaoGerarConta(imovel, aguaEsgotoZerado);
		
		assertTrue(naoGeraConta);
	}
	
	@Test
	public void primeiraCondicaoGeraContaSemAguaEsgotoZeradosELigado() throws Exception {
		aguaEsgotoZerado = false;
		boolean naoGeraConta = condicaoGeracaoConta.primeiraCondicaoNaoGerarConta(imovel, aguaEsgotoZerado);
		
		assertFalse(naoGeraConta);
	}

	@Test
	public void primeiraCondicaoNaoGeraContaSemAguaEsgotoZeradoEDesligado() throws Exception {
		aguaEsgotoZerado = false;

		ligacaoAguaSituacao.setId(0);
		ligacaoEsgotoSituacao.setId(0);
		
		boolean naoGeraConta = condicaoGeracaoConta.primeiraCondicaoNaoGerarConta(imovel, aguaEsgotoZerado);
		
		assertTrue(naoGeraConta);
	}
	
	@Test
	public void primeiraCondicaoNaoGeraContaSemAguaEsgotoZeradosDesligadoESemCondominio() throws Exception {
		aguaEsgotoZerado = false;
		
		ligacaoAguaSituacao.setId(0);
		ligacaoEsgotoSituacao.setId(0);
		
		boolean naoGeraConta = condicaoGeracaoConta.primeiraCondicaoNaoGerarConta(imovel, aguaEsgotoZerado);
		
		assertTrue(naoGeraConta);
	}
	
	@Test
	public void primeiraCondicaoNaoGeraContaSemAguaEsgotoZeradosDesligadoEComCondominio() throws Exception {
		aguaEsgotoZerado = false;

		ligacaoAguaSituacao.setId(0);
		ligacaoEsgotoSituacao.setId(0);
		
		imovel.setImovelCondominio(new Imovel());
		
		boolean naoGeraConta = condicaoGeracaoConta.primeiraCondicaoNaoGerarConta(imovel, aguaEsgotoZerado);
		
		assertFalse(naoGeraConta);
	}
}