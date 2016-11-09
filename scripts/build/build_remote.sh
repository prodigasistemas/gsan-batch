#!/bin/bash

CURRENT_PATH=$(pwd)

# Realiza o build
cd $GSAN_BATCH_PATH

project_version=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec)

echo "$project_version" > .version

war_file="gsan-batch-${project_version}.war"

if [ -z "$NAO_EXECUTAR_MAVEN" ]; then
  echo "Realizando o build ..."

  mvn clean package -Dmaven.test.skip=true
fi

cd target

# Transfere o build para o servidor
if [ -z $PORTA ]; then
  echo "Porta SSH (22):"
  read porta
  if [ -z $porta ]; then
    porta=22
  fi
else
  porta=$PORTA
fi

if [ -z $USUARIO ]; then
  echo "Usuario Remoto ($USER):"
  read usuario
  if [ -z $usuario ]; then
    usuario=$USER
  fi
else
  usuario=$USUARIO
fi

if [ -z $IP_REMOTO ]; then
  echo "IP Remoto (127.0.0.1):"
  read ip_remoto
  if [ -z $ip_remoto ]; then
    ip_remoto=127.0.0.1
  fi
else
  ip_remoto=$IP_REMOTO
fi

if [ -z $CAMINHO_REMOTO ]; then
  echo "Caminho Remoto (/tmp):"
  read caminho_remoto
  if [ -z $caminho_remoto ]; then
    caminho_remoto=/
  fi
else
  caminho_remoto=$CAMINHO_REMOTO
fi

echo "> Transferindo $war_file para $ip_remoto:$caminho_remoto ..."

scp -P $porta $war_file $usuario@$ip_remoto:$caminho_remoto

cd $CURRENT_PATH

exit 0