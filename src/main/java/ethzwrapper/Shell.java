package ethzwrapper;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import common.ShellInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class Shell implements ShellInterface {
    protected Connection conn;
    private Integer exitStatus;

    public Shell() {
    }

    public Shell(String host, String username, String password) {
        connect(host, username, password);
    }

    public void connect(String host, String username, String password) {
        try{
            // ReporterNG.logComponent(String.format("Connect SSH: user=%s, user=%s, password=%s", serverAddress, username, "***"));
            conn = new Connection(host);
            conn.connect();
            boolean isAuthenticated = conn.authenticateWithPassword(username, password);
            // ReporterNG.logTechnical(isAuthenticated ? "Established" : "NOT established: Authentication failed.");
        }catch (IOException ex){
            // ReporterNG.logTechnical("NOT established: " + ex.getMessage());
        }
    }

    public void disconnect() {
        conn.close();
    }

    public Boolean isConnected() {
        return conn.isAuthenticationComplete();
    }

    public String exec(String command) {
        return exec(command, false);
    }

    public String exec(String command, Boolean setPty) {
        String out = "", errOut = "";
        try {
            // ReporterNG.logComponent(command);
            Session sess = conn.openSession();
            if (setPty)
                sess.requestDumbPTY();

            sess.execCommand(command);
            InputStream stdout = new StreamGobbler(sess.getStdout());
            InputStream stderr = new StreamGobbler(sess.getStderr());

            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdout));
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderr));

            while (true) {
                String line = stdoutReader.readLine();
                if (line == null)
                    break;
                out += line + "\n";
            }

            while (true){
                String line = stderrReader.readLine();
                if (line == null)
                    break;
                errOut += line + "\n";
            }
            exitStatus = sess.getExitStatus();
            out = out.trim();
            sess.close();
            // ReporterNG.logTechnical(out + "\nError output: " + errOut.trim() + "\nExitCode: " + exitStatus);
        }
        catch (IOException ex) {
            // ReporterNG.logFailed("Shell execution exception: " + ex.toString());
        }

        return out;
    }

    public Integer getExitStatus() {
        return exitStatus;
    }
}
