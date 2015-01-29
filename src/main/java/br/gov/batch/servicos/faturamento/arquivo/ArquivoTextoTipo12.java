package br.gov.batch.servicos.faturamento.arquivo;

import java.math.BigDecimal;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.Categoria;
import br.gov.model.cadastro.ImovelPerfil;
import br.gov.model.micromedicao.ConsumoAnormalidade;
import br.gov.model.micromedicao.ConsumoAnormalidadeAcao;
import br.gov.model.micromedicao.LeituraAnormalidadeConsumo;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.micromedicao.ConsumoAnormalidadeAcaoRepositorio;

@Stateless
public class ArquivoTextoTipo12 extends ArquivoTexto {

	private final String TIPO_REGISTRO = "12";

	@EJB
	private ConsumoAnormalidadeAcaoRepositorio consumoAnormalidadeAcaoRepositorio;

	public String build() {
		List<ConsumoAnormalidadeAcao> listaAcoes = consumoAnormalidadeAcaoRepositorio.consumoAnormalidadeAcaoAtivo();

		for (ConsumoAnormalidadeAcao acao : listaAcoes) {
			builder.append(TIPO_REGISTRO);
			builder.append(getConsumoAnormalidade(acao.getConsumoAnormalidade()));
			builder.append(getCategoria(acao.getCategoria()));
			builder.append(getImovelPerfil(acao.getImovelPerfil()));
			builder.append(getLeituraAnormalidadeConsumoMes1(acao.getLeituraAnormalidadeConsumoMes1()));
			builder.append(getLeituraAnormalidadeConsumoMes2(acao.getLeituraAnormalidadeConsumoMes2()));
			builder.append(getLeituraAnormalidadeConsumoMes3(acao.getLeituraAnormalidadeConsumoMes3()));
			builder.append(getNumerofatorConsumoMes1(acao.getNumerofatorConsumoMes1()));
			builder.append(getNumerofatorConsumoMes2(acao.getNumerofatorConsumoMes2()));
			builder.append(getNumerofatorConsumoMes3(acao.getNumerofatorConsumoMes3()));
			builder.append(getDescricaoContaMensagemMes1(acao.getDescricaoContaMensagemMes1()));
			builder.append(getDescricaoContaMensagemMes2(acao.getDescricaoContaMensagemMes2()));
			builder.append(getDescricaoContaMensagemMes3(acao.getDescricaoContaMensagemMes3()));

			if (getQuantidadeLinhas() < listaAcoes.size()) {
				builder.append(System.getProperty("line.separator"));
			}
		}

		return builder.toString();
	}
	
	private String getDescricaoContaMensagemMes3(String descricaoContaMensagemMes3) {
		if (descricaoContaMensagemMes3 != null && !descricaoContaMensagemMes3.equals("")) {
			return Utilitarios.completaTexto(120, descricaoContaMensagemMes3);
		} else {
			return Utilitarios.completaTexto(120, "");
		}
	}
	
	private String getDescricaoContaMensagemMes2(String descricaoContaMensagemMes2) {
		if (descricaoContaMensagemMes2 != null && !descricaoContaMensagemMes2.equals("")) {
			return Utilitarios.completaTexto(120, descricaoContaMensagemMes2);
		} else {
			return Utilitarios.completaTexto(120, "");
		}
	}
	
	private String getDescricaoContaMensagemMes1(String descricaoContaMensagemMes1) {
		if (descricaoContaMensagemMes1 != null && !descricaoContaMensagemMes1.equals("")) {
			return Utilitarios.completaTexto(120, descricaoContaMensagemMes1);
		} else {
			return Utilitarios.completaTexto(120, "");
		}
	}

	private String getNumerofatorConsumoMes3(BigDecimal numerofatorConsumoMes3) {
		if (numerofatorConsumoMes3 != null && !numerofatorConsumoMes3.equals("")) {
			return Utilitarios.completaComZerosEsquerda(4, numerofatorConsumoMes3);
		} else {
			return Utilitarios.completaComZerosEsquerda(4, "");
		}
	}
	
	private String getNumerofatorConsumoMes2(BigDecimal numerofatorConsumoMes2) {
		if (numerofatorConsumoMes2 != null && !numerofatorConsumoMes2.equals("")) {
			return Utilitarios.completaComZerosEsquerda(4, numerofatorConsumoMes2);
		} else {
			return Utilitarios.completaComZerosEsquerda(4, "");
		}
	}
	
	private String getNumerofatorConsumoMes1(BigDecimal numerofatorConsumoMes1) {
		if (numerofatorConsumoMes1 != null && !numerofatorConsumoMes1.equals("")) {
			return Utilitarios.completaComZerosEsquerda(4, numerofatorConsumoMes1);
		} else {
			return Utilitarios.completaComZerosEsquerda(4, "");
		}
	}

	private String getLeituraAnormalidadeConsumoMes3(LeituraAnormalidadeConsumo leituraAnormalidadeConsumoMes3) {
		if (leituraAnormalidadeConsumoMes3 != null && leituraAnormalidadeConsumoMes3.getId() != null) {
			return Utilitarios.completaComZerosEsquerda(2, leituraAnormalidadeConsumoMes3.getId());
		} else {
			return Utilitarios.completaComZerosEsquerda(2, "");
		}
	}
	
	private String getLeituraAnormalidadeConsumoMes2(LeituraAnormalidadeConsumo leituraAnormalidadeConsumoMes2) {
		if (leituraAnormalidadeConsumoMes2 != null && leituraAnormalidadeConsumoMes2.getId() != null) {
			return Utilitarios.completaComZerosEsquerda(2, leituraAnormalidadeConsumoMes2.getId());
		} else {
			return Utilitarios.completaComZerosEsquerda(2, "");
		}
	}

	private String getLeituraAnormalidadeConsumoMes1(LeituraAnormalidadeConsumo leituraAnormalidadeConsumoMes1) {
		if (leituraAnormalidadeConsumoMes1 != null && leituraAnormalidadeConsumoMes1.getId() != null) {
			return Utilitarios.completaComZerosEsquerda(2, leituraAnormalidadeConsumoMes1.getId());
		} else {
			return Utilitarios.completaComZerosEsquerda(2, "");
		}
	}

	private String getImovelPerfil(ImovelPerfil imovelPerfil) {
		if (imovelPerfil != null && imovelPerfil.getId() != null && !imovelPerfil.getId().equals("")) {
			return Utilitarios.completaComZerosEsquerda(2, imovelPerfil.getId());
		} else {
			return Utilitarios.completaTexto(2, "");
		}
	}

	private String getCategoria(Categoria categoria) {
		if (categoria != null && categoria.getId() != null && !categoria.getId().equals("")) {
			return Utilitarios.completaComZerosEsquerda(2, categoria.getId());
		} else {
			return Utilitarios.completaTexto(2, "");
		}
	}

	private String getConsumoAnormalidade(ConsumoAnormalidade consumoAnormalidade) {
		if (consumoAnormalidade != null && consumoAnormalidade.getId() != null) {
			return Utilitarios.completaComZerosEsquerda(2, consumoAnormalidade.getId());
		} else {
			return Utilitarios.completaComZerosEsquerda(2, "");
		}
	}
}
