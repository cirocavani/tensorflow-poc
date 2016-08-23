# Servidor para o TensorFlow com Versionamento

A POC é a execução do tutorial sobre o TensorFlow Serving com versionamento do modelo.

O "algoritmo" desse tutorial é um classificador de imagem que faz reconhecimento de dígito usando dataset MNIST e Rede Neural.

Na prática, consiste de todo o processo de compilação do TensorFlow Serving e do TensorFlow para execução dos três requisitos desse trabalho: treinamento, servidor e cliente (essa "arquitetura" é o resultado final esperado para uma Aplicação TensorFlow).

Esse procedimento é executado pelo comando:

    ./main.sh

Essa POC consiste em:

1. criar uma imagem Docker do Ubuntu com Java 8, Bazel (ferramenta de build) e os pacotes para build do TensorFlow Serving.

    Arquivos: `Dockerfile`, `docker-setup.sh`

2. clonar o repositório git do TF Serving (TensorFlow é sub-módulo) e fazer build do script de treinamento (Python), do servidor (C++) e do cliente (Python).

    Arquivo: `serving-build.sh`

3. executar o treinamento com menos e mais exemplos salvando os dois modelos em disco, inicializar o servidor com o primeiro modelo, rodar o cliente para fazer várias requisições no servidor (enviando a imagem e recebendo o dígito / classe) mostrando no final a taxa de erro, adicionar o segundo modelo e rodar novamente o cliente mostrando um taxa de erro menor (ou seja, o servidor se atualizou).

    Arquivo: `serving-run.sh`

...

**Serving a TensorFlow Model**

https://tensorflow.github.io/serving/serving_advanced

https://github.com/tensorflow/serving/blob/master/tensorflow_serving/g3doc/serving_advanced.md
