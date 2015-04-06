#!bin/bash

CURRENT_PATH=$(pwd)

# Constroi o build
cd $GSAN_BATCH_PATH

project_version=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec)

zip_file="gsan-batch-${project_version}.war.zip"

mvn clean package -Dmaven.test.skip=true


# Compacta o build

cd target

zip -vr $zip_file *.war

# Transfere o build para o servidor
echo "Porta SSH (22):"
if [ -z $PORTA ]; then
  read porta
  if [ -z $porta ]; then
    porta=22
  fi
else
  porta=$PORTA
fi

echo "Usuario Remoto ($USER):"
if [ -z $USUARIO ]; then
  read usuario
  if [ -z $usuario ]; then
    usuario=$USER
  fi
else
  usuario=$USUARIO
fi

echo "IP Remoto (127.0.0.1):"
if [ -z $IP_REMOTO ]; then
  read ip_remoto
  if [ -z $ip_remoto ]; then
    ip_remoto=127.0.0.1
  fi
else
  ip_remoto=$IP_REMOTO
fi

echo "Caminho Remoto (/tmp):"
if [ -z $CAMINHO_REMOTO ]; then
  read caminho_remoto
  if [ -z $caminho_remoto ]; then
    caminho_remoto=/
  fi
else
  caminho_remoto=$CAMINHO_REMOTO
fi

scp -P $porta $zip_file $usuario@$ip_remoto:$caminho_remoto

# Apaga o build transferido e volta ao local inicial
rm -rf $zip_file

cd $CURRENT_PATH
