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
		
		return null;
	}
	
	public ContaTO vencimentoAlternativo(Imovel imovel, Date dataContaVencimento){
		ContaTO contaTO = new ContaTO();
		
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
					contaTO.setDiaVencimentoAlternativo(Utilitarios.obterUltimoDiaMes(dataContaVencimento));
				}
			}
			
		}
		
		return contaTO;
	}
}
