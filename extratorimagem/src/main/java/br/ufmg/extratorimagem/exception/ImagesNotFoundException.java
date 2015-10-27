package br.ufmg.extratorimagem.exception;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ImagesNotFoundException extends ProcessadorException
{
	private static final long serialVersionUID = 5183580370401638996L;

	public ImagesNotFoundException()
	{
		super();
	}

	public ImagesNotFoundException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ImagesNotFoundException(String message)
	{
		super(message);
	}

	public ImagesNotFoundException(Throwable cause)
	{
		super(cause);
	}
}
