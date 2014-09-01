package br.gov.batch.servicos.faturamento;

import java.math.BigDecimal;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import br.gov.model.Status;
import br.gov.model.atendimentopublico.LigacaoEsgoto;
import br.gov.model.cadastro.Cliente;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.ContaGeral;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.atendimentopublico.LigacaoEsgotoRepositorio;
import br.gov.servicos.cadastro.ClienteRepositorio;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.ContaGeralRepositorio;
import br.gov.servicos.faturamento.ContaRepositorio;
import br.gov.servicos.faturamento.FaturamentoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.to.ContaTO;
import br.gov.servicos.to.CreditosContaTO;
import br.gov.servicos.to.DebitosContaTO;
import br.gov.servicos.to.GerarContaTO;
import br.gov.servicos.to.ImpostosDeduzidosContaTO;

@Stateless
public class ContaBO {
	
	@EJB
	private ClienteRepositorio clienteRepositorio;
	
	@EJB
	private ContaGeralRepositorio contaGeralRepositorio;
	
	@EJB
	private ContaRepositorio contaRepositorio;
	
	@EJB
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorio; 
	
	@EJB
	private FaturamentoRepositorio faturamentoRepositorio;
	
	@EJB
	private SistemaParametrosRepositorio sistemaParametrosRepositorio;
	
	@EJB
	private LigacaoEsgotoRepositorio ligacaoEsgotoRepositorio;
	
	private SistemaParametros sistemaParametros;
	
	@PostConstruct
	private void init(){
		sistemaParametros = sistemaParametrosRepositorio.getSistemaParametros();
	}	

	@TransactionAttribute(TransactionAttributeType.MANDATORY)
	public Conta gerarConta(FaturamentoImovelTO faturamentoTO, DebitosContaTO debitosContaTO, CreditosContaTO creditosContaTO, 
							ImpostosDeduzidosContaTO impostosDeduzidosContaTO) throws Exception {
		
		GerarContaTO gerarTO = buildGerarContaTO(faturamentoTO, debitosContaTO, creditosContaTO, impostosDeduzidosContaTO);
		return gerarConta(gerarTO);
	}
	
	@TransactionAttribute(TransactionAttributeType.MANDATORY)
	public Conta gerarConta(GerarContaTO to) throws Exception{
		ContaGeral contaGeral = new ContaGeral();
		contaGeral.setIndicadorHistorico(Status.INATIVO.getId());

		Conta conta = buildConta(to);
		
		conta.setNumeroBoleto(sequencialGeracaoBoleto(sistemaParametros.getValorContaFichaComp(), conta));
		
		Long idConta = contaGeralRepositorio.salvar(contaGeral);
		conta.setId(idConta);
		conta.setContaGeral(contaGeral);
		
		contaRepositorio.salvar(conta);
		
		return conta;
	}
	
	public GerarContaTO buildGerarContaTO(FaturamentoImovelTO faturamentoTO, 
			DebitosContaTO gerarDebitoCobradoHelper, CreditosContaTO creditosContaTO,
			ImpostosDeduzidosContaTO impostosDeduzidosTO) throws Exception {
		
		GerarContaTO gerarTO = new GerarContaTO();
		gerarTO.setFaturamentoGrupo(faturamentoTO.getFaturamentoGrupo());
		gerarTO.setImovel(faturamentoTO.getImovel());
		gerarTO.setRota(faturamentoTO.getRota());
		gerarTO.setDataVencimentoRota(faturamentoTO.getDataVencimentoConta());
		gerarTO.setFaturamentoGrupo(faturamentoTO.getFaturamentoGrupo());
		gerarTO.setAnoMesFaturamento(faturamentoTO.getAnoMesFaturamento());
		gerarTO.setValorTotalCreditos(creditosContaTO.getValorTotalCreditos());
		gerarTO.setValorTotalDebitos(gerarDebitoCobradoHelper.getValorTotalDebito());
		gerarTO.setValorTotalImposto(impostosDeduzidosTO.getValorTotalImposto());

		LigacaoEsgoto ligacaoEsgoto = ligacaoEsgotoRepositorio.buscarLigacaoEsgotoPorIdImovel(faturamentoTO.getImovel().getId());
		if (ligacaoEsgoto != null){
			gerarTO.setPercentualEsgoto(this.verificarPercentualEsgotoAlternativo(ligacaoEsgoto, faturamentoTO.getImovel()));
			gerarTO.setPercentualColeta(ligacaoEsgoto.getPercentualAguaConsumidaColetada());
		} else {
			gerarTO.setPercentualEsgoto(BigDecimal.ZERO);
		}
		
		return gerarTO;
	}
	
