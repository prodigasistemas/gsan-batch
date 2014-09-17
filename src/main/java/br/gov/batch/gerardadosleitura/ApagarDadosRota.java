package br.gov.batch.gerardadosleitura;

import java.util.ArrayList;
import java.util.List;

import javax.batch.api.Batchlet;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import br.gov.batch.util.BatchUtil;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.model.micromedicao.Rota;
import br.gov.servicos.arrecadacao.DebitoAutomaticoMovimentoRepositorio;
import br.gov.servicos.cadastro.ClienteContaRepositorio;
import br.gov.servicos.cobranca.CobrancaDocumentoItemRepositorio;
import br.gov.servicos.cobranca.parcelamento.ParcelamentoItemRepositorio;
import br.gov.servicos.faturamento.ContaCategoriaConsumoFaixaRepositorio;
import br.gov.servicos.faturamento.ContaCategoriaRepositorio;
import br.gov.servicos.faturamento.ContaGeralRepositorio;
import br.gov.servicos.faturamento.ContaImpostosDeduzidosRepositorio;
import br.gov.servicos.faturamento.ContaImpressaoRepositorio;
import br.gov.servicos.faturamento.ContaRepositorio;
import br.gov.servicos.faturamento.CreditoRealizadoCategoriaRepositorio;
import br.gov.servicos.faturamento.CreditoRealizadoRepositorio;
import br.gov.servicos.faturamento.CreditoRealizarRepositorio;
import br.gov.servicos.faturamento.DebitoCobradoCategoriaRepositorio;
import br.gov.servicos.faturamento.DebitoCobradoRepositorio;
import br.gov.servicos.faturamento.DebitoCobrarRepositorio;
import br.gov.servicos.micromedicao.RotaRepositorio;

@Named
public class ApagarDadosRota implements Batchlet{
	private static Logger logger = Logger.getLogger(ApagarDadosRota.class);
	
	@EJB
	private ClienteContaRepositorio clienteContaRepositorio;

	@EJB
	private ContaImpostosDeduzidosRepositorio contaImpostosDeduzidosRepositorio;
	
	@EJB
	private DebitoAutomaticoMovimentoRepositorio debitoAutomaticoMovimentoRepositorio;
	
	@EJB
	private DebitoCobradoCategoriaRepositorio debitoCobradoCategoriaRepositorio;
	
	@EJB
	private DebitoCobradoRepositorio debitoCobradoRepositorio;
	
	@EJB
	private CreditoRealizadoCategoriaRepositorio creditoRealizadoCategoriaRepositorio;
	
	@EJB
	private CreditoRealizadoRepositorio creditoRealizadoRepositorio;
	
	@EJB
	private DebitoCobrarRepositorio debitoCobrarRepositorio;
	
	@EJB
	private CreditoRealizarRepositorio creditoRealizarRepositorio;
	
	@EJB
	private ContaGeralRepositorio contaGeralRepositorio;
	
	@EJB
	private ParcelamentoItemRepositorio parcelamentoItemRepositorio;
	
	@EJB
	private CobrancaDocumentoItemRepositorio cobrancaDocumentoItemRepositorio;
	
	@EJB
	private ContaRepositorio contaRepositorio;
	
	@EJB
	private ContaCategoriaConsumoFaixaRepositorio contaCategoriaConsumoFaixaRepositorio;
	
	@EJB
	private ContaCategoriaRepositorio contaCategoriaRepositorio;
	
	@EJB
	private ContaImpressaoRepositorio contaImpressaoRepositorio;
	
	@EJB
	private RotaRepositorio rotaRepositorio;
	
    @Inject
    private BatchUtil util;
    
	public String process() throws Exception {
    	Integer idRota = Integer.valueOf(util.parametroDoBatch("idRota"));
    	Integer referencia = Integer.valueOf(util.parametroDoBatch("anoMesFaturamento"));
    	Integer grupoFaturamento = Integer.valueOf(util.parametroDoBatch("idGrupoFaturamento"));
    	
    	logger.info("Exclusao de dados prefaturados para a rota: " + idRota);
    	
    	Rota rota = rotaRepositorio.findById(idRota);
    	
    	List<Long> idsContas  = new ArrayList<Long>();
    	List<Integer> idsImoveis = new ArrayList<Integer>();
    	
    	if (rota.isAlternativa()){
    		idsContas = contaRepositorio.idsContasDeImovelComRotaAlternativa(idRota, referencia, DebitoCreditoSituacao.PRE_FATURADA.getId(), grupoFaturamento);
    		idsImoveis = contaRepositorio.imoveisDeContasComRotaAlternativa(idRota, referencia, DebitoCreditoSituacao.PRE_FATURADA.getId(), grupoFaturamento);
    	}else{
    		idsContas = contaRepositorio.idsContasDeImovelSemRotaAlternativa(idRota, referencia, DebitoCreditoSituacao.PRE_FATURADA.getId(), grupoFaturamento);
    		idsImoveis = contaRepositorio.imoveisDeContasSemRotaAlternativa(idRota, referencia, DebitoCreditoSituacao.PRE_FATURADA.getId(), grupoFaturamento);
    	}
    	
    	if (idsContas.size() > 0){
    		contaImpressaoRepositorio.apagarImpressaoDasContas(idsContas);
    		contaCategoriaConsumoFaixaRepositorio.apagarCategoriaConsumoFaixaDasContas(idsContas);
    		contaCategoriaRepositorio.apagarCategoriaDasContas(idsContas);
    		clienteContaRepositorio.apagarClientesConta(idsContas);
    		contaImpostosDeduzidosRepositorio.apagarImpostosDeduzidosDeContas(idsContas);
    		debitoAutomaticoMovimentoRepositorio.apagarMovimentosDebitoAutomaticoDasConta(idsContas);
    		
    		List<Integer> idsDebitosCobrados = debitoCobradoRepositorio.debitosCobradosDasContas(idsContas);
    		debitoCobradoCategoriaRepositorio.apagarCategoriasdosDebitosCobrados(idsDebitosCobrados);
    		debitoCobradoRepositorio.apagarDebitosCobradosDasContas(idsContas);
    		
    		List<Integer> idsCreditosRealizados = creditoRealizadoRepositorio.creditosRealizadosDasContas(idsContas);
    		creditoRealizadoCategoriaRepositorio.apagarCategoriasDosCreditosRealizados(idsCreditosRealizados);
    		creditoRealizadoRepositorio.apagarCreditosRealizadosDasContas(idsContas);
    		
    		debitoCobrarRepositorio.reduzirParcelasCobradas(referencia, grupoFaturamento, idsImoveis);
    		creditoRealizarRepositorio.atualizarParcelas(referencia, idsImoveis);
    		creditoRealizarRepositorio.atualizarValorResidual(idsImoveis);
    		contaGeralRepositorio.alterarHistoricoParaContasDeletadasPorReprocessamento(idsContas);
    		parcelamentoItemRepositorio.eliminarParcelamentosDeContas(idsContas);
    		cobrancaDocumentoItemRepositorio.apagarItensCobrancaDasContas(idsContas);
    		contaRepositorio.apagar(idsContas);
    	}
		
		return null;
	}

	public void stop() throws Exception {
		
	}
}
