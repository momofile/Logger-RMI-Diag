// SPDX-License-Identifier: MIT
// Co-creator: momofile
import java.io.File;
import java.rmi.Naming;
import reflect.nya.al.protocol.LoggerProtocol;

/** Standalone probe: verifies registry + all four remote methods without
 *  launching the game. Mirrors exactly what the client's patch pass does. */
public class LoggerProbe {
    public static void main(String[] args) throws Exception {
        String url = "rmi://localhost:1099/LoggerIsTrash";
        System.out.println("[probe] lookup " + url);
        LoggerProtocol p = (LoggerProtocol) Naming.lookup(url);
        System.out.println("[probe] lookup OK, stub = " + p.getClass().getName());

        p.hs();
        p.p("probe");
        System.out.println("[probe] gtk() = " + p.gtk());

        byte[] dll = p.gt("DebugNative.dll");
        String magic = dll.length >= 2
                ? String.format("%02X%02X", dll[0], dll[1]) : "--";
        System.out.println("[probe] gt(DebugNative.dll) = " + dll.length
                + " bytes, magic = " + magic);
        if (dll.length < 2 || dll[0] != 'M' || dll[1] != 'Z') {
            System.out.println("[probe] ERROR - DLL fallback is not a valid PE image");
            System.exit(2);
        }

        // A text resource must NEVER come back as PE bytes. That was the
        // root cause of the second crash round (see CHANGELOG.md).
        byte[] srg = p.gt("resource/189Srg.srg");
        boolean binary = srg.length >= 2 && srg[0] == 'M' && srg[1] == 'Z';
        System.out.println("[probe] gt(resource/189Srg.srg) = " + srg.length + " bytes"
                + (binary ? "  <-- ERROR: DLL bytes leaking into a text resource!"
                          : (srg.length == 0 ? "  (not supplied - see payload/README.md)"
                                             : "  (real map present)")));
        if (binary) {
            System.out.println("[probe] FAILED");
            System.exit(2);
        }

        // Local preflight: report which optional user-supplied files exist.
        File res = new File("payload/resource");
        System.out.println("[probe] local payload/resource/ = "
                + (res.isDirectory() ? res.getAbsolutePath() : "MISSING"));
        for (String n : new String[]{"189Srg.srg", "189Notch.srg", "DebugNative.dll"}) {
            File f = new File(n.equals("DebugNative.dll")
                    ? "payload" : "payload/resource", n);
            System.out.println("[probe]   " + n + " : " + (f.isFile() ? f.length() + " bytes" : "absent"));
        }

        System.out.println("[probe] ALL OK - RMI chain is usable");
    }
}
