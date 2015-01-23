package br.gov.batch.servicos.faturamento.arquivo;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.Cliente;
import br.gov.model.cadastro.ClienteImovel;
import br.gov.model.cadastro.ClienteRelacaoTipo;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.ImovelContaEnvio;
import br.gov.model.cadastro.endereco.ClienteEndereco;
import br.gov.model.faturamento.FaturamentoParametro.NOME_PARAMETRO_FATURAMENTO;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ClienteEnderecoRepositorio;
import br.gov.servicos.faturamento.FaturamentoParametroRepositorio;

@Stateless
public class ArquivoTextoTipo01DadosCliente {

	@EJB
    private ClienteEnderecoRepositorio clienteEnderecoRepositorio;
	
	@EJB
    private FaturamentoParametroRepositorio repositorioParametros;
	
	private Map<Integer, StringBuilder> dadosCliente;
	
	private Imovel imovel;
	private Cliente clienteNomeConta = null;
    private Cliente clienteUsuario = null;
    private Cliente clienteResponsavel = null;

	
	public ArquivoTextoTipo01DadosCliente(Imovel imovel) {
		this.imovel = imovel;
	}
	
	public Map<Integer, StringBuilder> build() {
		dadosCliente = new HashMap<Integer, StringBuilder>();
		
	    escreverNomeCliente();
		escreverDadosClienteResonsavel();
		escreverIndicadorEmissaoConta();
		escreverCpfCnpjDoClienteUsuario();
		
		return dadosCliente;
	}

	private void escreverNomeCliente() {
		StringBuilder builder = new StringBuilder();
		
		if (imovel.getClienteImoveis().size() > 0) {
	        ClienteImovel clienteImovel = imovel.getClienteImoveis().get(0);
	
	        if (clienteImovel.nomeParaConta()) {
	            clienteNomeConta = clienteImovel.getCliente();
	        }
	
	        if (clienteImovel.getClienteRelacaoTipo().getId() == ClienteRelacaoTipo.USUARIO.intValue()) {
	            clienteUsuario = clienteImovel.getCliente();
	        } else {
	            clienteResponsavel = clienteImovel.getCliente();
	        }
	    }
	
	    builder.append(Utilitarios.completaComEspacosADireita(30, clienteUsuario.getNome()));
	    
	    dadosCliente.put(2, builder);
	}

	private void escreverDadosClienteResonsavel() {
		StringBuilder builder = new StringBuilder();
		
		if (clienteResponsavel != null) {
	        if (clienteNomeConta != null) {
	            builder.append(Utilitarios.completaComZerosEsquerda(9, clienteNomeConta.getId()));
	            builder.append(Utilitarios.completaComEspacosADireita(25, clienteNomeConta.getNome()));
	        } else {
	            builder.append(Utilitarios.completaComZerosEsquerda(9, clienteResponsavel.getId()));
	            builder.append(Utilitarios.completaComEspacosADireita(25, clienteResponsavel.getNome()));
	        }
	
	        if (imovel.enviarContaParaImovel()) {
	            builder.append(Utilitarios.completaComEspacosADireita(75, imovel.getEnderecoFormatadoAbreviado()));
	        } else {
	            ClienteEndereco clienteEndereco = clienteEnderecoRepositorio.pesquisarEnderecoCliente(clienteResponsavel.getId());
	
	            if (clienteEndereco != null) {
	                builder.append(Utilitarios.completaComEspacosADireita(75, clienteEndereco.getEnderecoFormatadoAbreviado().toString()));
	            }
	        }
	    } else {
	        builder.append(Utilitarios.completaComEspacosADireita(109, ""));
	    }
		
		dadosCliente.put(7, builder);
	}
	
	private void escreverCpfCnpjDoClienteUsuario() {
		StringBuilder builder = new StringBuilder();
		
		if (clienteUsuario != null && !clienteUsuario.equals("")) {
			builder.append(Utilitarios.completaComEspacosADireita(18, clienteUsuario.getCpfOuCnpj()));
		} else {
			builder.append(Utilitarios.completaComEspacosADireita(18, ""));
		}
		
		dadosCliente.put(35, builder);
    }
	
	private void escreverIndicadorEmissaoConta() {
		StringBuilder builder = new StringBuilder();
		
		Short indicadorEmissaoConta = new Short("1");

        boolean emitir = emitirConta(imovel.getImovelContaEnvio());

        if (clienteResponsavel != null && !emitir) {
            indicadorEmissaoConta = new Short("2");
        }

        builder.append(indicadorEmissaoConta);
        
        dadosCliente.put(15, builder);
    }
	
	public boolean emitirConta(Integer envioConta) {
        boolean emitir = true;

        boolean emitirFebraban = Boolean.valueOf(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.EMITIR_CONTA_CODIGO_FEBRABAN));

        if ((emitirFebraban && enviaConta(envioConta)) || enviaContaClienteResponsavelFinalGrupo(envioConta)) {
            emitir = false;
        }

        return emitir;
    }
	
	private boolean enviaConta(Integer envioConta) {
        return envioConta != null
                && (envioConta == ImovelContaEnvio.ENVIAR_CLIENTE_RESPONSAVEL.getId()
                        || envioConta == ImovelContaEnvio.NAO_PAGAVEL_IMOVEL_PAGAVEL_RESPONSAVEL.getId()
                        || envioConta == ImovelContaEnvio.ENVIAR_CONTA_BRAILLE.getId() || envioConta == ImovelContaEnvio.ENVIAR_CONTA_BRAILLE_RESPONSAVEL
                        .getId());
    }
	
	private boolean enviaContaClienteResponsavelFinalGrupo(Integer envioConta) {
        return envioConta != null && envioConta == ImovelContaEnvio.ENVIAR_CLIENTE_RESPONSAVEL_FINAL_GRUPO.getId();
    }
	
}
