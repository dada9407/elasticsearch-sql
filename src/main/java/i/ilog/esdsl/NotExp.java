package i.ilog.esdsl;

import net.sf.json.JSONObject;

public class NotExp extends BooleanExp {
	
	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		for (BooleanExp exp : exps) {
			if(exp == null) continue;
			JSONObject ej = exp.toJson();
			if(ej== null || ej.size() == 0){
				continue;
			}
			json.put("not", ej);
		}
		return json;
	}
	
	@Override
	public String toQueryString() {
		String query=exps.get(0).toQueryString();
		String booleanExp = "";
		if(exps.size()>0 && query!= null && query.trim().length()>0){
			booleanExp = " NOT (";
			booleanExp += "("+query+")";
			booleanExp += ")";
		}
		return booleanExp;
	}
}
