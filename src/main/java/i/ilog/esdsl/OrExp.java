package i.ilog.esdsl;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class OrExp extends BooleanExp {	
	
	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		JSONArray expjson = new JSONArray();
		for (BooleanExp exp : exps) {
			if(exp == null) continue;
			JSONObject ej = exp.toJson();
			if(ej == null || ej.size() == 0){
				continue;
			}
			expjson.add(ej);
		}
		if(expjson.size()>0){
			json.put("or", expjson);
		}
		return json;
	}
	
	@Override
	public String toQueryString() {
		String booleanExp = "";
		boolean first = true;
		for (BooleanExp exp : exps) {
			String query = exp.toQueryString();
			if(query == null || query.trim().length()==0){
				continue;
			}
			
			booleanExp += first?"":" OR ";
			booleanExp += " ("+query+") ";
			first = false;
		}
		return booleanExp;
	}
}
