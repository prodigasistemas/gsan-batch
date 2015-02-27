package br.gov.batch.servicos.faturamento;

import static br.gov.model.util.Utilitarios.completaComZerosEsquerda;
import static br.gov.model.util.Utilitarios.obterQuantidadeLinhasTexto;
import static br.gov.model.util.Utilitarios.quebraLinha;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.logging.Logger;

import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo01;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo02;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo03;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo04;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo05;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo06;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo07;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo08;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo09;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo10;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo11;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo12;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo13;
import br.gov.batch.servicos.faturamento.arquivo.ArquivoTextoTipo14;
import br.gov.batch.servicos.faturamento.to.ArquivoTextoTO;
import br.gov.batch.servicos.micromedicao.MovimentoRoteiroEmpresaBO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.cobranca.CobrancaDocumento;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.ArquivoTextoRoteiroEmpresa;
import br.gov.model.micromedicao.ArquivoTextoRoteiroEmpresaDivisao;
import br.gov.model.micromedicao.Rota;
import br.gov.model.micromedicao.SituacaoTransmissaoLeitura;
import br.gov.model.micromedicao.TipoServicoCelular;
import br.gov.model.util.IOUtil;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.cadastro.QuadraRepositorio;
import br.gov.servicos.cobranca.CobrancaDocumentoRepositorio;
import br.gov.servicos.faturamento.ContaRepositorio;
import br.gov.servicos.micromedicao.ArquivoTextoRoteiroEmpresaDivisaoRepositorio;
import br.gov.servicos.micromedicao.ArquivoTextoRoteiroEmpresaRepositorio;
import br.gov.servicos.micromedicao.RotaRepositorio;

@Stateless
public class GeradorArquivoTextoFaturamento {
    private static Logger logger = Logger.getLogger(GeradorArquivoTextoFaturamento.class);
    
	@EJB
	private ArquivoTextoRoteiroEmpresaDivisaoRepositorio arquivoDivisaoRepositorio;

	@EJB
	private ArquivoTextoRoteiroEmpresaRepositorio arquivoRepositorio;

	@EJB
	private ImovelRepositorio imovelRepositorio;

	@EJB
	private ContaRepositorio contaRepositorio;

	@EJB
	private CobrancaDocumentoRepositorio cobrancaDocumentoRepositorio;

	@EJB
	private MovimentoRoteiroEmpresaBO movimentoRoteiroEmpresaBO;
	
	@EJB
	private ContaBO contaBO;

	@EJB
	private QuadraRepositorio quadraRepositorio;
	
	private ArquivoTextoTO to;
	
    @EJB
	private ArquivoTextoTipo01 tipo01;
    
    @EJB
    private ArquivoTextoTipo02 tipo02;
    
    @EJB
    private ArquivoTextoTipo03 tipo03;

    @EJB
    private ArquivoTextoTipo04 tipo04;

    @EJB
    private ArquivoTextoTipo05 tipo05;

    @EJB
    private ArquivoTextoTipo06 tipo06;

    @EJB
    private ArquivoTextoTipo07 tipo07;

    @EJB
    private ArquivoTextoTipo08 tipo08;

    @EJB
    private ArquivoTextoTipo09 tipo09;

    @EJB
    private ArquivoTextoTipo10 tipo10;
    
    @EJB
    private ArquivoTextoTipo11 tipo11;
    
    @EJB
    private ArquivoTextoTipo12 tipo12;
    
    @EJB
    private ArquivoTextoTipo13 tipo13;
    
    @EJB
    private ArquivoTextoTipo14 tipo14;
    
    @EJB
    private RotaRepositorio rotaRepositorio;
	
