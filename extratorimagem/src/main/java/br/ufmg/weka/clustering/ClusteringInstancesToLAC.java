package br.ufmg.weka.clustering;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import br.ufmg.extratorimagem.exception.ProcessadorException;
import br.ufmg.extratorimagem.processador.constants.ProcessadorConstants;
import br.ufmg.lac.converter.LACQueryFileConverter;
import br.ufmg.lac.converter.LACScoreOutputFileConverter;
import br.ufmg.shell.util.ShellCommandExecutor;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ClusteringInstancesToLAC
{
	public static void main(String[] args) throws Exception
	{
		String diretorioBase = System.getenv("HOME") + "/extrai_descritores/tp_agrupamentos";
		String diretorioTreino = diretorioBase + "/dados_base_completa";
		String diretorioTeste = diretorioBase + "/dados_base_consulta";
		String arquivoPares = "/pares/pares_discretizados_cluster_###.arff";
		String diretorioSaidaBase = "lac_dataset";
		String arquivoOriginalBase = "/pares/pares.txt";

		Instances instanciasTreinoOriginais = DataSource.read(diretorioTreino + "/pares/pares_discretizados.arff");
		instanciasTreinoOriginais.setClassIndex(instanciasTreinoOriginais.numAttributes()-1);

		Instances instanciasTesteOriginais = DataSource.read(diretorioTeste + "/pares/pares_discretizados.arff");
		instanciasTesteOriginais.setClassIndex(instanciasTesteOriginais.numAttributes()-1);

		String[] clusters = {"canopy", "cobweb", "em", "farthest_first", "kmeans", "kmeans_with_canopy"};

		boolean converterArquivos = true;
		boolean executarLAC = true;

		for (String cluster : clusters)
		{
			System.out.println("\n\n##### Convertendo Cluster: " + cluster + " #####");

			// converte arquivo de treino
			String diretorioSaidaTreino = diretorioSaidaBase + "/" + cluster + "/treino";
			String arquivoTreinoWeka = StringUtils.replace(diretorioTreino + arquivoPares, "###", cluster);
			String arquivoTreinoWekaComClasse = StringUtils.replace(diretorioTreino + arquivoPares, "###", new StringBuilder(cluster).append("_com_classe").toString());
			String arquivoTreinoOriginal = diretorioTreino + arquivoOriginalBase;

			if (converterArquivos)
			{
				System.out.println("---> Gerando novo arquivo de treino com classe...");
				addClassToInstances(instanciasTreinoOriginais, arquivoTreinoWeka, arquivoTreinoWekaComClasse, cluster);

				System.out.println("---> Gerando arquivo de treino no formato do LAC...");
				LACQueryFileConverter converterTreino = new LACQueryFileConverter(diretorioBase, diretorioSaidaTreino, arquivoTreinoWekaComClasse, arquivoTreinoOriginal);
				converterTreino.convert(true, false);
			}

			// converte arquivo de teste
			String diretorioSaidaTeste = diretorioSaidaBase + "/" + cluster + "/teste";
			String arquivoTesteWeka = StringUtils.replace(diretorioTeste + arquivoPares, "###", cluster);
			String arquivoTesteWekaComClasse = StringUtils.replace(diretorioTeste + arquivoPares, "###", new StringBuilder(cluster).append("_com_classe").toString());
			String arquivoTesteOriginal = diretorioTeste + arquivoOriginalBase;

			if (converterArquivos)
			{
				System.out.println("---> Gerando novo arquivo de teste com classe...");
				addClassToInstances(instanciasTesteOriginais, arquivoTesteWeka, arquivoTesteWekaComClasse, cluster);

				System.out.println("---> Gerando arquivo de teste no formato do LAC...");
				LACQueryFileConverter converterTeste = new LACQueryFileConverter(diretorioBase, diretorioSaidaTeste, arquivoTesteWekaComClasse, arquivoTesteOriginal);
				converterTeste.convert(false, true);
			}

			// executa o LAC
			if (executarLAC)
			{
				System.out.println("---> Executando o LAC...");
				runLAC(diretorioBase, diretorioSaidaTreino, diretorioSaidaTeste, cluster);
			}

			// gera arquivo de saída com score
			System.out.println("---> Gerando arquivo de score...");
			String diretorioSaidaScore = "score_output/" + cluster;
			String arquivoSaidaProcessamentoLAC = diretorioBase +"/lac_output/" + cluster + "/" + ProcessadorConstants.LAC_OUTPUT_FILENAME;
			LACScoreOutputFileConverter scoreConverter = new LACScoreOutputFileConverter(diretorioBase, diretorioSaidaScore, arquivoSaidaProcessamentoLAC);
			scoreConverter.convert();
		}
	}

	/**
	 * Adiciona as classes às instâncias
	 * @param data
	 * @throws Exception
	 */
	private static void addClassToInstances(Instances instanciasOriginais, String arquivoClusterWeka, String arquivoTreinoWekaComClasse, String cluster) throws Exception
	{
		Instances instanciasCluster = DataSource.read(arquivoClusterWeka);

		if (instanciasCluster != null && instanciasCluster.numInstances() > 0)
		{
			// adiciona novo atributo
			instanciasCluster.insertAttributeAt(instanciasOriginais.classAttribute(), instanciasCluster.numAttributes());
			int classIndex = instanciasCluster.numAttributes()-1;
			instanciasCluster.setClassIndex(classIndex);

			for (int i = 0; i < instanciasCluster.numInstances(); i++)
			{
				Instance originalInstance = instanciasOriginais.instance(i);
				double classValue = originalInstance.value(instanciasOriginais.classIndex());

				Instance clusterInstance = instanciasCluster.instance(i);
				clusterInstance.setValue(classIndex, classValue);
			}

			save(instanciasCluster, arquivoTreinoWekaComClasse);
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
		BufferedWriter writer = null;
		try
		{
			writer = new BufferedWriter(new FileWriter(filename));
			writer.write(data.toString());
			writer.newLine();
			writer.flush();
			writer.close();
		}
		finally
		{
			IOUtils.closeQuietly(writer);
		}
	}


	/**
	 * @param args
	 * @return
	 * @throws ProcessorException
	 */
	public static void runLAC(String diretorioBase, String diretorioLACTreino, String diretorioLACTeste, String cluster) throws ProcessadorException
	{
		String diretorioExecucaoLAC = System.getenv("HOME") + "/extrai_descritores/lac";
		String diretorioSaidaLAC = diretorioBase +"/lac_output/" + cluster;

		String comando = "lazy";
		String dataSetTreino = new StringBuilder(diretorioBase).append(File.separator).append(diretorioLACTreino).append(File.separator).append(ProcessadorConstants.LAC_TRAINING_FILENAME).toString();
		String dataSetTeste = new StringBuilder(diretorioBase).append(File.separator).append(diretorioLACTeste).append(File.separator).append(ProcessadorConstants.LAC_TEST_FILENAME).toString();

		String[] parametros = { "-i" // dataset de treino
							  , dataSetTreino

							  // dataset de teste
							  , "-t"
							  , dataSetTeste

							  // suporte mínimo
							  , "-s" // parâmetro
							  , "1" // valor

							  // confiança mínima
							  , "-c" // parâmetro
							  , "0.01" // valor

							  // quantidade máxima de regras a serem geradas
							  , "-m" // parâmetro
							  , "2" // valor

							  // cache
							  , "-e" // parâmetro
							  , "10000000" // valor
							  };

		ShellCommandExecutor shell = new ShellCommandExecutor(diretorioExecucaoLAC, comando, parametros, diretorioSaidaLAC, ProcessadorConstants.LAC_OUTPUT_FILENAME);
		int status = shell.execute();
		if (status != 0)
		{
			String msgErro = StringUtils.EMPTY;
			if (shell.getSaidaErro() != null && !shell.getSaidaErro().isEmpty())
			{
				msgErro = StringUtils.join(shell.getSaidaErro().toArray(), '\n').trim();
			}
			throw new ProcessadorException(String.format("Ocorreu um erro inesperado na execução do LAC.\nDETALHE: \"%s\".", msgErro));
		}
	}
}
