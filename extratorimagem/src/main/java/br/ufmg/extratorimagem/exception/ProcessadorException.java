package br.ufmg.extratorimagem.exception;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ProcessadorException extends Exception
{
	private static final long serialVersionUID = -6224864304063710016L;

	/**
	 *
	 */
	public ProcessadorException()
	{
	}

	/**
	 * @param message
	 */
	public ProcessadorException(String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public ProcessadorException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ProcessadorException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
