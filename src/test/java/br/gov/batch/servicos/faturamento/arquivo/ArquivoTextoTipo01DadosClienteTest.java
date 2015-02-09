package br.gov.batch.servicos.faturamento.arquivo;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.model.cadastro.Cliente;
import br.gov.model.cadastro.ClienteImovel;
import br.gov.model.cadastro.ClienteRelacaoTipo;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.FaturamentoParametro.NOME_PARAMETRO_FATURAMENTO;
import br.gov.servicos.faturamento.FaturamentoParametroRepositorio;

@RunWith(EasyMockRunner.class)
public class ArquivoTextoTipo01DadosClienteTest {

	@TestSubject
	private ArquivoTextoTipo01DadosCliente arquivo;

	@Mock
	private FaturamentoParametroRepositorio repositorioParametros;

	private Imovel imovel;

	@Before
	public void setup() {
		imovel = new Imovel(1234567);

		Cliente clienteUsuario = new Cliente();
		clienteUsuario.setNome("MARIA JOSÉ DA SILVA");
		clienteUsuario.setCpf("11111111111");

		Cliente clienteResponsavel = new Cliente();
		clienteResponsavel.setNome("JOÃO ROBERTO SOUZA");
		clienteResponsavel.setCpf("222.222.222-22");

		ClienteImovel clienteImovelUsuario = new ClienteImovel();
		clienteImovelUsuario.setCliente(clienteUsuario);
		clienteImovelUsuario.setImovel(imovel);
		clienteImovelUsuario.setClienteRelacaoTipo(new ClienteRelacaoTipo(ClienteRelacaoTipo.USUARIO));

		ClienteImovel clienteImovelResponsavel = new ClienteImovel();
		clienteImovelResponsavel.setCliente(clienteResponsavel);
		clienteImovelResponsavel.setImovel(imovel);
		clienteImovelResponsavel.setClienteRelacaoTipo(new ClienteRelacaoTipo(ClienteRelacaoTipo.RESPONSAVEL));

		List<ClienteImovel> clientesImovel = new ArrayList<ClienteImovel>();
		clientesImovel.add(clienteImovelUsuario);
		clientesImovel.add(clienteImovelResponsavel);
		imovel.setClienteImoveis(clientesImovel);

		imovel.setImovelContaEnvio(1);

		arquivo = new ArquivoTextoTipo01DadosCliente(imovel);
	}

	@Test
	public void testaQuantidadeDados() {
		carregarMocks();

		Map<Integer, StringBuilder> mapDados = arquivo.build();

		assertNotNull(mapDados);
		assertEquals(4, mapDados.keySet().size());
	}

	@Test
	public void testaFormatoDados() {
		carregarMocks();

		StringBuilder linhaValida = new StringBuilder();
		linhaValida.append("MARIA JOSÉ DA SILVA           11111111111                                        ")
				   .append("                                                                            1");

		Map<Integer, StringBuilder> mapDados = arquivo.build();

		String linha = getLinha(mapDados);

		assertEquals(linha, linhaValida.toString());
	}

	private String getLinha(Map<Integer, StringBuilder> mapDados) {
		StringBuilder builder = new StringBuilder();

		Collection<StringBuilder> dados = mapDados.values();

		Iterator<StringBuilder> iterator = dados.iterator();
		while (iterator.hasNext()) {
			builder.append(iterator.next());
		}

		return builder.toString();
	}

	private void carregarMocks() {
		expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.ESCREVER_MENSAGEM_CONTA_TRES_PARTES)).andReturn("true").times(2);
		expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).andReturn("false").times(2);
		expect(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.REFERENCIA_ANTERIOR_PARA_QUALIDADE_AGUA)).andReturn("false").times(2);
		replay(repositorioParametros);
	}
}
