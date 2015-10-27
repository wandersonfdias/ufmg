imagesInputPath="imagens"

maskInputPath="./mascaras/"

maskOutputPath="./mascaras_out/"

outputDescrPath="./descritores/"

tmp="./tmp/"

# remove diretorios de descritores
if [ -d ${outputDescrPath} ]; then

	rm -r ${outputDescrPath}

fi

# cria o diretorio
mkdir -p ${outputDescrPath}

for file in `find $imagesInputPath -name *.jpg`
do
	echo "\n\n*** Extraindo descritores do arquivo: "$file" ***\n"
	file_dir=`dirname $file`"/"
	file_mask=`basename $file`
	file_mask=`basename $file .jpg`
	inputMaskName=${maskInputPath}${file_dir}${file_mask}.pgm
	inputImage=$file
	file=`basename $file`
	file=`basename $file .jpg` # retira a extensao da imagem

	#echo "file = "$file
	#echo "file_dir = "$file_dir
	#echo "inputImage = "$inputImage
	#echo "inputMaskName = "$inputMaskName
	#echo "outputDescrPath = "${outputDescrPath}

	###################
	# BIC descriptor
	###################
	descriptor="bic"
	descriptor_dir=${outputDescrPath}${descriptor}"/"${file_dir}
	descriptor_file=${descriptor_dir}${file}"."${descriptor}

	mkdir -p $descriptor_dir # garante a criacao do diretorio

	echo "\t => Extraindo descritor: "$descriptor
	./extrai_bic $inputImage $inputMaskName $descriptor_file # extrai o descritor


	###################
	# CCV descriptor
	###################
	descriptor="ccv"
	descriptor_dir=${outputDescrPath}${descriptor}"/"${file_dir}
	descriptor_file=${descriptor_dir}${file}"."${descriptor}
	descriptor_temp_dir=${tmp}"/descriptors/"${descriptor}"/"
	descriptor_temp_file=${descriptor_temp_dir}${file}"_convert."${descriptor}

	mkdir -p $descriptor_dir # garante a criacao do diretorio
	mkdir -p $descriptor_temp_dir # garante a criacao do diretorio

	echo "\t => Extraindo descritor: "$descriptor
	./extrai_ccv $inputImage $inputMaskName $descriptor_temp_file # extrai o descritor binario
	./ucharbin2txt $descriptor_temp_file $descriptor_file # converte o descritor em texto


	###################
	# GCH descriptor
	###################
	descriptor="gch"
	descriptor_dir=${outputDescrPath}${descriptor}"/"${file_dir}
	descriptor_file=${descriptor_dir}${file}"."${descriptor}
	descriptor_temp_dir=${tmp}"/descriptors/"${descriptor}"/"
	descriptor_temp_file=${descriptor_temp_dir}${file}"_convert."${descriptor}

	mkdir -p $descriptor_dir # garante a criacao do diretorio
	mkdir -p $descriptor_temp_dir # garante a criacao do diretorio

	echo "\t => Extraindo descritor: "$descriptor
	./extrai_gch $inputImage $inputMaskName $descriptor_temp_file # extrai o descritor binario
	./ucharbin2txt $descriptor_temp_file $descriptor_file # converte o descritor em texto


	###################
	# LCH descriptor
	###################
	descriptor="lch"
	descriptor_dir=${outputDescrPath}${descriptor}"/"${file_dir}
	descriptor_file=${descriptor_dir}${file}"."${descriptor}
	descriptor_temp_dir=${tmp}"/descriptors/"${descriptor}"/"
	descriptor_temp_file=${descriptor_temp_dir}${file}"_convert."${descriptor}

	mkdir -p $descriptor_dir # garante a criacao do diretorio
	mkdir -p $descriptor_temp_dir # garante a criacao do diretorio

	echo "\t => Extraindo descritor: "$descriptor
	./extrai_lch $inputImage $inputMaskName $descriptor_temp_file # extrai o descritor binario
	./ucharbin2txt $descriptor_temp_file $descriptor_file # converte o descritor em texto


	###################
	# Convert image PPM
	###################
	outputDir=${tmp}"/descriptors/ppm/"
	outputName=${outputDir}${file}".ppm"
	mkdir -p $outputDir # garante a criacao do diretorio
	convert $inputImage $outputName # converte o arquivo


	###################
	# ACC descriptor
	###################
	descriptor="acc"
	descriptor_dir=${outputDescrPath}${descriptor}"/"${file_dir}
	descriptor_file=${descriptor_dir}${file}"."${descriptor}
	descriptor_temp_dir=${tmp}"/descriptors/"${descriptor}"/"
	descriptor_temp_file=${descriptor_temp_dir}${file}"_convert."${descriptor}

	mkdir -p $descriptor_dir # garante a criacao do diretorio
	mkdir -p $descriptor_temp_dir # garante a criacao do diretorio

	echo "\t => Extraindo descritor: "$descriptor
	./extrai_acc $outputName $inputMaskName $descriptor_temp_file # extrai o descritor binario
	./ucharbin2txt $descriptor_temp_file $descriptor_file # converte o descritor em texto


	###################
	# EOAC descriptor
	###################
	descriptor="eoac"
	descriptor_dir=${outputDescrPath}${descriptor}"/"${file_dir}
	descriptor_file=${descriptor_dir}${file}"."${descriptor}

	mkdir -p $descriptor_dir # garante a criacao do diretorio

	echo "\t => Extraindo descritor: "$descriptor
	./extrai_eoac $outputName $descriptor_file # extrai o descritor


	###################
	# LAS descriptor
	###################
	descriptor="las"
	descriptor_dir=${outputDescrPath}${descriptor}"/"${file_dir}
	descriptor_file=${descriptor_dir}${file}"."${descriptor}
	descriptor_temp_dir=${tmp}"/descriptors/"${descriptor}"/"
	descriptor_temp_file=${descriptor_temp_dir}${file}"_convert."${descriptor}

	mkdir -p $descriptor_dir # garante a criacao do diretorio
	mkdir -p $descriptor_temp_dir # garante a criacao do diretorio

	echo "\t => Extraindo descritor: "$descriptor
	./extrai_las $outputName $descriptor_temp_file # extrai o descritor binario
	./floatbin2txt $descriptor_temp_file $descriptor_file # converte o descritor em texto


	###################
	# QCCH descriptor
	###################
	descriptor="qcch"
	descriptor_dir=${outputDescrPath}${descriptor}"/"${file_dir}
	descriptor_file=${descriptor_dir}${file}"."${descriptor}
	descriptor_temp_dir=${tmp}"/descriptors/"${descriptor}"/"
	descriptor_temp_file=${descriptor_temp_dir}${file}"_convert."${descriptor}

	mkdir -p $descriptor_dir # garante a criacao do diretorio
	mkdir -p $descriptor_temp_dir # garante a criacao do diretorio

	echo "\t => Extraindo descritor: "$descriptor
	./extrai_qcch $outputName $descriptor_temp_file # extrai o descritor binario
	./doublebin2txt $descriptor_temp_file $descriptor_file 0 # converte o descritor em texto


	###################
	# SID descriptor
	###################
	descriptor="sid"
	descriptor_dir=${outputDescrPath}${descriptor}"/"${file_dir}
	descriptor_file=${descriptor_dir}${file}"."${descriptor}
	descriptor_temp_dir=${tmp}"/descriptors/"${descriptor}"/"
	descriptor_temp_file=${descriptor_temp_dir}${file}"_convert."${descriptor}

	mkdir -p $descriptor_dir # garante a criacao do diretorio
	mkdir -p $descriptor_temp_dir # garante a criacao do diretorio

	echo "\t => Extraindo descritor: "$descriptor
	./extrai_sid $outputName $descriptor_temp_file # extrai o descritor binario
	./doublebin2txt $descriptor_temp_file $descriptor_file 1 # converte o descritor em texto


	###################
	# Convert image PGM
	###################
	outputDir=${tmp}"/descriptors/pgm/"
	outputName=${outputDir}${file}".pgm"
	mkdir -p $outputDir # garante a criacao do diretorio
	convert $inputImage $outputName # converte o arquivo


	###################
	# UNSER descriptor
	###################
	descriptor="unser"
	descriptor_dir=${outputDescrPath}${descriptor}"/"${file_dir}
	descriptor_file=${descriptor_dir}${file}"."${descriptor}
	descriptor_temp_dir=${tmp}"/descriptors/"${descriptor}"/"
	descriptor_temp_file=${descriptor_temp_dir}${file}"_convert."${descriptor}

	mkdir -p $descriptor_dir # garante a criacao do diretorio
	mkdir -p $descriptor_temp_dir # garante a criacao do diretorio

	echo "\t => Extraindo descritor: "$descriptor
	./extrai_unser $outputName $inputMaskName $descriptor_temp_file # extrai o descritor binario
	./ucharbin2txt $descriptor_temp_file $descriptor_file # converte o descritor em texto


	#############################
	#limpa arquivos temporarios
	rm -r ${tmp}

done

