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
		Integer numeroSequencialRota = null;
		Integer idQuadraFace = null;
		Short indicadorFaturamentoSituacaoAgua = null;
		Short indicadorFaturamentoSituacaoEsgoto = null;
		short indicadorParalisacaoFaturamentoAgua = 2;
		short indicadorParalisacaoFaturamentoEsgoto = 2;

		idGerenciaRegional = imovel.getLocalidade().getGerenciaRegional().getId();
		idLocalidade = imovel.getLocalidade().getId();
		if (imovel.getSetorComercial() != null) {
			idSetorComercial = imovel.getSetorComercial().getId();
		}
		if (imovel.getNumeroSequencialRota() != null) {
			numeroSequencialRota = imovel.getNumeroSequencialRota();
		}
		colecaoClienteImovelOUConta = imovel.getClienteImoveis();
		indicadorFaturamentoSituacaoAgua = imovel.getLigacaoAguaSituacao().getIndicadorFaturamentoSituacao();
		indicadorFaturamentoSituacaoEsgoto = imovel.getLigacaoEsgotoSituacao().getIndicadorFaturamentoSituacao();

		if (imovel.getQuadraFace() != null) {
			idQuadraFace = imovel.getQuadraFace().getId();
		}

		int[] consumoMedioLigacaoAgua = this.getControladorMicromedicao().obterVolumeMedioAguaEsgoto(imovel.getId(), faturamentoGrupo.getAnoMesReferencia(),
				LigacaoTipo.LIGACAO_AGUA, houveIntslacaoHidrometro);

		arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(6, String.valueOf(consumoMedioLigacaoAgua[0])));

		arquivoTextoRegistroTipo01.append(indicadorFaturamentoSituacaoAgua.toString());

		arquivoTextoRegistroTipo01.append(indicadorFaturamentoSituacaoEsgoto.toString());

		Short indicadorEmissaoConta = new Short("1");

		boolean naoEmitir = false;
		if (sistemaParametro.getCodigoEmpresaFebraban().equals(SistemaParametro.CODIGO_EMPRESA_FEBRABAN_CAERN)
				|| sistemaParametro.getCodigoEmpresaFebraban().equals(SistemaParametro.CODIGO_EMPRESA_FEBRABAN_COSANPA)) {
			if (imovel.getImovelContaEnvio() != null && (imovel.getImovelContaEnvio().getId().equals(ImovelContaEnvio.ENVIAR_CLIENTE_RESPONSAVEL_FINAL_GRUPO))) {
				naoEmitir = true;
			}
		} else {
			if (imovel.getImovelContaEnvio() != null
					&& (imovel.getImovelContaEnvio().getId().equals(ImovelContaEnvio.ENVIAR_CLIENTE_RESPONSAVEL)
							|| imovel.getImovelContaEnvio().getId().equals(ImovelContaEnvio.NAO_PAGAVEL_IMOVEL_PAGAVEL_RESPONSAVEL)
							|| imovel.getImovelContaEnvio().getId().equals(ImovelContaEnvio.ENVIAR_CONTA_BRAILLE) || imovel.getImovelContaEnvio().getId()
							.equals(ImovelContaEnvio.ENVIAR_CONTA_BRAILLE_RESPONSAVEL))) {
				naoEmitir = true;
			}
		}

		if (clienteResponsavel != null && naoEmitir) {
			indicadorEmissaoConta = new Short("2");
		}

		arquivoTextoRegistroTipo01.append(indicadorEmissaoConta.toString());

		 CONSUMO_MINIMO_AGUA
		if (imovel.getLigacaoAgua() != null && imovel.getLigacaoAgua().getNumeroConsumoMinimoAgua() != null
				&& !imovel.getLigacaoAgua().getNumeroConsumoMinimoAgua().equals("")) {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(6, imovel.getLigacaoAgua().getNumeroConsumoMinimoAgua().toString()));
		} else {
			arquivoTextoRegistroTipo01.append(Util.completaString("", 6));
		}

		 CONSUMO_MINIMO_ESGOTO
		if (imovel.getLigacaoEsgoto() != null && imovel.getLigacaoEsgoto().getConsumoMinimo() != null) {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(6, imovel.getLigacaoEsgoto().getConsumoMinimo().toString()));
		} else {
			arquivoTextoRegistroTipo01.append(Util.completaString("", 6));
		}

		 PERCENTUAL_ESGOTO_LIGACAO
		if (imovel.getLigacaoEsgoto() != null && imovel.getLigacaoEsgoto().getPercentualAguaConsumidaColetada() != null) {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(6,
					Util.formatarBigDecimalComPonto(imovel.getLigacaoEsgoto().getPercentualAguaConsumidaColetada())));
		} else {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(6, "0"));
		}

		BigDecimal percentualEsgoto = this.getControladorFaturamento().verificarPercentualEsgotoAlternativo(imovel, null);

		 if (conta != null){
		if (percentualEsgoto != null) {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(6,
			 Util.formatarBigDecimalComPonto(conta.getPercentualEsgoto())));
					Util.formatarBigDecimalComPonto(percentualEsgoto)));
		} else {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(6, "0"));
		}

		 TIPO_PO«O
		if (imovel.getPocoTipo() != null) {
			arquivoTextoRegistroTipo01.append(imovel.getPocoTipo().getId().toString());
		} else {
			arquivoTextoRegistroTipo01.append(Util.completaString("", 1));
		}

		 CONSUMO_TARIFA
		arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(2, imovel.getConsumoTarifa().getId().toString()));

		 CATEGORIA PRINCIPAL
		Collection colecaoCategoria = null;

		 ObtÈm a quantidade de economias por categoria
		colecaoCategoria = getControladorImovel().obterQuantidadeEconomiasCategoria(imovel);

		int consumoTotalReferenciaAltoConsumo = 0;
		int consumoTotalReferenciaEstouroConsumo = 0;
		int consumoTotalReferenciaBaixoConsumo = 0;
		int consumoMaximoCobrancaEstouroConsumo = 0;
		int maiorQuantidadeEconomia = 0;
		BigDecimal vezesMediaAltoConsumo = new BigDecimal(0);
		BigDecimal vezesMediaEstouroConsumo = new BigDecimal(0);
		BigDecimal percentualDeterminacaoBaixoConsumo = new BigDecimal(0);

		Iterator colecaoCategoriaIterator = colecaoCategoria.iterator();

		while (colecaoCategoriaIterator.hasNext()) {

			Categoria categoria = (Categoria) colecaoCategoriaIterator.next();

			 Multiplica o consumo por economia de referencia (consumo estouro)
			 pr n˙mero de economias do imÛvel
			consumoTotalReferenciaAltoConsumo = consumoTotalReferenciaAltoConsumo
					+ (categoria.getConsumoAlto().intValue() * categoria.getQuantidadeEconomiasCategoria().intValue());

			 Multiplica o consumo por economia de referencia (consumo estouro)
			 pr n˙mero de economias do imÛvel
			consumoTotalReferenciaEstouroConsumo = consumoTotalReferenciaEstouroConsumo
					+ (categoria.getConsumoEstouro().intValue() * categoria.getQuantidadeEconomiasCategoria().intValue());

			 Multiplica o consumo por economia de referencia (consumo estouro)
			 pr n˙mero de economias do imÛvel
			consumoMaximoCobrancaEstouroConsumo = consumoMaximoCobrancaEstouroConsumo
					+ (categoria.getNumeroConsumoMaximoEc().intValue() * categoria.getQuantidadeEconomiasCategoria().intValue());

			consumoTotalReferenciaBaixoConsumo = consumoTotalReferenciaBaixoConsumo
					+ (categoria.getMediaBaixoConsumo().intValue() * categoria.getQuantidadeEconomiasCategoria().intValue());

			 ObtÈm a maior quantidade de economias e a vezes mÈdia de estouro
			if (maiorQuantidadeEconomia < categoria.getQuantidadeEconomiasCategoria().intValue()) {

				maiorQuantidadeEconomia = categoria.getQuantidadeEconomiasCategoria().intValue();

				vezesMediaAltoConsumo = categoria.getVezesMediaAltoConsumo();
				vezesMediaEstouroConsumo = categoria.getVezesMediaEstouro();
				percentualDeterminacaoBaixoConsumo = categoria.getPorcentagemMediaBaixoConsumo();
			}
		}

		 CONSUMO_REFERENCIA_ESTOURO_CONSUMO
		if (consumoTotalReferenciaEstouroConsumo <= 999999) {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(6, consumoTotalReferenciaEstouroConsumo + ""));
		} else {
			arquivoTextoRegistroTipo01.append("999999");
		}

		 CONSUMO_REFERENCIA_ALTO_CONSUMO
		if (consumoTotalReferenciaAltoConsumo <= 999999) {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(6, consumoTotalReferenciaAltoConsumo + ""));
		} else {
			arquivoTextoRegistroTipo01.append("999999");
		}

		 CONSUMO_MEDIA_BAIXO_CONSUMO
		if (consumoTotalReferenciaBaixoConsumo <= 999999) {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(6, consumoTotalReferenciaBaixoConsumo + ""));
		} else {
			arquivoTextoRegistroTipo01.append("999999");
		}

		 FATOR_MULTIPLICACAO_MEDIA_ESTOURO_CONSUMO
		if (vezesMediaEstouroConsumo != null) {
			arquivoTextoRegistroTipo01.append(Util.completaString(vezesMediaEstouroConsumo.toString(), 4));
		} else {
			arquivoTextoRegistroTipo01.append(Util.completaString("", 4));
		}

		 FATOR_MULTIPLICACAO_MEDIA_ALTO_CONSUMO
		if (vezesMediaAltoConsumo != null) {
			arquivoTextoRegistroTipo01.append(Util.completaString(vezesMediaAltoConsumo.toString(), 4));
		} else {
			arquivoTextoRegistroTipo01.append(Util.completaString("", 4));
		}

		 PERCENTUAL_DETERMINACAO_BAIXO_CONSUMO
		if (percentualDeterminacaoBaixoConsumo != null) {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(6, Util.formatarBigDecimalComPonto(percentualDeterminacaoBaixoConsumo)));
		} else {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(6, "0"));
		}

		 CONSUMO_MAXIMO_COBRANCA_ESTOURO_CONSUMO
		if (consumoMaximoCobrancaEstouroConsumo <= 999999) {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(6, consumoMaximoCobrancaEstouroConsumo + ""));
		} else {
			arquivoTextoRegistroTipo01.append("999999");
		}

		 FATURAMENTO_GRUPO
		arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(3, faturamentoGrupo.getId().toString()));

		 CODIGO_ROTA
		arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(7, rota.getCodigo().toString()));

		arquivoTextoRegistroTipo01.append(Util.completaString("", 9));

		 N⁄MERO DA CONTA

		 Tipo da calculo da tarifa
		if (imovel.getConsumoTarifa() != null && imovel.getConsumoTarifa().getTarifaTipoCalculo() != null) {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(2, "" + imovel.getConsumoTarifa().getTarifaTipoCalculo().getId()));
		}

		 EndereÁo Atendimento 1™ Parte
		FiltroLocalidade filtroLocalidade = new FiltroLocalidade();

		filtroLocalidade.adicionarParametro(new ParametroSimples(FiltroLocalidade.ID, idLocalidade));

		filtroLocalidade.adicionarCaminhoParaCarregamentoEntidade("logradouroCep");
		filtroLocalidade.adicionarCaminhoParaCarregamentoEntidade("logradouroCep.cep");
		filtroLocalidade.adicionarCaminhoParaCarregamentoEntidade("logradouroCep.logradouro");
		filtroLocalidade.adicionarCaminhoParaCarregamentoEntidade("logradouroCep.logradouro.logradouroTipo");
		filtroLocalidade.adicionarCaminhoParaCarregamentoEntidade("logradouroCep.logradouro.logradouroTitulo");
		filtroLocalidade.adicionarCaminhoParaCarregamentoEntidade("enderecoReferencia");
		filtroLocalidade.adicionarCaminhoParaCarregamentoEntidade("logradouroBairro");
		filtroLocalidade.adicionarCaminhoParaCarregamentoEntidade("logradouroBairro.bairro");
		filtroLocalidade.adicionarCaminhoParaCarregamentoEntidade("logradouroBairro.bairro.municipio");
		filtroLocalidade.adicionarCaminhoParaCarregamentoEntidade("logradouroBairro.bairro.municipio.unidadeFederacao");
		filtroLocalidade.adicionarCaminhoParaCarregamentoEntidade("enderecoReferencia");

		Collection cLocalidade = (Collection) getControladorUtil().pesquisar(filtroLocalidade, Localidade.class.getName());

		Localidade localidade = (Localidade) cLocalidade.iterator().next();

		String descricaoAtendimento = localidade.getEnderecoFormatadoTituloAbreviado();

		arquivoTextoRegistroTipo01.append(Util.completaString(descricaoAtendimento, 70));

		 EndereÁo Atendimento 2™ Parte
		String dddMunicipio = "";
		if (localidade.getLogradouroBairro() != null && localidade.getLogradouroBairro().getBairro() != null
				&& localidade.getLogradouroBairro().getBairro().getMunicipio() != null
				&& localidade.getLogradouroBairro().getBairro().getMunicipio().getDdd() != null) {
			dddMunicipio = "" + localidade.getLogradouroBairro().getBairro().getMunicipio().getDdd();
		}

		String fome = "";
		if (localidade.getFone() != null) {
			fome = localidade.getFone();
		}

		arquivoTextoRegistroTipo01.append(Util.completaString(dddMunicipio + fome, 11));

		 Sequencial de rota
		if (numeroSequencialRota != null) {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(9, "" + numeroSequencialRota));
		} else {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(9, ""));
		}

		 Mensagem da conta em 3 partes
		EmitirContaHelper emitirContaHelper = new EmitirContaHelper();
		emitirContaHelper.setAmReferencia(faturamentoGrupo.getAnoMesReferencia());
		emitirContaHelper.setAnoMesFaturamentoGrupo(faturamentoGrupo.getAnoMesReferencia());
		emitirContaHelper.setIdFaturamentoGrupo(faturamentoGrupo.getId());
		emitirContaHelper.setIdGerenciaRegional(idGerenciaRegional);
		emitirContaHelper.setIdLocalidade(idLocalidade);
		emitirContaHelper.setIdSetorComercial(idSetorComercial);
		emitirContaHelper.setIdImovel(imovel.getId());
		 Caso a empresa seja a CAER
		if (sistemaParametro.getCodigoEmpresaFebraban().equals(SistemaParametro.CODIGO_EMPRESA_FEBRABAN_CAER)) {
			String[] mensagemContaDividida = getControladorFaturamento().obterMensagemConta(emitirContaHelper, sistemaParametro, 4, null);
			if (mensagemContaDividida != null) {
				 Parte 1
				arquivoTextoRegistroTipo01.append(Util.completaString(mensagemContaDividida[0], 60));
				 Parte 2
				arquivoTextoRegistroTipo01.append(Util.completaString(mensagemContaDividida[1], 60));
				 Parte 3
				arquivoTextoRegistroTipo01.append(Util.completaString(mensagemContaDividida[2], 60));
				 Parte 4
				arquivoTextoRegistroTipo01.append(Util.completaString(mensagemContaDividida[3], 60));
				 Parte 5
				arquivoTextoRegistroTipo01.append(Util.completaString(mensagemContaDividida[4], 60));

			} else {
				arquivoTextoRegistroTipo01.append(Util.completaString("", 300));
			}
		} else {
			String[] mensagemContaDividida = getControladorFaturamento().obterMensagemConta3Partes(emitirContaHelper, sistemaParametro);
			if (mensagemContaDividida != null) {
				 Parte 1
				arquivoTextoRegistroTipo01.append(Util.completaString(mensagemContaDividida[0], 100));
				 Parte 2
				arquivoTextoRegistroTipo01.append(Util.completaString(mensagemContaDividida[1], 100));
				 Parte 3
				arquivoTextoRegistroTipo01.append(Util.completaString(mensagemContaDividida[2], 100));

			} else {
				arquivoTextoRegistroTipo01.append(Util.completaString("", 300));
			}
		}

		 Incluir mensagem de quitaÁ„o anual de dÈbitos
		String msgQuitacaoDebitos = getControladorFaturamento().obterMsgQuitacaoDebitos(imovel, anoMesReferencia);

		arquivoTextoRegistroTipo01.append(Util.completaString(msgQuitacaoDebitos, 120));

		 Qualidade ¡gua
		Integer anoMesReferenciaQualidadeAgua = null;

		if (sistemaParametro.getNomeEmpresa() != null && sistemaParametro.getNomeEmpresa().equals(SistemaParametro.EMPRESA_COMPESA)) {
			anoMesReferenciaQualidadeAgua = Util.subtraiAteSeisMesesAnoMesReferencia(faturamentoGrupo.getAnoMesReferencia(), 1);
		} else {
			anoMesReferenciaQualidadeAgua = faturamentoGrupo.getAnoMesReferencia();
		}

		arquivoTextoRegistroTipo01 = arquivoTextoRegistroTipo01.append(gerarArquivoTextoQualidadeAgua(idLocalidade, idSetorComercial,
				anoMesReferenciaQualidadeAgua, idQuadraFace));

		Integer consumoMinimoImovel = null;
		consumoMinimoImovel = getControladorMicromedicao().obterConsumoMinimoLigacao(imovel, null);

		 CONSUMO MINIMO IM”VEL
		if (consumoMinimoImovel != null) {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(6, "" + consumoMinimoImovel));
		} else {
			arquivoTextoRegistroTipo01.append(Util.adicionarZerosEsquedaNumero(6, ""));

		}

		 CONSUMO MINIMO IM”VEL N√O MEDIDO
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
