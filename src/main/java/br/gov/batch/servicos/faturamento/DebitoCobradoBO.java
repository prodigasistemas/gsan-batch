package br.gov.batch.servicos.faturamento;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.MergeProperties;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.DebitoCobrado;
import br.gov.model.faturamento.DebitoCobrar;
import br.gov.model.faturamento.DebitoCobrarCategoria;
import br.gov.model.faturamento.DebitoCobrarGeral;
import br.gov.servicos.to.DebitoCobradoTO;

@Stateless
public class DebitoCobradoBO {

	@EJB
	private DebitoCobrarBO debitoCobrarBO;
	
	@EJB
	private DebitoCobradoCategoriaBO debitoCobradoCategoriaBO;
	
	@EJB
	private DebitoCobrarCategoriaBO debitoCobrarCategoriaBO;
	
	public DebitoCobradoTO gerarDebitoCobrado(Imovel imovel, int anoMesFaturamento){
		DebitoCobradoTO to = new DebitoCobradoTO();
		
		Collection<DebitoCobrar> colecaoDebitosACobrar = debitoCobrarBO.debitosCobrarVigentes(imovel);
		
		BigDecimal valorPrestacao = null;
		
		for (DebitoCobrar debitoACobrar : colecaoDebitosACobrar) {
			valorPrestacao = debitoACobrar.getValorPrestacao();
			
			valorPrestacao = valorPrestacao.add(debitoACobrar.getResiduoPrestacao()).setScale(2);
			
			DebitoCobrado debitoCobrado = new DebitoCobrado();
			MergeProperties.mergeProperties(debitoCobrado, debitoACobrar);

			DebitoCobrarGeral debitoACobrarGeral = new DebitoCobrarGeral();
			debitoACobrarGeral.setId(debitoACobrar.getId());
			debitoCobrado.setDebitoCobrarGeral(debitoACobrarGeral);
			debitoCobrado.setUltimaAlteracao(new Date());
			debitoCobrado.setValorPrestacao(valorPrestacao);
			debitoCobrado.setNumeroPrestacao(debitoACobrar.getNumeroPrestacaoDebito());
			debitoCobrado.setNumeroPrestacaoDebito((short) (debitoACobrar.getNumeroPrestacaoCobradas() + 1));
			
			List<DebitoCobrarCategoria> dCobrarCategoria   = debitoCobrarCategoriaBO.dividePrestacaoDebitoPelasEconomias(debitoACobrar.getId(), valorPrestacao);
			
			debitoACobrar.setNumeroPrestacaoCobradas(new Integer(debitoACobrar.getNumeroPrestacaoCobradas() + 1).shortValue());
			debitoACobrar.setAnoMesReferenciaPrestacao(anoMesFaturamento);

			to.addDebitoCobrado(debitoCobrado);
			to.addValorDebito(valorPrestacao);
			to.addDebitoCobrarAtualizado(debitoACobrar);
			to.setCategorias(debitoCobradoCategoriaBO.listaDebitoCobradoCategoriaPeloCobrar(dCobrarCategoria));
		}
		return to;
	}
}
