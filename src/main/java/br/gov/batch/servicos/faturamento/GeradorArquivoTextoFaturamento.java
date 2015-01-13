package br.gov.batch.servicos.faturamento;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.Imovel;
import br.gov.model.micromedicao.ArquivoTextoRoteiroEmpresa;
import br.gov.model.micromedicao.Rota;
import br.gov.model.micromedicao.SituacaoTransmissaoLeitura;
import br.gov.servicos.cadastro.ImovelRepositorio;
import br.gov.servicos.faturamento.ContaRepositorio;
import br.gov.servicos.micromedicao.ArquivoTextoRoteiroEmpresaDivisaoRepositorio;
import br.gov.servicos.micromedicao.ArquivoTextoRoteiroEmpresaRepositorio;

@Stateless
public class GeradorArquivoTextoFaturamento {
	@EJB
	private ArquivoTextoRoteiroEmpresaDivisaoRepositorio arquivoDivisaoRepositorio;
	
	@EJB
	private ArquivoTextoRoteiroEmpresaRepositorio arquivoRepositorio;
	
	@EJB
	private ImovelRepositorio imovelRepositorio;
	
	@EJB
	private ContaRepositorio contaRepositorio;
	
	
	public void gerar(Rota rota, Integer anoMesFaturamento){
		final int quantidadeRegistros = 3000;
		int primeiroRegistro = 0;
		
		List<Imovel> imoveis = imoveisParaGerarArquivoTextoFaturamento(rota, primeiroRegistro, quantidadeRegistros);
		
		List<Imovel> imoveisArquivo = new ArrayList<Imovel>();
		
		for (Imovel imovel : imoveis) {
			if (imovel.ehCondominio()) {
				if (imovel.existeHidrometro()) {
					List<Imovel> imoveisCondominio = imoveisCondominioParaGerarArquivoTextoFaturamento(rota, imovel.getId());
					
					boolean imovelMicroComConta = false;
					

					for (Imovel imovelCondominio : imoveisCondominio) {
						if (contaRepositorio.existeContaPreFaturada(imovelCondominio.getId(), anoMesFaturamento)){
							imovelMicroComConta = true;
							break;
						}
					}
					
					if (imovelMicroComConta){
						imoveisArquivo.add(imovel);
					}
				}
			}			
		}
	}

	public boolean existeArquivoTextoRota(Integer idRota, Integer anoMesReferencia){
		boolean retorno = true;
		
		ArquivoTextoRoteiroEmpresa arquivo = arquivoRepositorio.recuperaArquivoTextoRoteiroEmpresa(idRota, anoMesReferencia);

		if (arquivo != null) {
			if (arquivo.getSituacaoTransmissaoLeitura() == SituacaoTransmissaoLeitura.DISPONIVEL.getId()) {
				arquivoDivisaoRepositorio.deletaArquivoTextoRoteiroEmpresaDivisao(arquivo.getId());
				arquivoRepositorio.deletaArquivoTextoRoteiroEmpresa(arquivo.getId());
			} else {
				retorno = false;
			}
		}

		return retorno;
	}
	
	public List<Imovel> imoveisParaGerarArquivoTextoFaturamento(Rota rota, int primeiroRegistro, int quantidadeRegistros){
		List<Imovel> imoveisConsulta = null;
		
		if (rota.alternativa()) {
			imoveisConsulta = imovelRepositorio.imoveisParaGerarArquivoTextoFaturamentoPorRotaAlternativa(rota.getId(), primeiroRegistro, quantidadeRegistros);
		}else{
			imoveisConsulta = imovelRepositorio.imoveisParaGerarArquivoTextoFaturamento(rota.getId(), primeiroRegistro, quantidadeRegistros);
		}

		List<Imovel> imoveis = new ArrayList<Imovel>();
		
		for (Imovel imovel : imoveisConsulta) {
			if (imovel.pertenceACondominio() || imovel.ehCondominio() || imovel.existeHidrometroAgua() || imovel.existeHidrometroPoco()){
				imoveis.add(imovel);
			}
		}
			
		return imoveis;
	}
	
