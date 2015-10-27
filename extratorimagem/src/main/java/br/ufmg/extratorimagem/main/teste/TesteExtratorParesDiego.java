package br.ufmg.extratorimagem.main.teste;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.ufmg.extratorimagem.exception.ProcessadorException;
import br.ufmg.extratorimagem.extratorpares.ExtratorPares;
import br.ufmg.extratorimagem.extratorpares.dto.ParDTO;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class TesteExtratorParesDiego
{
	private static final Log LOG = LogFactory.getLog(TesteExtratorParesDiego.class);

	private static final int TOTAL_PARES_A_GERAR = 100000*1;

	private static Map<String, Boolean> controlePares;

	/**
	 * Teste
	 * @param args
	 */
	public static void main(String[] args)
	{
		LOG.info("Iniciando processamento...");

		String diretorioBase = System.getenv("HOME") + "/extrai_descritores";
		String diretorioImagens = "imagens";
		String diretorioDescritores = "descritores";
		String diretorioSaida = "pares";
		String arquivoPares = diretorioBase + "/diego/tag-classes-reduced.dat";
		controlePares = new HashMap<String, Boolean>();

		try
		{
			LOG.info("Montando pares de imagens...");
			List<ParDTO> pares = getParesImagens(diretorioBase, arquivoPares);

			ExtratorPares extrator = new ExtratorPares(diretorioBase, diretorioImagens, diretorioDescritores, diretorioSaida);
			extrator.processar(pares);
		}
		catch (ProcessadorException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LOG.info("Fim...");
	}

	private static List<ParDTO> getParesImagens(String diretorioBase, String arquivoPares) throws IOException
	{
		List<ParDTO> pares = new ArrayList<ParDTO>();

		FileReader fr = null;
		BufferedReader br = null;
		LineNumberReader lnr = null;

		try
		{
			fr = new FileReader(arquivoPares);
			br = new BufferedReader(fr);
			lnr = new LineNumberReader(br);

			int totalLineNumbers = getTotalLineNumbers(arquivoPares);
			Set<Integer> lineNumbers = getLineNumbers(totalLineNumbers);

			final String diretorioImagens = "imagens/imagens_diego";

			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				int lineNumber = lnr.getLineNumber();
				if (lineNumbers.contains(lineNumber))
				{
					String[] dadosLinha = StringUtils.split(line, StringUtils.SPACE);

					String[] dadosImagem1 = dadosLinha[0].split("/");
					String imagem1 = dadosImagem1[dadosImagem1.length-1];

					String[] dadosImagem2 = dadosLinha[1].split("/");
					String imagem2 = dadosImagem2[dadosImagem2.length-1];

					// verifica se o par existe
					if (existsPair(imagem1, imagem2))
					{
						continue;
					}

					// adiciona o par no mapa para controle
					controlePares.put(getPairKey(imagem1, imagem2, false), true);
					controlePares.put(getPairKey(imagem1, imagem2, true), true);

					imagem1 = getPathImagem(diretorioBase, diretorioImagens, imagem1);
					imagem2 = getPathImagem(diretorioBase, diretorioImagens, imagem2);
					int classe = Integer.valueOf(dadosLinha[2]);

					pares.add(new ParDTO(new File(imagem1), new File(imagem2), classe));
				}
			}

			// libera memória
			controlePares = new HashMap<String, Boolean>();

			return pares;
		}
		finally
		{
			IOUtils.closeQuietly(lnr);
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(fr);
		}
	}

	/**
	 * Gera a chave do par de imagem
	 * @param key1
	 * @param key2
	 * @param inverted
	 * @return
	 */
	private static String getPairKey(String key1, String key2, boolean inverted)
	{
		return new StringBuilder().append((!inverted ? key1 : key2)).append("_").append((!inverted ? key2 : key1)).toString();
	}

	/**
	 * Verifica se o par da imagem já existe
	 * @param key1
	 * @param key2
	 * @return
	 */
	private static boolean existsPair(String key1, String key2)
	{
		boolean exists = false;

		String key = getPairKey(key1, key2, false);
		exists = controlePares.containsKey(key);
		if (!exists)
		{
			String invertedKey = getPairKey(key1, key2, true);
			exists = controlePares.containsKey(invertedKey);
		}

		return exists;
	}

	private static int getTotalLineNumbers(String arquivoPares) throws IOException
	{
		int total = 0;

		FileReader fr = null;
		BufferedReader br = null;
		LineNumberReader lnr = null;

		try
		{
			File file = new File(arquivoPares);
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			lnr = new LineNumberReader(br);
			lnr.skip(file.length());

			total = lnr.getLineNumber();
		}
		finally
		{
			IOUtils.closeQuietly(lnr);
			IOUtils.closeQuietly(br);
			IOUtils.closeQuietly(fr);
		}

		return total;
	}

	private static Set<Integer> getLineNumbers(int totalLineNumbers)
	{
		Random random = new Random(System.currentTimeMillis());

		Set<Integer> lines = new HashSet<Integer>();
		do
		{
			lines.add(random.nextInt((totalLineNumbers-1)));
		}
		while (lines.size() < TOTAL_PARES_A_GERAR);

		return lines;
	}

	/**
	 * Obtém o path base para processamento
	 * @param dir
	 * @return
	 */
	private static String getPathImagem(String diretorioBase, String diretorioImagens, String imagem)
	{
		return new StringBuilder(diretorioBase).append(File.separator).append(diretorioImagens).append(File.separator).append(imagem).toString();
	}
}
