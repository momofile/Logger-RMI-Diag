// SPDX-License-Identifier: MIT
// Co-creator: momofile
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import reflect.nya.al.protocol.LoggerProtocol;

/**
 * A minimal local RMI service used to reproduce and diagnose a class of
 * client crashes caused by a missing on-host dependency.
 *
 * The target client performs, during its startup patch pass:
 *   Naming.lookup("rmi://localhost:1099/LoggerIsTrash")
 *   -> cast to reflect.nya.al.protocol.LoggerProtocol
 * When no registry listens on 1099, the resulting ConnectException is
 * rethrown as a RuntimeException onto the render thread and the client dies.
 *
 * Design notes learned the hard way (see CHANGELOG.md):
 *  - gt() is a GENERIC resource fetch, not just a DLL fetch. Text resources
 *    such as "resource/189Srg.srg" also arrive here. Returning PE bytes for
 *    a text resource corrupts the mapping parser and produces a downstream
 *    NullPointerException that looks like a completely unrelated bug.
 *  - p() must never throw: the client calls System.exit(1) in its catch
 *    branch, which closes the game without any crash report.
 *  - Every call is logged so a missing resource is visible in one line.
 *
 * No third-party resource files are bundled. See payload/README.md.
 */
public class LoggerServer {
    public static final int PORT = 1099;
    public static final String NAME = "LoggerIsTrash";
    public static final String URL = "rmi://localhost:" + PORT + "/" + NAME;

    private static final File PAYLOAD_DIR = new File("payload").getAbsoluteFile();
    private static final File REQUEST_LOG = new File("requests.log").getAbsoluteFile();
    private static final String[] MAP_NAMES = {"resource/189Srg.srg", "resource/189Notch.srg"};
    /** -Ddiag.redact=true hides client echo text in the log. Use before sharing logs. */
    private static final boolean REDACT = Boolean.getBoolean("diag.redact");

    public static final class Impl extends UnicastRemoteObject implements LoggerProtocol {
        private static final long serialVersionUID = 1L;

        Impl() throws RemoteException {
            super(0); // anonymous export port; the registry stub carries it
        }

        /** First call after lookup(). Must not throw. */
        public void hs() {
            log("[hs ] handshake");
        }

        /** Progress / credential echo from the client. Must not throw. */
        public void p(String message) {
            if (REDACT) {
                log("[p  ] <redacted, " + (message == null ? 0 : message.length()) + " chars>");
            } else {
                log("[p  ] " + message);
            }
        }

        /** Token / key request. A non-null placeholder is sufficient. */
        public String gtk() {
            log("[gtk] -> stub-key");
            return "stub-key";
        }

        /**
         * Generic resource fetch. Lookup order:
         *   1. payload/<requested path>
         *   2. payload/<basename>
         *   3. for *.dll only: the built-in no-op PE image
         *   4. otherwise: empty bytes + a MISS entry
         */
        public byte[] gt(String name) throws RemoteException {
            String raw = (name == null) ? "" : name.trim().replace('\\', '/');
            String base = new File(raw).getName();
            if (raw.startsWith("/") || raw.contains("..") || base.contains("..")) {
                throw new RemoteException("illegal resource name: " + name);
            }

            File[] candidates = { new File(PAYLOAD_DIR, raw), new File(PAYLOAD_DIR, base) };
            for (File f : candidates) {
                if (!f.isFile()) continue;
                try {
                    byte[] b = Files.readAllBytes(f.toPath());
                    log("[gt ] " + raw + " -> " + b.length + " bytes (payload: " + f.getName() + ")");
                    return b;
                } catch (IOException e) {
                    throw new RemoteException("cannot read " + f, e);
                }
            }

            if (raw.toLowerCase().endsWith(".dll")) {
                byte[] b = StubDll.bytes();
                log("[gt ] " + raw + " -> " + b.length + " bytes (built-in stub DLL)");
                return b;
            }

            log("[gt ] MISS " + raw + " -> 0 bytes; place the file at payload/" + raw);
            return new byte[0];
        }
    }

    private static synchronized void log(String s) {
        String line = "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + s;
        System.out.println(line);
        System.out.flush();
        try (FileWriter w = new FileWriter(REQUEST_LOG, true)) {
            w.write(line + "\r\n");
        } catch (IOException ignored) {
            // console output is enough if the log file is locked
        }
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        try {
            LocateRegistry.createRegistry(PORT);
            log("[boot] registry created on 127.0.0.1:" + PORT);
        } catch (ExportException e) {
            log("[boot] port " + PORT + " already has a registry, reusing it");
        }

        Impl impl = new Impl();
        Naming.rebind(URL, impl);
        log("[boot] bound " + URL);
        log("[boot] payload dir : " + PAYLOAD_DIR);
        log("[boot] request log  : " + REQUEST_LOG);
        boolean haveMap = false;
        for (String m : MAP_NAMES) {
            if (new File(PAYLOAD_DIR, m).isFile()) haveMap = true;
        }
        if (!haveMap) {
            log("[boot] WARNING: no 1.8.9 srg map in payload/resource/.");
            log("[boot]          The client will parse an empty map and fail later.");
            log("[boot]          See payload/README.md - resource files are user-supplied.");
        }
        log("[boot] ready. Ctrl+C to stop. -Ddiag.redact=true hides client echo text.");

        Object lock = new Object();
        synchronized (lock) {
            lock.wait();
        }
    }
}
