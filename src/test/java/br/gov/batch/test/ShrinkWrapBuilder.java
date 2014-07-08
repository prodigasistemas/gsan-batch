package br.gov.batch.test;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class ShrinkWrapBuilder {
	
	public static Archive<?> createDeployment(){
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true, "br.gov.model")
                .addPackages(true, "br.gov.model.arrecadacao")
                .addPackages(true, "br.gov.model.atendimentopublico")
                .addPackages(true, "br.gov.model.batch")
                .addPackages(true, "br.gov.model.cadastro")
                .addPackages(true, "br.gov.model.cobranca")
                .addPackages(true, "br.gov.model.converter")
                .addPackages(true, "br.gov.model.faturamento")
                .addPackages(true, "br.gov.model.financeiro")
                .addPackages(true, "br.gov.model.micromedicao")
                .addPackages(true, "br.gov.model.operacao")
                .addPackages(true, "br.gov.model.util")
                .addPackages(true, "br.gov.servicos.arrecadacao")
                .addPackages(true, "br.gov.servicos.batch")
                .addPackages(true, "br.gov.servicos.cadastro")
                .addPackages(true, "br.gov.servicos.faturamento")
                .addPackages(true, "br.gov.servicos.to")
            .addAsResource("persistence-test.xml", "META-INF/persistence.xml");		
	}
}