	public Conta buildConta(GerarContaTO to){
		Conta.Builder builder = new Conta.Builder();
		builder.imovel(to.getImovel())
			.referenciaFaturamento(to.getAnoMesFaturamento())
			.referenciaContabil(to.getAnoMesFaturamento())
			.dataVencimentoConta(this.determinarVencimentoConta(to.getImovel(), to.getDataVencimentoRota()))
			.validadeConta(sistemaParametros.getNumeroMesesValidadeConta())
			.indicadorAlteracaoVencimento((short) 2)
			.valorAgua(BigDecimal.ZERO)
			.valorEsgoto(BigDecimal.ZERO)
			.valorCreditos(to.getValorTotalCreditos())
			.valorDebitos(to.getValorTotalDebitos())
			.valorImposto(to.getValorTotalImposto())
			.percentualEsgoto(to.getPercentualEsgoto())
			.percentualColeta(to.getPercentualColeta())
			.debitoCreditoSituacaoAtual(DebitoCreditoSituacao.PRE_FATURADA)
			.faturamentoGrupo(to.getFaturamentoGrupo())
			.leiturasFaturamento(medicaoHistoricoRepositorio.buscarPorImovelEReferencia(to.getImovel().getId(), to.getAnoMesFaturamento()))
			.rota(to.getRota());
		
		return builder.build();
	}
	
	private Integer sequencialGeracaoBoleto(BigDecimal valorContaFichaCompensacao, Conta conta) {
		Integer sequencialContaBoleto = null;

		if (valorContaFichaCompensacao != null	&& conta.calculaValorTotal().compareTo(valorContaFichaCompensacao) > 0) {
			sequencialContaBoleto = faturamentoRepositorio.gerarSequencialContaBoleto();
		}

		return sequencialContaBoleto;
	}
	
	
	public Date determinarVencimentoConta(Imovel imovel, Date dataVencimentoRota){
		ContaTO contaTO = vencimentoAlternativo(imovel, dataVencimentoRota);
		
		if (contaTO.comVencimentoAlternativo()){
			if (contaTO.vencimentoMesSeguinte()){
				contaTO.adicionaMesAoVencimento();
			}else if (dataVencimentoRota.after(contaTO.getDataVencimentoConta())){
				Date dataAtualMaisDiasMinimoEmissao = Utilitarios.adicionarDias(new Date(),	sistemaParametros.getNumeroMinimoDiasEmissaoVencimento());
				
				if (dataAtualMaisDiasMinimoEmissao.after(contaTO.getDataVencimentoConta())){
					contaTO.adicionaMesAoVencimento();
				} else{
					contaTO.setDataVencimentoConta(dataVencimentoRota);
				}
			}
		}
		
		if (imovel.responsavelRecebeConta()
				&& !contaTO.comVencimentoAlternativo()
				&& !imovel.debitoEmConta()) {
			contaTO.adicionaDiasAoVencimento(sistemaParametros.getNumeroDiasAdicionaisCorreios());
		}

		return contaTO.getDataVencimentoConta();
	}
	
	private ContaTO vencimentoAlternativo(Imovel imovel, Date dataVencimentoRota){
		ContaTO contaTO = new ContaTO();
		contaTO.setDataVencimentoConta(dataVencimentoRota);
		if (imovel.existeDiaVencimento() && !imovel.emissaoExtratoFaturamento()) {
			contaTO.setDiaVencimentoAlternativo(imovel.getDiaVencimento());
			contaTO.setIndicadorVencimentoMesSeguinte(imovel.getIndicadorVencimentoMesSeguinte());
		} else {
			Cliente cliente = clienteRepositorio.buscarClienteResponsavelPorImovel(imovel.getId());

			if (cliente != null) {
				if (cliente.existeDiaVencimento()) {
					contaTO.setDiaVencimentoAlternativo(cliente.getDiaVencimento());
					contaTO.setIndicadorVencimentoMesSeguinte(cliente.getIndicadorVencimentoMesSeguinte());
				}
				else if (imovel.emissaoExtratoFaturamento()) {
					contaTO.setDiaVencimentoAlternativo(Utilitarios.obterUltimoDiaMes(contaTO.getDataVencimentoConta()));
				}
			}
		}
		
		return contaTO;
	}
	
	private BigDecimal verificarPercentualEsgotoAlternativo(LigacaoEsgoto ligacaoEsgoto, Imovel imovel) throws Exception {

		if (imovel.getLigacaoEsgotoSituacao().getSituacaoFaturamento().equals(Status.ATIVO) 
				&& ligacaoEsgoto.getNumeroConsumoPercentualAlternativo().intValue() >= 0 
				&& ligacaoEsgoto.getPercentualAlternativo() != null
				&& ligacaoEsgoto.getPercentualAlternativo().compareTo(ligacaoEsgoto.getPercentual()) == -1) {
			
			return ligacaoEsgoto.getPercentualAlternativo();
		} else {
			return BigDecimal.ZERO;
		}
	}
	
	public void setSistemaParametros(SistemaParametros sistemaParametros) {
		this.sistemaParametros = sistemaParametros;
	}
}
