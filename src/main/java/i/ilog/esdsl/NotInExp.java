package i.ilog.esdsl;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class NotInExp extends InExp {
	
	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		JSONObject inWrap = new JSONObject();
		JSONObject in = new JSONObject();
        if(builder != null && valueList.size() == 0) {
            prepareHandle();
        }
		in.put(column.getName(),valueList);
		inWrap.put("terms",in);
		json.put("not", inWrap);
		return json;
	}
	
	@Override
	public String toQueryString() {
		String booleanExp = "NOT (";
        if(builder != null && valueList.size() == 0) {
            prepareHandle();
        }
		boolean first = true;
		for(Object value : valueList){
			booleanExp += first?"":" OR ";
			booleanExp += " "+column.getName()+":"+(value instanceof Term?"\""+value+"\"":value)+" ";
			first=false;
		}
		booleanExp += ")";
		return booleanExp;
	}
}
