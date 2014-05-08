package jschwrapper;

import com.jcraft.jsch.*;
import common.ShellInterface;

import java.io.IOException;
import java.io.InputStream;


public class Shell implements ShellInterface {
    protected Session session;
    private Integer exitStatus;

    public Shell() {
    }

    public Shell(String host, String username, String password) {
        connect(host, username, password);
    }

    public void connect(String host, String username, String password) {
        try{
            // ReporterNG.logComponent(String.format("Connect SSH: user=%s, user=%s, password=%s", serverAddress, username, "***"));
            session = new JSch().getSession(username, host, 22);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setPassword(password);
            session.connect();
            // ReporterNG.logTechnical("Established");
        }catch (JSchException ex){
            // ReporterNG.logTechnical("NOT established: " + ex.getMessage());
        }
    }

    public void disconnect() {
        // ReporterNG.logComponent("Disconnect SSH");
        session.disconnect();
    }

    public Boolean isConnected() {
        return session.isConnected();
    }

    public String exec(String command) {
        return exec(command, false);
    }

    public String exec(String command, Boolean setPty) {
        InputStream in  = null;
        InputStream es = null;
        exitStatus = null;
        String out = "";
        try {
            // ReporterNG.logComponent(command);
            Channel channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err, true);

            in = channel.getInputStream();
            es = ((ChannelExec) channel).getErrStream();
            if (setPty)
                ((ChannelExec) channel).setPty(true);
            channel.connect();

            while (true) {
                out += readStream(in);
                if (channel.isClosed()) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                    // suppress
                }
            }
            exitStatus = channel.getExitStatus();
            String errorStream = readStream(es);
            // ReporterNG.logTechnical(out.trim() + "\nError stream: " + errorStream + "\nExit code " + exitStatus);
        } catch (IOException ex) {
            // ReporterNG.logFailed("Shell execution exception: " + ex.toString());
        } catch (JSchException ex) {
            // ReporterNG.logFailed("Shell execution exception: " + ex.toString());
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                // suppress
            }
            try {
                es.close();
            } catch (IOException ex) {
                // suppress
            }
        }
        return out.trim();
    }

    private String readStream(InputStream in) throws IOException {
        StringBuilder out = new StringBuilder();
        byte[] tmp = new byte[1024];
        while (in.available() > 0) {
            int i = in.read(tmp, 0, 1024);
            if (i < 0) {
                break;
            }
            out.append(new String(tmp));
        }
        return out.toString();
    }

    public Integer getExitStatus() {
        return exitStatus;
    }
}
