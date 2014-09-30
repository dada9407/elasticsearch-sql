package i.ilog.elasticsearch.plugin;
import i.ilog.esdsl.EsSqlLexer;
import i.ilog.esdsl.EsSqlParser;
import i.ilog.esdsl.TableSource;
import i.ilog.esdsl.EsQueryBuilder;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.client.Client;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestChannel;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.ActionListener;
import java.util.Map;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.RestStatus;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.action.support.IndicesOptions;
import org.antlr.runtime.RecognitionException;
import org.elasticsearch.rest.action.support.RestStatusToXContentListener;
import java.util.HashMap;

public class RestSqlAction extends BaseRestHandler{

    private Map<String,EsQueryBuilder> sql_cache;

    @Inject
    public RestSqlAction(Settings settings,Client client,RestController restController){
        super(settings,client);
        sql_cache = new HashMap<String,EsQueryBuilder>();
        restController.registerHandler(RestRequest.Method.POST,"/_sql/_explain",this);
        restController.registerHandler(RestRequest.Method.GET,"/_sql/_explain",this);
        restController.registerHandler(RestRequest.Method.POST,"/_sql",this);
        restController.registerHandler(RestRequest.Method.GET,"/_sql",this);
    }

    @Override
    public void handleRequest(final RestRequest request,final RestChannel channel, final Client client){
        String sql = request.param("sql"); 
        // get the content, and put it in the body
        if(sql == null || "".equals(sql.trim())){
            if (request.hasContent()) {
                sql = new String(request.content().toBytes());
            } else {
                String source = request.param("source");
                if (source != null) {
                    sql = source;
                }
            }
        }
        
        logger.info("++++++++++>sql is [{}]",sql);
        try{
            EsQueryBuilder builder = null;
            builder = parse(sql);


            logger.info("++++++++++>sql json is :[{}]",builder.toJson());
            if(request.path().endsWith("/_explain")){
                BytesRestResponse bytesRestResponse = new BytesRestResponse(RestStatus.OK,builder.toJson());
                channel.sendResponse(bytesRestResponse);
            }else{
                SearchRequest searchRequest = createSearchRequest(request,builder);
                searchRequest.listenerThreaded(false);
                client.search(searchRequest, new RestStatusToXContentListener<SearchResponse>(channel));
            }
        }catch(Exception e){
            logger.error(e.getMessage(),e);
            try{
                BytesRestResponse bytesRestResponse = new BytesRestResponse(channel,e);
                channel.sendResponse(bytesRestResponse);
            }catch(IOException e1){
                logger.error(e1.getMessage(),e1); 
            }

            return ;
        }
    }

    private EsQueryBuilder parse(String sql) throws RecognitionException{
        if(sql_cache.get(sql) != null) return sql_cache.get(sql);
        ANTLRStringStream source = new ANTLRStringStream(sql);
        EsSqlLexer lex = new EsSqlLexer(source);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        EsSqlParser g = new EsSqlParser(tokens, null);

        EsQueryBuilder builder = g.selectStatement();   
        sql_cache.put(sql,builder);
        return builder;
    }

    private SearchRequest createSearchRequest(RestRequest request,EsQueryBuilder builder){

        List<String> indices = new ArrayList<String>();
        List<String> types = new ArrayList<String>();
        boolean isAll = true;
        for(TableSource ts : builder.getTableSource()){
            if(ts.getIndex() == null) {
            }else if(ts.getIndex().equals("*")){
                isAll = true;
            }else{
                indices.add(ts.getIndex());
            }

            if(ts.getType() != null){
                types.add(ts.getType());
            }
        }

        SearchRequest searchRequest = new SearchRequest(indices.toArray(new String[0]));
        searchRequest.source(builder.toJson());
        

        searchRequest.types(types.toArray(new String[0]));
        searchRequest.routing(request.param("routing"));
        searchRequest.preference(request.param("preference"));
        searchRequest.indicesOptions(IndicesOptions.fromRequest(request, searchRequest.indicesOptions()));

        return searchRequest;
    }
}
