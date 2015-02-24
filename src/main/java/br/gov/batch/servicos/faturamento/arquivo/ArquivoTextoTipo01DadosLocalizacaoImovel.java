package br.gov.batch.servicos.faturamento.arquivo;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.Localidade;
import br.gov.model.micromedicao.Rota;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ImovelRepositorio;

@Stateless
public class ArquivoTextoTipo01DadosLocalizacaoImovel {

	private Map<Integer, StringBuilder> dadosLocalizacaoImovel;

	@EJB
	private ImovelRepositorio imovelRepositorio;

	private Imovel imovel;
	
	private Rota rota;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Map<Integer, StringBuilder> build(ArquivoTextoTO to) {
	    this.imovel = imovelRepositorio.obterPorID(to.getIdImovel());
	    this.rota   = to.getRota();
		
		dadosLocalizacaoImovel = new HashMap<Integer, StringBuilder>();
		
		dadosLocalizacaoImovel.put(0, new StringBuilder(Utilitarios.completaComEspacosADireita(25, imovel.getLocalidade().getGerenciaRegional().getNome())));
		dadosLocalizacaoImovel.put(1, new StringBuilder(Utilitarios.completaComEspacosADireita(25, imovel.getLocalidade().getDescricao())));
		dadosLocalizacaoImovel.put(22, new StringBuilder(Utilitarios.completaComZerosEsquerda(3, rota.getFaturamentoGrupo().getId())));
		dadosLocalizacaoImovel.put(23, new StringBuilder(Utilitarios.completaComZerosEsquerda(7, rota.getCodigo())));
		dadosLocalizacaoImovel.put(27, new StringBuilder(Utilitarios.completaComZerosEsquerda(9, imovel.getNumeroSequencialRota())));
		dadosLocalizacaoImovel.put(4, new StringBuilder(Utilitarios.completaComEspacosADireita(17, imovel.getInscricaoFormatadaSemPonto())));
		dadosLocalizacaoImovel.put(5, new StringBuilder(Utilitarios.completaComEspacosADireita(70, imovel.getEnderecoFormatadoResumido())));
		
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
        
        dadosLocalizacaoImovel.put(26, builder);
    }
}