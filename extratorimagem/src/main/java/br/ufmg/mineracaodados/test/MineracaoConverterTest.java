package br.ufmg.mineracaodados.test;

import br.ufmg.extratorimagem.exception.ProcessadorException;
import br.ufmg.mineracaodados.converter.MineracaoFileConverter;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class MineracaoConverterTest
{
	public static void main(String[] args)
	{
		try
		{
//			String[] dirs = {"s001_c0.01", "s005_c0.01", "s010_c0.01", "s025_c0.01"};
//			String[] dados = {"m2", "m3", "m4", "m5"};

			String[] dirs = {"s001_c0.01"};
			String[] dados = {"m10"};

			boolean excluirDiretorioSaida = false;
			for (String diretorio : dirs)
			{
				for (String prefix : dados)
				{
					System.out.println("Gerando dados para: " + diretorio + " - "+ prefix);
					String diretorioBase = System.getenv("HOME") + "/extrai_descritores";
					String diretorioSaida = "teste_mineracao_dataset/"+ diretorio + "/" +prefix;
					String arquivoSaidaLAC = diretorioBase + "/lac/saida/teste_mineracao/" + diretorio + "/" + prefix + ".log";

					MineracaoFileConverter converter = new MineracaoFileConverter(diretorioBase, diretorioSaida, arquivoSaidaLAC);
					converter.convert(excluirDiretorioSaida);
				}
				excluirDiretorioSaida = false;
			}
		}
		catch (ProcessadorException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
