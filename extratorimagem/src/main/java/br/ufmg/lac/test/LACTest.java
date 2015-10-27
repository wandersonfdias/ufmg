package br.ufmg.lac.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import br.ufmg.extratorimagem.exception.ProcessadorException;
import br.ufmg.extratorimagem.processador.constants.ProcessadorConstants;
import weka.classifiers.rules.LAC;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class LACTest
{
	private static final int IMAGE_PAIR_ID_ATTRIBUTE = 0;

	public static void main(String[] args)
	{
		String diretorioBase = System.getenv("HOME") + "/extrai_descritores";
		String arquivoOriginal = diretorioBase + "/pares/saida.txt";
		String arquivoWeka = diretorioBase + "/pares_discretizados.arff";
		String arquivoTeste = diretorioBase + "/lac_dataset/teste";
		String arquivoTreino = diretorioBase + "/lac_dataset/treino";

		BufferedReader reader = null;
		try
		{
			Instances fullDataSet = getFullDataSet(arquivoWeka, arquivoOriginal);

			// embaralha as instâncias
			fullDataSet.randomize(new Random(System.currentTimeMillis()));

			// obtém os ids das imagens de consulta
			Set<String> queriesId = getQueriesId(fullDataSet);

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
			LinkedList<Instance> instancias = new LinkedList<Instance>();
			for (int i = 0; i < fullDataSet.numInstances(); i++)
			{
				Instance instance = fullDataSet.instance(i);
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

			ArrayList<Attribute> attributes = new ArrayList<Attribute>();
			for (int i=0; i< fullDataSet.numAttributes(); i++)
			{
				attributes.add(fullDataSet.attribute(i));
			}

			Instances dadosTreino = new Instances(fullDataSet.relationName(), attributes, 0);
			Instances dadosTeste = new Instances(fullDataSet.relationName(), attributes, 0);

			// processa dados das instâncias do weka
			for (Instance instance : instancias)
			{
				String queryId = getQueryId(instance);

				if (queriesIdTreino.contains(queryId))
				{
					dadosTreino.add(instance);
				}
				else if (queriesIdTeste.contains(queryId))
				{
					dadosTeste.add(instance);
				}
			}

			System.out.println("teste = " + dadosTreino.size());
			System.out.println("treino = " + dadosTeste.size());

//			// dados de treino
//			reader = new BufferedReader(new FileReader(arquivoTreino));
//			Instances dadosTreino = new Instances(reader);
//			dadosTreino.setClassIndex(dadosTreino.numAttributes()-1);
//			reader.close();
//
//			// dados de teste
//			reader = new BufferedReader(new FileReader(arquivoTeste));
//			Instances dadosTeste = new Instances(reader);
//			dadosTeste.setClassIndex(dadosTeste.numAttributes()-1);

			LAC lac = new LAC();
			lac.setDebug(true);
			lac.setMaxRuleSize(2);
			lac.setMinConfidence(0.01);
			lac.setMinSupport(1);
			lac.buildClassifier(dadosTreino);
			System.out.println("fim");
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	private static Instances getFullDataSet(String arquivoWeka, String arquivoOriginal) throws ProcessadorException
	{
		BufferedReader reader = null;
		try
		{
			// importa os dados do arquivo do weka
			reader = new BufferedReader(new FileReader(arquivoWeka));
			Instances data = new Instances(reader);
			data.setClassIndex(data.numAttributes()-1);

			// adiciona a coluna "pair_id" às instâncias
			addPairIdToInstances(data, arquivoOriginal);

			return data;
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
	private static void addPairIdToInstances(Instances data, String arquivoOriginal) throws IOException
	{
		if (data != null && data.numInstances() > 0)
		{
			// lê o arquivo original e adiciona o attributo que identifica o par de imagens às instâncias do dataset
			List<String> linhasArquivoOriginal = FileUtils.readLines(new File(arquivoOriginal));

			Attribute attribute = new Attribute("pair_id", (FastVector) null);
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
	private static Set<String> getQueriesId(Instances data)
	{
		// 0 qid:100 #docid = 19

		Set<String> queries = new LinkedHashSet<String>();

		for (int i = 0; i < data.numInstances(); i++)
		{
			Instance instance = data.instance(i);
			String queryId = getQueryId(instance);
			if (!queries.contains(queryId))
			{
				queries.add(queryId); // adiciona a 1a imagem do par como id de consulta
			}
		}

		return queries;
	}

	private static String getQueryId(Instance instance)
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
}