	public GeradorArquivoTextoFaturamento() {
		super();

		to = new ArquivoTextoTO();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void gerar(Integer idRota, Date dataComando) {
	    Rota rota = rotaRepositorio.obterPorID(idRota);
	    
	    //TODO: Alterar no batch-manager para nao inserir rotas inativas
	    if (!rota.isAtiva())
	        return;
	    
	    if (rota.getId() != 697)
	    	return;

	    Integer anoMesFaturamento = rota.getFaturamentoGrupo().getAnoMesReferencia();
	    FaturamentoGrupo grupoFaturamento = rota.getFaturamentoGrupo();
	    
		final int quantidadeRegistros = 3000;
		int primeiroRegistro = 0;

        logger.info("Rota: " + idRota + " - Pesquisa de imoveis para gerar o arquivo: ");

		//TODO: Imoveis estao vindo repetidos. Testar isso!
		List<Imovel> imoveis = imoveisParaGerarArquivoTextoFaturamento(rota, primeiroRegistro, quantidadeRegistros);
		
		if (imoveis.isEmpty()){
		    return;
		}

		List<Imovel> imoveisArquivo = new ArrayList<Imovel>();

		List<ArquivoTextoRoteiroEmpresaDivisao> divisoes = new ArrayList<ArquivoTextoRoteiroEmpresaDivisao>();

		StringBuilder conteudo = new StringBuilder();

		logger.info("Rota: " + idRota + " - Leitura de imoveis: " + imoveis.size());
		for (Imovel imovel : imoveis) {
		    logger.info("Rota: " + idRota + "INICIO - Imovel para processamento id [" + imovel.getId() + "]");
			if (imovel.isCondominio()) {
				if (imovel.existeHidrometro()) {
					List<Imovel> imoveisCondominio = imoveisCondominioParaGerarArquivoTextoFaturamento(rota, imovel.getId());

					boolean imovelMicroComConta = false;

					for (Imovel imovelCondominio : imoveisCondominio) {
						if (contaRepositorio.existeContaPreFaturadaSemMovimento(imovelCondominio.getId(), anoMesFaturamento)) {
							imovelMicroComConta = true;
							break;
						}
					}

					if (imovelMicroComConta) {
						imoveisArquivo.add(imovel);
						conteudo.append(conteudo.length() > 0 ? quebraLinha : "");
						conteudo.append(gerarArquivoTexto(imovel, null, anoMesFaturamento, rota, grupoFaturamento, dataComando));

						for (Imovel micro : imoveisCondominio) {
							conteudo.append(quebraLinha)
							     .append(carregarArquivo(imovel, anoMesFaturamento, rota, grupoFaturamento, dataComando));
							imoveisArquivo.add(micro);
						}
					}
				}
			} else {
				conteudo.append(conteudo.length() > 0 ? quebraLinha : "");
				conteudo.append(carregarArquivo(imovel, anoMesFaturamento, rota, grupoFaturamento, dataComando));
				imoveisArquivo.add(imovel);
			}
			
		    if (rota.existeLimiteImoveis()) {
		        if (imoveisArquivo.size() >= rota.getNumeroLimiteImoveis()) {
		            divisoes.add(criarRoteiroArquivoDividido(conteudo, rota, anoMesFaturamento, imoveis));
		            conteudo = new StringBuilder();
		            imoveisArquivo.clear();
		        }
		    }
		    logger.info("Rota: " + idRota + "FIM - Imovel processado id [" + imovel.getId() + "]");
		}
		
		logger.info("Rota: " + idRota + " - Imoveis lidos");
		
		List<Imovel> imoveisPreFaturados = imovelRepositorio.obterImoveisComContasPreFaturadas(anoMesFaturamento, rota.getId());

		ArquivoTextoRoteiroEmpresa roteiro = criarRoteiroArquivoTexto(rota, imoveis.get(0), grupoFaturamento, anoMesFaturamento, imoveisPreFaturados.size());
		
		roteiro.setDivisoes(divisoes);
		
		for (int i = 0; i < divisoes.size() ; i++){
		    divisoes.get(i).acrescentaSequencial(i + 1);
		    divisoes.get(i).setArquivoTextoRoteiroEmpresa(roteiro);
		}
		
        if (rota.existeLimiteImoveis()) {
            divisoes.add(criarRoteiroArquivoDividido(conteudo, rota, anoMesFaturamento, imoveis));
        }
        
        arquivoRepositorio.salvar(roteiro);
        
        logger.info("Rota: " + idRota + " - Roteiro salvo");
        
        if (rota.existeLimiteImoveis()){
            divisoes.forEach(e -> IOUtil.criarArquivo(e.getNomeArquivo(), "", e.getConteudoArquivo().toString()));
        }else{
            conteudo.append(gerarPassosFinais());
            //TODO: Recuperar caminho  por parametros
            IOUtil.criarArquivo(roteiro.getNomeArquivo(), "/temp/", new StringBuilder(obterQuantidadeLinhasTexto(conteudo)).append(quebraLinha).append(conteudo).toString());
        }
        
        logger.info("Rota: " + idRota + " - Arquivo criado");
        
//        movimentoRoteiroEmpresaBO.gerarMovimentoRoteiroEmpresa(imoveisArquivo, rota);
	}
	
	
	public ArquivoTextoRoteiroEmpresaDivisao criarRoteiroArquivoDividido(StringBuilder texto, Rota rota, Integer anoMesFaturamento, List<Imovel> imoveis){
	    List<Integer> ids = new ArrayList<Integer>();
	    imoveis.forEach(e -> ids.add(e.getId()));
	    
	    Integer qtdImoveisDivididos = contaRepositorio.obterQuantidadeContasPreFaturadaPorImoveis(anoMesFaturamento, ids);
	    
	    ArquivoTextoRoteiroEmpresaDivisao roteiro = new ArquivoTextoRoteiroEmpresaDivisao();
	    
	    texto.append(gerarPassosFinais());
	    roteiro.setSituacaoTransmissaoLeitura(SituacaoTransmissaoLeitura.DISPONIVEL);
	    roteiro.setUltimaAlteracao(new Date());
        roteiro.setQuantidadeImovel(qtdImoveisDivididos);
        
        if (rota.getLeiturista() != null) {
            roteiro.setLeiturista(rota.getLeiturista());
            roteiro.setNumeroImei(rota.getLeiturista().getNumeroImei());
        }
	    
	    roteiro.setConteudoArquivo(new StringBuilder(obterQuantidadeLinhasTexto(texto)).append(quebraLinha).append(texto));
	    
	    return roteiro;
	}
	
	public ArquivoTextoRoteiroEmpresa criarRoteiroArquivoTexto(Rota rota, Imovel imovel, FaturamentoGrupo faturamentoGrupo, Integer anoMesFaturamento, Integer qtdImoveisComContaPF){
	    ArquivoTextoRoteiroEmpresa arquivo = new ArquivoTextoRoteiroEmpresa();
	    
	    StringBuilder nomeArquivo = new StringBuilder("G");
	    nomeArquivo.append(completaComZerosEsquerda(3, faturamentoGrupo.getId()));

	    //TODO: confirmar se a localidade para rota alternativa vem do imovel ou da rota
	    if (rota.isAlternativa()) {
	        nomeArquivo.append(completaComZerosEsquerda(3, rota.getSetorComercial().getLocalidade().getId()));
	        nomeArquivo.append(completaComZerosEsquerda(3, rota.getSetorComercial().getCodigo()));
	        
	        arquivo.setLocalidade(rota.getSetorComercial().getLocalidade());
	        arquivo.setCodigoSetorComercial1(rota.getSetorComercial().getCodigo());
	    }else{
	        nomeArquivo.append(completaComZerosEsquerda(3, imovel.getLocalidade().getId()));
	        nomeArquivo.append(completaComZerosEsquerda(3, imovel.getSetorComercial().getCodigo()));
	        
	        arquivo.setLocalidade(imovel.getLocalidade());
	        arquivo.setCodigoSetorComercial1(imovel.getSetorComercial().getCodigo());
	    }
	    
	    nomeArquivo.append(completaComZerosEsquerda(4, rota.getCodigo()));
	    nomeArquivo.append(completaComZerosEsquerda(6, anoMesFaturamento));
	    
        arquivo.setAnoMesReferencia(anoMesFaturamento);
        arquivo.setFaturamentoGrupo(faturamentoGrupo);
        arquivo.setEmpresa(rota.getEmpresa());
        arquivo.setRota(rota);
        arquivo.setNumeroSequenciaLeitura(rota.getNumeroSequenciaLeitura());
        
        int[] intervalorNumeroQuadra = quadraRepositorio.obterIntervaloQuadrasPorRota(rota.getId());
        
        arquivo.setNumeroQuadraInicial1(intervalorNumeroQuadra[0]);
        arquivo.setNumeroQuadraFinal1(intervalorNumeroQuadra[1]);

        arquivo.setQuantidadeImovel(qtdImoveisComContaPF);
        arquivo.setNomeArquivo(nomeArquivo.toString());

        arquivo.setLeiturista(rota.getLeiturista());
        arquivo.setCodigoLeiturista(rota.getLeiturista().getCodigoDDD());
        arquivo.setNumeroFoneLeiturista(rota.getLeiturista().getNumeroFone());
        
        if (rota.getNumeroLimiteImoveis() == null) {
            arquivo.setNumeroImei(rota.getLeiturista().getNumeroImei());
        }
        
        if (rotaSoComImoveisInformativos(qtdImoveisComContaPF)) {
            arquivo.setSituacaoTransmissaoLeitura(SituacaoTransmissaoLeitura.TRANSMITIDO.getId());
        } else{
            arquivo.setSituacaoTransmissaoLeitura(SituacaoTransmissaoLeitura.DISPONIVEL.getId());
        }

        arquivo.setUltimaAlteracao(new Date());

        arquivo.setServicoTipoCelular(TipoServicoCelular.IMPRESSAO_SIMULTANEA.getId());

	    return arquivo;
	}
	
	private boolean rotaSoComImoveisInformativos(Integer qtdImoveisComContaPF) {
        return qtdImoveisComContaPF > 0 ? false : true;
    }


	//TODO: Verificar no original onde essa chamada eh feita. Aqui nao usa
	public boolean existeArquivoTextoRota(Integer idRota, Integer anoMesReferencia) {
		boolean retorno = true;

		ArquivoTextoRoteiroEmpresa arquivo = arquivoRepositorio.pesquisarPorRotaEReferencia(idRota, anoMesReferencia);

		if (arquivo != null) {
			if (arquivo.getSituacaoTransmissaoLeitura() == SituacaoTransmissaoLeitura.DISPONIVEL.getId()) {
				arquivoDivisaoRepositorio.deletarPorArquivoTextoRoteiroEmpresa(arquivo.getId());
				arquivoRepositorio.excluir(arquivo.getId());
			} else {
				retorno = false;
			}
		}

		return retorno;
	}

	public List<Imovel> imoveisParaGerarArquivoTextoFaturamento(Rota rota, int primeiroRegistro, int quantidadeRegistros) {
		List<Imovel> imoveisConsulta = null;

		if (rota.alternativa()) {
			imoveisConsulta = imovelRepositorio.buscarImoveisParaGerarArquivoTextoFaturamentoPorRotaAlternativa(rota.getId(), primeiroRegistro, quantidadeRegistros);
		} else {
			imoveisConsulta = imovelRepositorio.buscarImoveisParaGerarArquivoTextoFaturamento(rota.getId(), primeiroRegistro, quantidadeRegistros);
		}

		List<Imovel> imoveis = new ArrayList<Imovel>();

		for (Imovel imovel : imoveisConsulta) {
			if (contaBO.emitirConta(imovel) || imovel.pertenceACondominio() || imovel.isCondominio() || imovel.existeHidrometroAgua() || imovel.existeHidrometroPoco()){
				imoveis.add(imovel);
			}
		}

		return imoveis;
	}

	public List<Imovel> imoveisCondominioParaGerarArquivoTextoFaturamento(Rota rota, Integer idCondominio) {
		List<Imovel> imoveisConsulta = null;

		if (rota.alternativa()) {
			imoveisConsulta = imovelRepositorio.imoveisCondominioParaGerarArquivoTextoFaturamentoPorRotaAlternativa(idCondominio);
		} else {
			imoveisConsulta = imovelRepositorio.imoveisCondominioParaGerarArquivoTextoFaturamento(idCondominio);
		}

		List<Imovel> imoveis = new ArrayList<Imovel>();

		for (Imovel imovel : imoveisConsulta) {
			if (imovel.pertenceACondominio() || imovel.isCondominio() || imovel.existeHidrometro()) {
				imoveis.add(imovel);
			}
		}

		return imoveis;
	}

	public StringBuilder carregarCabecalhoArquivo(StringBuilder builder) {
		builder.insert(0, Utilitarios.obterQuantidadeLinhasTexto(builder));
		builder.append(System.getProperty("line.separator"));
		
		return builder;
	}
	
	public StringBuilder carregarArquivo(Imovel imovel, Integer anoMesReferencia, Rota rota, FaturamentoGrupo faturamentoGrupo, Date dataComando) {
		Conta conta = contaRepositorio.pesquisarContaArquivoTextoFaturamento(imovel.getId(), anoMesReferencia, faturamentoGrupo.getId());
		return gerarArquivoTexto(imovel, conta, anoMesReferencia, rota, faturamentoGrupo, dataComando);
	}

	public StringBuilder gerarArquivoTexto(Imovel imovel, Conta conta, Integer anoMesReferencia, Rota rota, FaturamentoGrupo faturamentoGrupo, Date dataComando) {

		CobrancaDocumento cobrancaDocumento = cobrancaDocumentoRepositorio.cobrancaDocumentoImpressaoSimultanea(
				Utilitarios.reduzirDias(dataComando, 10), imovel.getId());

		to = new ArquivoTextoTO(imovel, conta, anoMesReferencia, faturamentoGrupo, rota, cobrancaDocumento);
		
		to.setIdImovel(imovel.getId());

		StringBuilder arquivoTexto = new StringBuilder();
		
//		logger.info("INICIO - Linha 01");
		arquivoTexto.append(tipo01.build(to));
//		logger.info("FIM    - Linha 01");

//		logger.info("INICIO - Linha 02");
		arquivoTexto.append(tipo02.build(to));
//		logger.info("FIM    - Linha 02");

//		logger.info("INICIO - Linha 03");
		arquivoTexto.append(tipo03.build(to));
//		logger.info("FIM    - Linha 03");

//		logger.info("INICIO - Linha 04");
		arquivoTexto.append(tipo04.build(to));
//		logger.info("FIM    - Linha 04");

//		logger.info("INICIO - Linha 05");
		arquivoTexto.append(tipo05.build(to));
//		logger.info("FIM    - Linha 05");

//		logger.info("INICIO - Linha 06");
		arquivoTexto.append(tipo06.build(to));
//		logger.info("FIM    - Linha 06");

//		logger.info("INICIO - Linha 07");
		arquivoTexto.append(tipo07.build(to));
//		logger.info("FIM    - Linha 07");

//		logger.info("INICIO - Linha 08");
		arquivoTexto.append(tipo08.build(to));
//		logger.info("FIM    - Linha 08");

//		logger.info("INICIO - Linha 09");
		arquivoTexto.append(tipo09.build(to));
//		logger.info("FIM    - Linha 09");

//		logger.info("INICIO - Linha 10");
		arquivoTexto.append(tipo10.build(to));
//		logger.info("FIM    - Linha 10");
		
		return arquivoTexto;
	}

	private StringBuilder gerarPassosFinais() {
	    
	    StringBuilder arquivoTexto = new StringBuilder();
		
		arquivoTexto.append(tipo11.build(to));
		
		arquivoTexto.append(tipo12.build(to));

		arquivoTexto.append(tipo13.build(to));

		arquivoTexto.append(tipo14.build(to));
		
		return arquivoTexto;
	}
}
