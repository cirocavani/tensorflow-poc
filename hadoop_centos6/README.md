# Instalação do Hadoop no CentOS 6

(motivação: essa imagem corresponde a uma "aproximação" do ambiente de produção que usa RedHat EL 6 com o qual o CentOS6 é binário-compatível - o TensorFlow não é oficialmente suportado nesse sistema)

A POC é a configuração mínima do Hadoop no CentOS 6 para execução do treinamento com TensorFlow no YARN.

Na prática, consiste em rodar os servidores do HDFS (NameNode e DataNode) e do YARN (ResourceManager e NodeManager) para poder executar uma aplicação (ApplicationMaster) que instale o TensorFlow e rode o script Python de treinamento.

Essa POC é só a configuração do Hadoop no CentOS 6.

Esse procedimento é executado pelo comando:

    ./main.sh

Essa POC consiste em:

1. criar uma imagem Docker do CentOS 6 com Java 7 e Hadoop 2.5.

    Arquivos: `Dockerfile`, `docker-setup.sh`

2. configurar o Hadoop criando usuário, chaves SSH, XML de configuração e formatação do HDFS em um container

    Arquivo: `hadoop-setup.sh`

3. iniciar os servidores do Hadoop no container (deve ser executado toda vez que o container for iniciado)

    Arquivo: `hadoop-start.sh`

...

Para usar esse container:

    docker start hadoop_centos6

    docker exec hadoop_centos6 /hadoop-start.sh

    echo "$(docker exec hadoop_centos6 hostname -i)"

    > 172.17.0.2

    (todos os serviços do Hadoop estarão disponíveis nesse endereço)

    docker stop hadoop_centos6

Para acessar o container:

    docker exec -i -t hadoop_centos6 /bin/bash --login

...

**Cluster Setup**

http://hadoop.apache.org/docs/r2.5.2/hadoop-project-dist/hadoop-common/ClusterSetup.html
