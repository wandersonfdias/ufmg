__author__ = 'p060848'

import os,sys
import descritores as desc
import imagens as img

if(len(sys.argv)-1 != 4):
    print("use: gera_dataset_descritores.py <path_imgs_consulta> <path_imgs_base> <path_descritores> <file>")
    sys.exit(0)

path_imagens_consulta = sys.argv[1]
path_imagens_base = sys.argv[2]
path_descritores = sys.argv[3]
file_base_dados = sys.argv[4]

if (os.path.exists(path_imagens_consulta)):
    path_imagens_consulta = path_imagens_consulta
else:
    print("Diretorio "+path_imagens_consulta+" nao encontrado!")
    exit(0)

if (os.path.exists(path_imagens_base)):
    path_imagens_base = path_imagens_base
else:
    print("Diretorio "+path_imagens_base+" nao encontrado!")
    exit(0)

if (os.path.exists(path_descritores)):
    path_descritores = path_descritores
else:
    print("Diretorio "+path_descritores+" nao encontrado!")
    exit(0)

def coleta_descritores():
    descritores = desc.descritor()
    imagens = img.imagems()

    imagens.path_imagens_consulta = path_imagens_consulta
    imagens.path_imagens_base = path_imagens_base
    descritores.path_descritores = path_descritores
 
    arquivo = open(str(file_base_dados),"w")
    arquivo.write("imgs,acc,bic,ccv,eoac,gch,las,lch,qcch,sid,unser,class\n")

    for imgs_consulta in imagens.get_imagens_consulta():
        for imgs_base in imagens.get_imagens_base():
            descritores.img_consulta = imgs_consulta
            descritores.img_base = imgs_base
            linha = descritores.calcula_distancia_imagens_consulta_base()
            linha = str(linha).replace("[","").replace("]","").replace("'","")
            print("\n" + linha)
            arquivo.write(linha)
            arquivo.write("\n")

def menu():
    print(70*"#")
    print("#\tScript para gerar dataset de descritores das imagens ")
    print(70*"#")
    print("\nEm execucao...")

menu()
coleta_descritores()
