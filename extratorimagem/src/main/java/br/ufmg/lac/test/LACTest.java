package br.ufmg.lac.test;

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

import weka.classifiers.rules.LAC;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import br.ufmg.extratorimagem.exception.ProcessadorException;
import br.ufmg.extratorimagem.processador.constants.ProcessadorConstants;
import br.ufmg.lac.converter.LACQueryFileConverter;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class LACTest
{
	private static final Log LOG = LogFactory.getLog(LACTest.class);

	public static void main(String[] args)
	{
		String diretorioBase = System.getenv("HOME") + "/extrai_descritores/tp_agrupamentos";
		String arquivoParesOriginalTreino = diretorioBase + "/dados_base_completa/pares/pares.txt";
		String arquivoParesDiscretizadosTreino = diretorioBase + "/dados_base_completa/pares/pares_discretizados.arff";
		String arquivoParesOriginalTeste = diretorioBase + "/dados_base_consulta/pares/pares.txt";
		String arquivoParesDiscretizadosTeste = diretorioBase + "/dados_base_consulta/pares/pares_discretizados.arff";

		BufferedReader reader = null;
		try
		{
			LOG.info("[LEITURA INSTANCIAS TREINO E TESTE] INICIO");
			Instances intanciasTreino = getFullDataSet(arquivoParesDiscretizadosTreino, arquivoParesOriginalTreino);
			Instances intanciasTeste = getFullDataSet(arquivoParesDiscretizadosTeste, arquivoParesOriginalTeste);
			LOG.info("[LEITURA INSTANCIAS TREINO E TESTE] FIM");

			System.out.println("treino = " + intanciasTreino.size());
			System.out.println("teste = " + intanciasTeste.size());


			LAC lac = new LAC();
			lac.setDebug(true);
			lac.setMaxRuleSize(2);
			lac.setMinConfidence(0.01);
			lac.setMinSupport(1);

			LOG.info("[LAC - BUILD CLASSIFIER] INICIO");
			lac.buildClassifier(intanciasTreino);
			LOG.info("[LAC - BUILD CLASSIFIER] FIM");

			for (Instance testeInstance : intanciasTeste)
			{
				double[] distributionForInstance = lac.distributionForInstance(testeInstance);
//				System.out.println(distributionForInstance[0]);
			}

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
//			addPairIdToInstances(data, arquivoOriginal);

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
}
