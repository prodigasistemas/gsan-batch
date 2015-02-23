package br.gov.batch.servicos.faturamento.arquivo;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.util.Utilitarios;

@Stateless
public class ArquivoTextoTipo01 extends ArquivoTexto {
	private SortedMap<Integer, StringBuilder> mapDados;

	@EJB
	private ArquivoTextoTipo01DadosCobranca dadosCobranca;

	@EJB
	private ArquivoTextoTipo01DadosCliente dadosCliente;
	
	@EJB
	private ArquivoTextoTipo01DadosConsumo dadosConsumo;
	
	@EJB
	private ArquivoTextoTipo01DadosConta dadosConta;
	
	@EJB
	private ArquivoTextoTipo01DadosFaturamento dadosFaturamento;
	
	@EJB
	private ArquivoTextoTipo01DadosLocalizacaoImovel dadosLocalizacaoImovel;

	public ArquivoTextoTipo01() {
		super();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String build(ArquivoTextoTO to) {
	    mapDados = new TreeMap<Integer, StringBuilder>();
		builder.append(TIPO_REGISTRO_01_IMOVEL);
		builder.append(Utilitarios.completaComZerosEsquerda(9, String.valueOf(to.getImovel().getId())));

		builder.append(TIPO_REGISTRO_01_IMOVEL);
		
		builder.append(Utilitarios.completaComZerosEsquerda(9, String.valueOf(to.getImovel().getId())));
		
		to.setIdImovel(to.getImovel().getId());

		mapDados.putAll(dadosCobranca.build(to));
		
		mapDados.putAll(dadosCliente.build(to));
		
		mapDados.putAll(dadosConsumo.build(to));
		
		mapDados.putAll(dadosLocalizacaoImovel.build(to));
		
		mapDados.putAll(dadosConta.build(to));
		
		mapDados.putAll(dadosFaturamento.build(to));

		Iterator<Integer> iteratorKeys = mapDados.keySet().iterator();

		Iterator<StringBuilder> iterator = mapDados.values().iterator();

		while (iterator.hasNext()) {
			builder.append(iterator.next());
		}

		builder.append(System.getProperty("line.separator"));

		return builder.toString();
	}

	public ArquivoTextoTipo01DadosCobranca getDadosCobranca() {
		return dadosCobranca;
	}

	public void setDadosCobranca(ArquivoTextoTipo01DadosCobranca dadosCobranca) {
		this.dadosCobranca = dadosCobranca;
	}

	public ArquivoTextoTipo01DadosCliente getDadosCliente() {
		return dadosCliente;
	}

	public void setDadosCliente(ArquivoTextoTipo01DadosCliente dadosCliente) {
		this.dadosCliente = dadosCliente;
	}

	public ArquivoTextoTipo01DadosConsumo getDadosConsumo() {
		return dadosConsumo;
	}

	public void setDadosConsumo(ArquivoTextoTipo01DadosConsumo dadosConsumo) {
		this.dadosConsumo = dadosConsumo;
	}

	public ArquivoTextoTipo01DadosConta getDadosConta() {
		return dadosConta;
	}

	public void setDadosConta(ArquivoTextoTipo01DadosConta dadosConta) {
		this.dadosConta = dadosConta;
	}

	public ArquivoTextoTipo01DadosFaturamento getDadosFaturamento() {
		return dadosFaturamento;
	}

	public void setDadosFaturamento(ArquivoTextoTipo01DadosFaturamento dadosFaturamento) {
		this.dadosFaturamento = dadosFaturamento;
	}

	public ArquivoTextoTipo01DadosLocalizacaoImovel getDadosLocalizacaoImovel() {
		return dadosLocalizacaoImovel;
	}

	public void setDadosLocalizacaoImovel(ArquivoTextoTipo01DadosLocalizacaoImovel dadosLocalizacaoImovel) {
		this.dadosLocalizacaoImovel = dadosLocalizacaoImovel;
	}
}
