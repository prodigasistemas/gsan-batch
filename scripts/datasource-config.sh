#!/bin/bash

echo
echo "======================================="
echo "ATENCAO: O Wildfly deve estar iniciado!"
echo "======================================="
echo

if [ -z "$WILDFLY_HOME" ]; then
  echo "Informe o caminho do wildfly"
  read WILDFLY_HOME
fi

if [ ! -d "$WILDFLY_HOME" ]; then
  echo "O caminho $WILDFLY_HOME nao e um diretorio!"
  exit 1
fi

if [ ! -e "$WILDFLY_HOME" ]; then
  echo "O diretorio $WILDFLY_HOME nao foi encontrado!"
  exit 1
fi

export JBOSS_HOME=$WILDFLY_HOME

echo "Informe o caminho com o nome do arquivo do driver jdbc do postgresql"
read DRIVER_POSTGRESQL

if [ ! -e "$DRIVER_POSTGRESQL" ]; then
  echo "O arquivo $DRIVER_POSTGRESQL nao foi encontrado!"
  exit 1
fi

echo "Informe o IP do servidor de banco de dados (padrao: localhost)"
read SERVER_IP

[ -z "$SERVER_IP" ] && SERVER_IP=localhost

echo "Informe a porta do servidor de banco de dados (padrao: 5432)"
read SERVER_PORT

[ -z "$SERVER_PORT" ] && SERVER_PORT=5432

echo "Informe o nome do banco de dados (padrao: gsan_comercial)"
read SERVER_DATABASE

[ -z "$SERVER_DATABASE" ] && SERVER_DATABASE=gsan_comercial

echo "Informe o nome do usuario do servidor de banco de dados"
read PG_USER

if [ -z "$PG_USER" ]; then
  echo "Informe o usuario de acesso ao banco de dados!"
  exit 1
fi

echo "Informe a senha do usuario do servidor de banco de dados"
read PG_PASSWORD

if [ -z "$PG_PASSWORD" ]; then
  echo "Informe a senha de acesso ao banco de dados!"
  exit 1
fi

echo "> Adicionando o modulo do postgres no wildfly ..."

$WILDFLY_HOME/bin/jboss-cli.sh --connect --commands="module add --name=org.postgresql --resources=$DRIVER_POSTGRESQL --dependencies=javax.api"

echo "> Configurando o driver ..."

$WILDFLY_HOME/bin/jboss-cli.sh --connect --commands='./subsystem=datasources/jdbc-driver=postgres:add(driver-name="postgres", driver-module-name="org.postgresql", driver-xa-datasource-class-name="org.postgresql.xa.PGXADataSource")'

echo "> Criando o datasource ..."

$WILDFLY_HOME/bin/jboss-cli.sh --connect --commands="./subsystem=datasources/data-source=GsanDS:add(jndi-name=java:/jboss/datasources/GsanDS,connection-url=jdbc:postgresql://$SERVER_IP:$SERVER_PORT/$SERVER_DATABASE, driver-name=postgres, user-name=$PG_USER, password=$PG_PASSWORD)"

echo "Operacao concluida com sucesso!"
echo
