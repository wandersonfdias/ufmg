package br.ufmg.eval.converter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import br.ufmg.extratorimagem.exception.ProcessadorException;
import br.ufmg.extratorimagem.processador.constants.ProcessadorConstants;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class EvalFileConverter
{
	private static final String PREDICTION_FILE_PREFIX = "prediction";
	private static final String FEATURE_FILE_PREFIX = "feature";

	/**
	 * Arquivo de saída do LAC
	 */
	private String arquivoSaidaLAC;

	/**
	 * Arquivo de teste utilizado na entrada do LAC
	 */
	private String arquivoEntradaTesteLAC;

	/**
	 * Diretório dos arquivos de saída
	 */
	private String diretorioSaida;

	/**
	 * Diretório base para processamento das imagens
	 */
	private String diretorioBase;

	/**
	 *
	 */
	public EvalFileConverter(String diretorioBase, String diretorioSaida, String arquivoSaidaLAC, String arquivoEntradaTesteLAC)
	{
		this.diretorioBase = diretorioBase;
		this.diretorioSaida = diretorioSaida;
		this.arquivoSaidaLAC = arquivoSaidaLAC;
		this.arquivoEntradaTesteLAC = arquivoEntradaTesteLAC;
	}

	/**
	 * Converte o arquivo do WEKA em arquivos de treino/teste do LAC
	 * @throws ProcessadorException
	 */
	public void convert() throws ProcessadorException
	{
		try
		{
			// cria o diretório de saída
			this.createOutputDir();

			// gera o arquivo de prediction
			this.convertPredictionFile();

			// gera o arquivo de feature
			this.convertFeatureFile();
		}
		catch (IOException e)
		{
			throw new ProcessadorException(e.getMessage(), e);
		}
	}

	/**
	 * Gera o arquivo de feature
	 * @throws IOException
	 * @throws ProcessadorException
	 */
	private void convertFeatureFile() throws IOException, ProcessadorException
	{
		// lê o arquivo original e adiciona o attributo que identifica o par de imagens às instâncias do dataset
		List<String> linhasArquivo = FileUtils.readLines(new File(this.arquivoEntradaTesteLAC));

		// cria o arquivo de saída
		File file = this.createOutputFile(this.getFeatureFileName());

		for (String linhaArquivo : linhasArquivo)
		{
			// exemplo da linha: accordion.image_0001|cannon.image_0008 CLASS=0 w[1]='\'(0.1-0.2]\''
			String[] dadosLinha = StringUtils.split(linhaArquivo.trim(), ' ');

			// obtém ids da query e document
			String[] pairId = StringUtils.split(dadosLinha[0], ProcessadorConstants.PAIR_ID_SEPARATOR);
			String queryId = pairId[0];
			String documentId = pairId[1];

			// obtém o valor da classe
			String classValue = null;
			String classPrefix = "CLASS=";
			for (String dadoLinha : dadosLinha)
			{
				if (StringUtils.containsIgnoreCase(dadoLinha, classPrefix))
				{
					String dados[] = StringUtils.split(dadoLinha, '=');
					classValue = dados[1];
					break;
				}
			}

			// grava a linha no arquivo de saída
			// exemplo da linha de saída: 0 qid:1 #docid = 1
			StringBuilder linhaSaida = new StringBuilder();
			linhaSaida.append(classValue);
			linhaSaida.append(StringUtils.SPACE);
			linhaSaida.append("qid:");
			linhaSaida.append(queryId);
			linhaSaida.append(StringUtils.SPACE);
			linhaSaida.append("#docid = ");
			linhaSaida.append(documentId);

			this.writeLine(file, linhaSaida.toString());
		}
	}

	/**
	 * Gera o arquivo de prediction
	 * @throws IOException
	 * @throws ProcessadorException
	 */
	private void convertPredictionFile() throws IOException, ProcessadorException
	{
		// lê o arquivo original e adiciona o attributo que identifica o par de imagens às instâncias do dataset
		List<String> linhasArquivo = FileUtils.readLines(new File(this.arquivoSaidaLAC));

		// cria o arquivo de saída
		File file = this.createOutputFile(this.getPredictionFileName());

		final String SCORE_PREFIX = "Score[1]=";
		for (String linhaArquivo : linhasArquivo)
		{
			if (StringUtils.isNoneBlank(linhaArquivo) && StringUtils.containsIgnoreCase(linhaArquivo, SCORE_PREFIX))
			{
				String scoreValue = null;

				linhaArquivo = linhaArquivo.replace("= ", "=").trim();
				String[] dados = StringUtils.split(linhaArquivo, ' ');

				for (String dado : dados)
				{
					if (StringUtils.containsIgnoreCase(dado, SCORE_PREFIX))
					{
						String[] dadosScore = StringUtils.split(dado, '=');
						scoreValue = dadosScore[1];
						break;
					}
				}

				if (scoreValue != null)
				{
					// grava o valor do score no arquivo de saída
					this.writeLine(file, scoreValue);
				}
			}
		}
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

	private String getPredictionFileName()
	{
		String[] data = StringUtils.split(this.arquivoSaidaLAC, '/');

		StringBuilder sb = new StringBuilder();
		sb.append(PREDICTION_FILE_PREFIX);
		sb.append('_');
		String name = data[data.length-1];
		sb.append(name.substring(0, name.indexOf('.')));
		return sb.toString();
	}

	private String getFeatureFileName()
	{
		String[] data = StringUtils.split(this.arquivoSaidaLAC, '/');

		StringBuilder sb = new StringBuilder();
		sb.append(FEATURE_FILE_PREFIX);
		sb.append('_');
		String name = data[data.length-1];
		sb.append(name.substring(0, name.indexOf('.')));
		return sb.toString();
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
