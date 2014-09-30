package i.ilog.esdsl;

import net.sf.json.JSONObject;

public class WhereExp extends BooleanExp{
	private BooleanExp exp;

	public BooleanExp getExp() {
		return exp;
	}

	public void setExp(BooleanExp exp) {
		this.exp = exp;
	}

	@Override
	public JSONObject toJson() {
		return exp.toJson();
	}
	
	@Override
	public String toQueryString() {
		if(exp ==null ){
			return "*";
		}
		return exp.toQueryString();
	}
	
}
