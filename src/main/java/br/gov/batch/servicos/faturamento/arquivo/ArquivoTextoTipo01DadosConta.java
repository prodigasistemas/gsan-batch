package br.gov.batch.servicos.faturamento.arquivo;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.batch.servicos.faturamento.ExtratoQuitacaoBO;
import br.gov.batch.servicos.faturamento.MensagemContaBO;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.QuadraFace;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.faturamento.FaturamentoParametro.NOME_PARAMETRO_FATURAMENTO;
import br.gov.model.faturamento.QualidadeAgua;
import br.gov.model.faturamento.QualidadeAguaPadrao;
import br.gov.model.faturamento.TipoConta;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.faturamento.FaturamentoParametroRepositorio;
import br.gov.servicos.faturamento.QuadraFaceRepositorio;
import br.gov.servicos.faturamento.QualidadeAguaPadraoRepositorio;
import br.gov.servicos.faturamento.QualidadeAguaRepositorio;

@Stateless
public class ArquivoTextoTipo01DadosConta {

	private Map<Integer, StringBuilder> dadosConta;
	
	@EJB
    private ExtratoQuitacaoBO extratoQuitacaoBO;
	
	@EJB
    private MensagemContaBO mensagemContaBO;
	
	@EJB
    private FaturamentoParametroRepositorio repositorioParametros;
	
	@EJB
    private QualidadeAguaPadraoRepositorio qualidadeAguaPadraoRepositorio;
	
	@EJB
    private QuadraFaceRepositorio quadraFaceRepositorio;
	
	@EJB
    private QualidadeAguaRepositorio qualidadeAguaRepositorio;
	
    @EJB
    private ImovelRepositorio imovelRepositorio;

	private Imovel imovel;
	private Conta conta;
	private FaturamentoGrupo faturamentoGrupo;
	private Integer anoMesReferencia;
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Map<Integer, StringBuilder> build(ArquivoTextoTO to) {
	    this.imovel = imovelRepositorio.obterPorID(to.getIdImovel());
	    this.conta  = to.getConta();
	    this.faturamentoGrupo = to.getFaturamentoGrupo();
	    this.anoMesReferencia = to.getAnoMesReferencia();
		
		dadosConta = new HashMap<Integer, StringBuilder>();
		
		escreverVencimentoConta();
		escreverReferenciaConta();
		escreverCodigoConta();	
		escreverMensagemConta();
		escreverQualidadeDaAgua();
	    
	    dadosConta.put(29, new StringBuilder(Utilitarios.completaComEspacosADireita(120, extratoQuitacaoBO.obterMsgQuitacaoDebitos(imovel.getId(), anoMesReferencia))));
	
	    return dadosConta;
	}
	
	private void escreverVencimentoConta() {
		StringBuilder builder = new StringBuilder();
		
        if (conta != null) {
            builder.append(Utilitarios.formataData(conta.getDataVencimentoConta(), FormatoData.ANO_MES_DIA));
            builder.append(Utilitarios.formataData(conta.getDataValidadeConta(), FormatoData.ANO_MES_DIA));
        } else {
            builder.append(Utilitarios.completaComEspacosADireita(16, ""));
        }
        
        dadosConta.put(3, builder);
    }
	
	private void escreverReferenciaConta() {
		StringBuilder builder = new StringBuilder();
		
        if (conta != null) {
            builder.append(conta.getReferencia());
            builder.append(conta.getDigitoVerificadorConta());
        } else {
            builder.append(Utilitarios.completaComEspacosADireita(7, ""));
        }
        
        dadosConta.put(6, builder);
    }
	
	private void escreverCodigoConta() {
		StringBuilder builder = new StringBuilder();
		
        if (conta != null) {
            builder.append(Utilitarios.completaComZerosEsquerda(9, conta.getId()));
        } else {
            builder.append(Utilitarios.completaComEspacosADireita(9, ""));
        }
        
        dadosConta.put(24, builder);
    }
	
