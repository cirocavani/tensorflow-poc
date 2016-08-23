# Compilação do TensorFlow para CentOS 6

(motivação: o TensorFlow não é oficialmente suportado no RedHat EL 6, o binário é compilado para glibc 2.17 e o código C++ 11, ambos requisitos não disponíveis, mas é possível criar um binário do TensorFlow compatível)

A POC é a construção do binário do TensorFlow compatível com RedHat EL 6.

Na prática, consiste em instalar a versão mais recente do GCC disponível para o CentOS 6, construir a ferramenta de build Bazel (binário incompatível com a glibc) e construir o TensorFlow (com patch para linkage).

Essa POC é só para criar um pacote do TensorFlow.

Esse procedimento é executado pelo comando:

    ./main.sh

Essa POC consiste em:

1. criar uma imagem Docker do CentOS 6 com GCC 5, Java 8, Python 2.7 (conda) e Bazel (build).

    Arquivos: `Dockerfile`, `docker-setup.sh`

2. construir o TensorFlow clonando o branch 0.9, aplicando o patch para linkage e executando o gerador do pacote Python.

    Arquivos: `tensorflow-build.sh`

3. copiar o pacote do TensorFlow do container para o host.

O resultado é o arquivo na pasta da POC:

    tensorflow-0.9.0-py2-none-linux_x86_64.whl

...

https://www.tensorflow.org/versions/r0.9/get_started/os_setup.html#installing-from-sources

http://www.bazel.io/docs/install.html#compiling-from-source

https://www.softwarecollections.org/en/scls/rhscl/devtoolset-4/

https://github.com/tensorflow/tensorflow/issues/121
