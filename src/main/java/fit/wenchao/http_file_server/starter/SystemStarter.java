package fit.wenchao.http_file_server.starter;

import fit.wenchao.http_file_server.ConfigFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SystemStarter {
    @Autowired
    ConfigFile configFile;

    @PostConstruct
    public void systemStart() {
        if(!configFile.exists()) {
            // create config file if not exists
            configFile.create();
        }

    }
}
