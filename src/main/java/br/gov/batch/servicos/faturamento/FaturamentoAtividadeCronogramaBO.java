package br.gov.batch.servicos.faturamento;

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.FaturamentoAtividade;
import br.gov.model.faturamento.FaturamentoAtividadeCronograma;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.util.Utilitarios;
import br.gov.servicos.faturamento.FaturamentoAtividadeCronogramaRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;

@Stateless
public class FaturamentoAtividadeCronogramaBO {

	@EJB
	private FaturamentoAtividadeCronogramaRepositorio repositorio;
	
	@EJB
	private MedicaoHistoricoRepositorio medicaoHistoricoRepositorio;
	
	
	public Date obterDataPrevistaDoCronogramaAnterior(FaturamentoGrupo faturamentoGrupo, Integer idAtividade) {
		Integer referenciaAnterior = Utilitarios.reduzirMeses(faturamentoGrupo.getAnoMesReferencia(), 1);

		FaturamentoAtividadeCronograma cronograma = repositorio.buscarPorGrupoEAtividadeEReferencia(faturamentoGrupo.getId(), idAtividade, referenciaAnterior);
		Date dataLeitura = cronograma.getDataPrevista();

		if (dataLeitura == null) {
			dataLeitura = Utilitarios.reduzirDias(new Date(), 30);
		}
		
		return dataLeitura;
	}
	
	public Date obterDataLeituraAnteriorCronograma(Imovel imovel, FaturamentoGrupo grupo) {
		Integer anoMesReferenciaAnterior = Utilitarios.reduzirMeses(grupo.getAnoMesReferencia(), 1);
		
		Date dataLeitura = medicaoHistoricoRepositorio.buscarPorLigacaoAguaOuPoco(imovel.getId(), anoMesReferenciaAnterior).getDataLeituraAtualFaturamento();

		if (dataLeitura == null || dataLeitura.equals("")) {
			
			FaturamentoAtividadeCronograma cronograma = repositorio.buscarPorGrupoEAtividadeEReferencia(
					grupo.getId(), 
					FaturamentoAtividade.GERAR_ARQUIVO_LEITURA, 
					anoMesReferenciaAnterior);
			
			if (cronograma != null) {
				dataLeitura = cronograma.getDataPrevista();
			} else {
				cronograma = repositorio.buscarPorGrupoEAtividadeEReferencia(
						grupo.getId(), 
						FaturamentoAtividade.GERAR_ARQUIVO_LEITURA, 
						grupo.getAnoMesReferencia());
				
				dataLeitura = Utilitarios.reduzirDias(cronograma.getDataPrevista(), 30);
			}
		}
		return dataLeitura;
	}
	
}
