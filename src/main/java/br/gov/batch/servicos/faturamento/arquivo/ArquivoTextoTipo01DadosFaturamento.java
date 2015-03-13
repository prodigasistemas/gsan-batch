package br.gov.batch.servicos.faturamento.arquivo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.batch.servicos.faturamento.FaturamentoAtividadeCronogramaBO;
import br.gov.batch.servicos.faturamento.FaturamentoSituacaoBO;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.model.Status;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.FaturamentoAtividade;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.faturamento.FaturamentoSituacaoHistorico;
import br.gov.model.faturamento.FaturamentoSituacaoTipo;
import br.gov.model.util.FormatoData;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.faturamento.FaturamentoSituacaoTipoRepositorio;

@Stateless
public class ArquivoTextoTipo01DadosFaturamento {

	private Map<Integer, StringBuilder> dadosFaturamento;
	
	@EJB
    private FaturamentoSituacaoBO faturamentoSituacaoBO;
	
	@EJB
    private FaturamentoAtividadeCronogramaBO faturamentoAtividadeCronogramaBO;
	
	@EJB
    private FaturamentoSituacaoTipoRepositorio faturamentoSituacaoTipoRepositorio;
	
	@EJB
    private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;
	
   @EJB
    private ImovelRepositorio imovelRepositorio;

	private Imovel imovel;
	private FaturamentoGrupo faturamentoGrupo;
	private Conta conta;
	private Integer anoMesReferencia;
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Map<Integer, StringBuilder> build(ArquivoTextoTO to) {
	    this.imovel = to.getImovel();
	    this.conta  = to.getConta();
	    this.faturamentoGrupo = to.getFaturamentoGrupo();
	    this.anoMesReferencia = to.getAnoMesReferencia();

	    dadosFaturamento = new HashMap<Integer, StringBuilder>();
		
		Short paralisarAgua = faturamentoSituacaoBO.verificarParalisacaoFaturamentoAgua(imovel, anoMesReferencia).getId();
		Short paralisarsgoto = faturamentoSituacaoBO.verificarParalisacaoFaturamentoEsgoto(imovel, anoMesReferencia).getId();
		
	    escreverSituacaoAguaEsgoto();	
	    escreverDadosCondominio();
	    escreverIndicadorFaturamentoSituacao();
	    escreverSituacaoEspecialFaturamento(imovel, faturamentoGrupo);
	    escreverIndidacorAbastecimento();
		
	    dadosFaturamento.put(11, new StringBuilder(Utilitarios.completaComZerosEsquerda(2, imovel.getImovelPerfil().getId())));
	    dadosFaturamento.put(19, new StringBuilder(Utilitarios.completaComEspacosADireita(1, imovel.getPocoTipo())));
	    dadosFaturamento.put(37, new StringBuilder(Utilitarios.formataData(
	    		faturamentoAtividadeCronogramaBO.obterDataPrevistaDoCronogramaAnterior(faturamentoGrupo, FaturamentoAtividade.EFETUAR_LEITURA), 
	    		FormatoData.ANO_MES_DIA)));

	    dadosFaturamento.put(39, new StringBuilder(isImovelSazonal().toString()));
	    dadosFaturamento.put(40, new StringBuilder(paralisarAgua.toString()));
	    dadosFaturamento.put(41, new StringBuilder(paralisarsgoto.toString()));
	
	    return dadosFaturamento;
	}
	
	private void escreverSituacaoAguaEsgoto() {
		StringBuilder builder = new StringBuilder();
		
        if (conta != null) {
            builder.append(conta.getLigacaoAguaSituacao().getId());
            builder.append(conta.getLigacaoEsgotoSituacao().getId());
        } else {
            builder.append(imovel.getLigacaoAguaSituacao().getId());
            builder.append(imovel.getLigacaoEsgotoSituacao().getId());
        }
        
        dadosFaturamento.put(8, builder);
    }
	
	private void escreverIndicadorFaturamentoSituacao() {
		StringBuilder builder = new StringBuilder();
		
        if (conta != null) {
            builder.append(conta.getLigacaoAguaSituacao().getSituacaoFaturamento());
            builder.append(conta.getLigacaoEsgotoSituacao().getSituacaoFaturamento());
        } else {
            builder.append(imovel.getLigacaoAguaSituacao().getSituacaoFaturamento());
            builder.append(imovel.getLigacaoEsgotoSituacao().getSituacaoFaturamento());
        }

        dadosFaturamento.put(13, builder);
    }
	
