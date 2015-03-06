package br.gov.batch.microcoletor;

import static br.gov.persistence.util.IOUtil.arquivosFiltrados;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import br.gov.batch.util.BatchUtil;
import br.gov.model.util.Utilitarios;
import br.gov.persistence.util.IOUtil;

@Named
public class AgruparArquivosMicrocoletor implements ItemProcessor {
	private static Logger logger = Logger.getLogger(AgruparArquivosMicrocoletor.class);

   @Inject
    private BatchUtil util;

	public AgruparArquivosMicrocoletor() {
	}
	
    //FIXME: Parametrizar diretorio
	public Object processItem(Object param) throws Exception {
        String idGrupo = util.parametroDoBatch("idGrupoFaturamento");
        
    	logger.info("Agrupando os arquivos de microcoletor para o grupo");
    	
    	String[] wildcards = new String[]{"cons*." + idGrupo + "*.txt"};
    	
        List<File> arquivos = Arrays.asList(arquivosFiltrados("/tmp", wildcards));
        
        arquivos.sort(Comparator.naturalOrder());

    	StringBuilder txt = new StringBuilder();
    	
    	for (File file : arquivos) {
    		FileReader reader = new FileReader(file);
    		BufferedReader b = new BufferedReader(reader);
    		String linha = null;
    		while((linha = b.readLine()) != null){
    			txt.append(linha).append(Utilitarios.quebraLinha);
    			
    		}
    		b.close();
		}
    	
    	IOUtil.criarArquivoCompactado("final.txt", "/tmp/", txt.toString());
    	
//    	arquivos.forEach(e -> e.delete());
    	
        return param;
    }
}