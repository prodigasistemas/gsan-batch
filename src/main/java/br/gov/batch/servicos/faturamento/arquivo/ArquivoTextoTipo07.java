package br.gov.batch.servicos.faturamento.arquivo;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.cobranca.to.VencimentoAnteriorTO;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cobranca.CobrancaDocumento;
import br.gov.model.cobranca.CobrancaDocumentoItem;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cobranca.CobrancaDocumentoItemRepositorio;

@Stateless
public class ArquivoTextoTipo07 extends ArquivoTexto {

	@EJB
	private CobrancaDocumentoItemRepositorio cobrancaDocumentoItemRepositorio;

	private int quantidadeContas = 17;

	public ArquivoTextoTipo07() {
		super();
	}

	public String build(ArquivoTextoTO to) {
		CobrancaDocumento cobrancaDocumento = to.getCobrancaDocumento();
		Integer idImovel = to.getImovel().getId();
		
		if (cobrancaDocumento != null && !cobrancaDocumento.equals("")) {
			List<CobrancaDocumentoItem> listaCobrancaDocumentoItem = cobrancaDocumentoItemRepositorio.buscarCobrancaDocumentoItens(cobrancaDocumento.getId());

			if (listaCobrancaDocumentoItem != null && !listaCobrancaDocumentoItem.isEmpty()) {
				if (listaCobrancaDocumentoItem.size() > quantidadeContas) {
					buildLinhaQuantidadeContasSuperior(listaCobrancaDocumentoItem, idImovel);
				}

				int contadorImpressao = listaCobrancaDocumentoItem.size() - (quantidadeContas - 1);

				if (contadorImpressao <= 1) {
					for (CobrancaDocumentoItem item : listaCobrancaDocumentoItem) {
						buildLinha(item, idImovel);
					}
				} else {
					for (; contadorImpressao < listaCobrancaDocumentoItem.size(); contadorImpressao++) {
						buildLinha(listaCobrancaDocumentoItem.get(contadorImpressao), idImovel);
					}
				}
			}
		}
		
		return builder.toString();
	}

	private void buildLinha(CobrancaDocumentoItem item, Integer idImovel) {
		builder.append(TIPO_REGISTRO_07_COBRANCA);
		builder.append(Utilitarios.completaComZerosEsquerda(9, idImovel));
		builder.append(item.getContaGeral().getConta().getReferencia());
		builder.append(Utilitarios.completaComZerosEsquerda(14, Utilitarios.formatarBigDecimalComPonto(item.getValorItemCobrado())));
		builder.append(Utilitarios.formataData(item.getContaGeral().getConta().getDataVencimentoConta(), FormatoData.ANO_MES_DIA));
		builder.append(Utilitarios.completaComZerosEsquerda(14, Utilitarios.formatarBigDecimalComPonto(item.getValorAcrescimos())));
		builder.append(System.getProperty("line.separator"));
	}

	private void buildLinhaQuantidadeContasSuperior(List<CobrancaDocumentoItem> listaCobrancaDocumentoItem, Integer idImovel) {
		VencimentoAnteriorTO to = calcularValorDataVencimentoAnterior(listaCobrancaDocumentoItem, quantidadeContas);

		builder.append(TIPO_REGISTRO_07_COBRANCA);
		builder.append(Utilitarios.completaComZerosEsquerda(9, idImovel));
		builder.append("DB.ATE");
		builder.append(Utilitarios.completaComZerosEsquerda(14, Utilitarios.formatarBigDecimalComPonto(to.getValorAnterior())));
		builder.append(Utilitarios.formataData(to.getDataVencimentoAnterior(), FormatoData.ANO_MES_DIA));
		builder.append(Utilitarios.completaComZerosEsquerda(14, Utilitarios.formatarBigDecimalComPonto(to.getValorAcrescimosAnterior())));
		builder.append(System.getProperty("line.separator"));
	}

	public VencimentoAnteriorTO calcularValorDataVencimentoAnterior(List<CobrancaDocumentoItem> listaItens, int quantidadeContas) {
		VencimentoAnteriorTO to = new VencimentoAnteriorTO();

		int quantidadeItensAnteriores = (listaItens.size() - quantidadeContas) + 1;

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
