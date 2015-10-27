imagesInputPath="imagens"
imagensPgmOutputPath="imagens_pgm/imagens/"

rm -r $imagensPgmOutputPath

for file in `find $imagesInputPath -name *.jpg`
do
	#convert image to pgm
	inputImageName=$file
	fileDirOutput=`dirname $file`
	fileDirOutput=`expr "$fileDirOutput" : '[^/]*/\(.*\)'` # remove o primeiro diretorio da imagem
	fileOutput=`basename $file .jpg`".pgm" # gera o novo arquivo com nome da imagem, trocando sua extensao para .pgm
	directoryOutput=${imagensPgmOutputPath}${fileDirOutput}
	outputImageName=${directoryOutput}"/"${fileOutput}

	mkdir -p ${directoryOutput} # garante que a estrutura de diretorios/subdiretorios exista
	convert -compress none $inputImageName $outputImageName # converte a imagem original em pgm
done

