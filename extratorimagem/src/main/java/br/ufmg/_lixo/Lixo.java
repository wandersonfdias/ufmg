package br.ufmg._lixo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import br.ufmg.extratorimagem.constants.CommonConstants;
import br.ufmg.extratorimagem.exception.ImagesNotFoundException;

public class Lixo
{
	private static BigInteger fatorial(int i)
	{
		BigInteger valor = new BigInteger(String.valueOf(i));
		for (int j = (i-1); j>1; j--)
		{
			valor = valor.multiply(new BigInteger(String.valueOf(j)));
		}

		return valor;
	}

	public static void main(String[] args)
	{
//		int total = 8165;
//		int elementos = 2;
//		BigInteger denominador = fatorial(elementos);
//		denominador = denominador.multiply(fatorial(total-elementos));
//
//		BigInteger combinacoes = fatorial(total).divide(denominador);
//
//		System.out.println(combinacoes);
//		return;

		// TODO Auto-generated method stub
		String diretorioBase = System.getenv("HOME") + "/extrai_descritores";
		String diretorioImagens = diretorioBase + "/imagens";
		String arquivoTag = diretorioBase + "/diego/tag-classes-reduced.dat";

		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try
		{
			fileReader = new FileReader(arquivoTag);
			bufferedReader = new BufferedReader(fileReader);
			Set<String> imagens = new HashSet<String>();

			String linha = null;
			while ((linha = bufferedReader.readLine()) != null)
			{
				if (StringUtils.isNotBlank(linha))
				{
					String[] dadosLinha = StringUtils.split(linha, StringUtils.SPACE);

					String[] dadosImagem1 = dadosLinha[0].split("/");
					String imagem1 = dadosImagem1[dadosImagem1.length-1];

					String[] dadosImagem2 = dadosLinha[1].split("/");
					String imagem2 = dadosImagem2[dadosImagem2.length-1];

					imagens.add(imagem1);
					imagens.add(imagem2);
				}
				else
				{
					break;
				}
			}

			List<File> images = getImages(diretorioImagens);
			Set<String> imagensRemover = new HashSet<String>();
			for (File image : images)
			{
				if (!imagens.contains(image.getName()))
				{
					System.out.println(image.getName());
					// remove a imagem
//					image.delete();
				}
			}

			System.out.println("fim");
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			IOUtils.closeQuietly(fileReader);
			IOUtils.closeQuietly(bufferedReader);
		}
	}

	/**
	 * Obtém as imagens
	 * @return
	 * @throws ImagesNotFoundException
	 */
	private static List<File> getImages(String diretorioImagens)
	{
		Collection<File> files = FileUtils.listFiles(new File(diretorioImagens), new String[]{CommonConstants.EXTENSAO_IMAGEM}, true);
		return ((files != null && !files.isEmpty()) ? new LinkedList<File>(files) : null); // mantém a ordem da lista
	}
}
