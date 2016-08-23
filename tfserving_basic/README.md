# Servidor para TensorFlow sem versionamento

A POC é a execução do tutorial sobre o TensorFlow Serving sem versionamento do modelo.

O "algoritmo" desse tutorial é um classificador de imagem que faz reconhecimento de dígito usando dataset MNIST e Rede Neural.

Na prática, consiste de todo o processo de compilação do TensorFlow Serving e do TensorFlow para execução dos três requisitos desse trabalho: treinamento, servidor e cliente (essa "arquitetura" é o resultado final esperado para uma Aplicação TensorFlow).

Esse procedimento é executado pelo comando:

    ./main.sh

Essa POC consiste em:

1. criar uma imagem Docker do Ubuntu com Java 8, Bazel (ferramenta de build) e os pacotes para build do TensorFlow Serving.

    Arquivos: `Dockerfile`, `docker-setup.sh`

2. clonar o repositório git do TF Serving (TensorFlow é sub-módulo) e fazer build do script de treinamento (Python), do servidor (C++) e do cliente (Python).

    Arquivo: `serving-build.sh`

3. executar o treinamento que salva o modelo em disco, inicializar o servidor com esse modelo, rodar o cliente para fazer várias requisições no servidor (enviando a imagem e recebendo o dígito / classe) mostrando no final a taxa de erro.

    Arquivo: `serving-run.sh`

...

**Serving a TensorFlow Model**

https://tensorflow.github.io/serving/serving_basic

https://github.com/tensorflow/serving/blob/master/tensorflow_serving/g3doc/serving_basic.md
