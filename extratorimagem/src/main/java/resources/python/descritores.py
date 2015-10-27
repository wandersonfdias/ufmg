__author__ = 'p060848'
# coding=utf-8
import os
import imagens as im

class descritor():
    path_descritores=''
    descritores_img_query=[]
    descritores_img_resposta=[]
    img_consulta=''
    img_base=''


    def get_descritores_consulta(self,descritor):
        path = self.path_descritores+'\\'+descritor
        file = self.img_consulta + '.' + descritor
        arquivo = open(path+'\\'+file)
        vetor = arquivo.read()
        vetor = vetor.split()
        return vetor

    def get_descritores_base(self,descritor):
        path = self.path_descritores+'\\'+descritor
        file = self.img_base + '.' + descritor
        arquivo = open(path+'\\'+file)
        vetor = arquivo.read()
        vetor = vetor.split()
        return vetor

    def calcula_distancia_L1(self,descritor):
        distancia = 0
        vetor_img_query = self.get_descritores_consulta(descritor)
        vetor_img_query = vetor_img_query[1:]           #Retirando o primeiro elemento que é o tamanho do vetor
        vetor_img_resposta = self.get_descritores_base(descritor)
        vetor_img_resposta = vetor_img_resposta[1:]     #Retirando o primeiro elemento que é o tamanho do vetor
        i=0
        while (i < len(vetor_img_query)):
            vq = float(vetor_img_query[i])
            vr = float(vetor_img_resposta[i])
            distancia += abs(vq-vr)
            i=i+1
        return(distancia)

    def calcula_distancia_L1_bic(self,descritor):
        distancia = 0
        vetor_img_query = self.get_descritores_consulta(descritor)
        vetor_img_resposta = self.get_descritores_base(descritor)
        vetor_img_query = str(vetor_img_query).replace("'",'').replace('[','').replace(']','').split()
        vetor_img_resposta = str(vetor_img_resposta).replace("'",'').replace('[','').replace(']','').split()
        i=0
        while (i < len((vetor_img_query[1][:]))):
            vq = float(vetor_img_query[1][i])
            vr = float(vetor_img_resposta[1][i])
            distancia += abs(vq-vr)
            i=i+1
        return (distancia)

    def calcula_distancia_imagens(self,classe):
        linha = []
        linha.append(self.img_consulta+':'+self.img_base)
        linha.append(self.calcula_distancia_L1('acc'))
        linha.append(self.calcula_distancia_L1_bic('bic'))
        linha.append(self.calcula_distancia_L1('ccv'))
        linha.append(self.calcula_distancia_L1('eoac'))
        linha.append(self.calcula_distancia_L1('gch'))
        linha.append(self.calcula_distancia_L1('las'))
        linha.append(self.calcula_distancia_L1('lch'))
        linha.append(self.calcula_distancia_L1('qcch'))
        linha.append(self.calcula_distancia_L1('sid'))
        linha.append(self.calcula_distancia_L1('unser'))
        linha.append(classe)
        print(linha)
        return linha

    def calcula_distancia_imagens_consulta_base(self):
        linha = []
        linha.append(self.img_consulta+':'+self.img_base)
        linha.append(self.calcula_distancia_L1('acc'))
        linha.append(self.calcula_distancia_L1_bic('bic'))
        linha.append(self.calcula_distancia_L1('ccv'))
        linha.append(self.calcula_distancia_L1('eoac'))
        linha.append(self.calcula_distancia_L1('gch'))
        linha.append(self.calcula_distancia_L1('las'))
        linha.append(self.calcula_distancia_L1('lch'))
        linha.append(self.calcula_distancia_L1('qcch'))
        linha.append(self.calcula_distancia_L1('sid'))
        linha.append(self.calcula_distancia_L1('unser'))
        # Adicionando a classe 0
        linha.append(0)
        return linha



    def calcula_distancia_imagens_semelhantes_distintas(self,img_consulta,num_imagens_distintas):
        imgs = im.imagems()
        linha = []

        print(img_consulta)
        for img_semelhante in imgs.get_imagens_semelhantes(img_consulta):
            img_semelhante = os.path.basename(img_semelhante).replace('.jpg','')
            self.img_consulta = img_consulta
            self.img_base = img_semelhante
            linha.append(self.calcula_distancia_imagens(1))

        for img_distinta in imgs.get_imagens_distintas(img_consulta,num_imagens_distintas):
            img_distinta = os.path.basename(img_distinta).replace('.jpg','')
            self.img_consulta = img_consulta
            self.img_base = img_distinta
            linha.append(self.calcula_distancia_imagens(0))

        return linha

    def gera_arquivo_treino(self,arquivo):

        pass