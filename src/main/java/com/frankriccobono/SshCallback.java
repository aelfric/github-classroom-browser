package com.frankriccobono;

import com.frankriccobono.github.EnvironmentConstants;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;

class SshCallback implements TransportConfigCallback {
    @Override
    public void configure(Transport transport) {
        SshTransport sshTransport = (SshTransport) transport;
        sshTransport.setSshSessionFactory(new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                // no configuration necessary
            }

            @Override
            protected JSch getJSch(OpenSshConfig.Host hostConfig,
                                   FS filesystem) throws JSchException {
                JSch jSch = super.getJSch(hostConfig, filesystem);
                jSch.addIdentity(
                    EnvironmentConstants.PRIVATE_KEY,
                    EnvironmentConstants.PASSPHRASE
                );
                return jSch;
            }
        });
    }
}
