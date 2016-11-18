package br.gov.batch.servicos.faturamento;

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.jboss.logging.Logger;

import br.gov.batch.servicos.cadastro.ImovelBO;
import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.FaturamentoAtividade;
import br.gov.model.faturamento.FaturamentoAtividadeCronograma;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.model.micromedicao.Rota;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.faturamento.FaturamentoAtividadeCronogramaRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;

@Stateless
public class FaturamentoAtividadeCronogramaBO {
	
	private static Logger logger = Logger.getLogger(FaturamentoAtividadeCronogramaBO.class);

	@EJB
	private FaturamentoAtividadeCronogramaRepositorio repositorio;
	
	@EJB
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorio;
	
	@EJB
	private ImovelBO imovelBO;
	
	
	public Date obterDataPrevistaDoCronogramaAnterior(FaturamentoGrupo faturamentoGrupo, Integer idAtividade) {
		Integer referenciaAnterior = Utilitarios.reduzirMeses(faturamentoGrupo.getAnoMesReferencia(), 1);

		FaturamentoAtividadeCronograma cronograma = repositorio.buscarPorGrupoEAtividadeEReferencia(faturamentoGrupo.getId(), idAtividade, referenciaAnterior);
		Date dataLeitura = cronograma.getDataPrevista();

		if (dataLeitura == null) {
			dataLeitura = Utilitarios.reduzirDias(new Date(), 30);
		}
		
		return dataLeitura;
	}
		
	public Date obterDataLeituraPorReferencia(Imovel imovel, Integer referencia) {
		try {
			FaturamentoGrupo grupo = imovelBO.pesquisarFaturamentoGrupoPelaReferencia(imovel.getId());
			
			return obterDataLeitura(imovel, grupo, referencia);
		} catch (Exception e) {
			logger.error(String.format("Erro ao obter data de leitura para o imovel [%s] na referencia [%s]",imovel.getId(), referencia), e);
			throw new RuntimeException(e);
		}
	}
	
	public Date obterDataLeituraAnterior(Imovel imovel, FaturamentoGrupo grupo) {
		Integer anoMesReferencia = Utilitarios.reduzirMeses(grupo.getAnoMesReferencia(), 1);
		
		return obterDataLeitura(imovel, grupo, anoMesReferencia);
	}
	
	public Date obterDataLeituraAtual(Imovel imovel, FaturamentoGrupo grupo) {
		Integer anoMesReferencia = grupo.getAnoMesReferencia();
		
		return obterDataLeitura(imovel, grupo, anoMesReferencia);
	}
	
	public Date obterDataLeitura(Imovel imovel, FaturamentoGrupo grupo, Integer anoMesReferencia) {
		MedicaoHistorico medicao = medicaoHistoricoRepositorio.buscarPorLigacaoAguaOuPoco(imovel.getId(), anoMesReferencia);
		
		Date dataLeitura = medicao != null ? medicao.getDataLeituraAtualFaturamento() : null;

		if (dataLeitura == null || dataLeitura.equals("")) {
			dataLeitura = obterDataLeituraPeloCronograma(grupo, anoMesReferencia);
		}
		
		return dataLeitura;
	}

	public Date obterDataLeituraPeloCronograma(FaturamentoGrupo grupo, Integer anoMesReferencia) {
		Date dataLeitura;
		FaturamentoAtividadeCronograma cronograma = repositorio.buscarPorGrupoEAtividadeEReferencia(
				grupo.getId(), 
				FaturamentoAtividade.GERAR_ARQUIVO_LEITURA, 
				anoMesReferencia);
		
		if (cronograma != null) {
			dataLeitura = cronograma.getDataPrevista();
		} else {
			cronograma = repositorio.buscarPorGrupoEAtividadeEReferencia(
					grupo.getId(), 
					FaturamentoAtividade.GERAR_ARQUIVO_LEITURA, 
					grupo.getAnoMesReferencia());
			
			dataLeitura = Utilitarios.reduzirDias(cronograma.getDataPrevista(), 30);
		}
		return dataLeitura;
	}
	
	public long obterDiferencaDiasCronogramas(Rota rota, Integer idFaturamentoAtividade) {

		Date dataCronogramaMesAtual = repositorio.pesquisarFaturamentoAtividadeCronogramaDataPrevista(
				rota.getFaturamentoGrupo().getId(),idFaturamentoAtividade, rota.getFaturamentoGrupo().getAnoMesReferencia());
		
		Date dataCronogramaMesAnterior = repositorio.pesquisarFaturamentoAtividadeCronogramaDataPrevista(
				rota.getFaturamentoGrupo().getId(),idFaturamentoAtividade, 
				Utilitarios.reduzirMeses(rota.getFaturamentoGrupo().getAnoMesReferencia(), 1));
		
		if (dataCronogramaMesAnterior != null) {
			return Utilitarios.obterDiferencaEntreDatas(dataCronogramaMesAnterior, dataCronogramaMesAtual);
		} else {
			return rota.getNumeroDiasConsumoAjuste();
		}
	}
}
