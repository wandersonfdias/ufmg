__author__ = 'p060848'
# coding=utf-8

import os
import pandas as pd
import glob
import random

class imagems():

    imagens = ''
    path_imagens_consulta = ''
    path_imagens_base = ''

    #def coletaImagens(self):
    #    """
    #    Cria a serie com o nome de todas as imagens a partir dos diretorios.
    #    """
    #
    #    imagens = pd.Series(os.listdir(self.path_imagens_consulta))
    #    rng = range(1, len(os.listdir(self.path_imagens_consulta)))
    #    imagens = imagens.reindex(rng)
    #    return (imagens)

    def get_imagens_consulta(self):
        imgs=[]
        for file in os.listdir(self.path_imagens_consulta):
            file = file.replace(".jpg","")
            imgs.append(file)
        return imgs

    def get_imagens_base(self):
        imgs=[]
        for file in os.listdir(self.path_imagens_base):
            file = file.replace(".jpg","")
            imgs.append(file)
        return imgs

    #def get_imagens_semelhantes(self,imagem_consulta):
    #    imgs=[]
    #    for file in glob.glob(self.path+imagem_consulta+'/*.jpg'):
    #        imgs.append(file)
    #    return imgs
    #
    #def get_imagens_distintas(self,imagen_consulta,num_imagens_distintas):
    #    imgs=[]
    #    for file in os.listdir(self.path):
    #        imgs.append(file)
    #    imgs.pop(imgs.index(imagen_consulta))
    #    imgs = random.sample(imgs,num_imagens_distintas)
    #    return imgs



    def get_index_from_nome(self,serie,nome_imagem):
        """
        Retorna o indice na serie da imagem passada por parametro.
        @param serie: serie com os dados.
        @param nome_imagem: nome da imagem que deseja recuperar o indice
        @return: indice da imagem.
        """
        index = serie[serie == nome_imagem].index.tolist()
        index = str(index).replace('[','').replace(']','')
        return index

    def get_nome_from_index(self,serie,index):
        """
        Retorna o nome da imagem a partir do nome.
        @param serie: serie com os dados.
        @param index: indice da imagem que deseja o nome.
        @return: nome da imagem.
        """
        nome = serie[index]
        return nome

    def remomeia_imagens_padding(self):
        global path_imagens_consulta
        prefixo='im'
        for img in self.coletaImagens().values:
            num = self.get_index_from_nome(self.imagens,img)
            num = num.zfill(4)
            novo_nome = prefixo + num + '.jpg'
            os.rename(os.path.join(path_imagens_consulta,img), os.path.join(path_imagens_consulta,novo_nome))

    def renomeia_diretorios_padding(self):
        path_experimento='/home/felipe/projeto/projeto_pesquisa/experimento/'
        prefixo='im'
        for img in self.coletaImagens().values:
            if(os.path.isdir(path_experimento+img[:-4])):
                num = self.get_index_from_nome(self.imagens,img)
                num = num.zfill(4)
                novo_nome = prefixo + num
                os.rename(os.path.join(path_experimento,img[:-4]),os.path.join(path_experimento,novo_nome))

    def renomeia_imagens_diretorios(self):
        self.coletaImagens()
        path_experimento='/home/felipe/projeto/projeto_pesquisa/experimento/'
        prefixo='im'
        for dir in os.listdir(path_experimento):
            for file in glob.glob(path_experimento+dir+'/*.jpg'):
                nome_img = os.path.basename(file)
                num = self.get_index_from_nome(self.imagens,nome_img)
                num = num.zfill(4)
                novo_nome = prefixo+num+'.jpg'
                os.rename(os.path.join(path_experimento+dir,nome_img),os.path.join(path_experimento+dir,novo_nome))