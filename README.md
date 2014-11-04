gsan-batch
==========

Projeto com nova arquitetura para processamento de batchs

Passos de criação de um novo batch

1. Criar um arquivo com o nome do batch na pasta resources/META-INF/batch-jobs/
2. Criar um script de migração para inserir um registro na tabela batch.processo. Importante salientar que o conteúdo da coluna proc_nmarquivobatch deve ter o nome do arquivo de configuração do batch. Os scripts estão no projeto gsan-persistence.

