package br.gov.batch.servicos.faturamento.arquivo;

import static br.gov.model.util.Utilitarios.completaComEspacosADireita;
import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.completaTexto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.logging.Logger;

import br.gov.batch.servicos.faturamento.ContaBO;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.Cliente;
import br.gov.model.cadastro.ClienteImovel;
import br.gov.model.cadastro.ClienteRelacaoTipo;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.endereco.ClienteEndereco;
import br.gov.servicos.cadastro.ClienteEnderecoRepositorio;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.faturamento.FaturamentoParametroRepositorio;

@Stateless
public class ArquivoTextoTipo01DadosCliente {
    
    private static Logger logger = Logger.getLogger(ArquivoTextoTipo01DadosCliente.class);

	@EJB
    private ClienteEnderecoRepositorio clienteEnderecoRepositorio;
	
	@EJB
    private FaturamentoParametroRepositorio repositorioParametros;
	
    @EJB
    private ImovelRepositorio imovelRepositorio;

	@EJB
	private ContaBO contaBO;
	
	private Map<Integer, StringBuilder> dadosCliente;
	
	private Imovel imovel;
	private Cliente clienteNomeConta = null;
    private Cliente clienteUsuario = null;
    private Cliente clienteResponsavel = null;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Map<Integer, StringBuilder> build(ArquivoTextoTO to) {
	    this.imovel = to.getImovel();
		dadosCliente = new HashMap<Integer, StringBuilder>();
		
	    escreverNomeCliente();
		escreverDadosClienteResponsavel();
		escreverIndicadorEmissaoConta();
		escreverCpfCnpjDoClienteUsuario();
		
		return dadosCliente;
	}

	private void escreverNomeCliente() {
		StringBuilder builder = new StringBuilder();
		
		List<ClienteImovel> clientesAtivos = imovel.getClienteImoveis().stream()
												.filter(c -> c.getDataFimRelacao() == null)
												.filter(c -> c.getClienteRelacaoTipo().getId().intValue() != ClienteRelacaoTipo.PROPRIETARIO.intValue())
												.collect(Collectors.toList());
		
		for(ClienteImovel clienteImovel : clientesAtivos) {
			if (clienteImovel.nomeParaConta()) {
				clienteNomeConta = clienteImovel.getCliente();
			}
			
			if (clienteImovel.getClienteRelacaoTipo().getId() == ClienteRelacaoTipo.USUARIO.intValue()) {
				clienteUsuario = clienteImovel.getCliente();
			} else {
				clienteResponsavel = clienteImovel.getCliente();
			}
	    }
	
		if (clienteUsuario != null) {
		    builder.append(completaComEspacosADireita(30, clienteUsuario.getNome()));
		} else {
		    builder.append(completaComEspacosADireita(30, ""));
		}
	    
	    dadosCliente.put(2, builder);
	}

	private void escreverDadosClienteResponsavel() {
		StringBuilder builder = new StringBuilder();
		
		if (clienteResponsavel != null) {
	        if (clienteNomeConta != null) {
	            builder.append(completaComZerosEsquerda(9, clienteNomeConta.getId()));
	            builder.append(completaComEspacosADireita(25, clienteNomeConta.getNome()));
	        } else {
	            builder.append(completaComZerosEsquerda(9, clienteResponsavel.getId()));
	            builder.append(completaComEspacosADireita(25, clienteResponsavel.getNome()));
	        }
	
	        if (imovel.enviarContaParaImovel()) {
	            builder.append(completaComEspacosADireita(75, imovel.getEnderecoFormatadoAbreviado()));
	        } else {
	            ClienteEndereco clienteEndereco = clienteEnderecoRepositorio.pesquisarEnderecoCliente(clienteResponsavel.getId());
	
	            if (clienteEndereco != null) {
	                builder.append(completaComEspacosADireita(75, clienteEndereco.getEnderecoFormatadoAbreviado().toString()));
	            }else{
	                builder.append(completaTexto(75, ""));
	            }
	        }
	    } else {
	        builder.append(completaComEspacosADireita(109, ""));
	    }
		
		dadosCliente.put(7, builder);
	}
	
	private void escreverCpfCnpjDoClienteUsuario() {
		StringBuilder builder = new StringBuilder();
		
		if (clienteUsuario != null && !clienteUsuario.equals("")) {
			builder.append(completaComEspacosADireita(18, clienteUsuario.getCpfOuCnpj()));
		} else {
			builder.append(completaComEspacosADireita(18, ""));
		}
		
		dadosCliente.put(35, builder);
    }
	
	private void escreverIndicadorEmissaoConta() {
		StringBuilder builder = new StringBuilder();
		
		Short indicadorEmissaoConta = new Short("1");

        boolean emitir = contaBO.emitirConta(imovel);

        if (clienteResponsavel != null && !emitir) {
            indicadorEmissaoConta = new Short("2");
        }

        builder.append(indicadorEmissaoConta.toString());
        
        dadosCliente.put(14, builder);
    }	
}
