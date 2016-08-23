# TensorFlow Project

O trabalho consistiu em identificar e resolver todos os requisitos para o desenvolvimento de uma Aplicação TensorFlow em uma infraestrutura de BigData.

Para esse trabalho, a "anatomia" de uma Aplicação TensorFlow que foi considerada consiste em dois componentes:

1. o treinamento (aprendizado): o código é em Python, precisa de acesso a dados e poder de processamento.

    Esse programa deve ser empacotado para rodar no YARN (Hadoop, RedHat EL 6)

2. a API de consulta (inferência): o código é em C++, recebe requisições com dados "reais" e retorna o resultado a partir da versão mais recente de um "modelo treinado"

    Esse programa deve ser empacotado para rodar no Tsuru (Ubuntu LTS 14.04)

...

Para integrar uma aplicação que usa essa "Inteligência Artificial", é necessário usar um Cliente que "faça requisições" ao servidor de inferência (2).

Essa funcionalidade pode ser implementada em qualquer linguagem e é feita com Python nos exemplos do TensorFlow.

Para esse trabalho, o interesse é integrar com Aplicações em Scala, logo um cliente Java satisfaz o requisito (Java 7).


## Provas de Conceito

Todas as POCs rodam dentro do Docker e foram testadas no Linux e no Mac (usando Docker on Mac).

Além do Docker, não precisa de mais nada instalado na máquina.

A POC [tflearn_wide_n_deep](#tflearn_wide_n_deep) foi o aquecimento rodando um exemplo do TensorFlow.

As POCs [tfserving_basic](#tfserving_basic), [tfserving_advanced](#tfserving_advanced) e [skeleton_project](#skeleton_project) correspondem ao trabalho de criar um projeto que consiste de algoritmo Python para treinamento e servidor C++ para servir o modelo (tem um cliente Python para validar o funcionamento).

As POCs [yarn_training](#yarn_training), [tensorflow_centos6](#tensorflow_centos6), [tensorflow_installer](#tensorflow_installer), [hadoop_centos6](#hadoop_centos6) e [hadoop_ubuntu1604](#hadoop_ubuntu1604) correspondem ao trabalho de fazer o treinamento usando YARN (Hadoop) no RedHat EL 6 (produção).

A POC [client_java](#client_java) corresponde ao trabalho de usar em uma aplicação Java/Scala um serviço do TensorFlow.

A POC [server_tsuru](#server_tsuru) corresponde ao trabalho de criar uma app no Tsuru para servir um modelo treinado do TensorFlow.


#### [tflearn_wide_n_deep](tflearn_wide_n_deep/)

Usando TF Learn para criação de modelo.

#### [tfserving_basic](tfserving_basic/)

Usando TF Serving para servidor de modelo sem versionamento.

#### [tfserving_advanced](tfserving_advanced/)

Usando TF Serving para servidor de modelo com versionamento.

#### [skeleton_project](skeleton_project/)

Usando Bazel para build de um projeto TensorFlow.

#### [yarn_training](yarn_training/)

Usando YARN para treinar um modelo com TensorFlow.

#### [tensorflow_centos6](tensorflow_centos6/)

Build do TensorFlow para CentOS 6.

#### [tensorflow_installer](tensorflow_installer/)

Instalador para o TensorFlow usando shell script autocontido (sem download).

#### [hadoop_centos6](hadoop_centos6/)

Instância do Hadoop rodando no CentOS 6 para rodar o treinamento do TensorFlow.

#### [hadoop_ubuntu1604](hadoop_ubuntu1604/)

Instância do Hadoop rodando no Ubuntu 16.04 para rodar o treinamento do TensorFlow.

#### [client_java](client_java/)

Usando gRPC para construir um Cliente Java que faz requisições em um servidor de modelo TensorFlow Serving.

#### [server_tsuru](server_tsuru/)

Usando Tsuru para servir um modelo treinado do TensorFlow.
