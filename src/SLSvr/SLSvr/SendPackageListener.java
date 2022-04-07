package SLSvr.SLSvr;

import java.io.DataOutputStream;
import java.io.IOException;

public interface SendPackageListener {
    void send(DataOutputStream out, Package p) throws IOException;
}
