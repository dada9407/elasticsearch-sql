package i.ilog.esdsl;

import java.util.List;
import java.util.ArrayList;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

public class TopHitsFacet implements ElementInterface,Facet{


    private int size;
	/**
	 * get the value of size
	 * @return the value of size
	 */
	public int getSize(){
		return this.size;
	}
	/**
	 * set a new value to size
	 * @param size the new value to be used
	 */
	public void setSize(int size) {
		this.size=size;
	}
    private List<Column> sortList;
	/**
	 * get the value of sortList
	 * @return the value of sortList
	 */
	public List<Column> getSortList(){
		return this.sortList;
	}
	/**
	 * set a new value to sortList
	 * @param sortList the new value to be used
	 */
	public void setSortList(List<Column> sortList) {
		this.sortList=sortList;
	}

    private List<Column> fields;
	/**
	 * get the value of fields
	 * @return the value of fields
	 */
	public List<Column> getFields(){
		return this.fields;
	}
	/**
	 * set a new value to fields
	 * @param fields the new value to be used
	 */
	public void setFields(List<Column> fields) {
		this.fields=fields;
	}

    public TopHitsFacet(){
        this.size = 0;
        this.sortList = new ArrayList<Column>();
        this.fields = new ArrayList<Column>();
    }

    public JSONObject toJson(){
        JSONObject json = new JSONObject();
			JSONObject facet = new JSONObject();
				JSONObject topHits = new JSONObject();
                    JSONArray sorts = new JSONArray();
                    for(Column c : sortList){
                        JSONObject s = new JSONObject();
                        if(c.isFunction()){
                            JSONObject script = new JSONObject();
                            script.put("script",c.toScript());
                            script.put("order",c.getSortType());
                            script.put("type","number");
                            s.put("_script",script);
                        }else{
                            s.put(c.getName(), c.getSortType());
                        }
                        sorts.add(s); 
                    }
				topHits.put("sort", sorts);
                
                JSONObject source = new JSONObject();
                JSONArray include = new JSONArray();
                boolean isAllColumn = false;
                for(Column c : fields){
                    isAllColumn = "all".equals(c.getType());
                    include.add(c.getName()); 
                }
                if(!isAllColumn){
                    source.put("include", include);
                    topHits.put("_source",source);
                }
				topHits.put("size", size);
			facet.put("top_hits", topHits);
		json.put("top_tag_hits", facet);
		return json;   
    };

    public void prepareHandle(){
    
    };
}
