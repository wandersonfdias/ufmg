package br.ufmg.extratorimagem.main.teste;

import br.ufmg.extratorimagem.exception.ProcessadorException;
import br.ufmg.extratorimagem.extratorpares.ExtratorPares;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class TesteExtratorPares
{
	/**
	 * Teste
	 * @param args
	 */
	public static void main(String[] args)
	{
		String diretorioBase = System.getenv("HOME") + "/extrai_descritores";
		String diretorioImagens = "imagens";
		String diretorioDescritores = "descritores";
		String diretorioSaida = "pares";

		try
		{
			ExtratorPares extrator = new ExtratorPares(diretorioBase, diretorioImagens, diretorioDescritores, diretorioSaida);
			extrator.processar();
		}
		catch (ProcessadorException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
