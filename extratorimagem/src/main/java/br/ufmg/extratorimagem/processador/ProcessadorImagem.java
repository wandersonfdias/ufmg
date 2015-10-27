package br.ufmg.extratorimagem.processador;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.ufmg.extratorimagem.exception.ProcessadorImagemException;
import br.ufmg.extratorimagem.processador.constants.ProcessadorConstants;
import br.ufmg.extratorimagem.utils.ProcessadorUtils;

/**
 * Processador de imagens.
 * Calcula as distâncias de uma imagem corrente em relação à um conjunto de imagens.
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ProcessadorImagem
{
	private static final Log LOG = LogFactory.getLog(ProcessadorImagem.class);

	private File currentImage;
	private List<File> images;
	private List<File> descriptors;

	public ProcessadorImagem(File currentImage, List<File> images, List<File> descriptors)
	{
		super();
		this.currentImage = currentImage;
		this.images = images;
		this.descriptors = descriptors;
	}

	/**
	 * Realiza o processamento de uma imagem
	 * @return
	 * @throws ProcessadorImagemException
	 */
	public String processar() throws ProcessadorImagemException
	{
		StringBuilder saida = new StringBuilder(StringUtils.EMPTY);

		for (File imageFile : this.images)
		{
			if (LOG.isDebugEnabled())
			{
				System.out.printf("imagem origem: %s - imagem destino: %s", this.currentImage.getName(), imageFile.getName());
				System.out.println();
			}

			for (File descriptorFile : this.descriptors)
			{
				String[] descriptorForCurrentImage = this.getDescriptorValuesForImage(descriptorFile, this.currentImage);
				String[] descriptorForImage = this.getDescriptorValuesForImage(descriptorFile, imageFile);

				BigDecimal distance = this.calculateDescriptorDistance(descriptorForCurrentImage, descriptorForImage);
				this.appendDistanceToLine(saida, distance);

				if (LOG.isDebugEnabled())
				{
					System.out.printf("descritor: %s - distância: %s", descriptorFile.getName(), distance);
					System.out.println();
				}
			}

			if (LOG.isDebugEnabled())
			{
				System.out.println();
				System.out.println(StringUtils.repeat("-", 20));
			}
		}

		return saida.toString();
	}

	/**
	 * Adiciona uma distância à linha da imagem
	 * @param line
	 * @param distance
	 */
	private void appendDistanceToLine(StringBuilder line, BigDecimal distance)
	{
		if (line.length() > 0)
		{
			line.append(ProcessadorConstants.FIELD_SEPARATOR);
		}
		line.append(distance.doubleValue());
	}

	/**
	 * Calcula a distância entre um tipo de descritor de duas imagens
	 * @param descriptorForCurrentImage
	 * @param descriptorForImage
	 * @return
	 */
	private BigDecimal calculateDescriptorDistance(String[] descriptorForCurrentImage, String[] descriptorForImage)
	{
		MathContext context = new MathContext(ProcessadorConstants.DESCRIPTOR_PRECISION_VALUE); // define a precisão
		BigDecimal distance = new BigDecimal(0, context);

		for (int i=0; i<descriptorForCurrentImage.length; i++)
		{
			BigDecimal currentImageValue = new BigDecimal(descriptorForCurrentImage[i], context);
			BigDecimal imageValue = new BigDecimal(descriptorForImage[i], context);

			distance = distance.add(currentImageValue.subtract(imageValue).abs());
		}

		return distance;
	}

	/**
	 * Obtém os valores de um descritor de uma imagem
	 * @param descriptorFile
	 * @param imageFile
	 * @return
	 * @throws ProcessadorImagemException
	 */
	private String[] getDescriptorValuesForImage(File descriptorFile, File imageFile) throws ProcessadorImagemException
	{
		String descriptorFileName = null;
		try
		{
			String[] descriptors = null;

			descriptorFileName = getDescriptorName(descriptorFile, imageFile);
			String descriptorContent = FileUtils.readFileToString(new File(descriptorFileName));

			if (StringUtils.isBlank(descriptorContent))
			{
				throw new ProcessadorImagemException(String.format("Não foi possível obter o conteúdo do descritor '%s'.", descriptorFileName));
			}
			else
			{
				String[] data = StringUtils.split(descriptorContent.trim(), "\n");
				if (data.length < 2)
				{
					throw new ProcessadorImagemException(String.format("Conteúdo do descritor '%s' está fora do padrão. Padrão: <total de atributos>\\n<valores dos atributos>'.", descriptorFileName));
				}
				else
				{
					int attributes = Integer.valueOf(data[0]);
					String content = data[1];

					String[] contentData = StringUtils.split(content, StringUtils.SPACE);
					if (contentData.length == attributes || contentData.length > 1)
					{
						descriptors = contentData;
					}
					else
					{
						descriptors = content.split(StringUtils.EMPTY);
					}
				}
			}

			return descriptors;
		}
		catch (IOException e)
		{
			throw new ProcessadorImagemException("Ocorreu um erro ao obter o conteúdo do descritor '" + descriptorFileName + "'.",e );
		}
	}

	/**
	 * Obtém o o nome completo de um descritor
	 * @param descriptorFile
	 * @param imageFile
	 * @return
	 */
	private String getDescriptorName(File descriptorFile, File imageFile)
	{
		StringBuilder descriptorName = new StringBuilder(descriptorFile.getPath());
		descriptorName.append(File.separator);
		descriptorName.append(ProcessadorUtils.getNameWithoutExtension(imageFile.getName()));
		descriptorName.append(ProcessadorConstants.FILENAME_SEPARATOR);
		descriptorName.append(descriptorFile.getName());

		return descriptorName.toString();
	}
}
