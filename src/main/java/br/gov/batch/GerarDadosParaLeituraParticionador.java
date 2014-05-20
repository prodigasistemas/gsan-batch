package br.gov.batch;

import java.util.Properties;

import javax.batch.api.partition.PartitionMapper;
import javax.batch.api.partition.PartitionPlan;
import javax.batch.api.partition.PartitionPlanImpl;
import javax.batch.runtime.context.JobContext;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import br.gov.ejb.ImovelEJB;

@Named
public class GerarDadosParaLeituraParticionador implements PartitionMapper {
	
	private static Logger logger = Logger.getLogger(GerarDadosParaLeituraParticionador.class);

    @EJB
    private ImovelEJB ejb;
    
    @Inject
    private JobContext jobCtx;    

    public PartitionPlan mapPartitions() throws Exception {
        return new PartitionPlanImpl() {
        	
        	public int getThreads(){
        		return Integer.valueOf(jobCtx.getProperties().getProperty("num_particoes"));
        	}

            public int getPartitions() {
            	return Integer.valueOf(jobCtx.getProperties().getProperty("num_particoes"));
            }

            public Properties[] getPartitionProperties() {
                long totalItems = ejb.quantidadeImoveis();
                
                logger.info("Numero de itens a serem processados: " + totalItems);
                
                long partItems = (long) totalItems / getPartitions();
                long remItems = totalItems % getPartitions();

                Properties[] props = new Properties[getPartitions()];

                for (int i = 0; i < getPartitions(); i++) {
                    props[i] = new Properties();
                    props[i].put("primeiroItem", String.valueOf(i * partItems));
                    if (i == getPartitions() - 1) {
                        props[i].put("numItens", String.valueOf(partItems + remItems));
                    } else {
                        props[i].put("numItens", String.valueOf(partItems));
                    }
                }
                return props;
            }
        };
    }
}
