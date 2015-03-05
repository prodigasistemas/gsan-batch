package br.gov.batch.microcoletor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;

import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Named;

import org.jboss.logging.Logger;

import br.gov.model.util.IOUtil;
import br.gov.model.util.Utilitarios;

@Named
public class AgruparArquivos implements ItemProcessor {
	private static Logger logger = Logger.getLogger(AgruparArquivos.class);

	
	public AgruparArquivos() {
	}

    public Object processItem(Object param) throws Exception {
    	logger.info("Escrevendo arquivo");
    	
    	File dir = new File("/tmp");
    	FilenameFilter filtro = new FilenameFilter() {			
			public boolean accept(File dir, String name) {
				return name.contains("cons");
			}
		};
		
    	File[] arquivos = dir.listFiles(filtro);
    	
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
    	
    	IOUtil.criarArquivo("final.txt", dir.getAbsolutePath() + "/", txt.toString());
    	
        return param;
    }
}