package br.gov.batch.servicos.faturamento.arquivo;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.batch.servicos.arrecadacao.PagamentoBO;
import br.gov.batch.servicos.faturamento.AguaEsgotoBO;
import br.gov.batch.servicos.faturamento.EsgotoBO;
import br.gov.batch.servicos.faturamento.ExtratoQuitacaoBO;
import br.gov.batch.servicos.faturamento.FaturamentoAtividadeCronogramaBO;
import br.gov.batch.servicos.faturamento.FaturamentoSituacaoBO;
import br.gov.batch.servicos.faturamento.MensagemContaBO;
import br.gov.batch.servicos.micromedicao.ConsumoBO;
import br.gov.batch.servicos.micromedicao.HidrometroBO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cobranca.CobrancaDocumento;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.Rota;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ClienteEnderecoRepositorio;
import br.gov.servicos.cadastro.ImovelSubcategoriaRepositorio;
import br.gov.servicos.faturamento.FaturamentoParametroRepositorio;
import br.gov.servicos.faturamento.FaturamentoSituacaoRepositorio;
import br.gov.servicos.faturamento.FaturamentoSituacaoTipoRepositorio;
import br.gov.servicos.faturamento.QuadraFaceRepositorio;
import br.gov.servicos.faturamento.QualidadeAguaPadraoRepositorio;
import br.gov.servicos.faturamento.QualidadeAguaRepositorio;

@Stateless
public class ArquivoTextoTipo01 {
    private Imovel imovel;

    private Conta conta;
    
    private FaturamentoGrupo faturamentoGrupo;
    
    private Rota rota;
    
    private CobrancaDocumento cobrancaDocumento;

    @EJB
    private ClienteEnderecoRepositorio clienteEnderecoRepositorio;

    @EJB
    private FaturamentoParametroRepositorio repositorioParametros;
    
    @EJB
    private QualidadeAguaPadraoRepositorio qualidadeAguaPadraoRepositorio;

    @EJB
    private QualidadeAguaRepositorio qualidadeAguaRepositorio;
    
    @EJB
    private QuadraFaceRepositorio quadraFaceRepositorio;

    @EJB
    private FaturamentoSituacaoTipoRepositorio faturamentoSituacaoTipoRepositorio;
    
    @EJB
    private FaturamentoAtividadeCronogramaBO faturamentoAtividadeCronogramaBO;
    
    @EJB
    private FaturamentoSituacaoBO faturamentoSituacaoBO;
    
    @EJB
    private HidrometroBO hidrometroBO;
    
    @EJB
    private EsgotoBO esgotoBO;
    
    @EJB
    private AguaEsgotoBO aguaEsgotoBO;

    @EJB
    private ImovelSubcategoriaRepositorio imovelSubcategoriaRepositorio;
    
    @EJB
    private FaturamentoSituacaoRepositorio faturamentoSituacaoRepositorio;
    
    @EJB
    private MensagemContaBO mensagemContaBO;
    
    @EJB
    private ExtratoQuitacaoBO extratoQuitacaoBO;
    
    @EJB
    private PagamentoBO pagamentoBO;

    private StringBuilder builder = new StringBuilder();
    
    private SortedMap<Integer,StringBuilder> mapDados;
    
    @EJB
    private ConsumoBO consumoBO;

    private ArquivoTextoTipo01DadosCobranca dadosCobranca;
    private ArquivoTextoTipo01DadosCliente dadosCliente;
    private ArquivoTextoTipo01DadosConsumo dadosConsumo;
    private ArquivoTextoTipo01DadosConta dadosConta;
    private ArquivoTextoTipo01DadosFaturamento dadosFaturamento;
    private ArquivoTextoTipo01DadosLocalizacaoImovel dadosLocalizacaoImovel;
    
    private final String TIPO_REGISTRO = "01";
    
    public ArquivoTextoTipo01() {
        builder = new StringBuilder();
    }

    public String build() {
        mapDados = new TreeMap<Integer, StringBuilder>();
    	
	    builder.append(TIPO_REGISTRO);
	    builder.append(Utilitarios.completaComZerosEsquerda(9, String.valueOf(imovel.getId())));
	
	    mapDados.putAll(dadosCobranca.build());
	    mapDados.putAll(dadosCliente.build());
	    mapDados.putAll(dadosConsumo.build());
	    mapDados.putAll(dadosLocalizacaoImovel.build());
	    mapDados.putAll(dadosConta.build());
	    mapDados.putAll(dadosFaturamento.build());
	    
	    Iterator<Integer> itKeys = mapDados.keySet().iterator();
	    		
	    while (itKeys.hasNext()) {
	    	System.out.println(itKeys.next());
	    }
	    		
	    Iterator<StringBuilder> it = mapDados.values().iterator();
	    
	    while (it.hasNext()) {
	    	builder.append(it.next());
	    }
	    
	    builder.append(System.getProperty("line.separator"));
	    
 	    return builder.toString();
 	    
    }

	public Imovel getImovel() {
		return imovel;
	}

	public void setImovel(Imovel imovel) {
		this.imovel = imovel;
	}

	public FaturamentoGrupo getFaturamentoGrupo() {
		return faturamentoGrupo;
	}

	public void setFaturamentoGrupo(FaturamentoGrupo faturamentoGrupo) {
		this.faturamentoGrupo = faturamentoGrupo;
	}

	public Rota getRota() {
		return rota;
	}

	public void setRota(Rota rota) {
		this.rota = rota;
	}

	public Conta getConta() {
		return conta;
	}

	public void setConta(Conta conta) {
		this.conta = conta;
	}

	public CobrancaDocumento getCobrancaDocumento() {
		return cobrancaDocumento;
	}

	public void setCobrancaDocumento(CobrancaDocumento cobrancaDocumento) {
		this.cobrancaDocumento = cobrancaDocumento;
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
