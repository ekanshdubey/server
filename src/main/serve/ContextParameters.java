

public class ContextParameters {
    private String Servlet_name;
    private String Servlet_class;
    private String Url_pattern;
    private String id;


    void setServlet_name(String servlet_name) {
        this.Servlet_name = servlet_name;
    }

    void setServlet_class(String servlet_class) {
        this.Servlet_class = servlet_class;
    }

    void setUrl_pattern(String url_pattern) {
        this.Url_pattern = url_pattern;
    }

    void setId(String id1) {
        this.id = id1;
    }

    String getServlet_name() {
        return Servlet_name;
    }

    String getServlet_class() {
        return Servlet_class;
    }

    String getUrl_pattern() {
        return Url_pattern;
    }

    @Override
    public String toString() {
        return "Item [servlet name =" + Servlet_name + " servlet class =" + Servlet_class + " Url pattern = " + Url_pattern + " ]";
    }
}