	private void escreverSituacaoEspecialFaturamento(Imovel imovel, FaturamentoGrupo faturamentoGrupo) {
		StringBuilder builder = new StringBuilder();
		
		if (imovel.possuiFaturamentoSituacaoTipo()) {

			FaturamentoSituacaoHistorico faturamentoSituacaoHistorico = faturamentoSituacaoBO.obterFaturamentoSituacaoVigente(imovel, anoMesReferencia);
			
			if (faturamentoSituacaoHistorico != null) {

				FaturamentoSituacaoTipo faturamentoSituacaoTipo = faturamentoSituacaoTipoRepositorio.situacaoTipoDoImovel(imovel.getId());

				builder.append(Utilitarios.completaComZerosEsquerda(2, imovel.getFaturamentoSituacaoTipo().getId()));
				builder.append(Utilitarios.completaComZerosEsquerda(2, faturamentoSituacaoTipo.getLeituraAnormalidadeConsumoSemLeitura().getId()));
				builder.append(Utilitarios.completaComZerosEsquerda(2, faturamentoSituacaoTipo.getLeituraAnormalidadeConsumoComLeitura().getId()));
				builder.append(Utilitarios.completaComZerosEsquerda(2, faturamentoSituacaoTipo.getLeituraAnormalidadeLeituraSemLeitura().getId()));
				builder.append(Utilitarios.completaComZerosEsquerda(2, faturamentoSituacaoTipo.getLeituraAnormalidadeLeituraComLeitura().getId()));
				builder.append(Utilitarios.completaComZerosEsquerda(6, faturamentoSituacaoHistorico.getConsumoAguaMedido()));
				builder.append(Utilitarios.completaComZerosEsquerda(6, faturamentoSituacaoHistorico.getConsumoAguaNaoMedido()));
				builder.append(Utilitarios.completaComZerosEsquerda(6, faturamentoSituacaoHistorico.getVolumeEsgotoMedido()));
				builder.append(Utilitarios.completaComZerosEsquerda(6, faturamentoSituacaoHistorico.getVolumeEsgotoNaoMedido()));
				builder.append(Utilitarios.completaComEspacosADireita(1, imovel.getFaturamentoSituacaoTipo().getValidoAgua()));
				builder.append(Utilitarios.completaComEspacosADireita(1, imovel.getFaturamentoSituacaoTipo().getValidoEsgoto()));

			} else {
				builder.append(Utilitarios.completaComEspacosADireita(36, ""));
			}
		} else {
			builder.append(Utilitarios.completaComEspacosADireita(36, ""));
		}
		
		dadosFaturamento.put(36, builder);
    }
	
	private void escreverIndidacorAbastecimento() {
		StringBuilder builder = new StringBuilder();
		
		if (imovel.getLigacaoAguaSituacao() != null && imovel.getLigacaoAguaSituacao().getIndicadorAbastecimento() != null) {
        	builder.append(Utilitarios.completaComEspacosADireita(1, imovel.getLigacaoAguaSituacao().getIndicadorAbastecimento()));
		} else {
			builder.append(Utilitarios.completaComEspacosADireita(1, ""));
		}
		
		dadosFaturamento.put(38, builder);
	}
	
	private void escreverDadosCondominio() {
		StringBuilder builder = new StringBuilder();
		
        if (imovel.getImovelCondominio() != null) {
            builder.append(Utilitarios.completaComZerosEsquerda(9, imovel.getImovelCondominio().getId()));
        } else {
            builder.append(Utilitarios.completaComEspacosADireita(9, ""));
        }
        
        builder.append(imovel.getIndicadorImovelCondominio().toString());
        
        dadosFaturamento.put(10, builder);
    }
	
	private Short isImovelSazonal() {
		Collection<ICategoria> subcategorias = imovelSubcategoriaRepositorio.buscarSubcategoria(imovel.getId());

		for(ICategoria subcategoria : subcategorias) {
			if (subcategoria.getIndicadorSazonalidade() != null && subcategoria.getIndicadorSazonalidade() == Status.ATIVO.getId()) {
				return Status.ATIVO.getId();
			}
		}
		return Status.INATIVO.getId();
	}
}
