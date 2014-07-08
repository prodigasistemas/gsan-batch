package br.gov.batch.servicos.faturamento;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.Status;
import br.gov.model.cadastro.Cliente;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cadastro.ImovelContaEnvio;
import br.gov.model.cadastro.SistemaParametros;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.ContaGeral;
import br.gov.model.faturamento.DebitoCreditoSituacao;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ClienteRepositorio;
import br.gov.servicos.cadastro.SistemaParametrosRepositorio;
import br.gov.servicos.faturamento.ContaGeralRepositorio;
import br.gov.servicos.faturamento.ContaRepositorio;
import br.gov.servicos.faturamento.FaturamentoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.to.ContaTO;
import br.gov.servicos.to.GerarContaTO;

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
	
	private SistemaParametros sistemaParametros;
	
	public Conta gerarConta(GerarContaTO to){
		ContaGeral contaGeral = new ContaGeral();
		contaGeral.setIndicadorHistorico(Status.INATIVO.getId());
		contaGeral.setUltimaAlteracao(new java.sql.Date(Calendar.getInstance().getTimeInMillis()));

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
			.leiturasFaturamento(medicaoHistoricoRepositorio.obterPorImovelEReferencia(to.getImovel().getId(), to.getAnoMesFaturamento()))
			.rota(to.getRota());
		
		
		Conta conta = builder.build();
		conta.setNumeroBoleto(sequencialGeracaoBoleto(sistemaParametros.getValorContaFichaComp(), conta));
		
		Long idConta = contaGeralRepositorio.salvar(contaGeral);
		conta.setId(idConta);
		conta.setContaGeral(contaGeral);
		
		contaRepositorio.salvar(conta);
		
		return conta;
	}
	
	public Integer sequencialGeracaoBoleto(BigDecimal valorContaFichaCompensacao, Conta conta) {
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
				}
			}
		}
		
		if ((imovel.getImovelContaEnvio() == ImovelContaEnvio.ENVIAR_CLIENTE_RESPONSAVEL || imovel.getImovelContaEnvio() == ImovelContaEnvio.NAO_PAGAVEL_IMOVEL_PAGAVEL_RESPONSAVEL)
				&& !contaTO.comVencimentoAlternativo()
				&& imovel.getIndicadorDebitoConta() == Status.INATIVO) {
			contaTO.adicionaDiasAoVencimento(sistemaParametros.getNumeroDiasAdicionaisCorreios());
		}

		return contaTO.getDataVencimentoConta();
	}
	
	public ContaTO vencimentoAlternativo(Imovel imovel, Date dataVencimentoRota){
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
}
