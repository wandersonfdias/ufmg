package br.ufmg.extratorimagem.utils;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public final class ProcessadorUtils
{
	private ProcessadorUtils()
	{
	}

	public static String getHeaderName(String imageName, String descriptorName)
	{
		final String separator = "_";
		StringBuilder header = new StringBuilder();

		header.append(abreviateImageName(imageName));
		header.append(separator);
		header.append(descriptorName);

		return header.toString();
	}

	public static  String abreviateImageName(String imageName)
	{
		final String prefix = "i";
		String name = removeCaracteresNaoNumericos(imageName);

		return new StringBuilder(prefix).append(name).toString();
	}

	public static  String removeCaracteresNaoNumericos(String valor)
	{
		return (valor == null ? StringUtils.EMPTY : valor.replaceAll("[^0-9]", StringUtils.EMPTY));
	}

	public static  String getNameWithoutExtension(String name)
	{
		return StringUtils.split(name, '.')[0];
	}

	public static  String getDirWithoutBaseDir(File file, String baseDir)
	{
		if (file != null)
		{
			String dir = file.getParent().substring((file.getParent().indexOf(baseDir) + baseDir.length()));
			if (dir != null && dir.length() > 0)
			{
				return (dir.startsWith("/") ? dir.substring(1) : dir);
			}
		}
		return null;
	}
}
