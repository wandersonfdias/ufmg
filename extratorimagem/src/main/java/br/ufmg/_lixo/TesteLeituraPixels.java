package br.ufmg._lixo;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

public class TesteLeituraPixels
{

	public static void main(String[] args)
	{
		String diretorioBase = System.getenv("HOME") + "/extrai_descritores";
		String diretorioImagens = diretorioBase + "/imagens/imagens_diego";
		String imagem = diretorioImagens + "/0000bc538df09a1e445440826f765d41c7ac1d1e.jpg";
		imagem = diretorioImagens + "/0016fb6a9a65f3dbe12ae3f838640dd8c27ac35b.jpg";

		try
		{
			Mat img = Highgui.imread(imagem);

			File imgagefile = new File(imagem);
			BufferedImage read = ImageIO.read(imgagefile);

			WritableRaster raster = read.getRaster();

			DataBuffer dataBuffer = raster.getDataBuffer();

			byte[] pixels = (byte[])raster.getDataElements(0, 0, read.getWidth(), read.getHeight(), null);

			int[] pixels2 = raster.getPixels(0, 0, raster.getWidth(), raster.getHeight(), (int[])null);

			PixelGrabber grabber = new PixelGrabber(read, 0, 0, read.getWidth(), read.getHeight(), false);
			grabber.grabPixels();
			int[] pixels3 = (int[]) grabber.getPixels();


			int w = read.getWidth(null);
			int h = read.getHeight(null);
			int[] rgbs = new int[w*h];
			read.getRGB(0, 0, w, h, rgbs, 0, w);

//			int[] pixels = ((DataBufferInt)read.getRaster().getDataBuffer()).getData();

			System.out.println("fim");
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
	}

}
