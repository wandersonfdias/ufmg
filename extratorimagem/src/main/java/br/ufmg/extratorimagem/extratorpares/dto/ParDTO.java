package br.ufmg.extratorimagem.extratorpares.dto;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ParDTO implements Serializable
{
	private static final long serialVersionUID = 4274166138893935495L;

	private File imagem1;
	private File imagem2;
	private Map<String, BigDecimal> distancias = new LinkedHashMap<String, BigDecimal>();
	private int classe;

	/**
	 * Construtor
	 * @param imagem1
	 * @param imagem2
	 * @param classe
	 */
	public ParDTO(File imagem1, File imagem2, int classe)
	{
		super();
		this.imagem1 = imagem1;
		this.imagem2 = imagem2;
		this.classe = classe;
	}

	/**
	 * @return the imagem1
	 */
	public File getImagem1()
	{
		return imagem1;
	}

	/**
	 * @return the imagem2
	 */
	public File getImagem2()
	{
		return imagem2;
	}

	/**
	 * @return the distancias
	 */
	public Map<String, BigDecimal> getDistancias()
	{
		return distancias;
	}

	/**
	 * @param distancias the distancias to set
	 */
	public void setDistancias(Map<String, BigDecimal> distancias)
	{
		this.distancias = distancias;
	}

	/**
	 * Obtém a classe do par
	 * @return
	 */
	public int getClasse()
	{
		return classe;
	}
}
