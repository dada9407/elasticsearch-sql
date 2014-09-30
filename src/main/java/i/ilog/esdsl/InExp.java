package i.ilog.esdsl;

//import i.framework.ilog.search.elasticsearch.HTTPUtil;
//import i.framework.ilog.search.elasticsearch.Kelastic;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

public class InExp extends BooleanExp {
	protected List valueList = new ArrayList();
	protected Column column = null;
    protected EsQueryBuilder builder;

	public List getValueList() {
		return valueList;
	}
	
	

	public void setValueList(List valueList) {
		this.valueList = valueList;
	}



	public Column getColumn() {
		return column;
	}



	public void setColumn(Column column) {
		this.column = column;
	}

    public EsQueryBuilder getBuilder() {
		return builder;
	}

	public void setBuilder(EsQueryBuilder builder) {
		this.builder = builder;
	}
    
	
	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		JSONObject in = new JSONObject();
        if(builder != null && valueList.size() == 0) {
            prepareHandle();
        }
		in.put(column.getName(),valueList);
		json.put("terms",in);
		return json;
	}
	
	@Override
	public String toQueryString() {
		String booleanExp = "(";
		boolean first = true;
        if(builder != null && valueList.size() == 0) {
            prepareHandle();
        }
		for(Object value : valueList){
			booleanExp += first?"":" OR ";
			booleanExp += " "+column.getName()+":"+(value instanceof Term?"\""+value+"\"":value)+" ";
			first = false;
		}
		booleanExp += ")";
		return booleanExp;
	}

    @Override
	public void prepareHandle() {
        //System.out.println(">>>>>>>>>>>>>>>>>>>prepareHandle:start");
		// TODO Auto-generated method stub
		/*if(builder.selectList == null || builder.selectList.size() == 0) {
			builder.selectList = new ArrayList<Column>(1);
            builder.selectList.add(column);
		} else if(builder.selectList.size() > 1){
			throw new RuntimeException("in的子查询select部分不能包含多个字段！");
		}
		builder.selectList.add(column);
          List<TableSource> list = builder.getTableSource();
          Kelastic k = new Kelastic();
          HTTPUtil util = new HTTPUtil();
          String jstr = builder.toJson();
          JSONObject json = JSONObject.fromObject(jstr);
          List indices = new ArrayList();
          for(TableSource ts : builder.getTableSource()){
        	  indices.add(ts.getIndex());
          }
          String result = util.post(k.index_path(StringUtils.join(indices, ",") + "/_search"),json);
        //System.out.println(">>>>>>>>>>>>>>>>>>>prepareHandle:end>>>>>"+result);
          getValueListOfSubquery(result);*/
	}
	
	private void getValueListOfSubquery(String result) {
		JSONObject obj = JSONObject.fromObject(result);
        if(!obj.containsKey("hits")) {
      	  throw new RuntimeException("in里面的子查询出错！错误信息：" + result);
        }
        if(obj.containsKey("aggregations")) {
        	JSONObject aggrs = obj.getJSONObject("aggregations");
        	String key = aggrs.keySet().iterator().next().toString();
        	JSONObject facet = aggrs.getJSONObject(key);
        	String key1 = facet.keySet().iterator().next().toString();
        	JSONArray fields = facet.getJSONArray(key1);
        	for (int i = 0; i < fields.size(); i++) {
        		JSONObject f = fields.getJSONObject(i);
        		valueList.add(new Term(String.valueOf(f.get("key"))));
			}
        } else {
	        JSONObject allHits = obj.getJSONObject("hits");
	        if(allHits.size() != 0) {
	        	JSONArray hits = allHits.getJSONArray("hits");
	        	for (int i = 0; i < hits.size(); i++) {
					JSONObject hit = hits.getJSONObject(i);
					JSONObject source = hit.getJSONObject("fields");
                    String columnName = builder.selectList.get(0).getName();
                    for(Object el : source.getJSONArray(columnName)){
                        valueList.add(new Term(String.valueOf(el)));
                    }
				}
	        }
        }
        obj.clear();
	}    
	
}
