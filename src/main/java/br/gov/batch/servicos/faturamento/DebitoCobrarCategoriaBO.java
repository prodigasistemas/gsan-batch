package br.gov.batch.servicos.faturamento;

import java.math.BigDecimal;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.faturamento.DebitoCobrarCategoria;
import br.gov.servicos.faturamento.DebitoCobrarCategoriaRepositorio;

@Stateless
public class DebitoCobrarCategoriaBO {
	
	@EJB
	private DebitoCobrarCategoriaRepositorio repositorio;
	
	public List<DebitoCobrarCategoria> dividePrestacaoDebitoPelasEconomias(Long debitoCobrarId, BigDecimal valorPrestacao){
		
		List<DebitoCobrarCategoria> debitos = repositorio.listaPeloDebitoCobrar(debitoCobrarId);

		int totalEconomias = 0;
		
		for (DebitoCobrarCategoria debito : debitos) {
			totalEconomias += debito.getQuantidadeEconomia();
		}
		
		BigDecimal fator = valorPrestacao.divide(new BigDecimal(totalEconomias), 2, BigDecimal.ROUND_DOWN);
		
		BigDecimal total = new BigDecimal(0);
		
		for (DebitoCobrarCategoria debito : debitos) {
			BigDecimal valorPorCategoria = fator.multiply(new BigDecimal(debito.getQuantidadeEconomia()));
			
			BigDecimal valorTruncado = valorPorCategoria.setScale(2, BigDecimal.ROUND_DOWN);
			
			debito.setValorPrestacaoEconomia(valorTruncado);
			
			total = total.add(valorTruncado);
		}
		
		total = total.setScale(7);
		
		if (total.setScale(2, BigDecimal.ROUND_HALF_UP).compareTo(valorPrestacao.setScale(2, BigDecimal.ROUND_HALF_UP)) == -1) {
			BigDecimal diferenca = valorPrestacao.subtract(total);

			diferenca = diferenca.setScale(2, BigDecimal.ROUND_HALF_UP);

			debitos.get(0).addResiduoPrestacao(diferenca);
		}
		
		return debitos;
	}	
}