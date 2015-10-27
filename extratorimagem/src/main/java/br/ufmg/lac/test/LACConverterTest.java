package br.ufmg.lac.test;

import br.ufmg.extratorimagem.exception.ProcessadorException;
import br.ufmg.lac.converter.LACFileConverter;


/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class LACConverterTest
{
	public static void main(String[] args)
	{
		try
		{
			String diretorioBase = System.getenv("HOME") + "/extrai_descritores";
			String diretorioSaida = "lac_dataset";
			String arquivoWeka = diretorioBase + "/pares_discretizados.arff";
			String arquivoOriginal = diretorioBase + "/pares/saida.txt";

			LACFileConverter converter = new LACFileConverter(diretorioBase, diretorioSaida, arquivoWeka, arquivoOriginal);
			converter.convert();
		}
		catch (ProcessadorException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
