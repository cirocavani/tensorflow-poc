# Cliente Java para um Servidor do TensorFlow

A POC é a criação de um cliente Java para fazer inferência em um serviço do TensorFlow.

Na prática, consiste em gerar o código do protocolo de comunicação usando o gRPC (Protobuf) e usar esse código para acessar o serviço do TensorFlow, a especificação do protocolo faz parte da implementação do serviço.

O "algoritmo" desse projeto é um classificador de imagem que faz reconhecimento de dígito usando dataset MNIST e Rede Neural.

Esse procedimento é executado pelo comando:

    ./main.sh

Essa POC consiste em:

1. criar uma imagem Docker do Ubuntu com Java 8.

    Arquivos: `Dockerfile`, `docker-setup.sh`

2. inicializar o container criado em [skeleton_project](../skeleton_project), projeto standalone e rodar o servidor do TensorFlow (rodando o segundo modelo).

3. construir e executar o código Java cliente para fazer várias requisições no servidor (enviando a imagem e recebendo o dígito / classe) mostrando no final a taxa de erro e acurácia.

    Arquivo: `client-run.sh`

...

**gRPC Java**

https://github.com/grpc/grpc-java

http://www.grpc.io/docs/tutorials/basic/java.html
