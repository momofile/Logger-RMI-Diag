// SPDX-License-Identifier: MIT
// Co-creator: momofile
package reflect.nya.al.protocol;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Minimal interoperability declaration, reconstructed from the public method
 * signatures of the remote interface used by the target client.
 *
 * Only the four method signatures are reproduced, because a Java RMI stub
 * cannot be cast without a class of exactly this name and shape. No
 * implementation code from the upstream project is included here.
 * See NOTICE.md for the full third-party statement.
 */
public interface LoggerProtocol extends Remote {
    byte[] gt(String name) throws RemoteException;
    void hs() throws RemoteException;
    void p(String message) throws RemoteException;
    String gtk() throws RemoteException;
}
