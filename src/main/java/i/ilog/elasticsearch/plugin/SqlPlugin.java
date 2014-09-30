package i.ilog.elasticsearch.plugin;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;

public class SqlPlugin extends AbstractPlugin{
    public SqlPlugin(){};
    public String name() {
        return "sql";
    }

    public String description() {
        return "sql query by sql.";
    }

    public void onModule(RestModule module) {
        System.out.println(">>>>>>>>>>>init sql module"+module.toString());
        module.addRestAction(RestSqlAction.class);
    }

}