	public List<Imovel> imoveisCondominioParaGerarArquivoTextoFaturamento(Rota rota, Integer idCondominio){
		List<Imovel> imoveisConsulta = null;
		
		if (rota.alternativa()) {
			imoveisConsulta = imovelRepositorio.imoveisCondominioParaGerarArquivoTextoFaturamentoPorRotaAlternativa(idCondominio);
		}else{
			imoveisConsulta = imovelRepositorio.imoveisCondominioParaGerarArquivoTextoFaturamento(idCondominio);
		}

		List<Imovel> imoveis = new ArrayList<Imovel>();
		
		for (Imovel imovel : imoveisConsulta) {
			if (imovel.pertenceACondominio() || imovel.ehCondominio() || imovel.existeHidrometroAgua() || imovel.existeHidrometroPoco()){
				imoveis.add(imovel);
			}
		}
			
		return imoveis;
	}
	/*
	public Object[] gerarArquivoTexto(Imovel imovel, Conta conta,
			Integer anoMesReferencia, Rota rota,
			FaturamentoGrupo faturamentoGrupo,
			SistemaParametro sistemaParametro, Date dataComando)
			throws ControladorException {

		StringBuilder arquivoTexto = new StringBuilder();

		int quantidadeLinhas = 0;

		Date dataEmissao = Util.subtrairNumeroDiasDeUmaData(dataComando, 10);

		CobrancaDocumento cobrancaDocumento = null;
		try {

			cobrancaDocumento = repositorioCobranca
					.pesquisarCobrancaDocumentoImpressaoSimultanea(dataEmissao,
							imovel.getId());

		} catch (ErroRepositorioException ex) {
			sessionContext.setRollbackOnly();
			throw new ControladorException("erro.sistema", ex);
		}

		Object[] retorno = new Object[2];

		 REGISTRO_TIPO_01
		arquivoTexto.append(this.gerarArquivoTextoRegistroTipo01(imovel, conta,
				anoMesReferencia, rota, faturamentoGrupo, sistemaParametro,
				cobrancaDocumento));
		quantidadeLinhas = quantidadeLinhas + 1;

		 REGISTRO_TIPO_02
		Object[] tipo2 = this.gerarArquivoTextoRegistroTipo02(imovel, conta,
				sistemaParametro);
		arquivoTexto.append(tipo2[0]);
		int quantidadeTipo2 = (Integer) tipo2[1];
		quantidadeLinhas = quantidadeLinhas + quantidadeTipo2;

		 REGISTRO_TIPO_03
		Object[] tipo3 = this.gerarArquivoTextoRegistroTipo03(imovel,
				anoMesReferencia);
		arquivoTexto.append(tipo3[0]);
		int quantidadeTipo3 = (Integer) tipo3[1];
		quantidadeLinhas = quantidadeLinhas + quantidadeTipo3;

		 REGISTRO_TIPO_04
		Object[] tipo4 = this.gerarArquivoTextoRegistroTipo04(conta);
		arquivoTexto.append(tipo4[0]);
		int quantidadeTipo4 = (Integer) tipo4[1];
		quantidadeLinhas = quantidadeLinhas + quantidadeTipo4;

		 REGISTRO_TIPO_05
		Object[] tipo5 = this.gerarArquivoTextoRegistroTipo05(conta);
		arquivoTexto.append(tipo5[0]);
		int quantidadeTipo5 = (Integer) tipo5[1];
		quantidadeLinhas = quantidadeLinhas + quantidadeTipo5;

		 REGISTRO_TIPO_06
		Object[] tipo6 = this.gerarArquivoTextoRegistroTipo06(conta);
		arquivoTexto.append(tipo6[0]);
		int quantidadeTipo6 = (Integer) tipo6[1];
		quantidadeLinhas = quantidadeLinhas + quantidadeTipo6;

		 REGISTRO_TIPO_07
		Object[] tipo7 = this.gerarArquivoTextoRegistroTipo07(imovel,
				sistemaParametro, cobrancaDocumento);
		arquivoTexto.append(tipo7[0]);
		int quantidadeTipo7 = (Integer) tipo7[1];
		quantidadeLinhas = quantidadeLinhas + quantidadeTipo7;

		 REGISTRO_TIPO_08
		Object[] tipo8 = this.gerarArquivoTextoRegistroTipo08(imovel,
				anoMesReferencia, sistemaParametro);
		arquivoTexto.append(tipo8[0]);
		int quantidadeTipo8 = (Integer) tipo8[1];
		quantidadeLinhas = quantidadeLinhas + quantidadeTipo8;

		 Parte dos dados das tarifas e faixas
		 REGISTRO_TIPO_09 e TIPO_10
		Object[] registroTipo9e10 = gerarArquivoTextoRegistroDadosTarifa09(
				imovel, sistemaParametro, anoMesReferencia, faturamentoGrupo);

		arquivoTexto.append(registroTipo9e10[0]);
		int quantidadeTipo9 = (Integer) registroTipo9e10[1];
		quantidadeLinhas = quantidadeLinhas + quantidadeTipo9;

		retorno[0] = arquivoTexto;
		retorno[1] = quantidadeLinhas;

		return retorno;
	}
	*/
	