	private void escreverMensagemConta() {
		StringBuilder builder = new StringBuilder();
		
        boolean mensagemEmTresPartes = Boolean.valueOf(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.ESCREVER_MENSAGEM_CONTA_TRES_PARTES));
        
        String[] mensagemConta = null;
        
        if (mensagemEmTresPartes){
            mensagemConta = mensagemContaBO.obterMensagemConta3Partes(imovel, anoMesReferencia, faturamentoGrupo.getId());

            builder.append(Utilitarios.completaComEspacosADireita(100, mensagemConta[0]));
            builder.append(Utilitarios.completaComEspacosADireita(100, mensagemConta[1]));
            builder.append(Utilitarios.completaComEspacosADireita(100, mensagemConta[2]));
        }else{
            mensagemConta = mensagemContaBO.obterMensagemConta(imovel, anoMesReferencia, TipoConta.CONTA_DEBITO_AUTOMATICO);
            
            builder.append(Utilitarios.completaComEspacosADireita(60, mensagemConta[0]));
            builder.append(Utilitarios.completaComEspacosADireita(60, mensagemConta[1]));
            builder.append(Utilitarios.completaComEspacosADireita(60, mensagemConta[2]));            
            builder.append(Utilitarios.completaComEspacosADireita(60, mensagemConta[3]));            
            builder.append(Utilitarios.completaComEspacosADireita(60, mensagemConta[4]));            
        }
        
