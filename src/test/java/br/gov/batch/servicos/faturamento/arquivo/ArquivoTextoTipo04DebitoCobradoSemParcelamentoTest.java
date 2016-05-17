package br.gov.batch.servicos.faturamento.arquivo;

import static br.gov.model.util.Utilitarios.quebraLinha;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.gov.model.cadastro.Imovel;
import br.gov.model.faturamento.Conta;
import br.gov.model.faturamento.DebitoTipo;
import br.gov.servicos.faturamento.DebitoCobradoRepositorio;
import br.gov.servicos.to.DebitoCobradoNaoParceladoTO;

public class ArquivoTextoTipo04DebitoCobradoSemParcelamentoTest {

	@InjectMocks
	private ArquivoTextoTipo04 arquivo;
	
	@Mock
	private DebitoCobradoRepositorio debitoCobradoRepositorioMock;

	Conta conta = null;
	
	DebitoTipo multaPorImpontualidade = new DebitoTipo(80);
	DebitoTipo instalacaoRamalAgua    = new DebitoTipo(90);
	DebitoTipo jurosMora              = new DebitoTipo(91);
	DebitoTipo atualizacaoMonetaria   = new DebitoTipo(94);
	
	@Before
	public void setup() {
	    multaPorImpontualidade.setDescricao("MULTA POR IMPONTUALIDADE");
	    jurosMora.setDescricao("JUROS DE MORA");
	    atualizacaoMonetaria.setDescricao("ATUALIZACAO MONETARIA");
	    instalacaoRamalAgua.setDescricao("INSTALACAO DE RAMAL DE AGUA");
		
	    
        Imovel imovel = new Imovel(3516458);
        conta = new Conta(10);
        conta.setImovel(imovel);
	    
		arquivo = new ArquivoTextoTipo04();
		
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void tresTiposDeDebitoComAteCincoMeses(){
	    carregarMocks(colecaoDebitosEmTresTiposComAteCincoMeses());
	    
	    StringBuilder texto = new StringBuilder();
	    texto.append("04003516458MULTA POR IMPONTUALIDADE 09/2014 10/2014 11/2014                                          00000000001.36      ")
	    .append(quebraLinha)
	    .append("04003516458JUROS DE MORA 09/2014 10/2014 11/2014                                                     00000000000.99      ")
	    .append(quebraLinha)
	    .append("04003516458ATUALIZACAO MONETARIA 09/2014                                                             00000000000.22      ")
	    .append(quebraLinha);
	    
	    StringBuilder retorno = arquivo.buildDebitoCobradoSemParcelamento(conta);
	    
	    assertEquals(texto.toString(), retorno.toString());
	}

	@Test
	public void tresTiposDeDebitoComMaisDeCincoMeses(){
	    carregarMocks(colecaoDebitosComMaisDeCincoMeses());
	    
	    StringBuilder texto = new StringBuilder();
	    texto.append("04003516458MULTA POR IMPONTUALIDADE 09/2014 10/2014 11/2014 12/2014 01/2015 E OUTRAS                 00000000002.15      ")
	    .append(quebraLinha);
	    
	    StringBuilder retorno = arquivo.buildDebitoCobradoSemParcelamento(conta);
	    
	    assertEquals(texto.toString(), retorno.toString());
	}
	
	
	@Test
	public void debitosContendoServicoParcelado(){
	    carregarMocks(colecaoDebitosContendoUmParcelamento());
	    
	    StringBuilder texto = new StringBuilder();
	    texto
	    .append("04003516458MULTA POR IMPONTUALIDADE 10/2014                                                          00000000001.57      ")
	    .append(quebraLinha)
	    .append("04003516458INSTALACAO DE RAMAL DE AGUA PARCELA 005/005                                               00000000039.03      ")
	    .append(quebraLinha)
	    .append("04003516458JUROS DE MORA 10/2014                                                                     00000000000.84      ")
	    .append(quebraLinha);

	    StringBuilder retorno = arquivo.buildDebitoCobradoSemParcelamento(conta);
	    
	    assertEquals(texto.toString(), retorno.toString());
	}
	
    private List<DebitoCobradoNaoParceladoTO> colecaoDebitosEmTresTiposComAteCincoMeses(){
        List<DebitoCobradoNaoParceladoTO> debitos = new ArrayList<DebitoCobradoNaoParceladoTO>();
        
        DebitoCobradoNaoParceladoTO to = new DebitoCobradoNaoParceladoTO();
        to.setAnoMesReferencia(201409);
        to.setNumeroPrestacaoDebito(Short.valueOf("1"));
        to.setTotalPrestacao(Short.valueOf("1"));
        to.setValorPrestacao(new BigDecimal(0.46));
        to.setDebitoTipo(multaPorImpontualidade.getId());
        to.setDescricaoTipoDebito(multaPorImpontualidade.getDescricao());
        debitos.add(to);
        
        to = new DebitoCobradoNaoParceladoTO();
        to.setAnoMesReferencia(201410);
        to.setNumeroPrestacaoDebito(Short.valueOf("1"));
        to.setTotalPrestacao(Short.valueOf("1"));
        to.setValorPrestacao(new BigDecimal(0.45));
        to.setDebitoTipo(multaPorImpontualidade.getId());
        to.setDescricaoTipoDebito(multaPorImpontualidade.getDescricao());
        debitos.add(to);

        to = new DebitoCobradoNaoParceladoTO();
        to.setAnoMesReferencia(201411);
        to.setNumeroPrestacaoDebito(Short.valueOf("1"));
        to.setTotalPrestacao(Short.valueOf("1"));
        to.setValorPrestacao(new BigDecimal(0.45));
        to.setDebitoTipo(multaPorImpontualidade.getId());
        to.setDescricaoTipoDebito(multaPorImpontualidade.getDescricao());
        debitos.add(to);
        
        to = new DebitoCobradoNaoParceladoTO();
        to.setAnoMesReferencia(201409);
        to.setNumeroPrestacaoDebito(Short.valueOf("1"));
        to.setTotalPrestacao(Short.valueOf("1"));
        to.setValorPrestacao(new BigDecimal(0.58));
        to.setDebitoTipo(jurosMora.getId());
        to.setDescricaoTipoDebito(jurosMora.getDescricao());
        debitos.add(to);
        
        to = new DebitoCobradoNaoParceladoTO();
        to.setAnoMesReferencia(201410);
        to.setNumeroPrestacaoDebito(Short.valueOf("1"));
        to.setTotalPrestacao(Short.valueOf("1"));
        to.setValorPrestacao(new BigDecimal(0.32));
        to.setDebitoTipo(jurosMora.getId());
        to.setDescricaoTipoDebito(jurosMora.getDescricao());
        debitos.add(to);
        
        to = new DebitoCobradoNaoParceladoTO();
        to.setAnoMesReferencia(201411);
        to.setNumeroPrestacaoDebito(Short.valueOf("1"));
        to.setTotalPrestacao(Short.valueOf("1"));
        to.setValorPrestacao(new BigDecimal(0.09));
        to.setDebitoTipo(jurosMora.getId());
        to.setDescricaoTipoDebito(jurosMora.getDescricao());
        debitos.add(to);
        
        to = new DebitoCobradoNaoParceladoTO();
        to.setAnoMesReferencia(201409);
        to.setNumeroPrestacaoDebito(Short.valueOf("1"));
        to.setTotalPrestacao(Short.valueOf("1"));
        to.setValorPrestacao(new BigDecimal(0.22));
        to.setDebitoTipo(atualizacaoMonetaria.getId());
        to.setDescricaoTipoDebito(atualizacaoMonetaria.getDescricao());
        debitos.add(to);
        
        return debitos;
    }
    
    private List<DebitoCobradoNaoParceladoTO> colecaoDebitosComMaisDeCincoMeses(){
        List<DebitoCobradoNaoParceladoTO> debitos = new ArrayList<DebitoCobradoNaoParceladoTO>();
        
        DebitoCobradoNaoParceladoTO to = new DebitoCobradoNaoParceladoTO();
        to.setAnoMesReferencia(201409);
        to.setNumeroPrestacaoDebito(Short.valueOf("1"));
        to.setTotalPrestacao(Short.valueOf("1"));
        to.setValorPrestacao(new BigDecimal(0.22));
        to.setDebitoTipo(multaPorImpontualidade.getId());
        to.setDescricaoTipoDebito(multaPorImpontualidade.getDescricao());
        debitos.add(to);
        
        to = new DebitoCobradoNaoParceladoTO();
        to.setAnoMesReferencia(201410);
        to.setNumeroPrestacaoDebito(Short.valueOf("1"));
        to.setTotalPrestacao(Short.valueOf("1"));
        to.setValorPrestacao(new BigDecimal(0.45));
        to.setDebitoTipo(multaPorImpontualidade.getId());
        to.setDescricaoTipoDebito(multaPorImpontualidade.getDescricao());
        debitos.add(to);

        to = new DebitoCobradoNaoParceladoTO();
        to.setAnoMesReferencia(201411);
        to.setNumeroPrestacaoDebito(Short.valueOf("1"));
        to.setTotalPrestacao(Short.valueOf("1"));
        to.setValorPrestacao(new BigDecimal(0.45));
        to.setDebitoTipo(multaPorImpontualidade.getId());
        to.setDescricaoTipoDebito(multaPorImpontualidade.getDescricao());
        debitos.add(to);
        
        to = new DebitoCobradoNaoParceladoTO();
        to.setAnoMesReferencia(201412);
        to.setNumeroPrestacaoDebito(Short.valueOf("1"));
        to.setTotalPrestacao(Short.valueOf("1"));
        to.setValorPrestacao(new BigDecimal(0.45));
        to.setDebitoTipo(multaPorImpontualidade.getId());
        to.setDescricaoTipoDebito(multaPorImpontualidade.getDescricao());
        debitos.add(to);
        
        to = new DebitoCobradoNaoParceladoTO();
        to.setAnoMesReferencia(201501);
        to.setNumeroPrestacaoDebito(Short.valueOf("1"));
        to.setTotalPrestacao(Short.valueOf("1"));
        to.setValorPrestacao(new BigDecimal(0.45));
        to.setDebitoTipo(multaPorImpontualidade.getId());
        to.setDescricaoTipoDebito(multaPorImpontualidade.getDescricao());
        debitos.add(to);
        
        to = new DebitoCobradoNaoParceladoTO();
        to.setAnoMesReferencia(201502);
        to.setNumeroPrestacaoDebito(Short.valueOf("1"));
        to.setTotalPrestacao(Short.valueOf("1"));
        to.setValorPrestacao(new BigDecimal(0.13));
        to.setDebitoTipo(multaPorImpontualidade.getId());
        to.setDescricaoTipoDebito(multaPorImpontualidade.getDescricao());
        debitos.add(to);
        
        
        return debitos;
    }

    private List<DebitoCobradoNaoParceladoTO> colecaoDebitosContendoUmParcelamento(){
        List<DebitoCobradoNaoParceladoTO> debitos = new ArrayList<DebitoCobradoNaoParceladoTO>();
        
        DebitoCobradoNaoParceladoTO to = new DebitoCobradoNaoParceladoTO();
        to.setAnoMesReferencia(201410);
        to.setNumeroPrestacaoDebito(Short.valueOf("1"));
        to.setTotalPrestacao(Short.valueOf("1"));
        to.setValorPrestacao(new BigDecimal(1.57));
        to.setDebitoTipo(multaPorImpontualidade.getId());
        to.setDescricaoTipoDebito(multaPorImpontualidade.getDescricao());
        debitos.add(to);
        
        to = new DebitoCobradoNaoParceladoTO();
        to.setAnoMesReferencia(null);
        to.setNumeroPrestacaoDebito(Short.valueOf("5"));
        to.setTotalPrestacao(Short.valueOf("5"));
        to.setValorPrestacao(new BigDecimal(39.03));
        to.setDebitoTipo(instalacaoRamalAgua.getId());
        to.setDescricaoTipoDebito(instalacaoRamalAgua.getDescricao());
        debitos.add(to);

        to = new DebitoCobradoNaoParceladoTO();
        to.setAnoMesReferencia(201410);
        to.setNumeroPrestacaoDebito(Short.valueOf("1"));
        to.setTotalPrestacao(Short.valueOf("1"));
        to.setValorPrestacao(new BigDecimal(0.84));
        to.setDebitoTipo(jurosMora.getId());
        to.setDescricaoTipoDebito(jurosMora.getDescricao());
        debitos.add(to);
        
        return debitos;
    }
    
    private void carregarMocks(List<DebitoCobradoNaoParceladoTO> debitos) {
        when(debitoCobradoRepositorioMock.pesquisarDebitoCobradoSemParcelamento(conta.getId())).thenReturn(debitos);
    }
}
