package i.ilog.esdsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class EsQueryBuilder {
	List<Column> selectList;
	List<TableSource> tableSource;
	List<Column> sortList;
	List<Column> groupList;
	BooleanExp filter = null;
	int from = 1;
	int size = 50;
	List<Column> aggsColumn;
	List<Facet> facets;
	
	public EsQueryBuilder(){
		this.selectList = new ArrayList<Column>();
		this.tableSource = new ArrayList<TableSource>();
		this.sortList = new ArrayList<Column>();
		this.groupList = new ArrayList<Column>(); 
		this.aggsColumn = new ArrayList<Column>();
		this.facets = new ArrayList<Facet>();
	}

	public List<Column> getSelectList() {
		return selectList;
	}

	public void setSelectList(List<Column> selectList) {
        for(Column c : selectList){
					if(c.getFunction() != null){
						if(isFacetFun(c)){
							aggsColumn.add(c);
						}
                    }
        }
		this.selectList = selectList;
	}
	
	
	
	

	public List<Column> getSortList() {
		return sortList;
	}

	public void setSortList(List<Column> sortList) {
		this.sortList = sortList;
	}

	public List<TableSource> getTableSource() {
		return tableSource;
	}

	public void setTableSource(List<TableSource> tableSource) {
		this.tableSource = tableSource;
	}
	
	public void setFilter(BooleanExp filter){
		this.filter = filter;
	}

	public List<Column> getGroupList() {
		return groupList;
	}

	public void setGroupList(List<Column> groupList) {
		this.groupList = groupList;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	
	
	public List<Column> getAggsColumn() {
		return aggsColumn;
	}

	public void setAggsColumn(List<Column> aggsColumn) {
		this.aggsColumn = aggsColumn;
	}

	public List<Facet> getFacets() {
		return facets;
	}

	public void setFacets(List<Facet> facets) {
		this.facets = facets;
	}

	public static Map getAndMap(){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("and", new ArrayList());
		return map;
	}
	
	public static Map getOrMap(){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("or", new ArrayList());
		return map;
	}
	
	public static Map<String,Object> getNotMap(){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("not", new ArrayList());
		return map;
	}
	
	public static void addToAndMap(Map<String,Object> map,Object exp){
		((List)map.get("and")).add(exp);
	}
	
	public static void addToOrMap(Map<String,Object> map,Object exp){
		((List)map.get("or")).add(exp);
	}
	
	public static void addToNotMap(Map<String,Object> map,Object exp){
		((List)map.get("not")).add(exp);
	}
	
	public static void addAllToAndMap(Map<String,Object> map,List exp){
		((List)map.get("and")).addAll(exp);
	}
	
	public static void addAllToOrMap(Map<String,Object> map,List exp){
		((List)map.get("or")).add(exp);
	}
	
	public static void addAllToNotMap(Map<String,Object> map,List exp){
		((List)map.get("not")).add(exp);
	}

	@Override
	public String toString() {
		return /*"columns:"+selectList+",tableSource:"+tableSource+",from:"+from+",size:"+size+",sort:"+this.sortList+",group:"+this.getGroupList()
			+*/",filter:"+filter.toJson().toString();
		
	}
	
	public String toJson(){
		
		boolean haveFacetFunc = false;
		
		JSONObject json = new JSONObject();
		json.put("from", from);
		json.put("size", size);
		if(sortList.size()>0){
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
            if(sorts.size()>0){
			    json.put("sort", sorts);
            }
		}
		if(selectList.size()>0 && (!selectList.get(0).getName().equals("*"))){
			JSONArray fields = new JSONArray();
			JSONObject scriptFields = new JSONObject();
				for(Column c : selectList){
					if(c.getFunction() != null){
						if(isFacetFun(c)){
							haveFacetFunc = true;
							continue;
						}
						JSONObject scriptField = new JSONObject();
						scriptField.put("script", c.getFunction().toScript());
						scriptFields.put(c.getAlias()!=null?c.getAlias():c.getName(), scriptField);
					}else if(c.getAlias()!=null){
                        JSONObject scriptField = new JSONObject();
						scriptField.put("script", c.toScript());
						scriptFields.put(c.getAlias()!=null?c.getAlias():c.getName(), scriptField);
                    }else{
						fields.add(c.getName());
					}
				}
            if(fields.size()>0){
			    json.put("fields", fields);
            }
            if(scriptFields.size()>0){
			    json.put("script_fields", scriptFields);
            }
		}
		JSONObject rootQuery = new JSONObject();
			JSONObject filtered = new JSONObject();
			JSONObject query = new JSONObject();
			JSONObject querystring = new JSONObject();
			String queryStr = filter==null ? "*":filter.toQueryString();
			querystring.put("query", filter==null || queryStr== null || "".equals(queryStr.trim())?"*":queryStr);
			//querystring.put("default_field", "@message");
			query.put("query_string", querystring);
				filtered.put("query", query);
				if(filter != null){
					filtered.put("filter", filter.toJson());
				}
		rootQuery.put("filtered", filtered);
		json.put("query", rootQuery);
		
		if(groupList!=null && groupList.size()>0 || (haveFacetFunc&&facets.size()<1)){
            if(groupList!=null && groupList.size()>0){
			    json.put("aggs", getAggs());
            }else{
                addStatFacet(json);
            }
		}else if(facets.size()>0){
			addFacetAggs(json);
		}
		
		return json.toString();
	}
	
	public String toQueryString(){
		return filter.toQueryString();
	}
	
	private JSONObject getAggs(){
		JSONObject aggs = new JSONObject();
//			JSONObject aggs_facet = new JSONObject();
//				JSONObject term = new JSONObject();
//				boolean first = true;
//				String script = "";
//				for(Column c : groupList){
//					script += first?"":"+'~'+";
//					script += c.toScript();
//					first = false;
//				}
//				term.put("script", script);
				//修改aggs为多层group_by_field
				int length = groupList.size();
				List<JSONObject> aggsList = new ArrayList<JSONObject>();
				for (int i = length; i > 0; i--) {
					JSONObject groupbyMap = new JSONObject();
					JSONObject groupbyTermMap = new JSONObject();
					JSONObject groupbyfieldMap = new JSONObject();
					
					Column c = groupList.get(i - 1);
					String groupbyName = "group_by_" + c.toString();
                    if(c.isFunction()){
                        groupbyfieldMap.put("script", c.toScript());
                    }else{
                        groupbyfieldMap.put("field", c.toString());
                    }
					
                    groupbyfieldMap.put("size", size);
					groupbyTermMap.put("terms", groupbyfieldMap);
					groupbyMap.put(groupbyName, groupbyTermMap);
					aggsList.add(groupbyMap);
				}
				
				if(aggsList.size() > 0 && facets.size()>0){
					addFacetAggs(aggsList.get(0).getJSONObject("group_by_" + groupList.get(length - 1).toString()));
				}else if(aggsList.size() > 0 && aggsColumn.size()>0){
					addStatFacet(aggsList.get(0).getJSONObject("group_by_" + groupList.get(length - 1).toString()));
				}
				
				for (int i = 0; i < aggsList.size(); i++) {
					if(i < aggsList.size() - 1){
						Column c = groupList.get(length - 2 - i);
						((JSONObject) aggsList.get(i + 1).get("group_by_"  + c.toString())).put("aggs", aggsList.get(i));
					}
				}
				
				aggs = aggsList.get(aggsList.size() - 1);
//            if(!script.equals("")){
//			    aggs_facet.put("terms", term);
//            }

			
//		aggs.put("facet", aggs_facet);
		
		return aggs;
	}
	
	private void addStatFacet(JSONObject facet){
		JSONObject aggs = new JSONObject();
		boolean isHave = false;
		for(Column c : aggsColumn){
			if(!c.getFunction().getFunName().equalsIgnoreCase("count")){
					JSONObject stat = new JSONObject();
					JSONObject field = new JSONObject();
                    Column facet_col = (Column)c.getFunction().getParamList().get(0); //目前，只支持1个参数的统计函数           
					String facet_field=facet_col.getName();
					String funName = c.getFunction().getFunName();
                    if(facet_col.isFunction()){
                        field.put("script",facet_col.toScript());
                    }else{
					    field.put("field",facet_field);
                    }
					stat.put(funName, field);
					aggs.put(funName+"_"+(c.getAlias() != null ?c.getAlias():facet_field), stat);
					isHave = true;
			}
		}
		if(isHave)
			facet.put("aggs", aggs);
	}
	
	private void addFacetAggs(JSONObject root){
        JSONObject parent = root;
        for(Facet facet : this.facets){
            JSONObject facetJson = facet.toJson();
            if(facet instanceof DateHisFacet){
                 addStatFacet(facetJson.getJSONObject("facet"));
             }
             if(parent.containsKey("aggs")){
                 String key = (String)facetJson.keySet().iterator().next();
                 parent.getJSONObject("aggs").put(key,facetJson.get(key));
             }else{
                 parent.put("aggs", facetJson);
             }
             if(facet instanceof DateHisFacet){
                 parent = parent.getJSONObject("aggs").getJSONObject("facet");
             }else{
                 parent = parent.getJSONObject("aggs");
             }
        }
    }

	
	private boolean isFacetFun(Column c){
		Function fun = c.getFunction();
		if(innerIsFacetFun(c)) {
			return true;
		}
		for(Object obj : fun.getParamList()){
			if(obj instanceof Function){
				if(innerIsFacetFun((Column)obj)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean innerIsFacetFun(Column c){
		Function fun = c.getFunction();
		if(fun.getFunName().equalsIgnoreCase("count")
				|| fun.getFunName().equalsIgnoreCase("max")
				|| fun.getFunName().equalsIgnoreCase("min")
				|| fun.getFunName().equalsIgnoreCase("sum")
				|| fun.getFunName().equalsIgnoreCase("avg")){
			return true;
		}
		
		for(Object obj : fun.getParamList()){
			if(obj instanceof Column){
				if(innerIsFacetFun((Column)obj)) {
					return true;
				}
			}
		}
		return false;
	}
	
}