	/*
	
	public StringBuilder gerarArquivoTextoRegistroTipo01(Imovel imovel,  Integer anoMesReferencia, Rota rota, FaturamentoGrupo faturamentoGrupo,
			SistemaParametro sistemaParametro, CobrancaDocumento cobrancaDocumento) throws ControladorException {

		StringBuilder arquivoTextoRegistroTipo01 = new StringBuilder();

		Cliente clienteUsuario = null;
		Cliente clienteResponsavel = null;

		Integer idGerenciaRegional = null;
		Set colecaoClienteImovelOUConta = null;
		String inscricaoImovel = null;
		String idLigacaoAguaSituacao = null;
		String idLigacaoEsgotoSituacao = null;
		String idImovelPerfil = null;
		Integer idLocalidade = null;
		Integer idSetorComercial = null;
		Integer idQuadraFace = null;
		short indicadorParalisacaoFaturamentoAgua = 2;
		short indicadorParalisacaoFaturamentoEsgoto = 2;

		idGerenciaRegional = imovel.getLocalidade().getGerenciaRegional().getId();
		idLocalidade = imovel.getLocalidade().getId();
		if (imovel.getSetorComercial() != null) {
			idSetorComercial = imovel.getSetorComercial().getId();
		}

		colecaoClienteImovelOUConta = imovel.getClienteImoveis();

		if (imovel.getQuadraFace() != null) {
			idQuadraFace = imovel.getQuadraFace().getId();
		}

		Integer consumoMinimoImovel = null;
		consumoMinimoImovel = getControladorMicromedicao().obterConsumoMinimoLigacao(imovel, null);

		if (consumoMinimoImovel != null) {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(6, "" + consumoMinimoImovel));
		} else {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(6, ""));

		}

		int consumoMinimoNaoMedido = getControladorMicromedicao().obterConsumoNaoMedido(imovel);

		arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(6, "" + consumoMinimoNaoMedido));

		if (cobrancaDocumento != null && !cobrancaDocumento.equals("")) {
			 Documento de CobranÁa
			arquivoTextoRegistroTipo01.append(Util.completaString(cobrancaDocumento.getId() + "", 9));

			String representacaoNumericaCodBarra = "";
			 ObtÈm a representaÁ„o numÈrica do
			 cÛdigode
			 barra
			representacaoNumericaCodBarra = this.getControladorArrecadacao().obterRepresentacaoNumericaCodigoBarra(5, cobrancaDocumento.getValorDocumento(),
					cobrancaDocumento.getLocalidade().getId(), cobrancaDocumento.getImovel().getId(), null, null, null, null,
					String.valueOf(cobrancaDocumento.getNumeroSequenciaDocumento()), cobrancaDocumento.getDocumentoTipo().getId(), null, null, null);

			 57. CÛdigo de Barras do Documento do CobranÁa
			arquivoTextoRegistroTipo01.append(representacaoNumericaCodBarra);

		} else {
			arquivoTextoRegistroTipo01.append(Util.completaString("", 57));
		}

		 CPF ou CNPJ do CLIENTE
		String cpfCnpj = "";
		if (clienteUsuario != null && !clienteUsuario.equals("")) {
			if (clienteUsuario.getCpf() != null && !clienteUsuario.getCpf().equals("")) {
				cpfCnpj = clienteUsuario.getCpf();
			} else {
				if (clienteUsuario.getCnpj() != null && !clienteUsuario.getCnpj().equals("")) {
					cpfCnpj = clienteUsuario.getCnpj();
				}
			}
		}
		arquivoTextoRegistroTipo01.append(Util.completaString(cpfCnpj, 18));

		 GERA AS COLUNAS DA SITUA«√O ESPECIAL DE FATURAMENTO
		arquivoTextoRegistroTipo01.append(gerarDadosSituacaoEspecialFaturamento(imovel, faturamentoGrupo));

		 DATA DE LEITURA ANTERIOR N√O MEDIDO

		 DATA LEITURA ANTERIOR E DATA LEITURA ATUAL
		Integer anoMesFaturamentoAnterior = Util.subtrairMesDoAnoMes(anoMesReferencia, 1);

		Date dataLeituraAnteriorFaturamento = null;

		dataLeituraAnteriorFaturamento = (Date) repositorioFaturamento.pesquisarFaturamentoAtividadeCronogramaDataPrevista(faturamentoGrupo.getId(),
				FaturamentoAtividade.EFETUAR_LEITURA, anoMesFaturamentoAnterior);

		if (dataLeituraAnteriorFaturamento == null || dataLeituraAnteriorFaturamento.equals("")) {
			dataLeituraAnteriorFaturamento = Util.subtrairNumeroDiasDeUmaData(new Date(), 30);
		}

		arquivoTextoRegistroTipo01.append(Util.formatarDataAAAAMMDD(dataLeituraAnteriorFaturamento));

		 INDICADOR ABASTECIMENTO
		if (imovel.getLigacaoAguaSituacao() != null && !imovel.getLigacaoAguaSituacao().equals("")) {
			arquivoTextoRegistroTipo01.append(Util.completaString(imovel.getLigacaoAguaSituacao().getIndicadorAbastecimento() + "", 1));
		} else {
			arquivoTextoRegistroTipo01.append(Util.completaString("", 1));
		}

		 verificar se o imÛvel È Sazonal
		boolean imovelSazonal = false;

		 [UC0108] - Obter Quantidade de Economias por Subcategoria
		Collection colecaoCategoriaOUSubcategoria = this.getControladorImovel().obterQuantidadeEconomiasSubCategoria(imovel.getId());

		Iterator itSubcategoria = colecaoCategoriaOUSubcategoria.iterator();

		while (itSubcategoria.hasNext()) {

			Subcategoria subcategoria = (Subcategoria) itSubcategoria.next();

			if (subcategoria.getIndicadorSazonalidade().equals(ConstantesSistema.SIM)) {

				imovelSazonal = true;
				break;
			}
		}

		if (imovelSazonal) {
			arquivoTextoRegistroTipo01.append("1");
		} else {
			arquivoTextoRegistroTipo01.append("2");
		}

		if (imovel.getFaturamentoSituacaoTipo() != null && !imovel.getFaturamentoSituacaoTipo().equals("")) {
			FiltroFaturamentoSituacaoHistorico filtroFaturamentoSituacaoHistorico = new FiltroFaturamentoSituacaoHistorico();
			filtroFaturamentoSituacaoHistorico.adicionarParametro(new ParametroSimples(FiltroFaturamentoSituacaoHistorico.ID_IMOVEL, imovel.getId()));
			filtroFaturamentoSituacaoHistorico.adicionarParametro(new ParametroNulo(FiltroFaturamentoSituacaoHistorico.ANO_MES_FATURAMENTO_RETIRADA));
			Collection<FaturamentoSituacaoHistorico> colFiltroFaturamentoSituacaoHistorico = this.getControladorUtil().pesquisar(
					filtroFaturamentoSituacaoHistorico, FaturamentoSituacaoHistorico.class.getName());

			FaturamentoSituacaoHistorico faturamentoSituacaoHistorico = (FaturamentoSituacaoHistorico) Util
					.retonarObjetoDeColecao(colFiltroFaturamentoSituacaoHistorico);

			if ((faturamentoSituacaoHistorico != null
					&& faturamentoGrupo.getAnoMesReferencia() >= faturamentoSituacaoHistorico.getAnoMesFaturamentoSituacaoInicio() && faturamentoGrupo
					.getAnoMesReferencia() <= faturamentoSituacaoHistorico.getAnoMesFaturamentoSituacaoFim())) {

				if (imovel.getFaturamentoSituacaoTipo().getIndicadorParalisacaoFaturamento() != null
						&& imovel.getFaturamentoSituacaoTipo().getIndicadorParalisacaoFaturamento().equals(ConstantesSistema.INDICADOR_USO_ATIVO)
						&& imovel.getFaturamentoSituacaoTipo().getIndicadorValidoAgua().equals(ConstantesSistema.INDICADOR_USO_ATIVO)) {

					indicadorParalisacaoFaturamentoAgua = 1;
				}

				if (imovel.getFaturamentoSituacaoTipo().getIndicadorParalisacaoFaturamento() != null
						&& imovel.getFaturamentoSituacaoTipo().getIndicadorParalisacaoFaturamento().equals(ConstantesSistema.INDICADOR_USO_ATIVO)
						&& imovel.getFaturamentoSituacaoTipo().getIndicadorValidoEsgoto().equals(ConstantesSistema.INDICADOR_USO_ATIVO)) {

					indicadorParalisacaoFaturamentoEsgoto = 1;
				}

			}
		}

		arquivoTextoRegistroTipo01.append("" + indicadorParalisacaoFaturamentoAgua);

		arquivoTextoRegistroTipo01.append("" + indicadorParalisacaoFaturamentoEsgoto);

		if (imovel.getCodigoDebitoAutomatico() != null && !imovel.getCodigoDebitoAutomatico().equals("")) {
			arquivoTextoRegistroTipo01.append(Util.completaString("" + imovel.getCodigoDebitoAutomatico(), 9));
		} else {
			arquivoTextoRegistroTipo01.append(Util.completaString("", 9));
		}

		if (imovel.getLigacaoEsgoto() != null && imovel.getLigacaoEsgoto().getPercentualAlternativo() != null) {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(6,
					Util.formatarBigDecimalComPonto(imovel.getLigacaoEsgoto().getPercentualAlternativo())));
		} else {
			arquivoTextoRegistroTipo01.append(Util.completaString("", 6));
		}

		if (imovel.getLigacaoEsgoto() != null && imovel.getLigacaoEsgoto().getNumeroConsumoPercentualAlternativo() != null) {
			arquivoTextoRegistroTipo01
					.append(Util.adicionarZerosEsquedaNumero(6, imovel.getLigacaoEsgoto().getNumeroConsumoPercentualAlternativo().toString()));
		} else {
			arquivoTextoRegistroTipo01.append(Util.completaString("", 6));
		}

		if (cobrancaDocumento != null) {
			arquivoTextoRegistroTipo01.append(Util.formatarDataAAAAMMDD(cobrancaDocumento.getEmissao()));
		} else {
			arquivoTextoRegistroTipo01.append(Util.completaString("", 8));
		}

		int[] consumoMedioLigacaoEsgoto = this.getControladorMicromedicao().obterVolumeMedioAguaEsgoto(imovel.getId(), faturamentoGrupo.getAnoMesReferencia(),
				LigacaoTipo.LIGACAO_ESGOTO, houveIntslacaoHidrometro);

		arquivoTextoRegistroTipo01.append(System.getProperty("line.separator"));

		return arquivoTextoRegistroTipo01;
	}		
	*/
}
