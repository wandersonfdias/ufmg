package br.ufmg.shell.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.ufmg.extratorimagem.exception.ProcessadorException;

/**
 * Abstrai a execução de comandos no sistema operacional
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ShellCommandExecutor
{
	private static final Log LOG = LogFactory.getLog(ShellCommandExecutor.class);

	// parâmetros de entrada
	private String diretorioExecucao;
	private String comando;
	private String[] parametros;
	private String diretorioSaidaProcessamento;
	private String arquivoSaidaProcessamento;

	// informações de saída
	private List<String> saidaPadrao;
	private List<String> saidaErro;

	/**
	 * Construtor
	 * @param diretorioExecucao
	 * @param comando
	 * @param parametros
	 * @param arquivoSaidaProcessamento
	 */
	public ShellCommandExecutor(String diretorioExecucao, String comando, String[] parametros, String diretorioSaidaProcessamento, String arquivoSaidaProcessamento)
	{
		this.diretorioExecucao = diretorioExecucao;
		this.comando = comando;
		this.parametros = parametros;
		this.diretorioSaidaProcessamento = diretorioSaidaProcessamento;
		this.arquivoSaidaProcessamento = arquivoSaidaProcessamento;
	}

	/**
	 * Executa o programa com respectivos parâmetros
	 * @return Status da execução do programa
	 * @throws ProcessadorException
	 */
	public int execute() throws ProcessadorException
	{
		int statusSaida = -1;
		Process process = null;
		InputStream errorStream = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;

		try
		{
			// cria o diretório de saída
			this.createOutputDir();

			// executa o comando
			ProcessBuilder p = new ProcessBuilder();
			p.directory(new File(this.diretorioExecucao));
			p.command(this.getCommandWithArguments());
			p.redirectOutput(new File(new StringBuilder(this.diretorioSaidaProcessamento).append(File.separator).append(this.arquivoSaidaProcessamento).toString()));
			process = p.start();

			// aguarda a execução do comando
			statusSaida = process.waitFor();

			errorStream = process.getErrorStream();
			inputStream = process.getInputStream();
			outputStream = process.getOutputStream();

			this.saidaErro = this.readInputStream(errorStream);
			this.saidaPadrao = this.readInputStream(inputStream);
		}
		catch (IOException e)
		{
			LOG.error(e.getMessage(), e);
			throw new ProcessadorException(e.getMessage(), e);
		}
		catch (InterruptedException e)
		{
			LOG.error(e.getMessage(), e);
			throw new ProcessadorException(e.getMessage(), e);
		}
		finally
		{
			IOUtils.closeQuietly(errorStream);
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);

			if (process != null && statusSaida != 0)
			{
				// garante a finalização do processo
				process.destroy();
			}
		}

		return statusSaida;
	}

	private List<String> getCommandWithArguments()
	{
		List<String> commands = new ArrayList<String>();
		commands.add(new StringBuilder("./").append(this.comando).toString());

		if (this.parametros != null && this.parametros.length > 0)
		{
			commands.addAll(Arrays.asList(parametros));
		}

		return commands;
	}

	private List<String> readInputStream(InputStream inputStream) throws IOException
	{
		List<String> lines = null;
		InputStreamReader isr = null;

		try
		{
			isr = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
			lines = IOUtils.readLines(isr);
		}
		finally
		{
			IOUtils.closeQuietly(isr);
		}

		return lines;
	}

	private void createOutputDir() throws ProcessadorException
	{
		File dir = new File(this.diretorioSaidaProcessamento);

		if (dir.exists())
		{
			try
			{
				// remove o diretório recursivamente
				FileUtils.deleteDirectory(dir);
			}
			catch (IOException e)
			{
				throw new ProcessadorException(String.format("Ocorreu um erro ao excluir o diretório de saída '%s'.", this.diretorioSaidaProcessamento), e);
			}
		}

		// cria o diretório
		boolean dirCreated = dir.mkdirs();
		if (!dirCreated)
		{
			throw new ProcessadorException(String.format("Não foi possível criar o diretório de saída '%s'.", this.diretorioSaidaProcessamento));
		}
	}

	/**
	 * @return the saidaPadrao
	 */
	public List<String> getSaidaPadrao()
	{
		return saidaPadrao;
	}

	/**
	 * @return the saidaErro
	 */
	public List<String> getSaidaErro()
	{
		return saidaErro;
	}
}
