path="$PWD"

# remove mascaras
for file in `find ${path}/mascaras/ -name *.pgm`
do
	rm $file 2> /dev/null
done

# remove imagens segmentadas
for file in `find ${path}/imagens_pgm/ -name *.pgm`
do
	rm $file 2> /dev/null
done

# remove diretorio temporario
rm -r ${path}/tmp 2> /dev/null
