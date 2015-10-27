package br.ufmg.extratorimagem.utils;

import java.io.File;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public final class ImageUtils
{
	private static final String EXTENSAO_IMAGEM = "jpg";

	private ImageUtils()
	{
	}

	/**
	 * Verifica se o arquivo é uma imagem válida
	 * @param file
	 * @return
	 */
	public static boolean isValidImage(File file)
	{
		boolean image = false;

		if (file != null && file.isFile() && file.exists() && file.getName().endsWith(EXTENSAO_IMAGEM))
		{
			image = true;
		}

		return image;
	}
}
