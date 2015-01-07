gsan-batch
==========

Projeto com nova arquitetura para processamento de batchs

Projetos relacionados

* gsan-persistence
* gsan-processos
* gsan-batch-manager

Tecnologias utilizadas

* Java EE 7
* Java 8
* Rails 4
* Ruby 2
* Servidor de aplicação: WildFly 8.0.0
* Banco de dados: Postgres 9.3.4

Configuração da aplicação Java:

* Instale o driver do postgres no Wildfly (pode usar a lib armazenada na pasta migracoes/drivers do projeto gsan-persistence)
* Crie um datasource no arquivo standalone-full.xml com o jndi 'java:jboss/datasources/GsanDS' (o mesmo do persistence.xml)
* Execute a aplicação com a versão full do wildfly (standalone-full.xml)

Passos de criação de um novo batch

1. Criar um arquivo com o nome do batch na pasta resources/META-INF/batch-jobs/
2. Criar um script de migração para inserir um registro na tabela batch.processo. 
Olhe um exemplo na pasta migracoes/scripts do projeto gsan-persistence
Importante salientar que o conteúdo da coluna proc_nmarquivobatch deve ter o nome do arquivo de configuração do batch. Os scripts estão no projeto gsan-persistence.
3. Crie uma view com o mesmo nome do batch na pasta views/templates do projeto gsan-batch-manager. Esta view contem os campos que formam os parâmetros do processo.

