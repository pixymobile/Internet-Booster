package com.evato.intrernetoptimizer.PingUtility;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

/**
 * Created by arahmatt on 2/25/2018.
 */
public class PingNative {

    // This class is not to be instantiated
    private PingNative() {
    }

    public static PingResult ping(InetAddress host, int timeOutMillis) throws IOException, InterruptedException {
        PingResult pingResult = new PingResult(host);
        StringBuilder echo = new StringBuilder();
        Runtime runtime = Runtime.getRuntime();

        int timeoutSeconds = timeOutMillis / 1000;
        if (timeoutSeconds < 0) timeoutSeconds = 1;

        String address = host.getHostAddress();
        String pingCommand = "ping";

        if (address != null) {
            if (IPTools.isIPv6Address(address)) {
                // If we detect this is a ipv6 address, change the to the ping6 binary
                pingCommand = "ping6";
            } else if (!IPTools.isIPv4Address(address)) {
                // Address doesn't look to be ipv4 or ipv6, but we could be mistaken
                Log.w("AndroidNetworkTools", "Could not identify " + address + " as ipv4 or ipv6, assuming ipv4");
            }
        } else {
            // Not sure if getHostAddress ever returns null, but if it does, use the hostname as a fallback
            address = host.getHostName();
        }

        Process proc = runtime.exec(pingCommand + " -c 1 -w " + timeoutSeconds + " " + address);
        proc.waitFor();
        int exit = proc.exitValue();
        String pingError;
        if (exit == 0) {
            InputStreamReader reader = new InputStreamReader(proc.getInputStream());
            BufferedReader buffer = new BufferedReader(reader);
            String line;
            while ((line = buffer.readLine()) != null) {
                echo.append(line).append("\n");
            }
            return getPingStats(pingResult, echo.toString());
        } else if (exit == 1) {
            pingError = "failed, exit = 1";
        } else {
            pingError = "error, exit = 2";
        }
        pingResult.error = pingError;
        return pingResult;
    }


    public static PingResult getPingStats(PingResult pingResult, String s) {
        Log.v("AndroidNetworkTools", "Ping String: " + s);
        String pingError;
        if (s.contains("0% packet loss")) {
            int start = s.indexOf("/mdev = ");
            int end = s.indexOf(" ms\n", start);
            pingResult.fullString = s;
            if (start == -1 || end == -1) {
                // TODO: We failed at parsing, maybe we should fix ;)
                pingError = "Error: " + s;
            } else {
                s = s.substring(start + 8, end);
                String stats[] = s.split("/");
                pingResult.isReachable = true;
                pingResult.result = s;
                pingResult.timeTaken = Float.parseFloat(stats[1]);
                return pingResult;
            }
        } else if (s.contains("100% packet loss")) {
            pingError = "100% packet loss";
        } else if (s.contains("% packet loss")) {
            pingError = "partial packet loss";
        } else if (s.contains("unknown host")) {
            pingError = "unknown host";
        } else {
            pingError = "unknown error in getPingStats";
        }
        pingResult.error = pingError;
        return pingResult;
    }
}
