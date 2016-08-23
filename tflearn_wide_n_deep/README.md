# Rodando TensorFlow

A POC é a execução do tutorial sobre o "algoritmo" usado na recomendação de app do Google Play.

Na prática, é um classificador binário que responde se uma pessoa ganha mais ou menos de 50 mil dólares baseado em dados do Censo, é uma combinação de Logistic Regression com Rede Neural.

Esse procedimento é executado pelo comando:

    ./main.sh

Essa POC consiste em:

1. criar uma imagem Docker do Ubuntu com TensorFlow 0.9 instalado.

    Arquivos: `Dockerfile`, `docker-setup.sh`

2. provisionar o código do algoritmo (Python) e os dados em um container.

    Arquivo: `tflearn-setup.sh`

3. executar o algoritmo que faz a transformação das features dos dados do Censo, treina o modelo combinado de Logistic Regression e Rede Neural e valida no dataset de teste retornando a acurácia.

    Arquivo: `tflearn-run.sh`

(o modelo é salvo em disco, execuções subsequentes fazem treinamento incremental do modelo)

...

**Wide & Deep Learning: Better Together with TensorFlow**

https://research.googleblog.com/2016/06/wide-deep-learning-better-together-with.html

**Tutorial**

https://www.tensorflow.org/versions/r0.9/tutorials/linear/overview.html

https://www.tensorflow.org/versions/r0.9/tutorials/wide/index.html

https://www.tensorflow.org/versions/r0.9/tutorials/wide_and_deep/index.html
