for file in `find . -name *.pgm`
do

	mv "$file" "${file%\_label.pgm}.pgm" # retira o _label dos nomes dos arquivos

done
