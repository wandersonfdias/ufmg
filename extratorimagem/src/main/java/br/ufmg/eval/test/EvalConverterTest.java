package br.ufmg.eval.test;

import br.ufmg.eval.converter.EvalFileConverter;
import br.ufmg.extratorimagem.exception.ProcessadorException;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class EvalConverterTest
{
	public static void main(String[] args)
	{
		try
		{
			String[] dados = {"m2"}; //, "m3", "m4", "m5"};

			for (String prefix : dados)
			{
				System.out.println("Gerando dados para: "+ prefix);
				String diretorioBase = System.getenv("HOME") + "/extrai_descritores";
				String diretorioSaida = "eval_dataset/"+prefix;
//				String arquivoSaidaLAC = diretorioBase + "/lac/saida/" + prefix + ".log";
				String arquivoSaidaLAC = diretorioBase + "/lac/saida/teste_mineracao/s001_c0.01/" + prefix + "_prediction.log";
				String arquivoEntradaTesteLAC = diretorioBase + "/lac_dataset/teste";

				EvalFileConverter converter = new EvalFileConverter(diretorioBase, diretorioSaida, arquivoSaidaLAC, arquivoEntradaTesteLAC);
				converter.convert();

			}
		}
		catch (ProcessadorException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
