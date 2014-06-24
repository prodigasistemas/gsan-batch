package br.gov.batch.servicos.faturamento;

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.Status;
import br.gov.model.cadastro.Cliente;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.ImovelContaEnvio;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.Conta;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ClienteRepositorio;
import br.gov.servicos.to.ContaTO;

@Stateless
public class ContaBO {
	
	@EJB
	private ClienteRepositorio clienteRepositorio;
	
	@EJB
	private SistemaParametros sistemaParametros;
	
	public Conta gerarConta(Imovel imovel, Date dataVencimentoRota){
		Conta.Builder builder = new Conta.Builder();
		builder.dataVencimentoConta(this.determinarVencimentoConta(imovel, dataVencimentoRota));
		Conta conta = builder.build();
		
		return conta;
	}
	
	public Date determinarVencimentoConta(Imovel imovel, Date dataVencimentoRota){
		ContaTO contaTO = vencimentoAlternativo(imovel, dataVencimentoRota);
		
		if (contaTO.comVencimentoAlternativo()){
			if (contaTO.vencimentoMesSeguinte()){
				contaTO.adicionaMesAoVencimento();
			}else if (dataVencimentoRota.after(contaTO.getDataVencimentoConta())){
				Date dataAtualMaisDiasMinimoEmissao = Utilitarios.adicionarDias(new Date(),	sistemaParametros.getNumeroMinimoDiasEmissaoVencimento());
				
				if (dataAtualMaisDiasMinimoEmissao.after(contaTO.getDataVencimentoConta())){
					contaTO.adicionaMesAoVencimento();
				}
			}
		}
		
		if ((imovel.getImovelContaEnvio() == ImovelContaEnvio.ENVIAR_CLIENTE_RESPONSAVEL || imovel.getImovelContaEnvio() == ImovelContaEnvio.NAO_PAGAVEL_IMOVEL_PAGAVEL_RESPONSAVEL)
				&& !contaTO.comVencimentoAlternativo()
				&& imovel.getIndicadorDebitoConta() == Status.INATIVO) {
			contaTO.adicionaDiasAoVencimento(sistemaParametros.getNumeroDiasAdicionaisCorreios());
		}

		return contaTO.getDataVencimentoConta();
	}
	
	public ContaTO vencimentoAlternativo(Imovel imovel, Date dataVencimentoRota){
		ContaTO contaTO = new ContaTO();
		contaTO.setDataVencimentoConta(dataVencimentoRota);
		if (imovel.existeDiaVencimento() && !imovel.emissaoExtratoFaturamento()) {
			contaTO.setDiaVencimentoAlternativo(imovel.getDiaVencimento());
			contaTO.setIndicadorVencimentoMesSeguinte(imovel.getIndicadorVencimentoMesSeguinte());
		} else {
			Cliente cliente = clienteRepositorio.buscarClienteResponsavelPorImovel(imovel.getId());

			if (cliente != null) {
				if (cliente.existeDiaVencimento()) {
					contaTO.setDiaVencimentoAlternativo(cliente.getDiaVencimento());
					contaTO.setIndicadorVencimentoMesSeguinte(cliente.getIndicadorVencimentoMesSeguinte());
				}
				else if (imovel.emissaoExtratoFaturamento()) {
					contaTO.setDiaVencimentoAlternativo(Utilitarios.obterUltimoDiaMes(contaTO.getDataVencimentoConta()));
				}
			}
		}
		
		return contaTO;
	}
}
