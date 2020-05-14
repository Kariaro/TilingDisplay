package render.gui;

import java.awt.Window.Type;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import render.main.Tiling;
import tiling.util.TilingUtil;

public class GuiFileChooser {
	public static JFileChooser fileChooser;
	public static JFrame parentFrame;
	
	private FileFilter TILING_FILTER = new FileFilter() {
		public String getDescription() { return "Tiling Files"; }
		public boolean accept(File f) {
			String name = f.getName().toLowerCase();
			
			if(Tiling.DEBUG) {
				if(name.endsWith(".debug")) return true;
			}
			
			return f.isDirectory() || name.endsWith(".example") || name.endsWith(".tiling");
		}
	};
	
	public GuiFileChooser() {
		TilingUtil.execute(() -> {
			UIManager.put("FileChooser.readOnly", Boolean.TRUE);
			
			fileChooser = new JFileChooser();
			fileChooser.addChoosableFileFilter(TILING_FILTER);
			fileChooser.setFileFilter(TILING_FILTER);
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setRequestFocusEnabled(true);
			
			parentFrame = new JFrame();
			parentFrame.setType(Type.POPUP);
			parentFrame.setIconImage(Tiling.ICON);
			parentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			parentFrame.setSize(0, 0);
			parentFrame.setLocation(-1000, -1000);
			parentFrame.setAlwaysOnTop(true);
		});
	}
	
	private volatile boolean hasSelection;
	private boolean isSelecting;
	
	private File selectedFile;
	private long lastModified;
	
	public void openDialog() {
		if(isSelecting || parentFrame == null) return;
		isSelecting = true;
		
		SwingUtilities.invokeLater(() -> {
			parentFrame.setVisible(true);
			parentFrame.requestFocus();
			
			fileChooser.setCurrentDirectory(Tiling.customTilingFolder);
			
			int returnVal = fileChooser.showDialog(parentFrame, "Open");
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				File last = this.selectedFile;
				this.selectedFile = fileChooser.getSelectedFile();
				
				if(selectedFile != null) {
					if(last == null) {
							this.hasSelection = true;
					} else {
						if(!last.getAbsolutePath().equals(selectedFile.getAbsolutePath())) {
							this.hasSelection = true;
						}
					}
				}
				
				this.lastModified = selectedFile.lastModified();
			}
			
			parentFrame.setVisible(false);
			isSelecting = false;
		});
	}
	
	public void setSelectedFile(File file) {
		this.selectedFile = file;
		if(file != null) {
			this.lastModified = file.lastModified();
		}
	}
	
	public File getSelectedFile() {
		return selectedFile;
	}
	
	public String getAbsolutePath() {
		if(selectedFile == null) return null;
		return selectedFile.getAbsolutePath();
	}
	
	public boolean hasSelectedFile() {
		return selectedFile != null;
	}
	
	public String getFileName() {
		if(selectedFile == null) return null;
		return selectedFile.getName();
	}
	
	public synchronized boolean hasSelectionChanged() {
		boolean selection = hasSelection;
		hasSelection = false;
		return selection;
	}
	
	public synchronized boolean hasFileChanged() {
		if(selectedFile == null) return false;
		
		long currentTime = selectedFile.lastModified();
		if(lastModified != currentTime) {
			lastModified = currentTime;
			
			return selectedFile.length() != 0;
		}
		
		return false;
	}
}
