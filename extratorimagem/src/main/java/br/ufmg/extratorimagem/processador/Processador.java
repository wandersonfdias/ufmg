package br.ufmg.extratorimagem.processador;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.ufmg.extratorimagem.constants.CommonConstants;
import br.ufmg.extratorimagem.exception.DescriptorsNotFoundException;
import br.ufmg.extratorimagem.exception.ImagesNotFoundException;
import br.ufmg.extratorimagem.exception.ProcessadorException;
import br.ufmg.extratorimagem.processador.constants.ProcessadorConstants;
import br.ufmg.extratorimagem.utils.ProcessadorUtils;

/**
 * Processador principal de imagens e descritores
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class Processador
{
	private static final Log LOG = LogFactory.getLog(Processador.class);

	/**
	 * Diretório base para processamento das imagens
	 */
	private String diretorioBase;

	/**
	 * Diretório base das imagens
	 */
	private String diretorioImagens;

	/**
	 * Diretório base dos descritores
	 */
	private String diretorioDescritores;

	/**
	 * Diretório dos arquivos de saída
	 */
	private String diretorioSaida;

	/**
	 * Construtor padrão
	 * @param diretorioBase
	 * @param diretorioImagens
	 * @param diretorioDescritores
	 * @param diretorioSaida
	 */
	public Processador(String diretorioBase, String diretorioImagens, String diretorioDescritores, String diretorioSaida)
	{
		super();
		this.diretorioBase = StringUtils.trimToNull(diretorioBase);
		this.diretorioImagens = StringUtils.trimToNull(diretorioImagens);
		this.diretorioDescritores = StringUtils.trimToNull(diretorioDescritores);
		this.diretorioSaida = StringUtils.trimToNull(diretorioSaida);
	}

	/**
	 * Valida os parâmetros de entrada
	 * @return
	 */
	private boolean isValidParameters()
	{
		return (StringUtils.isNotBlank(this.diretorioBase) && StringUtils.isNotBlank(this.diretorioImagens)
				&& StringUtils.isNotBlank(this.diretorioDescritores) && StringUtils.isNotBlank(this.diretorioSaida));
	}

	/**
	 * Inicia o processamento das imagens e gera o arquivo de saída
	 * @throws ImagesNotFoundException
	 */
	public void processar() throws ProcessadorException
	{
		if (!this.isValidParameters())
		{
			throw new ProcessadorException(String.format("Parâmetros de entrada informados inválidos. Diretório base: '%s' -  Diretório imagens: '%s' -  Diretório descritores: '%s' -  Diretório saída: '%s'", this.diretorioBase, this.diretorioImagens, this.diretorioDescritores, this.diretorioSaida));
		}
		else
		{
			Collection<File> imagens = this.getImages();
			if (imagens == null || imagens.isEmpty())
			{
				throw new ImagesNotFoundException("Não foi encontrado nenhuma imagem para processamento.");
			}
			else
			{
				List<File> descriptors = this.getDescriptors();
				if (descriptors == null || descriptors.isEmpty())
				{
					throw new DescriptorsNotFoundException("Não foi encontrado nenhum descritor de imagens.");
				}
				else
				{
					try
					{
						// processa as imagens (uso de LinkedList é para preservar a ordem dos itens da lista)
						this.processar(new LinkedList<File>(imagens), new LinkedList<File>(descriptors));
					}
					catch (ProcessadorException e)
					{
						LOG.error(e.getMessage(), e);
						throw e;
					}
				}
			}
		}
	}

	/**
	 * Processa as imagens e descritores
	 * @param imagens
	 * @param descritores
	 * @throws ProcessadorException
	 */
	private void processar(List<File> images, List<File> descriptors) throws ProcessadorException
	{
		File arquivoSaida = this.createOutputFile();

		// cria o cabeçalho do arquivo
		this.createHeaderFile(arquivoSaida, images, descriptors);

		// processa as imagens
		for (File imageFile : images)
		{
			if (LOG.isDebugEnabled())
			{
				System.out.printf("Processando imagem: %s", imageFile.getName());
				System.out.println();
			}

			ProcessadorImagem processadorImagem = new ProcessadorImagem(imageFile, images, descriptors);
			String line = processadorImagem.processar();

			try
			{
				List<String> data = new ArrayList<String>();
				data.add(line.toString());
				FileUtils.writeLines(arquivoSaida, data, ProcessadorConstants.LINE_SEPARATOR, true);
			}
			catch (IOException e)
			{
				throw new ProcessadorException(String.format("Ocorreu um erro ao gravar linha no arquivo de saída '%s'.",  arquivoSaida.getAbsolutePath()), e);
			}
		}
	}

	/**
	 * Cria o cabeçalho do arquivo
	 * @param arquivoSaida
	 * @param imagens
	 * @param descriptors
	 * @throws ProcessadorException
	 */
	private void createHeaderFile(File arquivoSaida, List<File> imagens, List<File> descriptors) throws ProcessadorException
	{
		StringBuilder line = new StringBuilder();

		for (File imageFile : imagens)
		{
			String imageName = ProcessadorUtils.getNameWithoutExtension(imageFile.getName());

			for (File descriptorFile : descriptors)
			{
				String descriptorName = descriptorFile.getName();
				if (line.length() > 0)
				{
					line.append(ProcessadorConstants.FIELD_SEPARATOR);
				}
				line.append(ProcessadorUtils.getHeaderName(imageName, descriptorName));
			}
		}

		try
		{
			List<String> data = new ArrayList<String>();
			data.add(line.toString());
			FileUtils.writeLines(arquivoSaida, data, ProcessadorConstants.LINE_SEPARATOR, true);
		}
		catch (IOException e)
		{
			throw new ProcessadorException(String.format("Ocorreu um erro ao gravar o cabeçalho no arquivo de saída '%s'.", arquivoSaida.getAbsolutePath()), e);
		}
	}

	/**
	 * Cria o arquivo de sáida
	 * @return
	 * @throws ProcessadorException
	 */
	private File createOutputFile() throws ProcessadorException
	{
		String path = this.getFullPathSaida();
		File dir = new File(path);

		if (dir.exists())
		{
			try
			{
				// remove o diretório recursivamente
				FileUtils.deleteDirectory(dir);
			}
			catch (IOException e)
			{
				throw new ProcessadorException(String.format("Ocorreu um erro ao excluir o diretório de saída '%s'.", path), e);
			}
		}

		// cria o diretório
		boolean dirCreated = dir.mkdir();
		if (!dirCreated)
		{
			throw new ProcessadorException(String.format("Não foi possível criar o diretório de saída '%s'.", path));
		}

		String arquivo = new StringBuilder(path).append(ProcessadorConstants.ARQUIVO_SAIDA).toString();
		File file = new File(arquivo);
		file.setWritable(true);
		file.setReadable(true);

		try
		{
			boolean created = file.createNewFile();
			if (!created)
			{
				throw new ProcessadorException(String.format("Não foi possível criar o arquivo de saída '%s'.", arquivo));
			}
		}
		catch (IOException e)
		{
			throw new ProcessadorException(String.format("Não foi possível criar o arquivo de saída '%s'.", arquivo), e);
		}

		return file;
	}

	/**
	 * Obtém as imagens
	 * @return
	 * @throws ImagesNotFoundException
	 */
	private Collection<File> getImages() throws ImagesNotFoundException
	{
		try
		{
			String fullPathImagem = this.getFullPathImagem();
			return FileUtils.listFiles(new File(fullPathImagem), new String[]{CommonConstants.EXTENSAO_IMAGEM}, false); // não pesquisa em subdiretórios
		}
		catch (IllegalArgumentException e)
		{
			throw new ImagesNotFoundException("Não foi encontrado nenhuma imagem para processamento.", e);
		}
	}

	/**
	 * Obtém os descritores
	 * @return
	 * @throws DescriptorsNotFoundException
	 */
	private List<File> getDescriptors() throws DescriptorsNotFoundException
	{
		try
		{
			List<File> lista = new LinkedList<File>();

			String fullPathDescriptors = this.getFullPathDescritores();

			IOFileFilter filter = new IOFileFilter()
			{
				public boolean accept(File dir, String name)
				{
					return accept(dir);
				}

				public boolean accept(File file)
				{
					// só aceita diretórios
					return file.isDirectory();
				}
			};

			Iterator<File> iterator = FileUtils.iterateFilesAndDirs(new File(fullPathDescriptors), filter, filter);
			while (iterator.hasNext())
			{
				File file = iterator.next();
				if (file != null && !file.getName().equalsIgnoreCase(this.diretorioDescritores))
				{
					lista.add(file);
				}
			}

			return lista;
		}
		catch (IllegalArgumentException e)
		{
			throw new DescriptorsNotFoundException("Não foi encontrado nenhum descritor de imagens.", e);
		}
	}

	/**
	 * Obtém o path completo referente ao diretório das imagens
	 * @return
	 */
	private String getFullPathImagem()
	{
		return this.getFullPath(this.diretorioImagens);
	}

	/**
	 * Obtém o path completo referente ao diretório dos descritores
	 * @return
	 */
	private String getFullPathDescritores()
	{
		return this.getFullPath(this.diretorioDescritores);
	}

	/**
	 * Obtém o path completo referente ao diretório de saída
	 * @return
	 */
	private String getFullPathSaida()
	{
		return this.getFullPath(this.diretorioSaida);
	}

	/**
	 * Obtém o path base para processamento
	 * @param dir
	 * @return
	 */
	private String getFullPath(String dir)
	{
		return new StringBuilder(this.diretorioBase).append(File.separator).append(dir).append(File.separator).toString();
	}
}
