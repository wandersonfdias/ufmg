package br.ufmg.extratorimagem.main.teste;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.ufmg.extratorimagem.exception.ProcessadorException;
import br.ufmg.extratorimagem.processador.Processador;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class TesteProcessador
{
	private static final Log LOG = LogFactory.getLog(Processador.class);

	/**
	 * Teste
	 * @param args
	 */
	public static void main(String[] args)
	{
		LOG.debug("debug");
		LOG.warn("warn");
		LOG.info("info");
		LOG.error("error");
		String diretorioBase = System.getenv("HOME") + "/extrai_descritores";
		String diretorioImagens = "imagens";
		String diretorioDescritores = "descritores";
		String diretorioSaida = "saida";

		try
		{
			Processador processador = new Processador(diretorioBase, diretorioImagens, diretorioDescritores, diretorioSaida);
			processador.processar();
		}
		catch (ProcessadorException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
