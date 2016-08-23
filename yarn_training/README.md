# Treinamento do TensorFlow no YARN

(motivação: usar a infraestrutura de armazenamento e processamento do Hadoop para rodar o treinamento com TensorFlow)

A POC é a criação de uma Aplicação YARN para rodar o treinamento com TensorFlow.

Na prática, consiste em criar uma aplicação Java baseada no exemplo de execução de shell script distribuído do YARN, essa aplicação é dividida em duas partes, uma que faz submissão do script e a outra que controla dentro do cluster a execução.

Esse procedimento depende do instalador do TensorFlow criado em [tensorflow_installer](../tensorflow_installer) por link simbólico e do container em [hadoop_centos6](../hadoop_centos6) estar rodando.

Esse procedimento é executado pelo comando:

    ./main.sh

Essa POC consiste em:

1. criar uma imagem Docker do Ubuntu com Java 8 e Bazel.

    Arquivos: `Dockerfile`, `docker-setup.sh`

2. construir a aplicação YARN usando Bazel e rodar a execução do treinamento "empacotado" no instalador (o treinamento é executado no outro container que roda o Hadoop)

    Arquivos: `tensorflow-yarn/` (diretório da aplicação Java), `tensorflow.sh` (instalador, link simbólico), `tensorflow-run.sh`

Para acessar o container:

    docker exec -i -t yarn_training /bin/bash --login

...

**Writing YARN Applications**

http://hadoop.apache.org/docs/r2.5.2/hadoop-yarn/hadoop-yarn-site/WritingYarnApplications.html

https://github.com/apache/hadoop-common/tree/release-2.5.0/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-distributedshell

https://github.com/apache/hadoop-common/blob/release-2.5.0/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-distributedshell/src/main/java/org/apache/hadoop/yarn/applications/distributedshell/Client.java

https://github.com/apache/hadoop-common/blob/release-2.5.0/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-distributedshell/src/main/java/org/apache/hadoop/yarn/applications/distributedshell/ApplicationMaster.java

http://www.bazel.io/docs/tutorial/java.html
