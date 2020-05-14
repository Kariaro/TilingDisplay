package tiling.parser;

public class TilingProgram {
	private final String filePath;
	private final String[] lines;
	private int lineIndex = -1;
	
	private String currentLine;
	private int errorCode;
	
	public TilingProgram(String filePath, String content) {
		this.filePath = filePath;
		content = content.replace("\r\n", "\n");
		content = content.replace("\t", "  ");
		lines = content.split("\n");
	}
	
	public int length() {
		return lines.length;
	}
	
	public String getNextLine() {
		String next;
		do {
			if(++lineIndex >= lines.length) {
				currentLine = null;
				return null;
			}
			
			next = lines[lineIndex];
			currentLine = next;
		} while(next.trim().isEmpty() || next.trim().indexOf('#') == 0);
		return next;
	}
	
	public String getCurrentLine() {
		return currentLine;
	}
	
	public boolean hasMoreLines() {
		return lineIndex < lines.length;
	}
	
	public String getLine(int index) {
		return lines[index];
	}
	
	public int getLineIndex() {
		return lineIndex;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public boolean hasErrors() {
		return errorCode != 0;
	}
	
	public void addError(int code) {
		errorCode |= code;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
}
