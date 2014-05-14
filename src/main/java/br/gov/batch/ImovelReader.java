package br.gov.batch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.batch.api.chunk.AbstractItemReader;
import javax.enterprise.context.Dependent;
import javax.inject.Named;

import br.gov.model.cadastro.Imovel;

@Named
@Dependent
public class ImovelReader extends AbstractItemReader {
	
//    @EJB
//    private ImovelEJB ejb;
    
    private Iterator<Imovel> imoveis;
    
    public ImovelReader() {
	}

    public void open(Serializable checkpoint) throws Exception {
//    	List<Imovel> i = new ArrayList<Imovel>();
//   	Imovel a = new Imovel();
//    	a.setId(1);
//    	i.add(a);
    	
//    	imoveis = i.iterator();
    }

    public String readItem() {
        return "";
    }
}