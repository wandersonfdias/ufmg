imagensInputPath="imagens"
mascaraspath="../mascaras/"
for file in `find $imagensInputPath -name *.pgm`
do
	echo "\n*** Processando arquivo $file...***\n"

	image_pgm=$file
	./gera_seeds $image_pgm > seeds.txt
	./gradient $image_pgm seeds.txt 1
	dir_grad=`dirname $image_pgm`
	file_grad=`basename $image_pgm`
	file_grad=`basename $file_grad .pgm`
	file_grad=$dir_grad"/"$file_grad'_imggrad.pgm'
	./watershed $image_pgm $file_grad seeds.txt

	#excluindo formatos desnecessarios,precisa apenas do label
	base_file_name=`basename $file_grad _imggrad.pgm`
	base_file_name=$dir_grad"/"$base_file_name
	rm -f $file_grad
	rm -f $base_file_name'_objmap.pgm'
	rm -f $base_file_name'_result.pgm'

done

if [ -d ${mascaraspath}${imagensInputPath} ]; then

	rm -r ${mascaraspath}${imagensInputPath}
fi

for file in `find $imagensInputPath -name *label*`
do
	dir=`dirname $file`
	dir=${mascaraspath}${dir}
	mkdir -p $dir # garante a criação do diretorio/subdiretorios
	mv $file $dir
done