        dadosConta.put(28, builder);
    }
	
	private void escreverQualidadeDaAgua() {
    	Integer anoMesReferenciaQualidadeAgua = null;
    	
		if (!Boolean.valueOf(repositorioParametros.recuperaPeloNome(NOME_PARAMETRO_FATURAMENTO.REFERENCIA_ANTERIOR_PARA_QUALIDADE_AGUA))) {
			anoMesReferenciaQualidadeAgua = Utilitarios.reduzirMeses(faturamentoGrupo.getAnoMesReferencia(), 1);
		} else {
			anoMesReferenciaQualidadeAgua = faturamentoGrupo.getAnoMesReferencia();
		}

		gerarTextoQualidadeAgua(imovel.getLocalidade().getId(),
						imovel.getSetorComercial().getId(), 
						anoMesReferenciaQualidadeAgua,
						imovel.getQuadraFace().getId());
	}

	private void gerarTextoQualidadeAgua(Integer idLocalidade, Integer idSetorComercial,
			Integer anoMesReferenciaQualidadeAgua, Integer idQuadraFace) {
		
		QualidadeAgua qualidadeAgua = null; 
		QualidadeAguaPadrao qualidadeAguaPadrao = qualidadeAguaPadraoRepositorio.obterLista().iterator().next();
		QuadraFace quadraface = quadraFaceRepositorio.obterPorID(idQuadraFace);
		
		if(quadraface.possuiSistemaAbastecimento()){
			qualidadeAgua = qualidadeAguaRepositorio.buscarPorAnoMesESistemaAbastecimentoComFonteCaptacaoETipoCaptacao(anoMesReferenciaQualidadeAgua, 
					quadraface.getDistritoOperacional().getSetorAbastecimento().getSistemaAbastecimento().getId());
		}
		if(qualidadeAgua==null){
			qualidadeAgua = qualidadeAguaRepositorio.buscarPorAnoMesELocalidadeESetorComFonteCaptacao(anoMesReferenciaQualidadeAgua,
					idSetorComercial, idLocalidade);
		}
		
		preencherQualidadeAguaPadrao(qualidadeAguaPadrao);
		preencherQualidadeAgua(qualidadeAgua);
	}

	private void preencherQualidadeAgua(QualidadeAgua qualidadeAgua) {
		StringBuilder builder = new StringBuilder();
		
		if(qualidadeAgua!=null){
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getAnoMesReferencia()));
			builder.append(Utilitarios.completaComEspacosADireita(5, qualidadeAgua.getNumeroCloroResidual()));
			builder.append(Utilitarios.completaComEspacosADireita(5, qualidadeAgua.getNumeroIndiceTurbidez()));
			builder.append(Utilitarios.completaComEspacosADireita(5, qualidadeAgua.getNumeroIndicePh()));
			builder.append(Utilitarios.completaComEspacosADireita(5, qualidadeAgua.getNumeroIndiceCor()));
			builder.append(Utilitarios.completaComEspacosADireita(5, qualidadeAgua.getNumeroIndiceFluor()));
			builder.append(Utilitarios.completaComEspacosADireita(5, qualidadeAgua.getNumeroIndiceFerro()));
			builder.append(Utilitarios.completaComEspacosADireita(5, qualidadeAgua.getNumeroIndiceColiformesTotais()));
			builder.append(Utilitarios.completaComEspacosADireita(5, qualidadeAgua.getNumeroIndiceColiformesFecais()));
			builder.append(Utilitarios.completaComEspacosADireita(5, qualidadeAgua.getNumeroNitrato()));
			builder.append(Utilitarios.completaComEspacosADireita(5, qualidadeAgua.getNumeroIndiceColiformesTermotolerantes()));
			builder.append(Utilitarios.completaComEspacosADireita(30, qualidadeAgua.getFonteCaptacao().getDescricao()));
			
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeTurbidezExigidas()));
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeCorExigidas()));
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeCloroExigidas()));
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeFluorExigidas()));
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeColiformesTotaisExigidas()));
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeColiformesFecaisExigidas()));
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeColiformesTermotolerantesExigidas()));
			
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeTurbidezAnalisadas()));
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeCorAnalisadas()));
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeCloroAnalisadas()));
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeFluorAnalisadas()));
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeColiformesTotaisAnalisadas()));
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeColiformesFecaisAnalisadas()));
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeColiformesTermotolerantesAnalisadas()));
			
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeTurbidezConforme()));
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeCorConforme()));
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeCloroConforme()));
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeFluorConforme()));
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeColiformesTotaisConforme()));
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeColiformesFecaisConforme()));
			builder.append(Utilitarios.completaComEspacosADireita(6, qualidadeAgua.getQuantidadeColiformesTermotolerantesConforme()));
		}else{
			builder.append(Utilitarios.completaComEspacosADireita(212, qualidadeAgua));
		}
		
		dadosConta.put(31, builder);
	}
	
	private void preencherQualidadeAguaPadrao(QualidadeAguaPadrao qualidadeAguaPadrao) {
		StringBuilder builder = new StringBuilder();
		
		if(qualidadeAguaPadrao!=null){
			builder.append(Utilitarios.completaComEspacosADireita(20, qualidadeAguaPadrao.getDescricaoPadraoTurbidez()));
			builder.append(Utilitarios.completaComEspacosADireita(20, qualidadeAguaPadrao.getDescricaoPadraoPh()));
			builder.append(Utilitarios.completaComEspacosADireita(20, qualidadeAguaPadrao.getDescricaoPadraoCor()));
			builder.append(Utilitarios.completaComEspacosADireita(20, qualidadeAguaPadrao.getDescricaoPadraoCloro()));
			builder.append(Utilitarios.completaComEspacosADireita(20, qualidadeAguaPadrao.getDescricaoPadraoFluor()));
			builder.append(Utilitarios.completaComEspacosADireita(20, qualidadeAguaPadrao.getDescricaoPadraoFerro()));
			builder.append(Utilitarios.completaComEspacosADireita(20, qualidadeAguaPadrao.getDescricaoPadraoColiformesTotais()));
			builder.append(Utilitarios.completaComEspacosADireita(20, qualidadeAguaPadrao.getDescricaoPadraoColiformesFecais()));
			builder.append(Utilitarios.completaComEspacosADireita(20, qualidadeAguaPadrao.getDescricaoNitrato()));
			builder.append(Utilitarios.completaComEspacosADireita(20, qualidadeAguaPadrao.getDescricaoPadraoColiformesTermotolerantes()));
		}else{
			builder.append(Utilitarios.completaComEspacosADireita(200, qualidadeAguaPadrao));
		}
		dadosConta.put(30, builder);
	}
}
