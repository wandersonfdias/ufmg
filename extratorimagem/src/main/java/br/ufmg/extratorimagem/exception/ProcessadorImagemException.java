package br.ufmg.extratorimagem.exception;

/**
 * @author Wanderson Ferreira Dias - <code>wandersonf.dias@gmail.com</code>
 */
public class ProcessadorImagemException extends ProcessadorException
{
	private static final long serialVersionUID = -2144323208561683124L;

	/**
	 *
	 */
	public ProcessadorImagemException()
	{
		super();
	}

	/**
	 * @param message
	 */
	public ProcessadorImagemException(String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public ProcessadorImagemException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ProcessadorImagemException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
