package i.ilog.esdsl;

public class TableSource {
	String index;
	String type;
	String alias;
	
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	@Override
	public String toString() {
		return "{index:"+this.index+",type:"+this.type+",alias:"+this.alias+"}";
	}
	
	
	
}
