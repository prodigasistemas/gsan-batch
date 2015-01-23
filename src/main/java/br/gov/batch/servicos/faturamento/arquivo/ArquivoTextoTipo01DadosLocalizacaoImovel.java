package br.gov.batch.servicos.faturamento.arquivo;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;

import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.Localidade;
import br.gov.model.micromedicao.Rota;
import br.gov.model.util.Utilitarios;

@Stateless
public class ArquivoTextoTipo01DadosLocalizacaoImovel {

	private Map<Integer, StringBuilder> dadosLocalizacaoImovel;

	private Imovel imovel;
	private Rota rota;
	
	public ArquivoTextoTipo01DadosLocalizacaoImovel(Imovel imovel, Rota rota) {
		this.imovel = imovel;
		this.rota = rota;
	}
	
	public Map<Integer, StringBuilder> build() {
		
		dadosLocalizacaoImovel = new HashMap<Integer, StringBuilder>();
		
		dadosLocalizacaoImovel.put(0, new StringBuilder(Utilitarios.completaComEspacosADireita(25, imovel.getLocalidade().getGerenciaRegional().getNome())));
		dadosLocalizacaoImovel.put(1, new StringBuilder(Utilitarios.completaComEspacosADireita(25, imovel.getLocalidade().getDescricao())));
		dadosLocalizacaoImovel.put(23, new StringBuilder(Utilitarios.completaComZerosEsquerda(3, rota.getFaturamentoGrupo().getId())));
		dadosLocalizacaoImovel.put(24, new StringBuilder(Utilitarios.completaComZerosEsquerda(7, rota.getCodigo())));
		dadosLocalizacaoImovel.put(28, new StringBuilder(Utilitarios.completaComZerosEsquerda(9, imovel.getNumeroSequencialRota())));
		dadosLocalizacaoImovel.put(4, new StringBuilder(Utilitarios.completaComEspacosADireita(17, imovel.getInscricaoFormatadaSemPonto())));
		dadosLocalizacaoImovel.put(5, new StringBuilder(Utilitarios.completaComEspacosADireita(70, imovel.getEnderecoFormatadoAbreviado())));
		
		escreverDadosLocalidade();
	
		return dadosLocalizacaoImovel;
	}
	
	private void escreverDadosLocalidade() {
		StringBuilder builder = new StringBuilder();
		
        Localidade localidade = imovel.getLocalidade();
        StringBuilder descricaoAtendimento = localidade.getEnderecoFormatadoTituloAbreviado();
        builder.append(Utilitarios.completaComEspacosADireita(70, descricaoAtendimento));
        
        String dddMunicipio = "";
        if (localidade.getLogradouroBairro() != null && localidade.getLogradouroBairro().temMunicipio()) {
            dddMunicipio = Utilitarios.completaComEspacosADireita(2, localidade.getLogradouroBairro().getBairro().getMunicipio().getDdd());
        }

        String fone = localidade.getFone() != null ? localidade.getFone() : "";

        builder.append(Utilitarios.completaComEspacosADireita(11, dddMunicipio + fone));
        
        dadosLocalizacaoImovel.put(27, builder);
    }
}
