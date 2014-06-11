package br.gov.batch.servicos.faturamento;

import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.DebitoCobrar;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.DebitoCobrarRepositorio;

@Stateless
public class DebitoCobrarBO {

	@EJB
	private DebitoCobrarRepositorio debitoCobrarRepositorio;
	
	@EJB
	private SistemaParametrosRepositorio parametros;
	
	public Collection<DebitoCobrar> debitosCobrarVigentes(Imovel imovel){
		Collection<DebitoCobrar> lista = debitoCobrarRepositorio.debitosCobrarPorImovelComPendenciaESemRevisao(imovel);
		
		Integer anoMesFaturamento = parametros.getAnoMesFaturamento();
		
		Collection<DebitoCobrar> debitos = new ArrayList<DebitoCobrar>();
		
		for (DebitoCobrar debito : lista) {
			if (!(debito.parcelamentoAVencer(anoMesFaturamento) && debito.primeiraParcela())){
				debitos.add(debito);
			}
		}
		
		return debitos;
	}
}
