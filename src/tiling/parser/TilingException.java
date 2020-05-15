package tiling.parser;

import java.util.logging.Logger;

public class TilingException extends Exception {
	private static final Logger LOGGER = Logger.getLogger("TilingLoader");
	
	private static final long serialVersionUID = 1L;
	
	public final Exception cause;
	public final TilingProgram program;
	public final String message;
	public final int pointer;
	public final int errorCode;
	
	public TilingException(TilingProgram program, String message, int errorCode) {
		this(program, message, -1, errorCode, null);
	}
	
	public TilingException(TilingProgram program, String message, int pointer, int errorCode) {
		this(program, message, pointer, errorCode, null);
	}
	
	public TilingException(TilingProgram program, String message, int errorCode, Exception cause) {
		this(program, message, -1, errorCode, cause);
	}
	
	public TilingException(TilingProgram program, String message, int pointer, int errorCode, Exception cause) {
		this.errorCode = errorCode;
		this.message = message;
		this.pointer = pointer;
		this.program = program;
		this.program.addError(errorCode);
		this.cause = cause;
	}
	
	@Override
	public void printStackTrace() {
		String line = program.getCurrentLine();
		LOGGER.warning("#Error in '" + program.getFilePath() + "'");
		LOGGER.warning("");
		if(line != null) {
			if(line.length() > 256) line = line.substring(0, 256) + " ...";
			
			LOGGER.warning("Invalid command on line (" + program.getLineIndex() + ") '" + line + "'");
			LOGGER.warning(line);
		}
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < pointer; i++) sb.append("-");
		LOGGER.warning(sb.append("^ ").append(message).toString());
		
		if(cause != null) {
			cause.printStackTrace();
		}
	}
}
