#!/bin/bash
hora=$(date +"%H:%M:%S")
path="$PWD"
log="${path}/log.txt"

echo "***** [$hora] Script iniciado. *****" >> ${log}

echo "[$hora] Convertendo imagens para pgm.." >> ${log}
sh ${path}/convert_pgm.sh

echo "[$hora] Segmentando imagens.." >> ${log}
cd ${path}/imagens_pgm
sh ${path}/imagens_pgm/segmenta.sh

echo "[$hora] Renomeando imagens.." >> ${log}
cd ${path}/mascaras
sh ${path}/mascaras/renomeia.sh

echo "[$hora] Executando descritores.." >> ${log}
cd ${path}
sh ${path}/run_descriptors.sh

echo "[$hora] Limpando mascaras de imagens e temporarios " >> ${log}
#sh ${path}/limpa.sh

# PARTE DO PYTHON NAO EH UTILIZADA MAIS!!!
##echo "[$hora] Criando base de dados: dataset_descritores.txt)" >> ${log}
##python gera_dataset_descritores.py imagens_consulta imagens #descritores dataset_descritores.txt

echo "[$hora] Script Finalizado" >> ${log}

