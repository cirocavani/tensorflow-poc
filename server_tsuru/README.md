# Servidor do TensorFlow no Tsuru

A POC é a criação de uma aplicação do Tsuru para rodar o servidor do TensorFlow.

O "algoritmo" desse servidor é um classificador de imagem que faz reconhecimento de dígito usando dataset MNIST e Rede Neural.

Na prática, consiste em fazer o deploy do binário do servidor do TensorFlow em uma app do Tsuru.

Esse procedimento depende do binário do servidor do TensorFlow criado em [skeleton_project](../skeleton_project).

Esse procedimento depende de um ambiente do Tsuru externo.

A criação da app do Tsuru é manual e deve ser executada uma vez:

    tsuru app-create tf-skeleton python -p huge

Para acesso a API, é necessário gerar o token do usuário:

    tsuru token-show
    > API key: <SECRET>

Esse token e o endereço da API do Tsuru devem ser colocados no aquivo `client-run.sh`.

O procedimento de deploy é executado pelo comando:

    ./setup.sh
    ./deploy.sh

A execução do cliente que acessa a app:

    ./client-run.sh


Essa POC consiste em:

1. criar manualmente a app do Tsuru (uma vez)

2. copiar servidor e modelo treinado do [skeleton_project](../skeleton_project)

    Arquivo: `setup.sh`, `skeleton/` (diretório do servidor)

3. fazer deploy na app do Tsuru

    Arquivo: `deploy.sh`, `Procfile`, `skeleton/` (diretório do servidor)

4. rodar o cliente que acessa a app no Tsuru direto nas units

    Arquivo: `client-run.sh`

...

No momento, o Tsuru não tem suporte para HTTP2 através do roteador/ balanceador, contudo o gRPC permite uma solução paliativa que está implementada no projeto `tensorflow-tsuru`.

A solução consistem em fazer no cliente a "resolução" de endereço das units e o balanceamento, ignorando o balanceador / roteador do Tsuru.

O gRPC já tem API genérica para resolver o endereço dos servidores. Também tem suporte para Round-Robin.

O código é um NameResolver que usa a API do Tsuru para obter os endereços IPs das máquinas e as portas de cada container. A cada 1 min ou em caso de erro de conexão, é feita a atualização do cliente. O primeiro refresh é para quando units são adicionadas e o segundo acontece quando são removidas.

Contudo, quando uma unit é removida, o erro no gRPC é quase instantâneo e a API do Tsuru pode acabar retornando o endereço dessa unit por um instante. A solução para essa "consistência eventual" foi testar de conexão da lista retornada pela API.

O código:

[NameResolver](tensorflow-tsuru/src/main/java/tensorflow/tsuru/TsuruNameResolver.java)

Construção do "protocolo":

[ManagedChannel](tensorflow-tsuru/src/main/java/tensorflow/tsuru/Main.java#L33-38)

Referência:

http://www.grpc.io/grpc-java/javadoc/io/grpc/NameResolver.html

http://www.grpc.io/grpc-java/javadoc/io/grpc/util/RoundRobinLoadBalancerFactory.html

...

**Tsuru**

https://tsuru.io/

https://docs.tsuru.io/stable/installing/index.html
