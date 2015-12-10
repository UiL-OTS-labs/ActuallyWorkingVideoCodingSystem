package view.panels;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.ApplicationPreferences;
import controller.Globals;


/**
 * Shows a windows where the user can select a directory where the
 * project should be saved
 */
public class CSVExportSelector {
	
	/**
	 * Single instance
	 */
	
	private JFileChooser chooser;
	private int returnVal;
	private static String[] extensions = {"csv"};
	
	private static final String EXPORT_AS_PROJECT_FILTER 	= "CSV file (project)";
	private static final String EXPORT_AS_OVERVIEW_FILTER 	= "CSV file (overview)";
	
	public static final int EXPORT_AS_PROJECT = 0;
	public static final int EXPORT_AS_OVERVIEW = 1;
	
	/**
	 * Private constructor
	 */
	public CSVExportSelector() { 
		show();
	}
	
	/**
	 * Shows a file selector for the video file to be used
	 * @return	Path of selected file if valid, otherwise null
	 */
	public void show()
	{
		ApplicationPreferences prefs = Globals.getInstance().getPreferencesModel();
		chooser = new JFileChooser();
		chooser.setDialogTitle("Export project to CSV");
		
		JLabel lbl = new JLabel("<html><body>Again, this is just a shitload of text<br/>"
				+ "to see what would happen of we did this on the ground<br>"
				+ "of I don't even know what I am lorum ipsum dolem anymore"
				+ "if you catch my drift that is the only thing that is important"
				+ "is to keep talking as the bomb would explode if I stopped talking now"
				+ "and we don't want no bombs to explode also beware of punctuation as "
				+ "the grammar nazi's will show up if you start with that one</body></html>");
		
//		lbl.setUI(MultiLineLabelUI.labelUI);
		
		chooser.add(lbl);
		
		FileNameExtensionFilter export_project = new FileNameExtensionFilter(
				EXPORT_AS_PROJECT_FILTER, extensions);
		
		FileNameExtensionFilter export_overview = new FileNameExtensionFilter(EXPORT_AS_OVERVIEW_FILTER, extensions);
	    
	 
	    chooser.addChoosableFileFilter(export_project);
	    chooser.addChoosableFileFilter(export_overview);
	    chooser.setFileFilter(export_project);
	    
	    File here2go = new File(prefs.getLastCSVDirectory(prefs.getLastProjectDirectory(System.getProperty("user.home"))));
	    chooser.setCurrentDirectory(here2go);
		chooser.setAcceptAllFileFilterUsed(false);
	    
	    returnVal = chooser.showSaveDialog(chooser);
	    
	    if(isApproved()) {
	    	prefs.setLastCSVDirectory(chooser.getSelectedFile().getParent());
	    }
	}
	
	/**
	 * Method to check if a valid file was selected
	 * @return	True if a valid file was selected
	 */
	public boolean isApproved(){
		return (returnVal == JFileChooser.APPROVE_OPTION);
	}
	
	/**
	 * Method to get the selected path
	 * @return	String	file path
	 * @throws IllegalStateException if the file was not approved
	 */
	public String getFilePath() throws IllegalStateException{
		if (!isApproved()){
			throw new IllegalStateException("File was not approved"); 
		}
		String path = chooser.getSelectedFile().getPath();
		if (!(path.endsWith(".csv") || path.endsWith(".CSV")))
			path += ".csv";
		return path;
	}
	
	/**
	 * Method to extract the selected export type
	 * @param chooser	The chooser where an export type is selected
	 * @return			The integer constant belonging to that type
	 */
	public int getExporterMethod(){
		if(chooser.getFileFilter().getDescription() == EXPORT_AS_OVERVIEW_FILTER){
			return EXPORT_AS_OVERVIEW;
		} else {
			return EXPORT_AS_PROJECT;
		}
	}
}
