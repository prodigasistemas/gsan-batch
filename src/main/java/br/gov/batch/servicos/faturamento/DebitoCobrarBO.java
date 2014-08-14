package br.gov.batch.servicos.faturamento;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.hibernate.loader.plan.exec.process.spi.ReturnReader;

import br.gov.model.cadastro.Imovel;
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

	public Collection<DebitoCobrar> debitosCobrarVigentes(Imovel imovel){
		Collection<DebitoCobrar> lista = debitoCobrarRepositorio.debitosCobrarPorImovelComPendenciaESemRevisao(imovel);
		
		Integer anoMesFaturamento = parametros.getAnoMesFaturamento();
		
		Collection<DebitoCobrar> debitos = new ArrayList<DebitoCobrar>();
		
		for (DebitoCobrar debito : lista) {
			if (!(debito.pertenceParcelamento(anoMesFaturamento))){
				debitos.add(debito);
			}
		}
		
		return debitos;
	}

	public Collection<DebitoCobrar> debitosCobrarSemPagamentos(Imovel imovel) {
		Integer anoMesFaturamento = parametros.getAnoMesFaturamento();
		Collection<DebitoCobrar> lista = debitoCobrarRepositorio.debitosCobrarPorImovelComPendenciaESemRevisao(imovel);
		Collection<DebitoCobrar> debitos = new ArrayList<DebitoCobrar>();

		for (DebitoCobrar debito : lista) {
			if (!debito.pertenceParcelamento(anoMesFaturamento) && pagamentoRepositorio.debitoSemPagamento(debito.getId())){
				debitos.add(debito);
			}
		}
		return debitos;
	}
}
