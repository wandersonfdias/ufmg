package br.ufmg.weka.test;

import br.ufmg.weka.discretize.DiscretizeDataSet;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class DiscretizeTest
{
	public static void main(String[] args) throws Exception
	{
		String diretorioBase = System.getenv("HOME") + "/extrai_descritores";
		String wekaFileName = diretorioBase + "/pares.arff";
		String wekaDiscretizedFileName = diretorioBase + "/pares_discretizados.arff";
		DiscretizeDataSet.discretize(wekaFileName, wekaDiscretizedFileName);
	}
}
