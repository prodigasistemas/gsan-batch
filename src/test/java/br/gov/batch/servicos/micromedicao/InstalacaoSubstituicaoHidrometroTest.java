package br.gov.batch.servicos.micromedicao;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.batch.servicos.cadastro.ImovelBO;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.servicos.micromedicao.HidrometroInstalacaoHistoricoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.to.HidrometroTO;

public class InstalacaoSubstituicaoHidrometroTest {
    @InjectMocks
    private HidrometroBO hidrometroBO;
    
    @Mock
    private ImovelBO imovelBO;
    
    @Mock
    private MedicaoHistoricoRepositorio medicaoHistoricoRepositorio;
    
    @Mock
    private HidrometroInstalacaoHistoricoRepositorio hidrometroInstalacaoHistoricoRepositorio;
    
    @Mock
    private MedicaoHistoricoBO medicaoHistoricoBO;
    
    
    FaturamentoGrupo faturamentoGrupo;
    
    MedicaoHistorico medicaoHistoricoAtual;
    
    HidrometroTO hidrometroTO;
    
    @Before
    public void init(){
        hidrometroBO = new HidrometroBO();
        
        faturamentoGrupo = new FaturamentoGrupo();
        faturamentoGrupo.setAnoMesReferencia(201412);
        
        medicaoHistoricoAtual = new MedicaoHistorico();
        
        hidrometroTO = new HidrometroTO();
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2014);
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        hidrometroTO.setDataInstalacao(cal.getTime());
        
        MockitoAnnotations.initMocks(this);
    }

    
    @Test
    public void houveSubstituicaoPorqueDataUltimaMedicaoEhAnteriorADeInstalacao(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2014);
        cal.set(Calendar.MONTH, 10);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        medicaoHistoricoAtual.setDataLeituraAnteriorFaturamento(cal.getTime());
        
        mocksComMedicao();
        
        assertTrue(hidrometroBO.houveInstalacaoOuSubstituicao(1));
    }

    @Test
    public void houveInstalacao(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2014);
        cal.set(Calendar.MONTH, 10);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        medicaoHistoricoAtual.setDataLeituraAnteriorFaturamento(cal.getTime());
        
        mocksSemMedicao();
        
        assertTrue(hidrometroBO.houveInstalacaoOuSubstituicao(1));
    }

    
    private void mocksComMedicao() {
        when(imovelBO.pesquisarFaturamentoGrupo(1)).thenReturn(faturamentoGrupo);
        
        when(medicaoHistoricoRepositorio.buscarPorLigacaoAgua(1, 201412)).thenReturn(medicaoHistoricoAtual);
        
        when(hidrometroInstalacaoHistoricoRepositorio.dadosInstalacaoHidrometroAgua(1)).thenReturn(hidrometroTO);
    }

    private void mocksSemMedicao() {
        when(imovelBO.pesquisarFaturamentoGrupo(1)).thenReturn(faturamentoGrupo);
        
        when(medicaoHistoricoRepositorio.buscarPorLigacaoAgua(1, 201412)).thenReturn(null);
        
        when(hidrometroInstalacaoHistoricoRepositorio.dadosInstalacaoHidrometroAgua(1)).thenReturn(hidrometroTO);
        
        when(medicaoHistoricoBO.getMedicaoHistorico(1, 201411)).thenReturn(null);
    }
}
