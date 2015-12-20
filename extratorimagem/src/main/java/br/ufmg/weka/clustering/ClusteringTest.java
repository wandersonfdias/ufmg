package br.ufmg.weka.clustering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import weka.clusterers.AbstractClusterer;
import weka.clusterers.Canopy;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Cobweb;
import weka.clusterers.DBSCAN;
import weka.clusterers.EM;
import weka.clusterers.FarthestFirst;
import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.TechnicalInformationHandler;
import weka.core.converters.ArffLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.gui.explorer.ClustererAssignmentsPlotInstances;
import weka.gui.visualize.VisualizePanel;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ClusteringTest
{
	private static final Log LOG = LogFactory.getLog(ClusteringTest.class);

	private static boolean READ_SERIALIZED_CLUSTERERS = true; // TODO Wanderson

	public static void main(String args[]) throws Exception
	{
	    String diretorioBase = System.getenv("HOME") + "/extrai_descritores/tp_agrupamentos";
	    String diretorioPares = diretorioBase + "/dados_base_completa/pares/";
	    String arquivoWeka = diretorioPares + "/pares_discretizados.arff";
//	    String arquivoWeka = diretorioPares + "/teste_dbscan.arff";

	    String diretorioTeste = diretorioBase + "/dados_base_consulta/pares/";
	    String arquivoTesteWeka = diretorioTeste + "/pares_discretizados.arff";


		startKMeansTest(diretorioBase, diretorioPares, arquivoWeka, diretorioTeste, arquivoTesteWeka);
	    startKMeansWithCanopyTest(diretorioBase, diretorioPares, arquivoWeka, diretorioTeste, arquivoTesteWeka);
		startEMTest(diretorioBase, diretorioPares, arquivoWeka, diretorioTeste, arquivoTesteWeka);

		startCobwebTest(diretorioBase, diretorioPares, arquivoWeka, diretorioTeste, arquivoTesteWeka);
//		startDBScanTest(diretorioBase, diretorioPares, arquivoWeka, diretorioTeste, arquivoTesteWeka);

		startCanopyTest(diretorioBase, diretorioPares, arquivoWeka, diretorioTeste, arquivoTesteWeka);
//		startHierarchicalClustererTest(diretorioBase, diretorioPares, arquivoWeka, diretorioTeste, arquivoTesteWeka);

		startFarthestFirstTest(diretorioBase, diretorioPares, arquivoWeka, diretorioTeste, arquivoTesteWeka);
	}

	private static Instances getTestInstances(String arquivoTeste) throws Exception
	{
		Instances data = DataSource.read(arquivoTeste);
		data.deleteAttributeAt(data.numAttributes()-1); // remove o atributo referente à classe

		return data;
	}

	private static void startKMeansTest(String diretorioBase, String diretorioPares, String arquivoWeka, String diretorioTeste, String arquivoTesteWeka) throws Exception, IOException
	{
		LOG.info("\n\nSTARTING KMEANS TEST...");
		String arquivoClusterWeka = diretorioPares + "/pares_discretizados_cluster_kmeans.arff";
		String arquivoResultadoCluster= diretorioPares + "/resultado_cluster_kmeans.txt";
		String arquivoClusterTesteWeka = diretorioTeste + "/pares_discretizados_cluster_kmeans.arff";
		String arquivoResultadoClusterTeste = diretorioTeste + "/resultado_cluster_kmeans.txt";
		String clustererModelFile = diretorioPares + "/cluster_kmeans.model";

		BufferedReader reader = null;
		try
		{
			// importa os dados do arquivo do weka
			reader = new BufferedReader(new FileReader(arquivoWeka));
			Instances data = new Instances(reader);
			data.deleteAttributeAt(data.numAttributes()-1); // remove o atributo referente à classe

			SimpleKMeans clusterer = null;
			if (READ_SERIALIZED_CLUSTERERS)
			{
				File file = new File(clustererModelFile);
				if (file.isFile() && file.exists())
				{
					// obtém o cluster serializado anteriormente
					clusterer = readSerializedCluster(clustererModelFile);
				}
			}

			if (clusterer == null)
			{
				// Scheme:weka.clusterers.SimpleKMeans -V -N 5 -A "weka.core.EuclideanDistance -R first-last" -I 500 -O -S 10
				clusterer = new SimpleKMeans();
				clusterer.setDebug(true);
				clusterer.setDisplayStdDevs(true);
				clusterer.setNumClusters(5);
				clusterer.setPreserveInstancesOrder(true);
				clusterer.setSeed(100);
				clusterer.setDistanceFunction(new EuclideanDistance());
				clusterer.setMaxIterations(500);
				clusterer.setDontReplaceMissingValues(false);
				clusterer.setNumExecutionSlots(4);

				LOG.info("BUILD CLUSTERER START");
				clusterer.buildClusterer(data);
				LOG.info("BUILD CLUSTERER END");

				// serializa o cluster num arquivo, para testes posteriores
				serializeCluster(clusterer, clustererModelFile);
			}

			// gera saída para o treino
			ClusterEvaluation ce = new ClusterEvaluation();
			ce.setClusterer(clusterer);
			ce.evaluateClusterer(data);
			generateClusterOutput(arquivoClusterWeka, arquivoResultadoCluster, data, clusterer, ce);

			// classifica instâncias de teste conforme cluster gerado com base no treino
			classifyTestInstances(clusterer, data, arquivoTesteWeka, arquivoClusterTesteWeka, arquivoResultadoClusterTeste);
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			IOUtils.closeQuietly(reader);
		}

		LOG.info("ENDING OF KMEANS TEST...");
	}

	private static void startKMeansWithCanopyTest(String diretorioBase, String diretorioPares, String arquivoWeka, String diretorioTeste, String arquivoTesteWeka) throws Exception, IOException
	{
		LOG.info("\n\nSTARTING KMEANS WITH CANOPY TEST...");
		String arquivoClusterWeka = diretorioPares + "/pares_discretizados_cluster_kmeans_with_canopy.arff";
		String arquivoResultadoCluster= diretorioPares + "/resultado_cluster_kmeans_with_canopy.txt";
		String arquivoClusterTesteWeka = diretorioTeste + "/pares_discretizados_cluster_kmeans_with_canopy.arff";
		String arquivoResultadoClusterTeste = diretorioTeste + "/resultado_cluster_kmeans_with_canopy.txt";
		String clustererModelFile = diretorioPares + "/cluster_kmeans_with_canopy.model";

		BufferedReader reader = null;
		try
		{
			// importa os dados do arquivo do weka
			reader = new BufferedReader(new FileReader(arquivoWeka));
			Instances data = new Instances(reader);
			data.deleteAttributeAt(data.numAttributes()-1); // remove o atributo referente à classe

			SimpleKMeans clusterer = null;
			if (READ_SERIALIZED_CLUSTERERS)
			{
				File file = new File(clustererModelFile);
				if (file.isFile() && file.exists())
				{
					// obtém o cluster serializado anteriormente
					clusterer = readSerializedCluster(clustererModelFile);
				}
			}

			if (clusterer == null)
			{
				clusterer = new SimpleKMeans();
				clusterer.setReduceNumberOfDistanceCalcsViaCanopies(true);
				clusterer.setDebug(true);
				clusterer.setDisplayStdDevs(true);
				clusterer.setNumClusters(5);
				clusterer.setPreserveInstancesOrder(true);
				clusterer.setSeed(100);
				clusterer.setDistanceFunction(new EuclideanDistance());
				clusterer.setMaxIterations(500);
				clusterer.setDontReplaceMissingValues(false);
				clusterer.setNumExecutionSlots(4);

				LOG.info("BUILD CLUSTERER START");
				clusterer.buildClusterer(data);
				LOG.info("BUILD CLUSTERER END");

				// serializa o cluster num arquivo, para testes posteriores
				serializeCluster(clusterer, clustererModelFile);
			}

			// gera saída para o treino
			ClusterEvaluation ce = new ClusterEvaluation();
			ce.setClusterer(clusterer);
			ce.evaluateClusterer(data);
			generateClusterOutput(arquivoClusterWeka, arquivoResultadoCluster, data, clusterer, ce);

			// classifica instâncias de teste conforme cluster gerado com base no treino
			classifyTestInstances(clusterer, data, arquivoTesteWeka, arquivoClusterTesteWeka, arquivoResultadoClusterTeste);
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			IOUtils.closeQuietly(reader);
		}

		LOG.info("ENDING OF KMEANS WITH CANOPY TEST...");
	}

	private static void startEMTest(String diretorioBase, String diretorioPares, String arquivoWeka, String diretorioTeste, String arquivoTesteWeka) throws Exception, IOException
	{
		LOG.info("\n\nSTARTING EM TEST...");

		String arquivoClusterWeka = diretorioPares + "/pares_discretizados_cluster_em.arff";
		String arquivoResultadoCluster= diretorioPares + "/resultado_cluster_em.txt";
		String arquivoClusterTesteWeka = diretorioTeste + "/pares_discretizados_cluster_em.arff";
		String arquivoResultadoClusterTeste = diretorioTeste + "/resultado_cluster_em.txt";
		String clustererModelFile = diretorioPares + "/cluster_em.model";

		BufferedReader reader = null;
		try
		{
			// importa os dados do arquivo do weka
			reader = new BufferedReader(new FileReader(arquivoWeka));
			Instances data = new Instances(reader);
			data.deleteAttributeAt(data.numAttributes()-1); // remove o atributo referente à classe

			EM clusterer = null;
			if (READ_SERIALIZED_CLUSTERERS)
			{
				File file = new File(clustererModelFile);
				if (file.isFile() && file.exists())
				{
					// obtém o cluster serializado anteriormente
					clusterer = readSerializedCluster(clustererModelFile);
				}
			}

			if (clusterer == null)
			{
				clusterer = new EM();
				clusterer.setNumClusters(5);
				clusterer.setSeed(100);
				clusterer.setMaxIterations(500);
				clusterer.setNumExecutionSlots(4);
				clusterer.setNumFolds(5);

				LOG.info("BUILD CLUSTERER START");
				clusterer.buildClusterer(data);
				LOG.info("BUILD CLUSTERER END");

				// serializa o cluster num arquivo, para testes posteriores
				serializeCluster(clusterer, clustererModelFile);
			}

			// gera saída para o treino
			ClusterEvaluation ce = new ClusterEvaluation();
			ce.setClusterer(clusterer);
			ce.evaluateClusterer(data);

			generateClusterOutput(arquivoClusterWeka, arquivoResultadoCluster, data, clusterer, ce);

			// classifica instâncias de teste conforme cluster gerado com base no treino
			classifyTestInstances(clusterer, data, arquivoTesteWeka, arquivoClusterTesteWeka, arquivoResultadoClusterTeste);
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			IOUtils.closeQuietly(reader);
		}

		LOG.info("ENDING OF EM TEST...");
	}

	private static void startCobwebTest(String diretorioBase, String diretorioPares, String arquivoWeka, String diretorioTeste, String arquivoTesteWeka) throws Exception, IOException
	{
		LOG.info("\n\nSTARTING COBWEB TEST...");

		String arquivoClusterWeka = diretorioPares + "/pares_discretizados_cluster_cobweb.arff";
		String arquivoResultadoCluster= diretorioPares + "/resultado_cluster_cobweb.txt";
		String arquivoClusterTesteWeka = diretorioTeste + "/pares_discretizados_cluster_cobweb.arff";
		String arquivoResultadoClusterTeste = diretorioTeste + "/resultado_cluster_cobweb.txt";
		String clustererModelFile = diretorioPares + "/cluster_cobweb.model";

		BufferedReader reader = null;
		try
		{
			// importa os dados do arquivo do weka
			reader = new BufferedReader(new FileReader(arquivoWeka));
			Instances data = new Instances(reader);
			data.deleteAttributeAt(data.numAttributes()-1); // remove o atributo referente à classe

			Cobweb clusterer = null;
			if (READ_SERIALIZED_CLUSTERERS)
			{
				File file = new File(clustererModelFile);
				if (file.isFile() && file.exists())
				{
					// obtém o cluster serializado anteriormente
					clusterer = readSerializedCluster(clustererModelFile);
				}
			}

			if (clusterer == null)
			{
				clusterer = new Cobweb();
				clusterer.setSeed(100);
				clusterer.setAcuity(1.0);
				clusterer.setCutoff(0.1915);

				LOG.info("BUILD CLUSTERER START");
				ArffLoader loader = new ArffLoader();
				loader.setFile(new File(arquivoWeka));
				data = loader.getStructure();
				data.deleteAttributeAt(data.numAttributes()-1); // remove o atributo referente à classe
				clusterer.buildClusterer(data);
				Instance current;
				int totalInstances = 0;
				while ((current = loader.getNextInstance(data)) != null)
				{
					clusterer.updateClusterer(current);
					totalInstances++;
					if (totalInstances%1000==0)
					{
						System.out.println(totalInstances);
					}
				}
				clusterer.updateFinished();
				LOG.info("BUILD CLUSTERER END");

				// serializa o cluster num arquivo, para testes posteriores
				serializeCluster(clusterer, clustererModelFile);
			}

			// gera saída para o treino
			ClusterEvaluation ce = new ClusterEvaluation();
			ce.setClusterer(clusterer);
			ce.evaluateClusterer(data);
			generateClusterOutput(arquivoClusterWeka, arquivoResultadoCluster, data, clusterer, ce);

			// classifica instâncias de teste conforme cluster gerado com base no treino
			classifyTestInstances(clusterer, data, arquivoTesteWeka, arquivoClusterTesteWeka, arquivoResultadoClusterTeste);
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			IOUtils.closeQuietly(reader);
		}

		LOG.info("ENDING OF COBWEB TEST...");
	}

	private static void startDBScanTest(String diretorioBase, String diretorioPares, String arquivoWeka, String diretorioTeste, String arquivoTesteWeka) throws Exception, IOException
	{
		LOG.info("\n\nSTARTING DBSCAN TEST...");

		String arquivoClusterWeka = diretorioPares + "/pares_discretizados_cluster_dbscan.arff";
		String arquivoResultadoCluster= diretorioPares + "/resultado_cluster_dbscan.txt";
		String arquivoClusterTesteWeka = diretorioTeste + "/pares_discretizados_cluster_dbscan.arff";
		String arquivoResultadoClusterTeste = diretorioTeste + "/resultado_cluster_dbscan.txt";
		String clustererModelFile = diretorioPares + "/cluster_dbscan.model";

		BufferedReader reader = null;
		try
		{
			// importa os dados do arquivo do weka
			reader = new BufferedReader(new FileReader(arquivoWeka));
			Instances data = new Instances(reader);
			data.deleteAttributeAt(data.numAttributes()-1); // remove o atributo referente à classe

			DBSCAN clusterer = null;
			if (READ_SERIALIZED_CLUSTERERS)
			{
				File file = new File(clustererModelFile);
				if (file.isFile() && file.exists())
				{
					// obtém o cluster serializado anteriormente
					clusterer = readSerializedCluster(clustererModelFile);
				}
			}

			if (clusterer == null)
			{
				clusterer = new DBSCAN();
				clusterer.setEpsilon(0.6d);
				clusterer.setMinPoints(5);

				LOG.info("BUILD CLUSTERER START");
				clusterer.buildClusterer(data);
				LOG.info("BUILD CLUSTERER END");

				// serializa o cluster num arquivo, para testes posteriores
				serializeCluster(clusterer, clustererModelFile);
			}

			// gera saída para o treino
			ClusterEvaluation ce = new ClusterEvaluation();
			ce.setClusterer(clusterer);
			ce.evaluateClusterer(data);
			generateClusterOutput(arquivoClusterWeka, arquivoResultadoCluster, data, clusterer, ce);

			// classifica instâncias de teste conforme cluster gerado com base no treino
			classifyTestInstances(clusterer, data, arquivoTesteWeka, arquivoClusterTesteWeka, arquivoResultadoClusterTeste);
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			IOUtils.closeQuietly(reader);
		}

		LOG.info("ENDING OF DBSCAN TEST...");
	}

	private static void startCanopyTest(String diretorioBase, String diretorioPares, String arquivoWeka, String diretorioTeste, String arquivoTesteWeka) throws Exception, IOException
	{
		LOG.info("\n\nSTARTING CANOPY TEST...");

		String arquivoClusterWeka = diretorioPares + "/pares_discretizados_cluster_canopy.arff";
		String arquivoResultadoCluster= diretorioPares + "/resultado_cluster_canopy.txt";
		String arquivoClusterTesteWeka = diretorioTeste + "/pares_discretizados_cluster_canopy.arff";
		String arquivoResultadoClusterTeste = diretorioTeste + "/resultado_cluster_canopy.txt";
		String clustererModelFile = diretorioPares + "/cluster_canopy.model";

		BufferedReader reader = null;
		try
		{
			// importa os dados do arquivo do weka
			reader = new BufferedReader(new FileReader(arquivoWeka));
			Instances data = new Instances(reader);
			data.deleteAttributeAt(data.numAttributes()-1); // remove o atributo referente à classe

			Canopy clusterer = null;
			if (READ_SERIALIZED_CLUSTERERS)
			{
				File file = new File(clustererModelFile);
				if (file.isFile() && file.exists())
				{
					// obtém o cluster serializado anteriormente
					clusterer = readSerializedCluster(clustererModelFile);
				}
			}

			if (clusterer == null)
			{
				clusterer = new Canopy();
				clusterer.setNumClusters(5);
				clusterer.setSeed(100);

				LOG.info("BUILD CLUSTERER START");
				clusterer.buildClusterer(data);
				LOG.info("BUILD CLUSTERER END");

				// serializa o cluster num arquivo, para testes posteriores
				serializeCluster(clusterer, clustererModelFile);
			}

			// gera saída para o treino
			ClusterEvaluation ce = new ClusterEvaluation();
			ce.setClusterer(clusterer);
			ce.evaluateClusterer(data);
			generateClusterOutput(arquivoClusterWeka, arquivoResultadoCluster, data, clusterer, ce);

			// classifica instâncias de teste conforme cluster gerado com base no treino
			classifyTestInstances(clusterer, data, arquivoTesteWeka, arquivoClusterTesteWeka, arquivoResultadoClusterTeste);
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			IOUtils.closeQuietly(reader);
		}

		LOG.info("ENDING OF CANOPY TEST...");
	}

	private static void startHierarchicalClustererTest(String diretorioBase, String diretorioPares, String arquivoWeka, String diretorioTeste, String arquivoTesteWeka) throws Exception, IOException
	{
		LOG.info("\n\nSTARTING HIERARCHICAL CLUSTERER TEST...");

		String arquivoClusterWeka = diretorioPares + "/pares_discretizados_cluster_hierarquical.arff";
		String arquivoResultadoCluster= diretorioPares + "/resultado_cluster_hierarquical.txt";
		String arquivoClusterTesteWeka = diretorioTeste + "/pares_discretizados_cluster_hierarquical.arff";
		String arquivoResultadoClusterTeste = diretorioTeste + "/resultado_cluster_hierarquical.txt";
		String clustererModelFile = diretorioPares + "/cluster_hierarquical.model";

		BufferedReader reader = null;
		try
		{
			// importa os dados do arquivo do weka
			reader = new BufferedReader(new FileReader(arquivoWeka));
			Instances data = new Instances(reader);
			data.deleteAttributeAt(data.numAttributes()-1); // remove o atributo referente à classe

			HierarchicalClusterer clusterer = null;
			if (READ_SERIALIZED_CLUSTERERS)
			{
				File file = new File(clustererModelFile);
				if (file.isFile() && file.exists())
				{
					// obtém o cluster serializado anteriormente
					clusterer = readSerializedCluster(clustererModelFile);
				}
			}

			if (clusterer == null)
			{
				clusterer = new HierarchicalClusterer();
				clusterer.setNumClusters(5);
//			clusterer.setDistanceFunction(distanceFunction);
//			clusterer.setDistanceIsBranchLength(bDistanceIsHeight);
//			clusterer.setLinkType(newLinkType);

				LOG.info("BUILD CLUSTERER START");
				clusterer.buildClusterer(data);
				LOG.info("BUILD CLUSTERER END");

				// serializa o cluster num arquivo, para testes posteriores
				serializeCluster(clusterer, clustererModelFile);
			}

			// gera saída para o treino
			ClusterEvaluation ce = new ClusterEvaluation();
			ce.setClusterer(clusterer);
			ce.evaluateClusterer(data);
			generateClusterOutput(arquivoClusterWeka, arquivoResultadoCluster, data, clusterer, ce);

			// classifica instâncias de teste conforme cluster gerado com base no treino
			classifyTestInstances(clusterer, data, arquivoTesteWeka, arquivoClusterTesteWeka, arquivoResultadoClusterTeste);
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			IOUtils.closeQuietly(reader);
		}

		LOG.info("ENDING OF HIERARCHICAL CLUSTERER TEST...");
	}

	private static void startFarthestFirstTest(String diretorioBase, String diretorioPares, String arquivoWeka, String diretorioTeste, String arquivoTesteWeka) throws Exception, IOException
	{
		LOG.info("\n\nSTARTING FARTHEST FIRST CLUSTERER TEST...");

		String arquivoClusterWeka = diretorioPares + "/pares_discretizados_cluster_farthest_first.arff";
		String arquivoResultadoCluster= diretorioPares + "/resultado_cluster_farthest_first.txt";
		String arquivoClusterTesteWeka = diretorioTeste + "/pares_discretizados_cluster_farthest_first.arff";
		String arquivoResultadoClusterTeste = diretorioTeste + "/resultado_cluster_farthest_first.txt";
		String clustererModelFile = diretorioPares + "/cluster_farthest_first.model";

		BufferedReader reader = null;
		try
		{
			// importa os dados do arquivo do weka
			reader = new BufferedReader(new FileReader(arquivoWeka));
			Instances data = new Instances(reader);
			data.deleteAttributeAt(data.numAttributes()-1); // remove o atributo referente à classe

			FarthestFirst clusterer = null;
			if (READ_SERIALIZED_CLUSTERERS)
			{
				File file = new File(clustererModelFile);
				if (file.isFile() && file.exists())
				{
					// obtém o cluster serializado anteriormente
					clusterer = readSerializedCluster(clustererModelFile);
				}
			}

			if (clusterer == null)
			{
				clusterer = new FarthestFirst();
				clusterer.setNumClusters(5);
				clusterer.setSeed(100);

				LOG.info("BUILD CLUSTERER START");
				clusterer.buildClusterer(data);
				LOG.info("BUILD CLUSTERER END");

				// serializa o cluster num arquivo, para testes posteriores
				serializeCluster(clusterer, clustererModelFile);
			}

			// gera saída para o treino
			ClusterEvaluation ce = new ClusterEvaluation();
			ce.setClusterer(clusterer);
			ce.evaluateClusterer(data);
			generateClusterOutput(arquivoClusterWeka, arquivoResultadoCluster, data, clusterer, ce);

			// classifica instâncias de teste conforme cluster gerado com base no treino
			classifyTestInstances(clusterer, data, arquivoTesteWeka, arquivoClusterTesteWeka, arquivoResultadoClusterTeste);

//			LOG.info("BUILD CLUSTERER START");
//			MakeDensityBasedClusterer densityClusterer = new MakeDensityBasedClusterer(clusterer);
//			densityClusterer.buildClusterer(data);
//			LOG.info("BUILD CLUSTERER END");
//
//			ClusterEvaluation ce = new ClusterEvaluation();
//			ce.setClusterer(densityClusterer);
//			ce.evaluateClusterer(data);
//
//			generateClusterOutput(arquivoClusterWeka, arquivoResultadoCluster, data, densityClusterer, ce);
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			IOUtils.closeQuietly(reader);
		}

		LOG.info("ENDING OF FARTHEST FIRST CLUSTERER TEST...");
	}

	private static Instances classifyTestInstances(AbstractClusterer clusterer, Instances trainInstances, String arquivoTesteWeka, String arquivoClusterTesteWeka, String arquivoResultadoClusterTeste) throws Exception
	{
		LOG.info("CLASSIFYING TEST INSTANCES...");

		// obtém as instâncias de teste
		Instances testInstances = getTestInstances(arquivoTesteWeka);

		ClusterEvaluation ce = new ClusterEvaluation();
		ce.setClusterer(clusterer);
		ce.evaluateClusterer(testInstances);

		generateClusterOutput(arquivoClusterTesteWeka, arquivoResultadoClusterTeste, testInstances, clusterer, ce);

		LOG.info("END OF CLASSIFYING TEST INSTANCES...");

		return testInstances;
	}

	private static void generateClusterOutput(String arquivoClusterWeka, String arquivoResultadoCluster, Instances data, AbstractClusterer clusterer, ClusterEvaluation ce) throws IOException, Exception
	{
		// configura plotagem do cluster
		ClustererAssignmentsPlotInstances plotInstances = new ClustererAssignmentsPlotInstances();
		plotInstances.setClusterer(clusterer);
		plotInstances.setInstances(data);
		plotInstances.setClusterEvaluation(ce);
		plotInstances.setUp();

		// salva as instâncias com clusterer no arquivo de saída
		save(plotInstances.getPlotInstances(), arquivoClusterWeka);

		// grava resultado do cluster
		StringBuilder sb = new StringBuilder();
		sb.append("[PARAMETERS]:\n\n");

		if (clusterer.getOptions() != null && clusterer.getOptions().length > 0)
		{
			for (String option : clusterer.getOptions())
			{
				boolean parameter = (option.startsWith("-") && !NumberUtils.isNumber(option));

				sb.append((parameter ? " " : ":"));
				sb.append(option);
			}
		}
		sb.append("\n\n");
		if (clusterer instanceof TechnicalInformationHandler)
		{
			sb.append("[TECHNICAL INFORMATION]:\n\n").append(((TechnicalInformationHandler)clusterer).getTechnicalInformation());
			sb.append("\n\n");
		}
		sb.append("[RESULT]:\n\n").append(ce.clusterResultsToString());
		FileUtils.write(new File(arquivoResultadoCluster), sb.toString());

		/*
		 * exibe o cluster na tela
		 */
		String name = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
		String cname = clusterer.getClass().getName();
		if (cname.startsWith("weka.clusterers."))
		{
			name += cname.substring("weka.clusterers.".length());
		}
		else
		{
			name += cname;
		}

		name = name + " (" + data.relationName() + ")";
		VisualizePanel vp = new VisualizePanel();
		vp.setName(name);
		vp.addPlot(plotInstances.getPlotData(cname));

		// display data
//		JFrame jf = new JFrame("Weka Clusterer Visualize: " + vp.getName());
//		jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		jf.setSize(800, 800);
//		jf.getContentPane().setLayout(new BorderLayout());
//		jf.getContentPane().add(vp, BorderLayout.CENTER);
//		jf.setVisible(true);
	}

	private static void serializeCluster(AbstractClusterer clusterer, String clustererModelFile) throws Exception
	{
		LOG.info(String.format("WRITING CLUSTERER MODEL TO FILE %s.", clustererModelFile));
		SerializationHelper.write(clustererModelFile, clusterer);
		LOG.info(String.format("END OF WRITING CLUSTERER MODEL TO FILE %s.", clustererModelFile));
	}

	@SuppressWarnings("unchecked")
	private static <T extends AbstractClusterer> T readSerializedCluster(String clustererModelFile) throws Exception
	{
		LOG.info(String.format("READING CLUSTERER MODEL FROM FILE %s.", clustererModelFile));
		T clusterer = (T) SerializationHelper.read(clustererModelFile);
		LOG.info(String.format("END OF READING CLUSTERER MODEL FROM FILE %s.", clustererModelFile));

		return clusterer;
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

}
