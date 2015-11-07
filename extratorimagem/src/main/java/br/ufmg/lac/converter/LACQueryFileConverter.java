package br.ufmg.lac.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import br.ufmg.extratorimagem.exception.ProcessadorException;
import br.ufmg.extratorimagem.processador.constants.ProcessadorConstants;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class LACQueryFileConverter
{
	private static final Log LOG = LogFactory.getLog(LACQueryFileConverter.class);

	private static final String FIELD_SEPARATOR = " ";
	private static final int IMAGE_PAIR_ID_ATTRIBUTE = 0;
	private static final String ARQUIVO_TREINO = "treino";
	private static final String ARQUIVO_TESTE = "teste";

	private String arquivoWeka;

	/**
	 * Diretório base para processamento das imagens
	 */
	private String diretorioBase;

	/**
	 * Diretório dos arquivos de saída
	 */
	private String diretorioSaida;

	/**
	 * Arquivo original que resultou no arquivo do weka
	 */
	private String arquivoOriginal;

	public LACQueryFileConverter(String diretorioBase, String diretorioSaida, String arquivoWeka, String arquivoOriginal)
	{
		this.diretorioBase = diretorioBase;
		this.diretorioSaida = diretorioSaida;
		this.arquivoWeka = arquivoWeka;
		this.arquivoOriginal = arquivoOriginal;
	}

	/**
	 * Converte o arquivo do WEKA em arquivos de treino/teste do LAC
	 * @throws ProcessadorException
	 */
	public void convert() throws ProcessadorException
	{
		BufferedReader reader = null;
		try
		{
			LOG.info("Iniciando conversão...");

			// importa os dados do arquivo do weka
			LOG.info("Importando os dados do arquivo do weka...");
			reader = new BufferedReader(new FileReader(this.arquivoWeka));
			Instances data = new Instances(reader);
			data.setClassIndex(data.numAttributes()-1);

			// adiciona a coluna "pair_id" às instâncias
			this.addPairIdToInstances(data);

			// embaralha as instâncias
			data.randomize(new Random(System.currentTimeMillis()));

			// obtém os ids das imagens de consulta
			Set<String> queriesId = this.getQueriesId(data);

			// cria o diretório de saída
			this.createOutputDir();

			// cria os arquivos de treino/teste
			File trainingFile = this.createOutputFile(ARQUIVO_TREINO);
			File testFile = this.createOutputFile(ARQUIVO_TESTE);

			// separa percentuais para treino e teste
			final int percentualTreino = 80;
			final int percentualTeste = 100 - percentualTreino;

			int qtdeTreino = percentualTreino / 10;
			int qtdeTeste = percentualTeste / 10;

			if (qtdeTreino%2 == 0 && qtdeTeste%2 == 0)
			{
				qtdeTreino = qtdeTreino/2;
				qtdeTeste = qtdeTeste/2;
			}
			else if (qtdeTreino%3 == 0 && qtdeTeste%3 == 0)
			{
				qtdeTreino = qtdeTreino/3;
				qtdeTeste = qtdeTeste/3;
			}

			int qtdeTempSeparadaTreino = 0;
			int qtdeTempSeparadaTeste = 0;

			Set<String> queriesIdTreino = new LinkedHashSet<String>();
			Set<String> queriesIdTeste = new LinkedHashSet<String>();

			// separa queriesId para treino e teste
			LOG.info("Separando queriesId para treino e teste...");
			Iterator<String> iterator = queriesId.iterator();
			while (iterator.hasNext())
			{
				boolean adicionarTreino = (qtdeTempSeparadaTreino < qtdeTreino);
				boolean adicionarTeste = (!adicionarTreino && (qtdeTempSeparadaTeste < qtdeTeste));

				if (!adicionarTreino && !adicionarTeste)
				{
					qtdeTempSeparadaTreino = 0;
					qtdeTempSeparadaTeste = 0;
					adicionarTreino = true;
				}

				String queryId = iterator.next();

				if (adicionarTreino)
				{
					queriesIdTreino.add(queryId);
					qtdeTempSeparadaTreino++;
				}
				else if (adicionarTeste)
				{
					queriesIdTeste.add(queryId);
					qtdeTempSeparadaTeste++;
				}
			}

			// prepara e ordena as instâncias pela coluna "pair_id"
			LOG.info("Preparando e ordenando as instâncias pela coluna 'pair_id'...");
			LinkedList<Instance> instancias = new LinkedList<Instance>();
			for (int i = 0; i < data.numInstances(); i++)
			{
				Instance instance = data.instance(i);
				instancias.add(instance);
			}
			Collections.sort(instancias, new Comparator<Instance>()
			{
				public int compare(Instance o1, Instance o2)
				{
					String pairId1 = o1.toString(IMAGE_PAIR_ID_ATTRIBUTE);
					String pairId2 = o2.toString(IMAGE_PAIR_ID_ATTRIBUTE);
					return pairId1.compareToIgnoreCase(pairId2);
				}
			});

			// processa dados das instâncias do weka
			LOG.info("Processando dados das instâncias do weka e gravando arquivos de treino/teste...");
			for (Instance instance : instancias)
			{
				String queryId = this.getQueryId(instance);

				// obtém a linha da instância
				String line = this.getLine(instance);

				if (queriesIdTreino.contains(queryId))
				{
					// grava a linha de treino
					this.writeLine(trainingFile, line);
				}
				else if (queriesIdTeste.contains(queryId))
				{
					// grava a linha de teste
					this.writeLine(testFile, line);
				}
			}

			LOG.info("Fim...");
		}
		catch (FileNotFoundException e)
		{
			throw new ProcessadorException(e.getMessage(), e);
		}
		catch (IOException e)
		{
			throw new ProcessadorException(e.getMessage(), e);
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					// quiet close
				}
			}
		}
	}

	/**
	 * Adiciona a coluna "pair_id" às instâncias
	 * @param data
	 * @throws IOException
	 */
	private void addPairIdToInstances(Instances data) throws IOException
	{
		if (data != null && data.numInstances() > 0)
		{
			// lê o arquivo original e adiciona o attributo que identifica o par de imagens às instâncias do dataset
			List<String> linhasArquivoOriginal = FileUtils.readLines(new File(this.arquivoOriginal));

			Attribute attribute = new Attribute("pair_id", (List<String>) null);
			data.insertAttributeAt(attribute, 0);

			for (int i = 0; i < data.numInstances(); i++)
			{
				String linha = linhasArquivoOriginal.get((i+1)); // desconsidera a linha do cabeçalho
				String[] dadosLinha = linha.split(ProcessadorConstants.FIELD_SEPARATOR);
				String pairId = dadosLinha[0];

				Instance instance = data.instance(i);
				instance.setValue(0, pairId);
			}
			linhasArquivoOriginal = null;
		}
	}

	/**
	 * Obtém um conjunto de imagens de consulta
	 * @param data
	 * @return
	 */
	private Set<String> getQueriesId(Instances data)
	{
		// 0 qid:100 #docid = 19

		Set<String> queries = new LinkedHashSet<String>();

		for (int i = 0; i < data.numInstances(); i++)
		{
			Instance instance = data.instance(i);
			String queryId = this.getQueryId(instance);
			if (!queries.contains(queryId))
			{
				queries.add(queryId); // adiciona a 1a imagem do par como id de consulta
			}
		}

		return queries;
	}

	private String getQueryId(Instance instance)
	{
		String queryId = null;

		String pairId = instance.toString(IMAGE_PAIR_ID_ATTRIBUTE); // atributo que identifica o par de imagens
		if (StringUtils.isNotBlank(pairId))
		{
			String[] ids = StringUtils.split(pairId, ProcessadorConstants.PAIR_ID_SEPARATOR);
			queryId = ids[0];
		}

		return queryId;
	}

	/**
	 * Monta a linha convertida para o LAC através de uma instância do WEKA
	 * @param instance
	 * @return
	 */
	private String getLine(Instance instance)
	{
		// formato linha: 4547504 CLASS=0 w[1]='\'(-inf-0.000007]\'' w[2]='\'(-inf-0.000001]\'' w[3]='\'(-inf-0.600775]\'' w[4]='\'(4.5-inf)\'' w[5]='\'(-inf-0.1875]\'' w[6]='\'(0.598611-0.999802]\''
		StringBuilder line = new StringBuilder();
		line.append(instance.toString(IMAGE_PAIR_ID_ATTRIBUTE)); // atributo que identifica o par de imagens
		line.append(FIELD_SEPARATOR);
		line.append(this.getClass(instance));
		line.append(FIELD_SEPARATOR);

		int attributeNumber = 1;
		for (int j = 1; j < instance.numAttributes(); j++)
		{
			if (!instance.attribute(attributeNumber).isString() && j != instance.classIndex())
			{
				line.append(this.getAttributeValue(instance, j, attributeNumber));
				line.append(FIELD_SEPARATOR);
				attributeNumber++;
			}
		}
		return line.toString();
	}

	/**
	 * Obtém a classe da instância
	 * @param instance
	 * @return
	 */
	private String getClass(Instance instance)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("CLASS=");
		sb.append(instance.stringValue(instance.classIndex()));

		return sb.toString();
	}

	/**
	 * Obtém um atributo de uma instância
	 * @param instance
	 * @param attributeIndex
	 * @param attributeNumber
	 * @return
	 */
	private String getAttributeValue(Instance instance, int attributeIndex, int attributeNumber)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("w[");
		sb.append(attributeNumber);
		sb.append("]=");
		sb.append(instance.toString(attributeIndex));

		return sb.toString();
	}

	private void writeLine(File file, String line) throws IOException
	{
		List<String> data = new ArrayList<String>();
		data.add(line);
		FileUtils.writeLines(file, data, ProcessadorConstants.LINE_SEPARATOR, true);
	}

	private void createOutputDir() throws ProcessadorException
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
	}

	/**
	 * Cria o arquivo de sáida
	 * @return
	 * @throws ProcessadorException
	 */
	private File createOutputFile(String fileName) throws ProcessadorException
	{
		String path = this.getFullPathSaida();

		String arquivo = new StringBuilder(path).append(fileName).toString();
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
