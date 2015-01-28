package br.gov.batch.servicos.faturamento.arquivo;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.cobranca.to.VencimentoAnteriorTO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cobranca.CobrancaDocumento;
import br.gov.model.cobranca.CobrancaDocumentoItem;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cobranca.CobrancaDocumentoItemRepositorio;

@Stateless
public class ArquivoTextoTipo07 {

	private final String TIPO_REGISTRO = "07";

	@EJB
	private CobrancaDocumentoItemRepositorio cobrancaDocumentoItemRepositorio;

	private StringBuilder builder;
	private Imovel imovel;

	public ArquivoTextoTipo07() {
		builder = new StringBuilder();
	}

	public String build(Imovel imovel, CobrancaDocumento cobrancaDocumento, int quantidadeContas) {
		this.imovel = imovel;
		
		if (cobrancaDocumento != null && !cobrancaDocumento.equals("")) {
			List<CobrancaDocumentoItem> listaCobrancaDocumentoItem = cobrancaDocumentoItemRepositorio.buscarCobrancaDocumentoItens(
					cobrancaDocumento.getId());

			if (listaCobrancaDocumentoItem != null && !listaCobrancaDocumentoItem.isEmpty()) {
				if (listaCobrancaDocumentoItem.size() > quantidadeContas) {
					buildLinhaQuantidadeContasSuperior(listaCobrancaDocumentoItem, quantidadeContas);
				}
				
				int contadorImpressao = listaCobrancaDocumentoItem.size() - (quantidadeContas - 1);

				if (contadorImpressao <= 1) {
					for (CobrancaDocumentoItem item : listaCobrancaDocumentoItem) {
						buildLinha(item);
					}
				} else {
					while (contadorImpressao < listaCobrancaDocumentoItem.size()) {
						buildLinha(listaCobrancaDocumentoItem.get(contadorImpressao));
						contadorImpressao++;
					}
				}
			}
		}
		
		return builder.toString();
	}
	
	public int getQuantidadeLinhas() {
		String[] linhas = builder.toString().split(System.getProperty("line.separator"));
		return linhas.length;
	}

	private void buildLinha(CobrancaDocumentoItem item) {
		builder.append(TIPO_REGISTRO);
		builder.append(Utilitarios.completaComZerosEsquerda(9, imovel.getId()));
		builder.append(item.getContaGeral().getConta().getReferencia());
		builder.append(Utilitarios.completaComZerosEsquerda(14, Utilitarios.formatarBigDecimalComPonto(item.getValorItemCobrado())));
		builder.append(Utilitarios.formataData(item.getContaGeral().getConta().getDataVencimentoConta(), FormatoData.ANO_MES_DIA));
		builder.append(Utilitarios.completaComZerosEsquerda(14, Utilitarios.formatarBigDecimalComPonto(item.getValorAcrescimos())));
		builder.append(System.getProperty("line.separator"));
	}

	private void buildLinhaQuantidadeContasSuperior(List<CobrancaDocumentoItem> listaCobrancaDocumentoItem, int quantidadeContas) {
		VencimentoAnteriorTO to = calcularValorDataVencimentoAnterior(listaCobrancaDocumentoItem, quantidadeContas);

		builder.append(TIPO_REGISTRO);
		builder.append(Utilitarios.completaComZerosEsquerda(9, imovel.getId()));
		builder.append("DB.ATE");
		builder.append(Utilitarios.completaComZerosEsquerda(14, Utilitarios.formatarBigDecimalComPonto(to.getValorAnterior())));
		builder.append(Utilitarios.formataData(to.getDataVencimentoAnterior(), FormatoData.ANO_MES_DIA));
		builder.append(Utilitarios.completaComZerosEsquerda(14, Utilitarios.formatarBigDecimalComPonto(to.getValorAcrescimosAnterior())));
		builder.append(System.getProperty("line.separator"));
	}
	
	public VencimentoAnteriorTO calcularValorDataVencimentoAnterior(List<CobrancaDocumentoItem> listaItens, int quantidadeMaximaItens) {
		VencimentoAnteriorTO to = new VencimentoAnteriorTO();
		
		int quantidadeItensAnteriores = (listaItens.size() - quantidadeMaximaItens) + 1;

		for (int sequencia = 0; sequencia < listaItens.size(); sequencia++) {
			CobrancaDocumentoItem cobrancaDocumentoItem = listaItens.get(sequencia);
			
			to.addValorAnterior(cobrancaDocumentoItem.getValorItemCobrado());
			to.addValorAcrescimosAnterior(cobrancaDocumentoItem.getValorAcrescimos());
			
			if (sequencia == quantidadeItensAnteriores) {
				to.setDataVencimentoAnterior(cobrancaDocumentoItem.getContaGeral().getConta().getDataVencimentoConta());
				
				break;
			}
		}

		return to;
	}
}
