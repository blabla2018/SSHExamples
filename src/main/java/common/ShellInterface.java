package common;


public interface ShellInterface {
    public void connect(String host, String username, String password);
    public void disconnect();
    public Boolean isConnected();
    public String exec(String command);
    public String exec(String command, Boolean setPty);
    public Integer getExitStatus();
}
