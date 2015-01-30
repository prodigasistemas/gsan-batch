package br.gov.batch.servicos.faturamento.arquivo;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.Status;
import br.gov.model.micromedicao.ConsumoAnormalidade;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.micromedicao.ConsumoAnormalidadeRepositorio;

@Stateless
public class ArquivoTextoTipo13 {

	@EJB
	private ConsumoAnormalidadeRepositorio consumoAnormalidadeRepositorio;
	
	private StringBuilder builder;
	
	private final String TIPO_REGISTRO = "13";
	
	public String build(){
		builder = new StringBuilder();
		List<ConsumoAnormalidade> listaConsumoAnormalidade = consumoAnormalidadeRepositorio
				.listarConsumoAnormalidadePor(Status.ATIVO.getId());
		
		builder.append(TIPO_REGISTRO);
		for (ConsumoAnormalidade consumoAnormalidade : listaConsumoAnormalidade) {
			builder.append(Utilitarios.completaComZerosEsquerda(2, consumoAnormalidade.getId()));
			builder.append(Utilitarios.completaComEspacosADireita(120, consumoAnormalidade.getMensagemConta()));
			builder.append(System.getProperty("line.separator"));
		}
		
		return builder.toString();
	}
	
}
