package br.gov.batch.servicos.faturamento.arquivo;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.faturamento.AguaEsgotoBO;
import br.gov.batch.servicos.faturamento.EsgotoBO;
import br.gov.batch.servicos.faturamento.to.VolumeMedioAguaEsgotoTO;
import br.gov.batch.servicos.micromedicao.ConsumoBO;
import br.gov.batch.servicos.micromedicao.HidrometroBO;
import br.gov.model.cadastro.ICategoria;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.LigacaoTipo;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;

@Stateless
public class ArquivoTextoTipo01DadosConsumo {
	
	@EJB
    private HidrometroBO hidrometroBO;
	
	@EJB
    private AguaEsgotoBO aguaEsgotoBO;
	
	@EJB
    private EsgotoBO esgotoBO;
	
	@EJB
    private ConsumoBO consumoBO;
	
	@EJB
    private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;
	
	private Imovel imovel;
	
	private FaturamentoGrupo faturamentoGrupo;
	
	private Map<Integer, StringBuilder> dadosConsumo;
	
	public ArquivoTextoTipo01DadosConsumo(Imovel imovel, FaturamentoGrupo faturamentoGrupo) {
		this.imovel = imovel;
		this.faturamentoGrupo = faturamentoGrupo;
	}
	
	public Map<Integer, StringBuilder> build() {
		dadosConsumo = new HashMap<Integer, StringBuilder>();
		
		escreverConsumoMedioLigacao();
	    escreverConsumoMinimoAgua();
	    escreverConsumoMinimoEsgoto();
	    
	    escreverPercentualAguaConsumidaColetada();
	    
	    escreverPercentualEsgoto();
	    
	    dadosConsumo.put(21, new StringBuilder(Utilitarios.completaComZerosEsquerda(2, imovel.getConsumoTarifa().getId())));
	    
	    escreverDadosConsumoCategoria();

	    escreverEsgotoAlternativo();
 
	    dadosConsumo.put(32, new StringBuilder(Utilitarios.completaComZerosEsquerda(6, consumoBO.consumoMinimoLigacao(imovel.getId()))));
	    
	    dadosConsumo.put(33, new StringBuilder(Utilitarios.completaComZerosEsquerda(6, consumoBO.consumoNaoMedido(imovel.getId(), faturamentoGrupo.getAnoMesReferencia()))));
	    dadosConsumo.put(26, new StringBuilder(Utilitarios.completaComZerosEsquerda(2, imovel.tarifaTipoCalculo())));
	
	    return dadosConsumo;
	}
	
	private void escreverConsumoMedioLigacao() {
		StringBuilder builder = new StringBuilder();
		
		boolean houveInstalacaoHidrometro = hidrometroBO.houveInstalacaoOuSubstituicao(imovel.getId());
	    
	    VolumeMedioAguaEsgotoTO consumoMedioLigacaoAgua = aguaEsgotoBO.obterVolumeMedioAguaEsgoto(imovel.getId(), 
	    		faturamentoGrupo.getAnoMesReferencia(), LigacaoTipo.AGUA.getId(), houveInstalacaoHidrometro);
	    builder.append(Utilitarios.completaComZerosEsquerda(6, consumoMedioLigacaoAgua.getConsumoMedio()));
	
	    dadosConsumo.put(13, builder);
	}
	
	private void escreverConsumoMinimoAgua() {
		StringBuilder builder = new StringBuilder();
		
		if (imovel.getLigacaoAgua() != null) {
            builder.append(Utilitarios.completaComZerosEsquerda(6, imovel.getLigacaoAgua().getConsumoMinimoAgua()));
        } else {
            builder.append(Utilitarios.completaComEspacosADireita(6, ""));
        }

		dadosConsumo.put(16, builder);
    }
	
	private void escreverConsumoMinimoEsgoto() {
		StringBuilder builder = new StringBuilder();
		
        if (imovel.getLigacaoEsgoto() != null) {
            builder.append(Utilitarios.completaComZerosEsquerda(6, imovel.getLigacaoEsgoto().getConsumoMinimo()));
        } else {
            builder.append(Utilitarios.completaComEspacosADireita(6, ""));
        }
        
        dadosConsumo.put(17, builder);
    }
	
	private void escreverPercentualAguaConsumidaColetada() {
		StringBuilder builder = new StringBuilder();
		
        if (imovel.getLigacaoEsgoto() != null) {
            String numero = Utilitarios.formatarBigDecimalComPonto(imovel.getLigacaoEsgoto().getPercentualAguaConsumidaColetada());
            builder.append(Utilitarios.completaComZerosEsquerda(6, numero));
        } else {
            builder.append(Utilitarios.completaComZerosEsquerda(6, ""));
        }
        
        dadosConsumo.put(18, builder);
    }
	
