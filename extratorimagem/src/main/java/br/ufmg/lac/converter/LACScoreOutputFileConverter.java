package br.ufmg.lac.converter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.ufmg.extratorimagem.exception.ProcessadorException;
import br.ufmg.extratorimagem.processador.constants.ProcessadorConstants;

/**
 * Converte arquivos de saída do LAC em arquivos de sáida no formato "imagem_comparada;score".
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class LACScoreOutputFileConverter
{
	private static final Log LOG = LogFactory.getLog(LACScoreOutputFileConverter.class);

	/**
	 * Arquivo de saída do processamento do LAC
	 */
	private String arquivoSaidaProcessamentoLAC;

	/**
	 * Diretório dos arquivos de saída
	 */
	private String diretorioSaida;

	/**
	 * Diretório base para processamento das imagens
	 */
	private String diretorioBase;

	/**
	 * Construtor
	 * @param diretorioBase
	 * @param diretorioSaida
	 * @param arquivoSaidaProcessamentoLAC
	 * @param arquivoSaidaScore
	 */
	public LACScoreOutputFileConverter(String diretorioBase, String diretorioSaida, String arquivoSaidaProcessamentoLAC)
	{
		this.diretorioBase = diretorioBase;
		this.diretorioSaida = diretorioSaida;
		this.arquivoSaidaProcessamentoLAC = arquivoSaidaProcessamentoLAC;
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
			this.convertScoreFile();
		}
		catch (IOException e)
		{
			throw new ProcessadorException(e.getMessage(), e);
		}
	}

	/**
	 * Gera o arquivo de prediction
	 * @throws IOException
	 * @throws ProcessadorException
	 */
	private void convertScoreFile() throws IOException, ProcessadorException
	{
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;

		try
		{
			List<ImagemComparacaoDTO> scores = new ArrayList<ImagemComparacaoDTO>();
			final String SCORE_PREFIX = "Score[1]=";
			final String ID_PREFIX = "id=";

			// lê as linhas do arquivo de saída do processamento do LAC
			fileReader = new FileReader(new File(this.arquivoSaidaProcessamentoLAC));
			bufferedReader = new BufferedReader(fileReader);

			String linhaArquivo = null;
			while ((linhaArquivo = bufferedReader.readLine()) != null)
			{
				if (StringUtils.isNotBlank(linhaArquivo) && StringUtils.containsIgnoreCase(linhaArquivo, SCORE_PREFIX))
				{
					String pairId = null;
					Double scoreValue = null;

					linhaArquivo = linhaArquivo.replace("= ", "=").trim();
					String[] dados = StringUtils.split(linhaArquivo, ' ');

					for (String dado : dados)
					{
						if (StringUtils.containsIgnoreCase(dado, ID_PREFIX))
						{
							// obtém dados do id do para de imagem
							String[] dadosId = StringUtils.split(dado, '=');
							pairId = dadosId[1];
						}
						else if (StringUtils.containsIgnoreCase(dado, SCORE_PREFIX))
						{
							// obtém o valor do score de similaridade
							String[] dadosScore = StringUtils.split(dado, '=');
							scoreValue = new Double(dadosScore[1]);
						}
					}

					if (pairId != null && scoreValue != null)
					{
						String[] ids = StringUtils.split(pairId, '|');
						String imagemComparacao = ids[1];

						// guarda dados de saída
						scores.add(new ImagemComparacaoDTO(imagemComparacao, scoreValue));
					}
				}
			}

			// ordena os dados de saída, de forma decrescente, em relação ao valor do score
			Comparator<ImagemComparacaoDTO> comparator = Collections.reverseOrder(new Comparator<ImagemComparacaoDTO>()
			{
				public int compare(ImagemComparacaoDTO o1, ImagemComparacaoDTO o2)
				{
					return o1.getScore().compareTo(o2.getScore());
				}
			});
			Collections.sort(scores, comparator);

			// grava arquivo de saída contendo os scores
			this.writeOutputFile(scores);
		}
		catch (IOException e)
		{
			LOG.error(e.getMessage(), e);
			throw e;
		}
		finally
		{
			IOUtils.closeQuietly(fileReader);
			IOUtils.closeQuietly(bufferedReader);
		}
	}

	/**
	 * Grava arquivo de saída
	 * @param scores
	 * @throws ProcessadorException
	 */
	private void writeOutputFile(List<ImagemComparacaoDTO> scores) throws ProcessadorException
	{
		FileOutputStream out = null;

		try
		{
			final char fieldSeparator = ';';

			// cria o arquivo de saída
			File file = this.createOutputFile(ProcessadorConstants.SCORE_OUTPUT_FILENAME);
			out = FileUtils.openOutputStream(file, true);
			final BufferedOutputStream buffer = new BufferedOutputStream(out);

			// grava o cabeçalho do arquivo
			String headerLine = new StringBuilder().append("imagem_comparacao").append(fieldSeparator).append("score").toString();
			List<String> headerdata = new ArrayList<String>();
			headerdata.add(headerLine);
			IOUtils.writeLines(headerdata, ProcessadorConstants.LINE_SEPARATOR, buffer, Charsets.UTF_8);
			buffer.flush();

			for (ImagemComparacaoDTO dto : scores)
			{
				// grava a linha do score no arquivo de saída
				String line = new StringBuilder().append(dto.getImagem()).append(fieldSeparator).append(dto.getScore()).toString();
				List<String> lineData = new ArrayList<String>();
				lineData.add(line);
				IOUtils.writeLines(lineData, ProcessadorConstants.LINE_SEPARATOR, buffer, Charsets.UTF_8);
				buffer.flush();
			}

			buffer.flush();
			out.close();
		}
		catch (IOException e)
		{
			throw new ProcessadorException(String.format("Ocorreu um erro ao gravar linha no arquivo de saída '%s' no diretório '%s'.", ProcessadorConstants.SCORE_OUTPUT_FILENAME, this.getFullPathSaida()), e);
		}
		finally
		{
			IOUtils.closeQuietly(out);
		}
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
class ImagemComparacaoDTO
{
	private String imagem;
	private Double score;

	public ImagemComparacaoDTO(String imagem, Double score)
	{
		this.imagem = imagem;
		this.score = score;
	}

	/**
	 * @return the imagem
	 */
	public String getImagem()
	{
		return imagem;
	}

	/**
	 * @return the score
	 */
	public Double getScore()
	{
		return score;
	}
}