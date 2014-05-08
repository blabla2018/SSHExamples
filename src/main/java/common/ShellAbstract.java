package common;


public abstract class ShellAbstract {
    private Integer exitStatus;

    public void connect(String host, String username, String password){
    }

    public void disconnect() {
    }

    public Boolean isConnected() {
        return false;
    }

    public String exec(String command) {
        return null;
    }

    public String exec(String command, Boolean setPty) {
        return null;
    }

    public Integer getExitStatus() {
        return null;
    }

    private long getLongFromExecution(String command) {
        String output = exec(command);
        return output.isEmpty() ? 0 : Long.parseLong(output);
    }

    public long getLineCountInFile(String filename){
        long result = getLongFromExecution("wc " + filename + " -l  | cut -d' ' -f1");
        // ReporterNG.logComponent("Lines in file: " + result);
        return result;
    }

    public String getLastLineInFile(String filename){
        return exec("tail -n1 " + filename + "  | cut -d' ' -f1");
    }

    public long getLastLineNumberWithText(String filename, String searchText){
        long result = getLongFromExecution("less " + filename + " | grep -n '" + searchText + "' | tail -n1 | cut -d':' -f1");
        // ReporterNG.logComponent("Last line number with text '" + searchText + "' in file: " + result);
        return result;
    }

    public boolean hasErrorInLog(String filename, long readFromLine){
        return hasTextInLog(filename, readFromLine, "|0x[0-9A-F]\\{8\\}|-1|");
    }

    public boolean hasErrorInLog(String filename, String readFromText){
        return hasTextInLog(filename, readFromText, "|0x[0-9A-F]\\{8\\}|-1|");
    }

    public boolean hasTextInLog(String filename, long readFromLine, String searchText){
        return hasTextInLastNLines(filename, getLineCountInFile(filename) - readFromLine, searchText );
    }

    public boolean hasTextInLog(String filename, String sinceText, String searchText){
        // ReporterNG.logComponent("Reading log file " + filename);
        String output = exec("grep -A1000000 \"" + sinceText + "\" " + filename + " | grep \"" +searchText + "\" -a1 -n");
        // ReporterNG.logComponent(output);
        return exitStatus == 0 && !output.isEmpty();
    }

    public boolean hasTextInLastNLines(String filename, long nLines, String searchText){
        // ReporterNG.logComponent("Reading log file " + filename);
        String output = exec("tail -n " + nLines + " " +  filename + " | grep \"" +searchText + "\" -a1 -n");
        // ReporterNG.logComponent(output);
        return exitStatus == 0 && !output.isEmpty();
    }
}
