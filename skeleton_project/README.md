# Aplicação TensorFlow com Bazel (build)

(código do exemplo extraído para um projeto fora da árvore do TensorFlow Serving)

A POC é a criação de um projeto standalone baseado no código do tutorial sobre o TensorFlow Serving com versionamento do modelo.

O "algoritmo" desse projeto é um classificador de imagem que faz reconhecimento de dígito usando dataset MNIST e Rede Neural.

Na prática, consiste em separar o código para treinamento, servidor e cliente em um projeto que depende do TensorFlow Serving e usa a mesma ferramenta de build Bazel (fazendo o processo de compilação do TensorFlow Serving e do TensorFlow).

Esse procedimento é executado pelo comando:

    ./main.sh

Essa POC consiste em:

1. criar uma imagem Docker do Ubuntu 14.04 com Java 8, Python 2.7 (conda), Bazel (ferramenta de build) e os pacotes para build do TensorFlow Serving.

    Arquivos: `Dockerfile`, `docker-setup.sh`

2. copiar a estrutura do projeto standalone (chamado `skeleton`), clonar o TensorFlow Serving, fazer o build do script de treinamento (Python), do servidor (C++) e do cliente (Python).

    Arquivo: `skeleton-build.sh`, `skeleton/` (diretório do projeto)

3. executar o treinamento com menos e mais exemplos salvando os dois modelos em disco, inicializar o servidor com o primeiro modelo, rodar o cliente para fazer várias requisições no servidor (enviando a imagem e recebendo o dígito / classe) mostrando no final a taxa de erro, adicionar o segundo modelo e rodar novamente o cliente mostrando um taxa de erro menor (ou seja, o servidor se atualizou).

    Arquivo: `skeleton-run.sh`

...

**Project Bazel**

http://www.bazel.io/docs/tutorial/cpp.html
