package br.gov.batch.servicos.faturamento.arquivo;

import static br.gov.model.util.Utilitarios.completaComEspacosADireita;
import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.completaTexto;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.batch.servicos.faturamento.ContaBO;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.Cliente;
import br.gov.model.cadastro.ClienteImovel;
import br.gov.model.cadastro.ClienteRelacaoTipo;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.ImovelContaEnvio;
import br.gov.model.faturamento.FaturamentoParametro.NOME_PARAMETRO_FATURAMENTO;
import br.gov.servicos.cadastro.ClienteEnderecoRepositorio;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.faturamento.FaturamentoParametroRepositorio;

public class ArquivoTextoTipo01DadosClienteTest {

	@InjectMocks
	private ArquivoTextoTipo01DadosCliente arquivo;

	@Mock
	private FaturamentoParametroRepositorio repositorioParametros;
	
	@Mock
	private ImovelRepositorio repositorioImovel;
	
	@Mock
	private ClienteEnderecoRepositorio clienteEnderecoRepositorio;
	
	@Mock
	private ContaBO contaBO;

	private ArquivoTextoTO arquivoTextoTO;

	private Imovel imovel;
	
	private Cliente clienteResponsavel;
	
	private String naoEmitirConta = "2";

	@Before
	public void setup() {
		imovel = new Imovel(1234567);

		Cliente clienteUsuario = new Cliente();
		clienteUsuario.setNome("MARIA JOSE DA SILVA");
		clienteUsuario.setCpf("11111111111");

		clienteResponsavel = new Cliente();
		clienteResponsavel.setId(123124);
		clienteResponsavel.setNome("JOAO ROBERTO SOUZA");
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

		imovel.setImovelContaEnvio(ImovelContaEnvio.ENVIAR_CLIENTE_RESPONSAVEL);

		arquivo = new ArquivoTextoTipo01DadosCliente();
		
		arquivoTextoTO = new ArquivoTextoTO();
		arquivoTextoTO.setImovel(imovel);
		
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testaQuantidadeDados() {
		carregarMocks();

		Map<Integer, StringBuilder> mapDados = arquivo.build(arquivoTextoTO);

		assertNotNull(mapDados);
		assertEquals(4, mapDados.keySet().size());
	}

	@Test
	public void testaFormatoDados() {
		carregarMocks();

		StringBuilder linhaValida = new StringBuilder();
		linhaValida.append("MARIA JOSE DA SILVA           11111111111                                        ")
		    .append(completaComZerosEsquerda(9, clienteResponsavel.getId()))
		    .append(completaComEspacosADireita(25, clienteResponsavel.getNome()))
		    .append("                                                                            1");

		Map<Integer, StringBuilder> mapDados = arquivo.build(arquivoTextoTO);

        StringBuilder trechoResponsavel = new StringBuilder();
        trechoResponsavel.append(completaComZerosEsquerda(9, clienteResponsavel.getId()))
            .append(completaComEspacosADireita(25, clienteResponsavel.getNome()))
            .append(completaTexto(109-34, " "));

		assertEquals(completaComEspacosADireita(30, "MARIA JOSE DA SILVA"), mapDados.get(2).toString());
		assertEquals(trechoResponsavel.toString(), mapDados.get(7).toString());
		assertEquals(naoEmitirConta, mapDados.get(14).toString());
		assertEquals(completaComEspacosADireita(18, "11111111111"), mapDados.get(35).toString());
	}

	private void carregarMocks() {
	    when(contaBO.emitirConta(imovel)).thenReturn(Boolean.FALSE);
	    
	    when(repositorioImovel.obterPorID(imovel.getId())).thenReturn(imovel);
	    
	    when(clienteEnderecoRepositorio.pesquisarEnderecoCliente(clienteResponsavel.getId())).thenReturn(null);
	    
		when(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.ESCREVER_MENSAGEM_CONTA_TRES_PARTES)).thenReturn("true");
		when(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN)).thenReturn("false");
		when(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.REFERENCIA_ANTERIOR_PARA_QUALIDADE_AGUA)).thenReturn("false");
	}
}
