# Instalação do Hadoop no Ubuntu

(motivação: essa imagem corresponde ao ambiente em que o TensorFlow é oficialmente suportado, diferente do ambiente de produção RedHat, ou seja, o comportamento nesse ambiente deve representar o "funcionamento correto")

A POC é a configuração mínima do Hadoop no Ubuntu para execução do treinamento com TensorFlow no YARN.

Na prática, consiste em rodar os servidores do HDFS (NameNode e DataNode) e do YARN (ResourceManager e NodeManager) para poder executar uma aplicação (ApplicationMaster) que instale o TensorFlow e rode o script Python de treinamento.

Essa POC é só a configuração do Hadoop no Ubuntu.

Esse procedimento é executado pelo comando:

    ./main.sh

Essa POC consiste em:

1. criar uma imagem Docker do Ubuntu com Java 8 e Hadoop 2.5.

    Arquivos: `Dockerfile`, `docker-setup.sh`

2. configurar o Hadoop criando usuário, chaves SSH, XML de configuração e formatação do HDFS em um container

    Arquivo: `hadoop-setup.sh`

3. iniciar os servidores do Hadoop no container (deve ser executado toda vez que o container for iniciado)

    Arquivo: `hadoop-start.sh`

...

Para usar esse container:

    docker start hadoop_ubuntu1604

    docker exec hadoop_ubuntu1604 /hadoop-start.sh

    echo "$(docker exec hadoop_ubuntu1604 hostname -i)"

    > 172.17.0.2

    (todos os serviços do Hadoop estarão disponíveis nesse endereço)

    docker stop hadoop_ubuntu1604

Para acessar o container:

    docker exec -i -t hadoop_ubuntu1604 /bin/bash --login    

...

**Cluster Setup**

http://hadoop.apache.org/docs/r2.5.2/hadoop-project-dist/hadoop-common/ClusterSetup.html
