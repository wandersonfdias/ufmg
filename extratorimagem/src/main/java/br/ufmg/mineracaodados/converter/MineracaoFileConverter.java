package br.ufmg.mineracaodados.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import br.ufmg.extratorimagem.exception.ProcessadorException;
import br.ufmg.extratorimagem.extratorpares.dto.ParDTO;
import br.ufmg.extratorimagem.processador.constants.ProcessadorConstants;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class MineracaoFileConverter
{
	private static final String OUTPUT_FILE_PREFIX = "output";
	private static final MathContext MATH_CONTEXT = new MathContext(ProcessadorConstants.DESCRIPTOR_PRECISION_VALUE); // define a precisão

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
	public MineracaoFileConverter(String diretorioBase, String diretorioSaida, String arquivoSaidaLAC, String arquivoEntradaTesteLAC)
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

			// gera o arquivo de saída
			this.convertOutputFile();
		}
		catch (IOException e)
		{
			throw new ProcessadorException(e.getMessage(), e);
		}
	}

	/**
	 * Gera o arquivo de saída
	 * @throws IOException
	 * @throws ProcessadorException
	 */
	private void convertOutputFile() throws IOException, ProcessadorException
	{
		// cria o arquivo de saída
		File file = this.createOutputFile(this.getOutputFileName());

//		w[9]='\'(0.1-0.2]\''-->CLASS=0 size= 1 count= 26846 supp= 0.335357 conf= 0.982758
		final String PAIR_ID_PREFIX = "id=";
		final String RULE_PREFIX = "w[";
		final String SUPPORT_PREFIX = "supp=";
		final String CONFIANCE_PREFIX = "conf=";

		List<BigDecimal> suportes = new ArrayList<BigDecimal>();
		List<BigDecimal> confiancas = new ArrayList<BigDecimal>();

		FileReader fileReader = null;
		BufferedReader bufferedReader = null;

		try
		{
			fileReader = new FileReader(this.arquivoSaidaLAC);
			bufferedReader = new BufferedReader(fileReader);

			String linhaArquivo = null;
			while ((linhaArquivo = bufferedReader.readLine()) != null)
			{
				// identifica a linha do par
				if (StringUtils.containsIgnoreCase(linhaArquivo, PAIR_ID_PREFIX))
				{
					// suporte
					StringBuilder suporte = new StringBuilder();
					suporte.append("suporte = c(");
					suporte.append(StringUtils.join(suportes.iterator(), ','));
					suporte.append(");");

					// confianca
					StringBuilder confianca = new StringBuilder();
					confianca.append("confianca = c(");
					confianca.append(StringUtils.join(confiancas.iterator(), ','));
					confianca.append(");");

					StringBuilder line = new StringBuilder();
					line.append(linhaArquivo);
					line.append(ProcessadorConstants.LINE_SEPARATOR);
					line.append(suporte.toString());
					line.append(ProcessadorConstants.LINE_SEPARATOR);
					line.append(confianca.toString());
					line.append(ProcessadorConstants.LINE_SEPARATOR);

					// grava o valor do score no arquivo de saída
					this.writeLine(file, line.toString());

					// reseta valores de suporte e confiança
					suportes = new ArrayList<BigDecimal>();
					confiancas = new ArrayList<BigDecimal>();
				}
				else if (StringUtils.isNoneBlank(linhaArquivo) && StringUtils.startsWithIgnoreCase(linhaArquivo, RULE_PREFIX)) // identifica linha de regra
				{
					linhaArquivo = linhaArquivo.replace("= ", "=").trim();
					String[] dados = StringUtils.split(linhaArquivo, ' ');

					for (String dado : dados)
					{
						if (StringUtils.containsIgnoreCase(dado, SUPPORT_PREFIX))
						{
							String[] dadosSuporte = StringUtils.split(dado, '=');
							suportes.add(new BigDecimal(dadosSuporte[1], MATH_CONTEXT));
						}
						else if (StringUtils.containsIgnoreCase(dado, CONFIANCE_PREFIX))
						{
							String[] dadosConfianca = StringUtils.split(dado, '=');
							confiancas.add(new BigDecimal(dadosConfianca[1], MATH_CONTEXT));
						}
					}
				}
			}
		}
		finally
		{
			IOUtils.closeQuietly(fileReader);
			IOUtils.closeQuietly(bufferedReader);
		}
	}

	/**
	 * Normaliza os valores dos descritores
	 * @param pares
	 * @param descriptors
	 * @return
	 */
	private List<BigDecimal> normalizar(List<BigDecimal> valores)
	{
		List<BigDecimal> valoresNormalizados = new ArrayList<BigDecimal>();

		BigDecimal minValue = Collections.min(valores);
		BigDecimal maxValue = Collections.max(valores);

		for (BigDecimal currentValue: valores)
		{
			valoresNormalizados.add(this.normalize(currentValue, minValue, maxValue));
		}

		return valoresNormalizados;
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
		boolean dirCreated = dir.mkdirs();
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

	private String getOutputFileName()
	{
		String[] data = StringUtils.split(this.arquivoSaidaLAC, '/');

		StringBuilder sb = new StringBuilder();
		sb.append(OUTPUT_FILE_PREFIX);
		sb.append('_');
		String name = data[data.length-2];
		sb.append(name);
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
