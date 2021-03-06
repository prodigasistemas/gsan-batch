package br.gov.batch.servicos.faturamento;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.DebitoCobrar;
import br.gov.servicos.arrecadacao.pagamento.PagamentoRepositorio;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.DebitoCobrarRepositorio;

@Stateless
public class DebitoCobrarBO {

	@EJB
	private DebitoCobrarRepositorio debitoCobrarRepositorio;
	
	@EJB
	private PagamentoRepositorio pagamentoRepositorio;
	
	@EJB
	private SistemaParametrosRepositorio sistemaParametrosRepositorio;

	private SistemaParametros parametros;
	
	@PostConstruct
	public void init(){
		parametros = sistemaParametrosRepositorio.getSistemaParametros();
	}

	public Collection<DebitoCobrar> debitosCobrarVigentes(Integer idImovel){
		Collection<DebitoCobrar> lista = debitoCobrarRepositorio.debitosCobrarPorImovelComPendenciaESemRevisao(idImovel);
		
		Integer anoMesFaturamento = parametros.getAnoMesFaturamento();
		
		Collection<DebitoCobrar> debitos = new ArrayList<DebitoCobrar>();
		
		for (DebitoCobrar debito : lista) {
			if (!(debito.pertenceParcelamento(anoMesFaturamento))){
				debitos.add(debito);
			}
		}
		
		return debitos;
	}

	public Collection<DebitoCobrar> debitosCobrarSemPagamentos(Integer idImovel) {
		Integer anoMesFaturamento = parametros.getAnoMesFaturamento();
		Collection<DebitoCobrar> lista = debitoCobrarRepositorio.debitosCobrarPorImovelComPendenciaESemRevisao(idImovel);
		Collection<DebitoCobrar> debitos = new ArrayList<DebitoCobrar>();

		for (DebitoCobrar debito : lista) {
			if (!debito.pertenceParcelamento(anoMesFaturamento) && pagamentoRepositorio.debitoSemPagamento(debito.getId())){
				debitos.add(debito);
			}
		}
		return debitos;
	}
	
	public void atualizarDebitoCobrar(List<DebitoCobrar> debitosCobrar){
		debitoCobrarRepositorio.atualizarDebitoCobrar(debitosCobrar);
	}
}
