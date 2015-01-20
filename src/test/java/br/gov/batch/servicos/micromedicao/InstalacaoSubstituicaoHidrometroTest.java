package br.gov.batch.servicos.micromedicao;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.batch.servicos.cadastro.ImovelBO;
import br.gov.model.faturamento.FaturamentoGrupo;
import br.gov.model.micromedicao.MedicaoHistorico;
import br.gov.servicos.micromedicao.HidrometroInstalacaoHistoricoRepositorio;
import br.gov.servicos.micromedicao.MedicaoHistoricoRepositorio;
import br.gov.servicos.to.HidrometroTO;

@RunWith(EasyMockRunner.class)
public class InstalacaoSubstituicaoHidrometroTest {
    @TestSubject
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
    
    List<HidrometroTO> dadosHidrometro;
    
    @Before
    public void init(){
        hidrometroBO = new HidrometroBO();
        
        faturamentoGrupo = new FaturamentoGrupo();
        faturamentoGrupo.setAnoMesReferencia(201412);
        
        medicaoHistoricoAtual = new MedicaoHistorico();
        
        dadosHidrometro = new ArrayList<HidrometroTO>();
        HidrometroTO hidrometroTO = new HidrometroTO();
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2014);
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        hidrometroTO.setDataInstalacao(cal.getTime());
        dadosHidrometro.add(hidrometroTO);
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
        expect(imovelBO.pesquisarFaturamentoGrupo(1)).andReturn(faturamentoGrupo);
        replay(imovelBO);
        
        expect(medicaoHistoricoRepositorio.buscarPorLigacaoAgua(1, 201412)).andReturn(medicaoHistoricoAtual);
        replay(medicaoHistoricoRepositorio);
        
        expect(hidrometroInstalacaoHistoricoRepositorio.dadosInstalacaoHidrometro(1)).andReturn(dadosHidrometro);
        replay(hidrometroInstalacaoHistoricoRepositorio);
    }

    private void mocksSemMedicao() {
        expect(imovelBO.pesquisarFaturamentoGrupo(1)).andReturn(faturamentoGrupo);
        replay(imovelBO);
        
        expect(medicaoHistoricoRepositorio.buscarPorLigacaoAgua(1, 201412)).andReturn(null);
        replay(medicaoHistoricoRepositorio);
        
        expect(hidrometroInstalacaoHistoricoRepositorio.dadosInstalacaoHidrometro(1)).andReturn(dadosHidrometro).times(2);
        replay(hidrometroInstalacaoHistoricoRepositorio);
        
        expect(medicaoHistoricoBO.getMedicaoHistorico(1, 201411)).andReturn(null);
        replay(medicaoHistoricoBO);
    }
}