	private void escreverPercentualEsgoto() {
		StringBuilder builder = new StringBuilder();
		
        BigDecimal percentual = esgotoBO.percentualEsgotoAlternativo(imovel);
        
        builder.append(Utilitarios.completaComZerosEsquerda(6, Utilitarios.formatarBigDecimalComPonto(percentual)));
        
        dadosConsumo.put(19, builder);
    }
	
	private void escreverDadosConsumoCategoria() {
		StringBuilder builder = new StringBuilder();
		
        Collection<ICategoria> categorias = imovelSubcategoriaRepositorio.buscarQuantidadeEconomiasCategoria(imovel.getId());
        
        int consumoTotalReferenciaAltoConsumo = 0;
        int consumoTotalReferenciaEstouroConsumo = 0;
        int consumoTotalReferenciaBaixoConsumo = 0;
        int consumoMaximoCobrancaEstouroConsumo = 0;
        int maiorQuantidadeEconomia = 0;
        BigDecimal vezesMediaAltoConsumo = new BigDecimal(0);
        BigDecimal vezesMediaEstouroConsumo = new BigDecimal(0);
        BigDecimal percentualDeterminacaoBaixoConsumo = new BigDecimal(0);
        
        for (ICategoria categoria : categorias) {
            consumoTotalReferenciaAltoConsumo += categoria.getConsumoAlto().intValue() * categoria.getQuantidadeEconomias().intValue();
            
            consumoTotalReferenciaEstouroConsumo += categoria.getConsumoEstouro().intValue() * categoria.getQuantidadeEconomias().intValue();
            
            consumoMaximoCobrancaEstouroConsumo += categoria.getNumeroConsumoMaximoEc().intValue() * categoria.getQuantidadeEconomias().intValue();
            
            consumoTotalReferenciaBaixoConsumo += categoria.getMediaBaixoConsumo().intValue() * categoria.getQuantidadeEconomias().intValue();
            
            if (maiorQuantidadeEconomia < categoria.getQuantidadeEconomias().intValue()) {
                
                maiorQuantidadeEconomia = categoria.getQuantidadeEconomias().intValue();
                
                vezesMediaAltoConsumo = categoria.getVezesMediaAltoConsumo();
                vezesMediaEstouroConsumo = categoria.getVezesMediaEstouro();
                percentualDeterminacaoBaixoConsumo = categoria.getPorcentagemMediaBaixoConsumo();
            }
        }

        builder.append(Utilitarios.completaComZerosEsquerda(6, Math.min(consumoTotalReferenciaEstouroConsumo, 999999)));
        builder.append(Utilitarios.completaComZerosEsquerda(6, Math.min(consumoTotalReferenciaAltoConsumo, 999999)));
        builder.append(Utilitarios.completaComZerosEsquerda(6, Math.min(consumoTotalReferenciaBaixoConsumo, 999999)));
        builder.append(Utilitarios.completaComEspacosADireita(4, vezesMediaEstouroConsumo));
        builder.append(Utilitarios.completaComEspacosADireita(4, vezesMediaAltoConsumo));
        builder.append(Utilitarios.completaComZerosEsquerda(6, Utilitarios.formatarBigDecimalComPonto(percentualDeterminacaoBaixoConsumo)));
        builder.append(Utilitarios.completaComZerosEsquerda(6, Math.min(consumoMaximoCobrancaEstouroConsumo, 999999))); 
       
        dadosConsumo.put(22, builder);
    }
	
	private void escreverEsgotoAlternativo() {
		escreverPercentualAlternativoEsgoto();
		escreverConsumoPercentualAlternativoEsgoto();
	}
	private void escreverPercentualAlternativoEsgoto() {
		StringBuilder builder = new StringBuilder();
		
		if (imovel.possuiEsgoto() && imovel.getLigacaoEsgoto().possuiPercentualAlternativo()) {
			builder.append(Utilitarios.completaComZerosEsquerda(6, Utilitarios.formatarBigDecimalComPonto(imovel.getLigacaoEsgoto().getPercentualAlternativo())));
		} else {
			builder.append(Utilitarios.completaComEspacosADireita(6, ""));
		}
		 dadosConsumo.put(43, builder);
	}
	
	private void escreverConsumoPercentualAlternativoEsgoto() {
		StringBuilder builder = new StringBuilder();
		
		if (imovel.possuiEsgoto() && imovel.getLigacaoEsgoto().possuiNumeroConsumoPercentualAlternativo()) {
			builder.append(Utilitarios.completaComZerosEsquerda(6, imovel.getLigacaoEsgoto().getNumeroConsumoPercentualAlternativo()));
		} else {
			builder.append(Utilitarios.completaComEspacosADireita(6, ""));
		}
		 dadosConsumo.put(44, builder);
	}

}
