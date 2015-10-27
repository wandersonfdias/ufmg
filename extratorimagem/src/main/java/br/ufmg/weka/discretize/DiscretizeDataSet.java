package br.ufmg.weka.discretize;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Discretize;

/**
 * Discretiza um dataset não supervisionado do weka
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class DiscretizeDataSet
{
	private static final Log LOG = LogFactory.getLog(DiscretizeDataSet.class);

	/**
	 * loads the given ARFF file and sets the class attribute as the last
	 * attribute.
	 *
	 * @param filename the file to load
	 * @throws IOException
	 */
	private static Instances load(String filename) throws IOException
	{
		Instances result;
		BufferedReader reader;

		reader = new BufferedReader(new FileReader(filename));
		result = new Instances(reader);
		result.setClassIndex(result.numAttributes() - 1);
		reader.close();

		return result;
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
		BufferedWriter writer;

		writer = new BufferedWriter(new FileWriter(filename));
		writer.write(data.toString());
		writer.newLine();
		writer.flush();
		writer.close();
	}

	/**
	 * Discretize a original weka file to a new file
	 * @param wekaFileName
	 * @param wekaDiscretizedFileName
	 * @throws Exception
	 */
	public static void discretize(String wekaFileName, String wekaDiscretizedFileName) throws Exception
	{
		try
		{
			// load data (class attribute is assumed to be last attribute)
			Instances input = load(wekaFileName);

			// setup filter
			Discretize filter = new Discretize();
			filter.setInputFormat(input);

			// apply filter
			Instances output = Filter.useFilter(input, filter);

			// save output
			save(output, wekaDiscretizedFileName);
		}
		catch (Exception e)
		{
			LOG.error(e.getMessage(), e);
			throw e;
		}

	}

	/**
	 * Takes two arguments:
	 * <ol>
	 * <li>input weka file</li>
	 * <li>output discretized weka file</li>
	 * </ol>
	 *
	 * @param args the commandline arguments
	 * @throws Exception if something goes wrong
	 */
	public static void main(String[] args) throws Exception
	{
		discretize(args[0], args[1]);
	}
}
