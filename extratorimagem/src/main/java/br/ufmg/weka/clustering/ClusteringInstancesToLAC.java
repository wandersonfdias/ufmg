package br.ufmg.weka.clustering;

import org.apache.commons.lang3.StringUtils;

import br.ufmg.extratorimagem.exception.ProcessadorException;
import br.ufmg.lac.converter.LACQueryFileConverter;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ClusteringInstancesToLAC
{
	public static void main(String[] args) throws ProcessadorException
	{
		String diretorioBase = System.getenv("HOME") + "/extrai_descritores/tp_agrupamentos";
		String diretorioTreino = diretorioBase + "/dados_base_completa";
		String diretorioTeste = diretorioBase + "/dados_base_consulta";
		String arquivoPares = "/pares/pares_discretizados_cluster_###.arff";
		String diretorioSaidaBase = "lac_dataset";
		String arquivoOriginalBase = "/pares/pares.txt";


		String[] clusters = {"canopy", "cobweb", "em", "farthest_first", "kmeans", "kmeans_with_canopy"};

		for (String cluster : clusters)
		{
			System.out.println("\n\n##### Convertendo Cluster: " + cluster + " #####");

			// converte arquivo de treino
			System.out.println("---> Gerando arquivo de treino...");
			String diretorioSaidaTreino = diretorioSaidaBase + "/" + cluster + "/treino";
			String arquivoTreinoWeka = StringUtils.replace(diretorioTreino + arquivoPares, "###", cluster);
			String arquivoTreinoOriginal = diretorioTreino + arquivoOriginalBase;
			LACQueryFileConverter converterTreino = new LACQueryFileConverter(diretorioBase, diretorioSaidaTreino, arquivoTreinoWeka, arquivoTreinoOriginal);
			converterTreino.convert(true, false);

			// converte arquivo de teste
			System.out.println("---> Gerando arquivo de teste...");
			String diretorioSaidaTeste = diretorioSaidaBase + "/" + cluster + "/teste";
			String arquivoTesteWeka = StringUtils.replace(diretorioTeste + arquivoPares, "###", cluster);
			String arquivoTesteOriginal = diretorioTeste + arquivoOriginalBase;
			LACQueryFileConverter converterTeste = new LACQueryFileConverter(diretorioBase, diretorioSaidaTeste, arquivoTesteWeka, arquivoTesteOriginal);
			converterTeste.convert(false, true);
		}
	}
}
