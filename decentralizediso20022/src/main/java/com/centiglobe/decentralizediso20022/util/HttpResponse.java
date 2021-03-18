package com.centiglobe.decentralizediso20022.util;

import java.util.Enumeration;
import java.util.Hashtable;

public class HttpResponse {
    
    private static final String[][] statusMsg = new String[][] {
        new String[] {
            "Continue", "Switching Protocols", "Processing",
            "Early Hints"
        },
        new String[] {
            "OK", "Created", "Accepted",
            "Non-Authoritative Information", "No Content", "Reset Content",
            "Partial Content", "Multi-Status", "Already Reported",
            null, null, null,
            null, null, null,
            null, null, null,
            null, null, null,
            null, null, null,
            null, null, "IM Used"
        },
        new String[] {
            "Multiple Choices", "Moved Permanently", "Found",
            "See Other", "Not Modified", "Use Proxy",
            "Switch Proxy", "Temporary Redirect", "Permanent Redirect"
        },
        new String[] {
            "Bad Request", "Unauthorized", "Payment Required",
            "Forbidden", "Not Found", "Method Not Allowed",
            "Not Acceptable", "Proxy Authentication Required", "Request Timeout",
            "Conflict", "Gone", "Length Required",
            "Precondition Failed", "Payload Too Large", "URI Too Long",
            "Unsupported Media Type", "Range Not Satisfiable", "Expectation Failed",
            "I'm a teapot", null, null,
            "Misdirected Request", "Unprocessable Entity", "Locked",
            "Failed Dependency", "Too Early", "Upgrade Required",
            null, "Precondition Required", "Too Many Requests",
            null, "Request Header Fields Too Large", null,
            null, null, null,
            null, null, null,
            null, null, null,
            null, null, null,
            null, null, null,
            null, null, null,
            "Unavailable For Legal Reasons"
        },
        new String[] {
            "Internal Server Error", "Not Implemented", "Bad Gateway",
            "Service Unavailable", "Gateway Timeout", "HTTP Version Not Supported",
            "Variant Also Negotiates", "Insufficient Storage", "Loop Detected",
            null, "Not Extended", "Network Authentication Required"
        }
    };

    private int status;
    private Hashtable<String, String> headers;
    private String body;

    public HttpResponse(int status, Hashtable<String, String> headers, String body) {
        if (headers == null || body == null) {
            throw new NullPointerException();
        }
        String msg = null;
        try {
            msg = statusMsg[status / 100 - 1][status % 100];
        } catch (Exception e) { /* Ignore */ }
        if (msg == null) {
            throw new IllegalArgumentException();
        }
        this.status = status;
        this.headers = headers;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public String getStatusMessage() {
        return statusMsg[status / 100 - 1][status % 100];
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getBody() {
        return body;
    }

    public String getCookie(String name) {
        return getKey(getHeader("Set-Cookie"), name, ";");
    }

    private String getKey(String keyValues, String key, String separator) {
        if (keyValues != null && key != null && separator != null) {
            String[] tuples = keyValues.split(separator);

            for (int i = 0; i < tuples.length; i++) {
                String[] keyValue = tuples[i].split("=");
                if (keyValue.length == 2 && keyValue[0].trim().equals(key)) {
                    return keyValue[1];
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Enumeration<String> e = headers.keys(); e.hasMoreElements();) {
            String header = e.nextElement();
            builder.append(header + ": " + headers.get(header) + "\r\n");
        }
        return "HTTP/1.1 " + status + " " + getStatusMessage() + "\r\n" + builder.toString() + "\r\n" + body;
    }
}
