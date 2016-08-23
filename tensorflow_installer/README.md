# Instalador da Aplicação TensorFlow

(motivação: evitar download no ambiente de produção e evitar enviar múltiplos arquivos para o Hadoop)

A POC é a criação de um instalador para o algoritmo de treinamento com o TensorFlow que tenha todas as dependência e possa ser executado no RedHat EL 6.

Na prática, consiste em criar um pacote com TensorFlow, Python (conda) e todas as dependências que é embutido em um shell script que faz a instalação e executa o treinamento do TensorFlow.

Esse procedimento depende do pacote do TensorFlow criado em [tensorflow_centos6](../tensorflow_centos6) por link simbólico.

Esse procedimento é executado pelo comando:

    ./main.sh

Essa POC consiste em:

1. criar uma imagem Docker do CentOS 6.

    Arquivos: `Dockerfile`, `docker-setup.sh`

2. construir um shell script embutido com o binário do pacote com TensorFlow, Python (conda), dependências e código do algoritmo.

    Arquivos: `tensorflow-setup.sh` (cria instalador), `tensorflow-bin.sh` (shell script a ser embutido com pacote), `tensorflow-run.sh` (shell script a ser executado durante a instalação), `tensorflow-0.9.0-py2-none-linux_x86_64.whl` (pacote do TensorFlow, link simbólico)

3. copiar o instalador do TensorFlow do container para o host.

O resultado é o arquivo na pasta da POC:

    tensorflow.sh

Para acessar o container:

    docker exec -i -t tensorflow_installer /bin/bash --login

...

**How do Linux binary installers (.bin, .sh) work?**

http://stackoverflow.com/questions/955460/how-do-linux-binary-installers-bin-sh-work
