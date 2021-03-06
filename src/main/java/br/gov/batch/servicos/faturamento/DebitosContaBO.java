package br.gov.batch.servicos.faturamento;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.MergeProperties;
import br.gov.model.faturamento.DebitoCobrado;
import br.gov.model.faturamento.DebitoCobrar;
import br.gov.model.faturamento.DebitoCobrarCategoria;
import br.gov.model.faturamento.DebitoCobrarGeral;
import br.gov.servicos.to.DebitosContaTO;

@Stateless
public class DebitosContaBO {

	@EJB
	private DebitoCobrarBO debitoCobrarBO;
	
	@EJB
	private DebitoCobradoCategoriaBO debitoCobradoCategoriaBO;
	
	@EJB
	private DebitoCobrarCategoriaBO debitoCobrarCategoriaBO;
		
	public DebitosContaTO gerarDebitosConta(Integer idImovel, int anoMesFaturamento){
		DebitosContaTO to = new DebitosContaTO();
		
		Collection<DebitoCobrar> colecaoDebitosACobrar = debitoCobrarBO.debitosCobrarVigentes(idImovel);
		
		BigDecimal valorPrestacao = null;
		
		for (DebitoCobrar debitoACobrar : colecaoDebitosACobrar) {
			valorPrestacao = debitoACobrar.getValorPrestacao();
			
			valorPrestacao = valorPrestacao.add(debitoACobrar.getResiduoPrestacao()).setScale(2);
			
			DebitoCobrado debitoCobrado = new DebitoCobrado();
			MergeProperties.mergeProperties(debitoCobrado, debitoACobrar);

			DebitoCobrarGeral debitoACobrarGeral = new DebitoCobrarGeral();
			debitoACobrarGeral.setId(debitoACobrar.getId());
			debitoCobrado.setId(null);
			debitoCobrado.setDebitoCobrarGeral(debitoACobrarGeral);
			debitoCobrado.setDebitoTipo(debitoACobrar.getDebitoTipo());
			debitoCobrado.setUltimaAlteracao(new Date());
			debitoCobrado.setValorPrestacao(valorPrestacao);
			debitoCobrado.setNumeroPrestacao(debitoACobrar.getNumeroPrestacaoDebito());
			debitoCobrado.setNumeroPrestacaoDebito((short) (debitoACobrar.getNumeroPrestacaoCobradas() + 1));
			debitoCobrado.setUltimaAlteracao(new Date());
			debitoCobrado.setLancamentoItemContabil(debitoACobrar.getLancamentoItemContabil());
			debitoCobrado.setLocalidade(debitoACobrar.getLocalidade());
			debitoCobrado.setQuadra(debitoACobrar.getQuadra());
			debitoCobrado.setCodigoSetorComercial(debitoACobrar.getCodigoSetorComercial());
			debitoCobrado.setNumeroQuadra(debitoACobrar.getNumeroQuadra());
			debitoCobrado.setNumeroLote(debitoACobrar.getNumeroLote());
			debitoCobrado.setNumeroSubLote(debitoACobrar.getNumeroSubLote());
			debitoCobrado.setAnoMesReferenciaDebito(debitoACobrar.getAnoMesReferenciaDebito());
			debitoCobrado.setAnoMesCobrancaDebito(debitoACobrar.getAnoMesCobrancaDebito());
			debitoCobrado.setNumeroPrestacao(debitoACobrar.getNumeroPrestacaoDebito());
			debitoCobrado.setTipoFinanciamento(debitoACobrar.getTipoFinanciamento());
			debitoCobrado.setNumeroParcelaBonus(debitoACobrar.getNumeroParcelaBonus() != null ? debitoACobrar.getNumeroParcelaBonus() : 0);
			debitoCobrado.setNumeroPrestacaoDebito((short) (debitoACobrar.getNumeroPrestacaoCobradas() + 1));
			
			List<DebitoCobrarCategoria> dCobrarCategoria   = debitoCobrarCategoriaBO.dividePrestacaoDebitoPelasEconomias(debitoACobrar.getId(), valorPrestacao);
			
			debitoACobrar.setNumeroPrestacaoCobradas(new Integer(debitoACobrar.getNumeroPrestacaoCobradas() + 1).shortValue());
			debitoACobrar.setAnoMesReferenciaPrestacao(anoMesFaturamento);

			to.addDebitoCobrado(debitoCobrado);
			to.addValorDebito(valorPrestacao);
			to.addDebitoCobrarAtualizado(debitoACobrar);
			to.addCategorias(debitoCobrado, debitoCobradoCategoriaBO.listaDebitoCobradoCategoriaPeloCobrar(dCobrarCategoria));
		}
		return to;
	}
}