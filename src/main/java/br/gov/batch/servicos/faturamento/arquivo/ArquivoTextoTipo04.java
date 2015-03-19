package br.gov.batch.servicos.faturamento.arquivo;

import static br.gov.model.util.Utilitarios.completaComEspacosADireita;
import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.completaTexto;
import static br.gov.model.util.Utilitarios.completaTextoADireita;
import static br.gov.model.util.Utilitarios.formatarAnoMesParaMesAno;
import static br.gov.model.util.Utilitarios.formatarBigDecimalComPonto;
import static br.gov.model.util.Utilitarios.quebraLinha;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.faturamento.Conta;
import br.gov.servicos.faturamento.DebitoCobradoRepositorio;
import br.gov.servicos.to.DebitoCobradoNaoParceladoTO;
import br.gov.servicos.to.ParcelaDebitoCobradoTO;

@Stateless
public class ArquivoTextoTipo04 extends ArquivoTexto {
	@EJB
	private DebitoCobradoRepositorio debitoCobradoRepositorio;

	public ArquivoTextoTipo04() {
		super();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String build(ArquivoTextoTO to) {
	    //TODO: Criar TO para guardar os dados da linha04, para depois escrever no final
	    //TODO: Refatorar linha 04
	    
		Conta conta = to.getConta();
		
		if (conta != null) {
			Collection<ParcelaDebitoCobradoTO> colecaoDebitoCobradoDeParcelamento = debitoCobradoRepositorio.pesquisarDebitoCobradoParcelamento(conta.getId());

			if (!colecaoDebitoCobradoDeParcelamento.isEmpty()) {
				for (ParcelaDebitoCobradoTO debitoParcelamento : colecaoDebitoCobradoDeParcelamento) {
					builder.append(TIPO_REGISTRO_04_PARCELAMENTO);
					builder.append(completaComZerosEsquerda(9, conta.getImovel().getId()));
					builder.append(getDescricaoServicoParcelamento(debitoParcelamento));
					builder.append(completaComZerosEsquerda(14, formatarBigDecimalComPonto(debitoParcelamento.getTotalPrestacao())));
					builder.append(completaTexto(6, debitoParcelamento.getCodigoConstante()));
					builder.append(quebraLinha);
				}
			}
			
			builder.append(buildDebitoCobradoSemParcelamento(conta));
		}

		return builder.toString();
	}

	public StringBuilder buildDebitoCobradoSemParcelamento(Conta conta){
	    Collection<DebitoCobradoNaoParceladoTO> debitosCobradosNaoParcelados = debitoCobradoRepositorio.pesquisarDebitoCobradoSemParcelamento(conta.getId());
	    
	    Map<Integer, ArquivoTextoTipo04DebitoCobradoSemParcelamento> linhas = new LinkedHashMap<Integer, ArquivoTextoTipo04DebitoCobradoSemParcelamento>();
	    
	    debitosCobradosNaoParcelados.forEach(e -> {
	        ArquivoTextoTipo04DebitoCobradoSemParcelamento linha = linhas.get(e.getDebitoTipo());
	        
	        if (linha == null){
	            linha = new ArquivoTextoTipo04DebitoCobradoSemParcelamento();
	            linha.texto = new StringBuilder(e.getDescricaoTipoDebito()).append(" ");
	            linha.constante = e.getConstanteTipoDebito();
	            
	            linhas.put(e.getDebitoTipo(), linha);
	        }
	        
	        if (e.getAnoMesReferencia() == null){
                linha.texto.append("PARCELA ")
                    .append(completaComZerosEsquerda(3, e.getNumeroPrestacaoDebito()))
                    .append("/")
                    .append(completaComZerosEsquerda(3, e.getTotalPrestacao()));
	        }else{
	            if (linha.qtdMeses == 6){
	                linha.texto.append("E OUTRAS");
	            }else if (linha.qtdMeses < 6){
	                linha.texto.append(formatarAnoMesParaMesAno(e.getAnoMesReferencia())).append(" ");
	            }
	        }
	        
	        linha.soma = linha.soma.add(e.getValorPrestacao());
	        linha.qtdMeses++;
	    });
	    
	    StringBuilder texto = new StringBuilder();
	    
	    for(ArquivoTextoTipo04DebitoCobradoSemParcelamento item: linhas.values()){
	        texto
	        .append(TIPO_REGISTRO_04_PARCELAMENTO)
	        .append(completaComZerosEsquerda(9, conta.getImovel().getId().toString()))
	        .append(completaTextoADireita(90, item.texto))
	        .append(completaComZerosEsquerda(14, formatarBigDecimalComPonto(item.soma)))
	        .append(completaTexto(6, item.constante))
	        .append(quebraLinha);
	    }
	    
	    return texto;
	}

	private String getDescricaoServicoParcelamento(ParcelaDebitoCobradoTO debito) {
		StringBuilder descricao = new StringBuilder();
		descricao.append("PARCELAMENTO DE DEBITOS PARCELA ")
		         .append(completaComZerosEsquerda(3, String.valueOf(debito.getNumeroPrestacaoDebito())))
		         .append("/")
		         .append(completaComZerosEsquerda(3, String.valueOf(debito.getTotalParcela())));

		return completaComEspacosADireita(90, descricao.toString());
	}
	
	class ArquivoTextoTipo04DebitoCobradoSemParcelamento{
        Integer tipoDebito = -1;
	    StringBuilder texto = new StringBuilder();
	    BigDecimal soma = BigDecimal.ZERO;
	    Integer constante;
	    Integer qtdMeses = 1;
	}
}