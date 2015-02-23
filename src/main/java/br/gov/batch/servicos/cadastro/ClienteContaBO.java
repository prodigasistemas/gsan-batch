package br.gov.batch.servicos.cadastro;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.ClienteConta;
import br.gov.model.cadastro.ClienteImovel;
import br.gov.model.faturamento.Conta;
import br.gov.servicos.cadastro.ClienteContaRepositorio;
import br.gov.servicos.cadastro.ClienteImovelRepositorio;

@Stateless
public class ClienteContaBO {

	@EJB
	private ClienteImovelRepositorio clienteImovelRepositorio;
	
	@EJB
	private ClienteContaRepositorio clienteContaRepositorio;
	
	public void inserirClienteContaComImoveisAtivos(Integer idImovel, Conta conta) {
		List<ClienteImovel> clienteImovelAtivos = clienteImovelRepositorio.pesquisarClienteImovelAtivos(idImovel);

		clienteImovelAtivos.forEach(clienteImovel -> {
			ClienteConta clienteConta = new ClienteConta();
			clienteConta.setConta(conta);
			clienteConta.setCliente(clienteImovel.getCliente());
			clienteConta.setClienteRelacaoTipo(clienteImovel.getClienteRelacaoTipo());
			clienteConta.setIndicadorNomeConta(clienteImovel.getIndicadorNomeConta());
			clienteConta.setUltimaAlteracao(new Date());
			
			clienteContaRepositorio.inserir(clienteConta);
		});
	}
}
