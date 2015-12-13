package br.ufmg.extratorimagem.extratorpares;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.ufmg.extratorimagem.constants.CommonConstants;
import br.ufmg.extratorimagem.exception.DescriptorsNotFoundException;
import br.ufmg.extratorimagem.exception.ImagesNotFoundException;
import br.ufmg.extratorimagem.exception.ProcessadorException;
import br.ufmg.extratorimagem.exception.ProcessadorImagemException;
import br.ufmg.extratorimagem.extratorpares.dto.ParDTO;
import br.ufmg.extratorimagem.processador.constants.ProcessadorConstants;
import br.ufmg.extratorimagem.utils.ProcessadorUtils;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ExtratorPares
{
	private static final MathContext MATH_CONTEXT = new MathContext(ProcessadorConstants.DESCRIPTOR_PRECISION_VALUE);

	private static final int TOTAL_PARES_A_GERAR = 100000;

	private static final Log LOG = LogFactory.getLog(ExtratorPares.class);

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

	private Map<String, Boolean> pares;
	private Random randomGenerator;

	private static int contador = 0;

	/**
	 * Construtor padrão
	 * @param diretorioBase
	 * @param diretorioImagens
	 * @param diretorioDescritores
	 */
	public ExtratorPares(String diretorioBase, String diretorioImagens, String diretorioDescritores, String diretorioSaida)
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
	 * Inicializa variáveis para processamento
	 */
	private void inicializaVariavies()
	{
		this.pares = new HashMap<String, Boolean>();
		this.randomGenerator = new Random();
		this.randomGenerator.setSeed(System.currentTimeMillis());
		contador = 0;
	}

	/**
	 * Inicia o processamento das imagens e gera o arquivo de saída
	 * @throws ProcessadorException
	 */
	public void processar() throws ProcessadorException
	{
		this.inicializaVariavies();

		if (!this.isValidParameters())
		{
			throw new ProcessadorException(String.format("Parâmetros de entrada informados inválidos. Diretório base: '%s' -  Diretório imagens: '%s' -  Diretório descritores: '%s' - Diretório saída: '%s'.", this.diretorioBase, this.diretorioImagens, this.diretorioDescritores, this.diretorioSaida));
		}
		else
		{
			// obtém a lista de imagens
			List<File> imagens = this.getImages();
			if (imagens == null || imagens.isEmpty())
			{
				throw new ImagesNotFoundException("Não foi encontrado nenhuma imagem para processamento.");
			}
			else
			{
				// obtém a lista de descritores
				List<File> descriptors = this.getDescriptors();
				if (descriptors == null || descriptors.isEmpty())
				{
					throw new DescriptorsNotFoundException("Não foi encontrado nenhum descritor de imagens.");
				}
				else
				{
					// monta a lista de pares únicos de imagens
					List<ParDTO> pares = this.getParesImagens(imagens);

					// processa os pares
					this.processar(pares, descriptors);
				}
			}
		}
	}

	/**
	 * Inicia o processamento das imagens e gera o arquivo de saída
	 * @param pares
	 * @throws ProcessadorException
	 */
	public void processar(List<ParDTO> pares) throws ProcessadorException
	{
		this.inicializaVariavies();

		if (!this.isValidParameters())
		{
			throw new ProcessadorException(String.format("Parâmetros de entrada informados inválidos. Diretório base: '%s' -  Diretório imagens: '%s' -  Diretório descritores: '%s' - Diretório saída: '%s'.", this.diretorioBase, this.diretorioImagens, this.diretorioDescritores, this.diretorioSaida));
		}
		else
		{
			if (pares == null || pares.isEmpty())
			{
				throw new ImagesNotFoundException("Não foi encontrado nenhum par de imagens para processamento.");
			}
			else
			{
				// obtém a lista de descritores
				List<File> descriptors = this.getDescriptors();
				if (descriptors == null || descriptors.isEmpty())
				{
					throw new DescriptorsNotFoundException("Não foi encontrado nenhum descritor de imagens.");
				}
				else
				{
					// processa os pares
					this.processar(pares, descriptors);
				}
			}
		}
	}

	/**
	 * @param imagens
	 * @return
	 */
	private List<ParDTO> getParesImagens(List<File> imagens)
	{
		// monta a lista de pares únicos de imagens
		List<ParDTO> pares = new LinkedList<ParDTO>(); // mantém a ordem da lista
		do
		{
			Map<Integer, Integer> randomImagePair = this.getRandomImagePair(imagens.size());
			Entry<Integer, Integer> entry = randomImagePair.entrySet().iterator().next();
			Integer indexImage1 = entry.getKey();
			Integer indexImage2 = entry.getValue();

			File imagem1 = imagens.get(indexImage1);
			File imagem2 = imagens.get(indexImage2);
			pares.add(new ParDTO(imagem1, imagem2, this.getClasse(imagem1, imagem2)));
		}
		while (pares.size() < TOTAL_PARES_A_GERAR);

		return pares;
	}

	/**
	 * Obtém a classe da imagem
	 * @param imagem1
	 * @param imagem2
	 * @return
	 */
	private int getClasse(File imagem1, File imagem2)
	{
		int classe = 0;

		// se o diretório pai for o mesmo, assume-se que é da mesma classe
		if (imagem1.getParent().equalsIgnoreCase(imagem2.getParent()))
		{
			classe = 1;
		}

		return classe;
	}

	/**
	 * Processa os pares de imagens
	 * @param imagens
	 * @param descritores
	 * @throws ProcessadorException
	 */
	private void processar(final List<ParDTO> pares, final List<File> descriptors) throws ProcessadorException
	{
		LOG.info("Processando pares...");

		final ExecutorService poolThead = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		final List<ProcessadorImagemException> errosProcessamento = new ArrayList<ProcessadorImagemException>();

		for (final ParDTO par : pares)
		{
			if (LOG.isDebugEnabled())
			{
				System.out.printf("Processando par: %s - %s", par.getImagem1().getPath(), par.getImagem2().getPath());
				System.out.println();
			}

			poolThead.submit(new Runnable()
			{
				public void run()
				{
					try
					{
						// processa o par de imagens
						ProcessadorParImagem processadorParImagem = new ProcessadorParImagem(par, descriptors, diretorioBase);
						processadorParImagem.processar();
						processadorParImagem = null;
						contador++;
					}
					catch (ProcessadorImagemException e)
					{
						errosProcessamento.add(e);
					}
				}
			});
		}

		// Força a finalização do pool, sem cancelar as threads já iniciadas.
		poolThead.shutdown();
		int tempo = 1;
		do
		{
			try
			{
				if (tempo%5==0)
				{
					System.out.println("Pares processados: " + contador + " - " + tempo + "s");
				}
				Thread.sleep(1000);
				tempo++;
			}
			catch (InterruptedException e)
			{
				// ignora
			}
		}
		while(!poolThead.isTerminated());
		System.out.println("Pares processados: " + contador + " - " + tempo + "s");

		if (errosProcessamento.size() > 0)
		{
			LOG.error("Processamento Cancelado!!! Vide erros abaixo:");
			for (ProcessadorImagemException e : errosProcessamento)
			{
				LOG.error(e.getMessage(), e);
			}
			System.exit(1); // força sinal de erro
		}
		else
		{
			// normaliza os valores dos descritores dos pares
			LOG.info("Normalizando descritores dos pares...");
			this.normalizar(pares, descriptors);

			// grava o arquivo de saída
			this.writeOutputFile(pares, descriptors);
		}

		LOG.info("Fim do processamento");
	}

	/**
	 * Grava o arquivo de saída contendo os pares
	 * @param pares
	 * @param descriptors
	 * @throws ProcessadorException
	 */
	private void writeOutputFile(List<ParDTO> pares, List<File> descriptors) throws ProcessadorException
	{
		File arquivoSaida = this.createOutputFile();

		LOG.info(String.format("Gerando arquivo de saída: '%s'.", arquivoSaida.getPath()));

		// cria o cabeçalho do arquivo
		this.createHeaderFile(arquivoSaida, descriptors);

		FileOutputStream out = null;
		try
		{
			out = FileUtils.openOutputStream(arquivoSaida, true);
			final BufferedOutputStream buffer = new BufferedOutputStream(out);

			for (ParDTO par: pares)
			{
				try
				{
					StringBuilder line = new StringBuilder();
					line.append(this.getImagePairId(par));
					line.append(ProcessadorConstants.FIELD_SEPARATOR);
					line.append(this.getDescriptorsLine(par, descriptors));
					line.append(ProcessadorConstants.FIELD_SEPARATOR);
					line.append(par.getClasse());

					List<String> data = new ArrayList<String>();
					data.add(line.toString());
//					FileUtils.writeLines(arquivoSaida, data, ProcessadorConstants.LINE_SEPARATOR, true);
					IOUtils.writeLines(data, ProcessadorConstants.LINE_SEPARATOR, buffer, Charsets.UTF_8);
					buffer.flush();
				}
				catch (IOException e)
				{
					throw new ProcessadorException(String.format("Ocorreu um erro ao gravar linha no arquivo de saída '%s'.",  arquivoSaida.getAbsolutePath()), e);
				}
			}

			buffer.flush();
			out.close();
		}
		catch (IOException e)
		{
			throw new ProcessadorException(String.format("Ocorreu um erro ao gravar linha no arquivo de saída '%s'.",  arquivoSaida.getAbsolutePath()), e);
		}
		finally
		{
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * Monta a linha de descritores
	 * @param par
	 * @param descriptors
	 * @return
	 */
	private String getDescriptorsLine(ParDTO par, List<File> descriptors)
	{
		StringBuilder line = new StringBuilder();

		for (File descriptorFile : descriptors)
		{
			String descriptorName = descriptorFile.getName();

			if (line.length() > 0)
			{
				line.append(ProcessadorConstants.FIELD_SEPARATOR);
			}
			line.append(par.getDistancias().get(descriptorName));
		}

		return line.toString();
	}

	/**
	 * Normaliza os valores dos descritores
	 * @param pares
	 * @param descriptors
	 */
	private void normalizar(List<ParDTO> pares, List<File> descriptors)
	{
		for (File descriptorFile : descriptors)
		{
			String descriptorName = descriptorFile.getName();
			List<BigDecimal> valores = new ArrayList<BigDecimal>();
			for (ParDTO par: pares)
			{
				valores.add(par.getDistancias().get(descriptorName));
			}

			BigDecimal minValue = Collections.min(valores);
			BigDecimal maxValue = Collections.max(valores);

			for (ParDTO par: pares)
			{
				BigDecimal currentValue = par.getDistancias().get(descriptorName);
				BigDecimal normalizedValue = this.normalize(currentValue, minValue, maxValue);
				par.getDistancias().put(descriptorName, normalizedValue);
			}
		}
	}

	/**
	 * Normaliza um valor considerando o máximo/mínimo existente
	 * @param currentValue
	 * @param minValue
	 * @param maxValue
	 * @return
	 */
	private BigDecimal normalize(BigDecimal currentValue, BigDecimal minValue, BigDecimal maxValue)
	{
		/*
		 * FÓRMULA PARA NORMALIZAÇÃO: (currentValue - minValue) / (maxValue - minValue)
		 */

		// (maxValue - minValue)
		BigDecimal divisor = new BigDecimal(maxValue.doubleValue(), MATH_CONTEXT);
		divisor = divisor.subtract(minValue);

		BigDecimal newValue = new BigDecimal(currentValue.doubleValue(), MATH_CONTEXT);
		newValue = newValue.subtract(minValue); // (currentValue - minValue)
		newValue = newValue.divide(divisor, MATH_CONTEXT); // (currentValue - minValue) / (maxValue - minValue)

		return newValue;
	}

	/**
	 * Obtém o id para o par de imagens // TODO Wanderson - trocar os names por hashcode, após a demonstração
	 * @param par
	 * @return
	 */
	private String getImagePairId(ParDTO par)
	{
		StringBuilder sb = new StringBuilder();

		sb.append(par.getImagem1().getParentFile().getName());
		sb.append(".");
		sb.append(ProcessadorUtils.getNameWithoutExtension(par.getImagem1().getName()));
		sb.append(ProcessadorConstants.PAIR_ID_SEPARATOR);
		sb.append(par.getImagem2().getParentFile().getName());
		sb.append(".");
		sb.append(ProcessadorUtils.getNameWithoutExtension(par.getImagem2().getName()));

		return sb.toString();
	}

	/**
	 * Gera um par de imagem único e aleatório
	 * @param totalImagens
	 * @return
	 */
	private Map<Integer, Integer> getRandomImagePair(int totalImagens)
	{
		int imageKey1 = 0;
		int imageKey2 = 0;
		do
		{
			imageKey1 = this.randomGenerator.nextInt(totalImagens-1);
			imageKey2 = this.randomGenerator.nextInt(totalImagens-1);
		}
		while (existsPair(imageKey1, imageKey2));


		Map<Integer, Integer> pair = new HashMap<Integer, Integer>();
		pair.put(imageKey1, imageKey2);

		// adiciona o par no mapa para controle
		this.pares.put(this.getPairKey(imageKey1, imageKey2, false), true);
		this.pares.put(this.getPairKey(imageKey1, imageKey2, true), true);

		return pair;
	}

	/**
	 * Gera a chave do par de imagem
	 * @param key1
	 * @param key2
	 * @param inverted
	 * @return
	 */
	private String getPairKey(Integer key1, Integer key2, boolean inverted)
	{
		return new StringBuilder().append((!inverted ? key1 : key2)).append("_").append((!inverted ? key2 : key1)).toString();
	}

	/**
	 * Verifica se o par da imagem já existe
	 * @param key1
	 * @param key2
	 * @return
	 */
	private boolean existsPair(Integer key1, Integer key2)
	{
		boolean exists = false;

		String key = this.getPairKey(key1, key2, false);
		exists = this.pares.containsKey(key);
		if (!exists)
		{
			String invertedKey = this.getPairKey(key1, key2, true);
			exists = this.pares.containsKey(invertedKey);
		}

		return exists;
	}

	/**
	 * Obtém as imagens
	 * @return
	 * @throws ImagesNotFoundException
	 */
	private List<File> getImages() throws ImagesNotFoundException
	{
		try
		{
			String fullPathImagem = this.getFullPathImagem();
			Collection<File> files = FileUtils.listFiles(new File(fullPathImagem), new String[]{CommonConstants.EXTENSAO_IMAGEM}, true);
			return ((files != null && !files.isEmpty()) ? new LinkedList<File>(files) : null); // mantém a ordem da lista
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
			List<File> lista = new LinkedList<File>(); // mantém a ordem da lista
			String fullPathDescriptors = this.getFullPathDescritores();

			File directory = new File(fullPathDescriptors);
			FileFilter fileFilter = new FileFilter()
			{
				public boolean accept(File pathname)
				{
					// só aceita diretórios
					return pathname.isDirectory();
				}
			};

			File[] diretorios = directory.listFiles((FileFilter) fileFilter);

			if (diretorios != null && diretorios.length > 0)
			{
				lista.addAll(Arrays.asList(diretorios));
			}

			return (lista.isEmpty() ? null : lista);
		}
		catch (IllegalArgumentException e)
		{
			throw new DescriptorsNotFoundException("Não foi encontrado nenhum descritor de imagens.", e);
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
	 * Cria o cabeçalho do arquivo
	 * @param arquivoSaida
	 * @param imagens
	 * @param descriptors
	 * @throws ProcessadorException
	 */
	private void createHeaderFile(File arquivoSaida, List<File> descriptors) throws ProcessadorException
	{
		StringBuilder line = new StringBuilder();
		line.append("pair_id");

		for (File descriptorFile : descriptors)
		{
			line.append(ProcessadorConstants.FIELD_SEPARATOR);
			line.append(descriptorFile.getName());
		}

		line.append(ProcessadorConstants.FIELD_SEPARATOR);
		line.append("class");

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
