package br.ufmg.weka.test;

import java.util.Arrays;

import br.ufmg.weka.conversor.WekaConversor;

public class WekaConversorTest
{
	public static void main(String[] args) throws Exception
	{
		String diretorioBase = System.getenv("HOME") + "/extrai_descritores";
		String wekaFileName = diretorioBase + "/pares.arff";
		String dataSetFileName = diretorioBase + "/pares/saida.txt";
		String[] columnsToIgnore = {"pair_id"};
		String classColumn = "class";

		WekaConversor.generateWekaFile(dataSetFileName, wekaFileName, Arrays.asList(columnsToIgnore), classColumn);
	}
}
