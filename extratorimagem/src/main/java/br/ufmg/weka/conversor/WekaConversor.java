package br.ufmg.weka.conversor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import br.ufmg.extratorimagem.processador.constants.ProcessadorConstants;

/**
 * Generates a little ARFF file with numeric attribute types.
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class WekaConversor
{
	private static final Log LOG = LogFactory.getLog(WekaConversor.class);

	/**
	 * Gera o arquivo ARFF de um dataset de dados numéricos
	 * @param dataSetFileName Arquivo de dados para conversão
	 * @param wekaFileName Arquivo de saída
	 * @param columnsToIgnore Nomes das colunas (não numéricas) a serem ignoradas na geração do arquivo.
	 * @param classColumnName Nome da coluna que representa a classe.
	 * @throws Exception
	 */
	public static void generateWekaFile(String dataSetFileName, String wekaFileName, List<String> columnsToIgnore, String classColumnName) throws Exception
	{
		FileReader fr = null;
		BufferedReader br = null;
		try
		{
			fr = new FileReader(dataSetFileName);
			br = new BufferedReader(fr);

			Instances instances = null;
			ArrayList<Attribute> attributes = new ArrayList<Attribute>();

			String line = null;
			Set<String> distinctClasses = new LinkedHashSet<String>();
			List<String> instanceClasses = new ArrayList<String>();
			List<Integer> indexColumnsToIgnore = new ArrayList<Integer>();
			int fileClassColumnIndex = -1;
			int currentLine = 0;

			while ((line = br.readLine()) != null)
			{
				if (StringUtils.isBlank(line))
				{
					break;
				}

				String[] lineData = StringUtils.split(line, ProcessadorConstants.FIELD_SEPARATOR);

				if (currentLine == 0)
				{
					// processa a coluna de cabeçalho
					int columnIndex = 0;
					for (String column : lineData)
					{
						if (columnsToIgnore != null && columnsToIgnore.contains(column))
						{
							indexColumnsToIgnore.add(columnIndex);
						}

						columnIndex++;
					}

					// monta atributos das instâncias
					for (int i=0; i<lineData.length; i++)
					{
						if (classColumnName != null && classColumnName.equalsIgnoreCase(lineData[i]))
						{
							fileClassColumnIndex = i;
						}
						else if (!indexColumnsToIgnore.contains(i))
						{
							attributes.add(new Attribute(lineData[i]));
						}
					}

					// cria dataset
					String[] relationName = StringUtils.split(wekaFileName, '/'); // deixa só o nome final do arquivo como relation name
					instances = new ConversorInstances(relationName[relationName.length-1], attributes, 0);
				}
				else
				{
					double[] instanceValues = new double[instances.numAttributes()];
					int index = 0;
					String classValue = null;
					for (int i=0; i<lineData.length; i++)
					{
						if (!indexColumnsToIgnore.contains(i))
						{
							if (i == fileClassColumnIndex)
							{
								classValue = lineData[i];
								instanceClasses.add(classValue);
								distinctClasses.add(classValue);
							}
							else
							{
								instanceValues[index] = new Double(lineData[i]);
							}
							index++;
						}
					}

					// adiciona nova instância
					instances.add(new DenseInstance(1.0, instanceValues));
				}

				currentLine++;
			}

			// ordena as classes
			ArrayList<String> classes = new ArrayList<String>(distinctClasses);
			Collections.sort(classes);

			// adiciona a coluna da classe às instâncias
			int classIndex = instances.numAttributes();
			instances.insertAttributeAt(new Attribute(classColumnName, classes), classIndex);
			instances.setClassIndex(classIndex);

			// adiciona os valores das classes das respectivas instâncias
			for (int i=0; i<instances.size(); i++)
			{
				instances.get(i).setClassValue(instanceClasses.get(i));
			}

			// grava as instâncias no arquivo
			save(instances, wekaFileName);
		}
		catch (Exception e)
		{
			LOG.error(e.getMessage(), e);
			throw e;
		}
		finally
		{
			IOUtils.closeQuietly(fr);
			IOUtils.closeQuietly(br);
		}
	}

	/**
	 * saves the data to the specified file
	 *
	 * @param data the data to save to a file
	 * @param filename the file to save the data to
	 * @throws IOException
	 */
	private static void save(Instances data, String filename) throws IOException
	{
		FileWriter fw = null;
		BufferedWriter bw = null;

		try
		{
			fw = new FileWriter(filename);
			bw = new BufferedWriter(fw);
			bw.write(data.toString());
			bw.newLine();
			bw.flush();
			bw.close();
		}
		finally
		{
			IOUtils.closeQuietly(fw);
			IOUtils.closeQuietly(bw);
		}
	}
}
class ConversorInstances extends Instances
{
	private static final long serialVersionUID = 396632412736273459L;

	public ConversorInstances(String name, ArrayList<Attribute> attInfo, int capacity)
	{
		super(name, attInfo, capacity);
	}

	/**
	 * @see weka.core.Instances#stringWithoutHeader()
	 */
	@Override
	protected String stringWithoutHeader()
	{
		StringBuffer text = new StringBuffer();

		for (int i = 0; i < numInstances(); i++)
		{
			text.append(instance(i).toStringMaxDecimalDigits(15)); // converte a instância considerando um limite de casas decimais
			if (i < numInstances() - 1)
			{
				text.append('\n');
			}
		}
		return text.toString();
	}
}
