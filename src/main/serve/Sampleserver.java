import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;

import static org.apache.log4j.lf5.util.StreamUtils.getBytes;

public class Sampleserver {

    private static Logger logger = Logger.getLogger(Sampleserver.class.getName());

    public static void main(String[] args) throws Exception {
        Map<String, Object> map = new HashMap<>();
        logger.info("hi");
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        //server.createContext("/test", new MyHandler());
        StaXParser read = new StaXParser();
        List<ContextParameters> readConfig = read.readConfig("src/main/webapp/WEB-INF/web.xml");
        for (ContextParameters item : readConfig) {
            //Class cls = ClassLoader.getSystemClassLoader().loadClass(item.getServlet_class());
            HttpServlet servlet = (HttpServlet) Class.forName(item.getServlet_class()).newInstance();


            map.put(item.getUrl_pattern(), servlet);

            server.createContext(item.getUrl_pattern(), new MyHandler(map));
            // server.createContext("/"+item.getUrl_pattern());
        }
        server.setExecutor(null); // creates a default executor
        server.start();

    }

    static class MyHandler implements HttpHandler {
        private HttpServlet servlet;
        private Map map;
        private InputStream ex;

        public MyHandler() {

        }

        public MyHandler(Map<String, Object> map) {
            this.map = map;
        }


        private final class RequestWrapper extends HttpServletRequestWrapper {
            private final HttpExchange ex;
            private final Map<String, String[]> postData;
            //private final ServletInputStream is;
            private final Map<String, Object> attributes = new HashMap<>();

            private RequestWrapper(HttpServletRequest request, HttpExchange ex, Map<String, String[]> postdata) {
                super(request);
                this.ex = ex;
                this.postData = postdata;
                //   this.is = inputStream;
            }

            @Override
            public String getHeader(String name) {
                return ex.getRequestHeaders().getFirst(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                return new Vector<String>(ex.getRequestHeaders().get(name))
                        .elements();
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                return new Vector<String>(ex.getRequestHeaders().keySet())
                        .elements();
            }

            @Override
            public Object getAttribute(String name) {
                return attributes.get(name);
            }

            @Override
            public void setAttribute(String name, Object o) {
                this.attributes.put(name, o);
            }

            @Override
            public Enumeration<String> getAttributeNames() {
                return new Vector<String>(attributes.keySet()).elements();
            }

            @Override
            public String getMethod() {
                return ex.getRequestMethod();
            }

            @Override
            public ServletInputStream getInputStream() throws IOException {
                // return is;
                return null;
            }

            @Override
            public BufferedReader getReader() throws IOException {
                return new BufferedReader(new InputStreamReader(
                        getInputStream()));
            }

            @Override
            public String getPathInfo() {
                return ex.getRequestURI().getPath();
            }

            @Override
            public String getParameter(String name) {
                String[] arr = postData.get(name);
                return arr != null ? (arr.length > 1 ? Arrays.toString(arr)
                        : arr[0]) : null;
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return postData;

            }

            @Override
            public Enumeration<String> getParameterNames() {
                return new Vector<String>(postData.keySet()).elements();

            }
        }

        private final class ResponseWrapper extends HttpServletResponseWrapper {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final ServletOutputStream servletOutputStream = new ServletOutputStream() {

                @Override
                public boolean isReady() {
                    return false;
                }

                @Override
                public void setWriteListener(WriteListener writeListener) {

                }

                @Override
                public void write(int b) throws IOException {
                    outputStream.write(b);
                }
            };

            private final HttpExchange ex;
            private final PrintWriter printWriter;
            private int status = HttpServletResponse.SC_OK;

            private ResponseWrapper(HttpServletResponse response,
                                    HttpExchange ex) {
                super(response);
                this.ex = ex;
                printWriter = new PrintWriter(servletOutputStream);
            }

            @Override
            public void setContentType(String type) {
                ex.getResponseHeaders().add("Content-Type", type);
            }

            @Override
            public void setHeader(String name, String value) {
                ex.getResponseHeaders().add(name, value);
            }

            @Override
            public javax.servlet.ServletOutputStream getOutputStream() {
                return servletOutputStream;
            }

            @Override
            public void setContentLength(int len) {
                ex.getResponseHeaders().add("Content-Length", len + "");
            }

            @Override
            public void setStatus(int status) {
                this.status = status;
            }

            @Override
            public void sendError(int sc, String msg) {
                this.status = sc;
                if (msg != null) {
                    printWriter.write(msg);
                }
            }

            @Override
            public void sendError(int sc) {
                sendError(sc, null);
            }

            @Override
            public PrintWriter getWriter() {
                return printWriter;
            }

            public void complete() throws IOException {
                try {
                    printWriter.flush();
                    ex.sendResponseHeaders(status, outputStream.size());
                    if (outputStream.size() > 0) {
                        ex.getResponseBody().write(outputStream.toByteArray());
                    }
                    ex.getResponseBody().flush();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    ex.close();
                }
            }
        }


        @Override
        public void handle(HttpExchange ex) throws IOException {


            String is = ex.getRequestURI().toString();
            String[] inputurl = is.toString().split("\\?");
            Iterator<Map.Entry<String, Object>> itr = map.entrySet().iterator();


            while (itr.hasNext()) {
                Map.Entry<String, Object> entry = itr.next();

                if (entry.getKey().equals(inputurl[0])) {


                    this.servlet = (HttpServlet) entry.getValue();

                }

            }

            InputStream input = ex.getRequestBody();
            StringBuilder stringBuilder = new StringBuilder();

            new BufferedReader(new InputStreamReader(input))
                    .lines()
                    .forEach((String s) -> stringBuilder.append(s + "\n"));


            final byte[] inBytes = getBytes(ex.getRequestBody());
            ex.getRequestBody().close();

            final ByteArrayInputStream newInput = new ByteArrayInputStream(inBytes);


            Map<String, String[]> parsePostData = new HashMap<>();


            try {
                Map<String, String[]> result = splitQuery(stringBuilder.toString());
                if (result != null)
                    parsePostData.putAll(result);

            } catch (IllegalArgumentException e) {
                newInput.reset();
            }
            final Map<String, String[]> postdata = parsePostData;
            RequestWrapper req = new RequestWrapper(createUnimplementAdapter(HttpServletRequest.class), ex, postdata);
            ResponseWrapper resp = new ResponseWrapper(createUnimplementAdapter(HttpServletResponse.class), ex);
            try {

                servlet.service(req, resp);
            } catch (ServletException e) {
                e.printStackTrace();
            }
            resp.complete();

        }
    }


    @SuppressWarnings("unchecked")
    private static <T> T createUnimplementAdapter(Class<T> httpServletApi) {
        class UnimplementedHandler implements InvocationHandler {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {
                throw new UnsupportedOperationException("Not implemented: "
                        + method + ", args=" + Arrays.toString(args));
            }
        }

        return (T) Proxy.newProxyInstance(
                UnimplementedHandler.class.getClassLoader(),
                new Class<?>[]{httpServletApi},
                new UnimplementedHandler());
    }

    public static Map<String, String[]> splitQuery(String url) throws UnsupportedEncodingException {
        if (url == null) {
            return null;
        }
        final Map<String, String[]> query_pairs = new LinkedHashMap<>();
        final String[] pairs = url.split("&");
        for (String pair : pairs) {

            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new String[4]);
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            query_pairs.put(key, new String[]{value});
        }
        return query_pairs;
    }


}






