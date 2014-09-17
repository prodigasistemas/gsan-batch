package br.gov.batch.servicos.faturamento.arquivo;

import br.gov.batch.servicos.micromedicao.HidrometroBO;
import br.gov.model.cadastro.Cliente;
import br.gov.model.cadastro.ClienteImovel;
import br.gov.model.cadastro.ClienteRelacaoTipo;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.endereco.ClienteEndereco;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.arrecadacao.DebitoAutomaticoRepositorio;
import br.gov.servicos.cadastro.ClienteEnderecoRepositorio;
import br.gov.servicos.to.DadosBancariosTO;

//@Stateless
public class ArquivoTextoTipo01 {
	
	private Imovel imovel;
	
//	@EJB
	private ClienteEnderecoRepositorio clienteEnderecoRepositorio; 
	
//	@EJB
	private DebitoAutomaticoRepositorio debitoAutomaticoRepositorio;
	
//	@EJB
	private HidrometroBO hidrometroBO;
	
	private ArquivoTextoTipo01(Builder builder) {
		
	}

	public class Builder{
		
		Imovel imovel;
		
		String enderecoFormatado = "";
		
		public ArquivoTextoTipo01 build(){
			StringBuilder builder = new StringBuilder();
			
			builder.append("01");
			builder.append(Utilitarios.completaComZerosEsquerda(9, String.valueOf(imovel.getId())));
			builder.append(Utilitarios.completaTexto(25, imovel.getLocalidade().getGerenciaRegional().getNome()));
			builder.append(Utilitarios.completaTexto(25, imovel.getLocalidade().getDescricao()));
			
			Cliente clienteNomeConta = null;
			Cliente clienteUsuario = null;
			Cliente clienteResponsavel = null;

	
			if (imovel.getClienteImoveis().size() > 0){
				ClienteImovel clienteImovel = imovel.getClienteImoveis().get(0);
				
				if (clienteImovel.nomeParaConta()){
					clienteNomeConta = clienteImovel.getCliente();
				}
				
				if (clienteImovel.getClienteRelacaoTipo().getId() == ClienteRelacaoTipo.USUARIO.intValue()){
					clienteUsuario = clienteImovel.getCliente();
				}else{
					clienteResponsavel = clienteImovel.getCliente();
				}
			}

			builder.append(Utilitarios.completaTexto(30, clienteUsuario.getNome()));

			builder.append(Utilitarios.completaTexto(16, ""));

			builder.append(Utilitarios.completaTexto(17, imovel.getInscricaoFormatadaSemPonto()));

			builder.append(Utilitarios.completaTexto(70, enderecoFormatado == null ? "" : enderecoFormatado));

			builder.append(Utilitarios.completaTexto(7, ""));
			
			if (clienteResponsavel != null){
				if (clienteNomeConta != null){
					builder.append(Utilitarios.completaComZerosEsquerda(9, clienteNomeConta.getId()));
					builder.append(Utilitarios.completaTexto(25, clienteNomeConta.getNome()));
				}else{
					builder.append(Utilitarios.completaComZerosEsquerda(9, clienteResponsavel.getId()));
					builder.append(Utilitarios.completaTexto(25, clienteResponsavel.getNome()));
				}
				
				if (imovel.enviarContaParaImovel()){
					builder.append(Utilitarios.completaTexto(75, enderecoFormatado == null ? "" : enderecoFormatado));
				}else{
					ClienteEndereco clienteEndereco = clienteEnderecoRepositorio.pesquisarEnderecoCliente(clienteResponsavel.getId());
					
					if (clienteEndereco != null){
						builder.append(Utilitarios.completaTexto(75, clienteEndereco.getEnderecoFormatadoAbreviado().toString()));
					}
				}
			}else{
				builder.append(Utilitarios.completaTexto(109, ""));
			}

			builder.append(imovel.getLigacaoAguaSituacao().getId().toString());
			builder.append(imovel.getLigacaoEsgotoSituacao().getId().toString());

			DadosBancariosTO dadosBancarios =  debitoAutomaticoRepositorio.dadosBancarios(imovel.getId());

			if (dadosBancarios != null) {
				builder.append(Utilitarios.completaTexto(15, dadosBancarios.getDescricaoBanco()));
				builder.append(Utilitarios.completaTexto(5, dadosBancarios.getCodigoAgencia()));
			} else {
				builder.append(Utilitarios.completaTexto(20, ""));
			}
			
			if (imovel.getImovelCondominio() != null) {
				builder.append(Utilitarios.completaComZerosEsquerda(9, imovel.getImovelCondominio().getId()));
			} else {
				builder.append(Utilitarios.completaTexto(9, ""));
			}
			
			builder.append(imovel.getIndicadorImovelCondominio().toString());

			builder.append(Utilitarios.completaComZerosEsquerda(2, imovel.getImovelPerfil().getId()));
			

			boolean houveIntslacaoHidrometro = hidrometroBO.houveSubstituicao(imovel.getId());

			
			return new ArquivoTextoTipo01(this);
		}
		
		public Builder imovel(Imovel imovel){
			this.imovel = imovel;
			return this;
		}
		
		public Builder enderecoFormatado(String endereco){
			this.enderecoFormatado = endereco;
			return this;
		}
	}
}
