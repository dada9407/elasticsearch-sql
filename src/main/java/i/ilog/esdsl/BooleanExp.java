package i.ilog.esdsl;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

public class BooleanExp implements ElementInterface{
	List<BooleanExp> exps = new ArrayList();
	
	public void addExp(BooleanExp exp){
		exps.add(exp);
	}
	
	public void addAllExp(List<BooleanExp> es){
		exps.addAll(es);
	}
	
	public JSONObject toJson(){
		return null;
	}
	
	public String toQueryString(){
		return "*";
	}

    @Override
    public void prepareHandle() {
    
    }
}
