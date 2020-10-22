package automation.ssh;

import automation.configuration.ProjectConfiguration;
import automation.mailtrap.MailTrapClient;
import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SSHClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSHClient.class);

    String host = ProjectConfiguration.getConfigProperty("SSH.ip"); //"134.213.56.147";//"34.107.9.80";
    String user = ProjectConfiguration.getConfigProperty("SSH.user"); //"forge";//"olex_dyachuk";
    int port = Integer.valueOf(ProjectConfiguration.getConfigProperty("SSH.port")); //22;
    String privateKey = ProjectConfiguration.getConfigProperty("SSH.key");//"/Users/odiachuk/.ssh/google_id";

    /**
     * Run SSH command
     * @param command sh command
     */
    public void runCommand(String command){
        try {
            JSch jsch = new JSch();

            jsch.addIdentity(privateKey);
            LOGGER.info("Identity added ");

            Session session = jsch.getSession(user, host, port);
            LOGGER.info("Session created.");

            // disabling StrictHostKeyChecking may help to make connection but makes it insecure
            // see http://stackoverflow.com/questions/30178936/jsch-sftp-security-with-session-setconfigstricthostkeychecking-no

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();

            Channel channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            channel.setInputStream(null);
            channel.setOutputStream(System.out);
            ((ChannelExec) channel).setErrStream(System.err);
            InputStream in = channel.getInputStream();
            StringBuilder outBuff = new StringBuilder();

            int exitStatus = -1;

            channel.connect();

            byte[] tmp=new byte[1024];
            while(true){
                while(in.available()>0){
                    int i=in.read(tmp, 0, 1024);
                    if(i<0)break;
                    LOGGER.info(new String(tmp, 0, i));
                }
                if(channel.isClosed()){
                    if(in.available()>0) continue;
                    exitStatus = channel.getExitStatus();
                    break;
                }
                try{
                    Thread.sleep(1000);
                }catch(Exception ee){

                }
            }
            channel.disconnect();
            session.disconnect();

            // print the buffer's contents
            LOGGER.info(outBuff.toString());
            // print exit status
            LOGGER.info("Exit status of the execution: " + exitStatus);
            if ( exitStatus == 0 ) {
                LOGGER.info (" (OK)\n");
            } else {
                LOGGER.info (" (NOK)\n");
            }


//            Channel channel = session.openChannel("shell"); //"sftp");
//            channel.setInputStream(System.in);
//            channel.setOutputStream(System.out);
//            channel.connect();
//            System.out.println("shell channel connected....");

//            ChannelSftp c = (ChannelSftp) channel;
//
//            String fileName = "test.txt";
//            c.put(fileName, "./in/");
//            c.exit();
//            System.out.println("done");

        } catch (Exception e) {
            System.err.println(e);
        }
    }

    //IN PROGRESS
    public void runCommandUsingPassword(String command) {
        String password = "";
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user,host, 22);
            session.setUserInfo(new SSHUserInfo(user, password));
            session.connect();
            Channel channel = session.openChannel("shell");
            channel.setInputStream(new ByteArrayInputStream(command.getBytes(StandardCharsets.UTF_8)));
            channel.setOutputStream(System.out);
            InputStream in = channel.getInputStream();
            StringBuilder outBuff = new StringBuilder();
            int exitStatus = -1;

            channel.connect();

            while (true) {
                for (int c; ((c = in.read()) >= 0);) {
                    outBuff.append((char) c);
                }

                if (channel.isClosed()) {
                    if (in.available() > 0) continue;
                    exitStatus = channel.getExitStatus();
                    break;
                }
            }
            channel.disconnect();
            session.disconnect();

            // print the buffer's contents
            LOGGER.info (outBuff.toString());
            // print exit status
            System.out.print ("Exit status of the execution: " + exitStatus);
            if ( exitStatus == 0 ) {
                LOGGER.info (" (OK)\n");
            } else {
                LOGGER.info(" (NOK)\n");
            }

        } catch (IOException | JSchException ioEx) {
            LOGGER.error(ioEx.toString());
        }
    }
}
