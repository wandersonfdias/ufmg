package br.ufmg.extratorimagem.exception;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class DescriptorsNotFoundException extends ProcessadorException
{
	private static final long serialVersionUID = 624537106852580459L;

	public DescriptorsNotFoundException()
	{
		super();
	}

	public DescriptorsNotFoundException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public DescriptorsNotFoundException(String message)
	{
		super(message);
	}

	public DescriptorsNotFoundException(Throwable cause)
	{
		super(cause);
	}
}
