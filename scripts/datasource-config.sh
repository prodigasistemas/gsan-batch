#Adiciona o modulo do postgres no wildfly

./jboss-cli.sh --connect --commands='module add --name=org.postgresql --resources=<caminho_arquivo>/<nome_arquivo>.jar --dependencies=javax.api'

#Configura o driver

./jboss-cli.sh --connect --commands='./subsystem=datasources/jdbc-driver=postgres:add(driver-name="postgres", driver-module-name="org.postgresql", driver-xa-datasource-class-name="org.postgresql.xa.PGXADataSource")'

#Cria o datasource

./jboss-cli.sh --connect --commands='./subsystem=datasources/data-source=GsanDS:add(jndi-name=java:/jboss/datasources/GsanDS,connection-url= jdbc:postgresql://<ip_server>:5432/<database>,  driver-name=postgres, user-name=<usuer> , password=<passowrd>)'
