package PDFAnalyzerActors.Model;

public class Initializer {
	
    private String directoryPath;
    private String toIgnoreFilePath;
    private int wordsToRetrieve;
    private boolean split;
       
    public String getDirectoryPath() {
    	return directoryPath;
    }

    public String getToIgnoreFilePath() {
    	return toIgnoreFilePath;
    }
    
    public int getWordsToRetrieve() {
    	return wordsToRetrieve;
    }
    
    public boolean getSplit() {
    	return split;
    }
    
    public void setDirectoryPath(String directoryPath) {
    	this.directoryPath = directoryPath;    	
    }
    
    public void setToIgnoreFilePath(String toIgnoreFilePath) {
    	this.toIgnoreFilePath = toIgnoreFilePath;    	
    }
    
    public void setWordsToRetrive(int wordsToRetrieve) {
    	this.wordsToRetrieve = wordsToRetrieve;
    }
    
    public void setSplit(boolean split) {
    	this.split = split;
    }
}
