package br.gov.batch.servicos.faturamento;

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.Cliente;
import br.gov.model.cadastro.Imovel;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ClienteRepositorio;
import br.gov.servicos.to.ContaTO;

@Stateless
public class ContaBO {
	
	@EJB
	private ClienteRepositorio clienteRepositorio;
	
	public Date determinarVencimentoConta(Imovel imovel){
		ContaTO contaTO = new ContaTO();
		contaTO = vencimentoAlternativo(contaTO);
		
		if (contaTO.comVencimentoAlternativo()){
			if (contaTO.vencimentoMesSeguinte()){
				contaTO.adicionaMesAoVencimento();
			}else{
				contaTO.ajustaDiaVencimentoAlternativo();
				
				if (contaTO.vencimentoRotaSuperiorAoAlternativo()){
					Date dataAtualMaisDiasMinimoEmissao = Utilitarios.adicionarDias(new Date(),	contaTO.getNumeroMinimoDiasEmissaoVencimento());
					
					
				}
			}
		}
		return null;
	}
	
	public ContaTO vencimentoAlternativo(ContaTO contaTO){
		if (contaTO.getImovel().existeDiaVencimento() && !contaTO.getImovel().emissaoExtratoFaturamento()) {
			contaTO.setDiaVencimentoAlternativo(contaTO.getImovel().getDiaVencimento());
			contaTO.setIndicadorVencimentoMesSeguinte(contaTO.getImovel().getIndicadorVencimentoMesSeguinte());
		} else {
			Cliente cliente = clienteRepositorio.buscarClienteResponsavelPorImovel(contaTO.getImovel().getId());

			if (cliente != null) {
				if (cliente.existeDiaVencimento()) {
					contaTO.setDiaVencimentoAlternativo(cliente.getDiaVencimento());
					contaTO.setIndicadorVencimentoMesSeguinte(cliente.getIndicadorVencimentoMesSeguinte());
				}
				else if (contaTO.getImovel().emissaoExtratoFaturamento()) {
					contaTO.setDiaVencimentoAlternativo(Utilitarios.obterUltimoDiaMes(contaTO.getDataVencimentoConta()));
				}
			}
		}
		
		return contaTO;
	}
}
