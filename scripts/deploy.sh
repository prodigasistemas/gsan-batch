#!bin/bash

if [ -z $1 ]; then
        echo "Precisa especificar qual arquivo ZIP sera implantado, exemplo: => sh deploy.sh gsan-operacional-1.0.0.war.zip"
else
        # Descompacta o build especificado
        unzip $1

        mv $1 versions/

        # Para o servidor para o iniciar um novo deploy
        /etc/init.d/wildfly stop

        # Apaga as pastas temporarias e o build atual
        rm -rf tmp/
        rm -rf deployments/gsan*.war

        # Copia o build especificado para deploy
        cp gsan-operacional*.war deployments/gsan-operacional.war

        rm -rf gsan-operacional*.war

        # Inicializa o servidor para o novo deploy
        /etc/init.d/wildfly start
fi

