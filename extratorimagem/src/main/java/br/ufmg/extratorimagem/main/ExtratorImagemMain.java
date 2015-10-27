package br.ufmg.extratorimagem.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import br.ufmg.extratorimagem.utils.StreamUtils;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ExtratorImagemMain
{
	public static void main(String[] args)
	{
		try
		{

//			Process process = Runtime.getRuntime().exec(new String[]{"/extratorimagem/src/main/java/resources/extrai_descritores/main.sh"});

			String homeDir = System.getenv("HOME");
			String dir = homeDir + "/extrai_descritores";
			String shell = "./main.sh";

			ProcessBuilder p = new ProcessBuilder();
			p.directory(new File(dir));
			p.command(shell);
			Process process = p.start();

			InputStream errorStream = process.getErrorStream();
			InputStream inputStream = process.getInputStream();
			OutputStream outputStream = process.getOutputStream();

			String error = StreamUtils.readAsUTF8(errorStream);
			System.out.println("error : " + error);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
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
