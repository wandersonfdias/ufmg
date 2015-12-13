package br.ufmg.extratorimagem.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import br.ufmg.extratorimagem.utils.StreamUtils;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ExtratorImagemMain
{
	public static void main(String[] args)
	{
		InputStream errorStream = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		BufferedReader br = null;

		try
		{
			String homeDir = System.getenv("HOME");
			String dir = homeDir + "/extrai_descritores";
			String shell = "./lixo.sh";

			ProcessBuilder p = new ProcessBuilder();
			p.directory(new File(dir));
			p.command(shell);
			Process process = p.start();
			int statusSaida = process.waitFor();

//			p.redirectInput(Redirect.to(file))

			errorStream = process.getErrorStream();
			inputStream = process.getInputStream();
			outputStream = process.getOutputStream();

			br = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));

			String error = StreamUtils.readAsUTF8(errorStream);
			System.out.println("error : " + error);
			System.out.println("statusSaida : " + statusSaida);

			String line = null;
			System.out.println("=> saída");
			while ((line = br.readLine()) != null )
			{
				System.out.println(line);
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			IOUtils.closeQuietly(errorStream);
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);
			IOUtils.closeQuietly(br);
		}
	}

	private static String executeCommand(String command, String dir) {

		StringBuffer output = new StringBuffer();

		Process p;
		try
		{
			p = Runtime.getRuntime().exec(command, null, new File(dir));
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null)
			{
				output.append(line + "\n");
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return output.toString();

	}
}
